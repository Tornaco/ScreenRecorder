package dev.nick.app.screencast.camera;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import dev.nick.app.screencast.cast.ServiceProxy;

public class CameraPreviewServiceProxy extends ServiceProxy implements ICameraPreviewService {

    ICameraPreviewService mService;

    private CameraPreviewServiceProxy(Context context) {
        super(context, new Intent(context, CameraPreviewService.class));
        context.startService(new Intent(context, CameraPreviewService.class));
    }

    public static void show(final Context context, final int size) {
        new CameraPreviewServiceProxy(context).show(size);
    }

    public static void hide(final Context context) {
        new CameraPreviewServiceProxy(context).hide();
    }

    public static void setSize(final Context context, final int index) {
        new CameraPreviewServiceProxy(context).setSize(index);
    }

    @Override
    public void show(final int size) {
        setTask(new ProxyTask() {
            @Override
            public void run() throws RemoteException {
                mService.show(size);
            }

            @Override
            public boolean forUI() {
                return true;
            }
        });
    }

    @Override
    public void hide() {
        setTask(new ProxyTask() {
            @Override
            public void run() throws RemoteException {
                mService.hide();
            }

            @Override
            public boolean forUI() {
                return true;
            }
        });
    }

    @Override
    public boolean isShowing() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSize(final int index) {
        setTask(new ProxyTask() {
            @Override
            public void run() throws RemoteException {
                mService.setSize(index);
            }

            @Override
            public boolean forUI() {
                return true;
            }
        });
    }

    @Override
    public void onConnected(IBinder binder) {
        mService = (ICameraPreviewService) binder;
    }
}
