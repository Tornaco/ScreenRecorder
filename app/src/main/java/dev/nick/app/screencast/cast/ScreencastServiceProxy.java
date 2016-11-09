package dev.nick.app.screencast.cast;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.os.IBinder;
import android.os.RemoteException;

import dev.nick.app.screencast.camera.ThreadUtil;

public class ScreencastServiceProxy extends ServiceProxy implements IScreencaster {

    private IScreencaster mService;

    private ScreencastServiceProxy(Context context) {
        super(context, new Intent(context, ScreencastService.class));
        context.startService(new Intent(context, ScreencastService.class));
    }

    public static void start(final Context context, final MediaProjection projection, final boolean withAudio) {
        ThreadUtil.getWorkThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                new ScreencastServiceProxy(context).start(projection, withAudio);
            }
        });
    }

    public static void stop(final Context context) {
        ThreadUtil.getWorkThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                new ScreencastServiceProxy(context).stop();
            }
        });
    }

    public static void watch(final Context context, final ICastWatcher watcher) {
        ThreadUtil.getWorkThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                new ScreencastServiceProxy(context).watch(watcher);
            }
        });
    }

    public static void unWatch(final Context context, final ICastWatcher watcher) {
        ThreadUtil.getWorkThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                new ScreencastServiceProxy(context).unWatch(watcher);
            }
        });
    }

    public static boolean isCasting(final Context context) {
        return new ScreencastServiceProxy(context).isCasting();
    }

    @Override
    public void onConnected(IBinder binder) {
        mService = (IScreencaster) binder;
    }

    @Override
    public boolean start(final MediaProjection projection, final boolean withAudio) {
        setTask(new ProxyTask() {
            @Override
            public void run() throws RemoteException {
                mService.start(projection, withAudio);
            }
        }, "start");
        return true;
    }

    @Override
    public void stop() {
        setTask(new ProxyTask() {
            @Override
            public void run() throws RemoteException {
                mService.stop();
            }
        }, "stop");
    }

    @Override
    public boolean isCasting() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void watch(final ICastWatcher watcher) {
        setTask(new ProxyTask() {
            @Override
            public void run() throws RemoteException {
                mService.watch(watcher);
            }
        }, "watch");
    }

    @Override
    public void unWatch(final ICastWatcher watcher) {
        setTask(new ProxyTask() {
            @Override
            public void run() throws RemoteException {
                mService.unWatch(watcher);
            }
        }, "unWatch");
    }
}
