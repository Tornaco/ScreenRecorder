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

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.Vibrator;
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
import dev.nick.app.screencast.camera.CameraPreviewServiceProxy;
import dev.nick.app.screencast.camera.ThreadUtil;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.app.screencast.tools.MediaTools;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScreencastService extends Service implements IScreencaster, Handler.Callback {

    public static final String SCREENCASTER_NAME = "hidden:screen-recording";
    private static final String ACTION_STOP_SCREENCAST = "stop.recording";
    private static final String TAG = "TestSensorActivity";
    private static final int SENSOR_SHAKE = 10;
    private final List<ICastWatcher> mWatchers = new ArrayList<>();
    RecordingDevice mRecorder;
    boolean mIsCasting;
    ServiceBinder mBinder;

    Handler sensorEventHandler;

    private MediaProjection mProjection;
    private long startTime;
    private Timer timer;
    private Notification.Builder mBuilder;
    private Logger mLogger;
    private SoundPool mSoundPool;
    private int mStartSound, mStopSound;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_USER_BACKGROUND) ||
                    intent.getAction().equals(ACTION_STOP_SCREENCAST) ||
                    intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
                mLogger.info("onReceive:" + intent.getAction());
                stop();
                CameraPreviewServiceProxy.hide(context);
            }
        }
    };
    private SensorManager sensorManager;
    private Vibrator vibrator;
    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];
            int medumValue = 19;
            if (Math.abs(x) > medumValue || Math.abs(y) > medumValue || Math.abs(z) > medumValue) {
                Message msg = new Message();
                msg.what = SENSOR_SHAKE;
                sensorEventHandler.sendMessage(msg);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        mLogger = LoggerManager.getLogger(getClass());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mLogger.error("Bad android version.");
            return;
        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        sensorEventHandler = new Handler(this);

        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .build())
                .build();
        mStopSound = mSoundPool.load(this, R.raw.video_stop, 1);
        mStartSound = mSoundPool.load(this, R.raw.video_record, 1);
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
        mSoundPool.release();
        mSoundPool = null;
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
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

        // Find user preferred one.
        boolean landscape = SettingsProvider.get().orientation() == Orientations.L;
        int width, height;
        int preferredResIndex = SettingsProvider.get().resolutionIndex();
        if (preferredResIndex != ValidResolutions.INDEX_MASK_AUTO) {
            int[] resolution = ValidResolutions.$[preferredResIndex];
            if (landscape) {
                width = resolution[0];
                height = resolution[1];
            } else {
                height = resolution[0];
                width = resolution[1];
            }
            ret.x = width;
            ret.y = height;
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
        if (vd == null) {
            cleanup();
        }
    }

    private void stopCasting() {
        cleanup();
        if (!hasAvailableSpace()) {
            Toast.makeText(this, R.string.insufficient_storage, Toast.LENGTH_LONG).show();
        }
        if (mIsCasting && SettingsProvider.get().soundEffect()) {
            mSoundPool.play(mStopSound, 1.0f, 1.0f, 0, 0, 1.0f);
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendShareNotification(String recordingFilePath) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // share the screencast file
        mBuilder = createShareNotificationBuilder(recordingFilePath);
        notificationManager.notify(0, mBuilder.build());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Notification.Builder createShareNotificationBuilder(String file) {
        Intent sharingIntent = MediaTools.buildSharedIntent(this, new File(file));
        Intent chooserIntent = Intent.createChooser(sharingIntent, null);
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        long timeElapsed = SystemClock.elapsedRealtime() - startTime;

        mLogger.debug("Video complete: " + file);

        Intent open = MediaTools.buildOpenIntent(this, new File(file));
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

            if (SettingsProvider.get().soundEffect()) {
                mSoundPool.play(mStartSound, 1.0f, 1.0f, 0, 0, 1.0f);
            }

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
        mLogger.debug("stop");
        stopCasting();
    }

    @Override
    public boolean isCasting() {
        return mIsCasting;
    }

    private void notifyCasting() {
        synchronized (mWatchers) {
            final List<ICastWatcher> tmp = new ArrayList<>(mWatchers.size());
            tmp.addAll(mWatchers);
            ThreadUtil.getMainThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    for (ICastWatcher w : tmp) {
                        w.onStartCasting();
                    }
                }
            });
        }
    }

    private void notifyUncasting() {
        synchronized (mWatchers) {
            final List<ICastWatcher> tmp = new ArrayList<>(mWatchers.size());
            tmp.addAll(mWatchers);
            ThreadUtil.getMainThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    for (ICastWatcher w : tmp) {
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

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case SENSOR_SHAKE:
                mLogger.debug("Shaking!");
                boolean shouldHandle = SettingsProvider.get().shakeAction();
                if (!shouldHandle) return true;
                if (mIsCasting) {
                    vibrator.vibrate(100);
                    stop();
                }
                return true;
        }
        return false;
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
