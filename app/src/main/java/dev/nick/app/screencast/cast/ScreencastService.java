/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nick.app.screencast.cast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.camera.ThreadUtil;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.app.screencast.tools.MediaTools;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class ScreencastService extends Service implements IScreencaster {

    public static final String SCREENCASTER_NAME = "hidden:screen-recording";
    private static final String ACTION_STOP_SCREENCAST = "stop.recording";

    private final List<ICastWatcher> mWatchers = new ArrayList<>();
    RecordingDevice mRecorder;
    boolean mIsCasting;
    ServiceBinder mBinder;
    private MediaProjection mProjection;
    private long startTime;
    private Timer timer;
    private Notification.Builder mBuilder;
    private Logger mLogger;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_USER_BACKGROUND) ||
                    intent.getAction().equals(ACTION_STOP_SCREENCAST) ||
                    intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
                mLogger.info("onReceive:" + intent.getAction());
                stop();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null) mBinder = new ServiceBinder();
        return mBinder;
    }

    void cleanup() {
        String recorderPath = null;
        if (mRecorder != null) {
            recorderPath = mRecorder.getRecordingFilePath();
            mRecorder.stop();
            mRecorder = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        stopForeground(true);
        if (recorderPath != null) {
            sendShareNotification(recorderPath);
        }
        if (mProjection != null)
            mProjection.stop();
    }

    @Override
    public void onCreate() {
        mLogger = LoggerManager.getLogger(getClass());
        stopCasting();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_BACKGROUND);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(ACTION_STOP_SCREENCAST);
        registerReceiver(mBroadcastReceiver, filter);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        stopCasting();
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private boolean hasAvailableSpace() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = stat.getBlockSizeLong() * stat.getBlockCountLong();
        long megAvailable = bytesAvailable / 1048576;
        return megAvailable >= 100;
    }

    public void updateNotification(Context context) {
        long timeElapsed = SystemClock.elapsedRealtime() - startTime;
        mBuilder.setContentText(getString(R.string.video_length,
                DateUtils.formatElapsedTime(timeElapsed / 1000)));
        startForeground(1, mBuilder.build());
    }

    protected Point getNativeResolution() {
        DisplayManager dm = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        Display display = dm.getDisplay(Display.DEFAULT_DISPLAY);
        Point ret = new Point();
        try {
            display.getRealSize(ret);
        } catch (Exception e) {
            try {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                ret.x = (Integer) mGetRawW.invoke(display);
                ret.y = (Integer) mGetRawH.invoke(display);
            } catch (Exception ex) {
                display.getSize(ret);
            }
        }
        return ret;
    }

    void registerScreencaster(boolean withAudio) throws RemoteException {
        DisplayManager dm = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        Display display = dm.getDisplay(Display.DEFAULT_DISPLAY);
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        assert mRecorder == null;
        Point size = getNativeResolution();
        // size = new Point(1080, 1920);
        mRecorder = new RecordingDevice(this, size.x, size.y, withAudio);
        mRecorder.setProjection(mProjection);
        VirtualDisplay vd = mRecorder.registerVirtualDisplay(this,
                SCREENCASTER_NAME, size.x, size.y, metrics.densityDpi);
        if (vd == null)
            cleanup();
    }

    private void stopCasting() {
        cleanup();
        if (!hasAvailableSpace()) {
            Toast.makeText(this, R.string.insufficient_storage, Toast.LENGTH_LONG).show();
        }
        mIsCasting = false;
        notifyUncasting();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return START_NOT_STICKY;
        if (ACTION_STOP_SCREENCAST.equals(intent.getAction())) {
            stop();
        }
        return START_STICKY;
    }

    private Notification.Builder createNotificationBuilder() {
        Notification.Builder builder = new Notification.Builder(this)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_stat_device_access_video)
                .setContentTitle(getString(R.string.recording));
        Intent stopRecording = new Intent(ACTION_STOP_SCREENCAST);
        stopRecording.setClass(this, ScreencastService.class);
        builder.addAction(R.drawable.ic_stop, getString(R.string.stop),
                PendingIntent.getService(this, 0, stopRecording, PendingIntent.FLAG_UPDATE_CURRENT));
        return builder;
    }

    private void sendShareNotification(String recordingFilePath) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // share the screencast file
        mBuilder = createShareNotificationBuilder(recordingFilePath);
        notificationManager.notify(0, mBuilder.build());
    }

    private Notification.Builder createShareNotificationBuilder(String file) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("video/mp4");
        Uri uri = MediaTools.getImageContentUri(this, new File(file));
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, new File(file).getName());
        Intent chooserIntent = Intent.createChooser(sharingIntent, null);
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        long timeElapsed = SystemClock.elapsedRealtime() - startTime;

        mLogger.debug("Video complete: " + uri);

        Intent open = new Intent(Intent.ACTION_VIEW);
        open.setDataAndType(uri, "video/mp4");
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent =
                PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_CANCEL_CURRENT);

        return new Notification.Builder(this)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_stat_device_access_video)
                .setContentTitle(getString(R.string.recording_ready_to_share))
                .setContentText(getString(R.string.video_length,
                        DateUtils.formatElapsedTime(timeElapsed / 1000)))
                .addAction(R.drawable.ic_share, getString(R.string.share),
                        PendingIntent.getActivity(this, 0, chooserIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setContentIntent(contentIntent);
    }

    @Override
    public boolean start(final MediaProjection projection, final boolean withAudio) {
        if (!hasAvailableSpace()) {
            Toast.makeText(this, R.string.not_enough_storage, Toast.LENGTH_LONG).show();
            return false;
        }
        ThreadUtil.getWorkThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startInternal(projection, withAudio);
            }
        }, SettingsProvider.get().startDelay());
        return true;
    }

    boolean startInternal(MediaProjection projection, boolean withAudio) {
        mLogger.debug("start");
        mProjection = projection;
        try {
            if (!hasAvailableSpace()) {
                Toast.makeText(this, R.string.not_enough_storage, Toast.LENGTH_LONG).show();
                return false;
            }
            mIsCasting = true;
            notifyCasting();
            startTime = SystemClock.elapsedRealtime();
            registerScreencaster(withAudio);
            mBuilder = createNotificationBuilder();

            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateNotification(ScreencastService.this);
                }
            }, 100, 1000);
            return true;
        } catch (Exception e) {
            Log.e("Mirror", "error", e);
            return false;
        }
    }

    @Override
    public void stop() {
        mLogger.debug("stop" + Log.getStackTraceString(new Throwable()));
        stopCasting();
    }

    @Override
    public boolean isCasting() {
        return mIsCasting;
    }

    private void notifyCasting() {
        synchronized (mWatchers) {
            ThreadUtil.getMainThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    for (ICastWatcher w : mWatchers) {
                        w.onStartCasting();
                    }
                }
            });
        }
    }

    private void notifyUncasting() {
        synchronized (mWatchers) {
            ThreadUtil.getMainThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    for (ICastWatcher w : mWatchers) {
                        w.onStopCasting();
                    }
                }
            });
        }
    }

    @Override
    public void watch(ICastWatcher watcher) {
        synchronized (mWatchers) {
            if (!mWatchers.contains(watcher)) {
                mWatchers.add(watcher);
                notifySticky(watcher);
            }
        }
    }

    @Override
    public void unWatch(ICastWatcher watcher) {
        synchronized (mWatchers) {
            mWatchers.remove(watcher);
        }
    }

    void notifySticky(final ICastWatcher watcher) {
        ThreadUtil.getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mIsCasting) {
                    watcher.onStartCasting();
                } else {
                    watcher.onStopCasting();
                }
            }
        });
    }

    class ServiceBinder extends Binder implements IScreencaster {

        @Override
        public boolean start(MediaProjection projection, boolean withAudio) {
            return ScreencastService.this.start(projection, withAudio);
        }

        @Override
        public void stop() {
            ScreencastService.this.stop();
        }

        @Override
        public boolean isCasting() {
            return ScreencastService.this.isCasting();
        }

        @Override
        public void watch(ICastWatcher watcher) {
            ScreencastService.this.watch(watcher);
        }

        @Override
        public void unWatch(ICastWatcher watcher) {
            ScreencastService.this.unWatch(watcher);
        }
    }
}
