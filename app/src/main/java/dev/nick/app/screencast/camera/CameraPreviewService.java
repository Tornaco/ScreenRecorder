package dev.nick.app.screencast.camera;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.cast.IScreencaster;
import dev.nick.app.screencast.cast.ScreencastServiceProxy;
import dev.nick.logger.LoggerManager;

public class CameraPreviewService extends Service {

    private static final long WINDOW_FADE_TIME = 5 * 1000;

    private View mFloatView;
    private WindowManager mWindowManager;
    private LayoutParams mFloatContainerParams;
    private AudioFadeLayout mFloatViewContainer;

    private WindowSize mSize;

    private ServiceBinder mBinder;

    private OnTouchListener mFloatViewTouchListener = new OnTouchListener() {

        private int initialX;
        private int initialY;
        private float initialTouchX;
        private float initialTouchY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = mFloatContainerParams.x;
                    initialY = mFloatContainerParams.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    mFloatViewContainer.stopFading();
                    return true;
                case MotionEvent.ACTION_UP:
                    mWindowManager.updateViewLayout(mFloatViewContainer,
                            mFloatContainerParams);
                    mFloatViewContainer.startFading(WINDOW_FADE_TIME);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int diffX = (int) (event.getRawX() - initialTouchX);
                    int diffY = (int) (event.getRawY() - initialTouchY);
                    mFloatContainerParams.x = initialX + diffX;
                    mFloatContainerParams.y = initialY + diffY;
                    mWindowManager.updateViewLayout(mFloatViewContainer,
                            mFloatContainerParams);
                    return true;
            }
            return false;
        }
    };
    private IScreencaster.ICastWatcher mCastWatcher = new IScreencaster.ICastWatcher() {
        @Override
        public void onStartCasting() {

        }

        @Override
        public void onStopCasting() {
            hidePreview();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null) mBinder = new ServiceBinder();
        return mBinder;
    }

    public void showPreview(WindowSize size) {
        if (isShowing()) {
            return;
        }
        mSize = size;
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mFloatView = new SoftwareCameraPreview(this);
        mFloatContainerParams = new LayoutParams(
                mSize.w,
                mSize.h,
                LayoutParams.TYPE_TOAST,
                LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mFloatContainerParams.y = 0;
        mFloatContainerParams.x = 0;
        mFloatViewContainer = (AudioFadeLayout) LayoutInflater.from(this).inflate(R.layout.float_containor, null);
        mFloatViewContainer.setOnTouchListener(mFloatViewTouchListener);
        mFloatViewContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mWindowManager.addView(mFloatViewContainer, mFloatContainerParams);
        mFloatViewContainer.addView(mFloatView);
        mFloatViewContainer.startFading(WINDOW_FADE_TIME);
        LoggerManager.getLogger(getClass()).debug("Showing @size:" + mSize);
    }

    private void hidePreview() {
        if (isShowing()) {
            mWindowManager.removeView(mFloatViewContainer);
        }
    }

    private boolean isShowing() {
        return mFloatViewContainer != null && mFloatViewContainer.isAttachedToWindow() && mFloatView.isAttachedToWindow();
    }

    public void setSize(WindowSize size) {
        this.mSize = size;
        if (isShowing()) {
            mFloatContainerParams.width = size.w;
            mFloatContainerParams.height = size.h;
            mWindowManager.updateViewLayout(mFloatViewContainer,
                    mFloatContainerParams);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ScreencastServiceProxy.watch(this, mCastWatcher);
    }

    @Override
    public void onDestroy() {
        if (mFloatView != null) {
            mWindowManager.removeView(mFloatView);
        }
        ScreencastServiceProxy.watch(this, mCastWatcher);
        super.onDestroy();
    }

    public static class WindowSize {

        static WindowSize DEFAULT = new WindowSize(320, 480);
        static WindowSize LARGE = new WindowSize(480, 720);
        static WindowSize SMALL = new WindowSize(240, 360);

        int w, h;

        public WindowSize(int w, int h) {
            this.w = w;
            this.h = h;
        }

        @Override
        public String toString() {
            return "WindowSize{" +
                    "w=" + w +
                    ", h=" + h +
                    '}';
        }
    }

    class ServiceBinder extends Binder implements ICameraPreviewService {

        @Override
        public void show(int sizeIndex) {
            WindowSize size;
            switch (sizeIndex) {
                case PreviewSize.LARGE:
                    size = WindowSize.LARGE;
                    break;
                case PreviewSize.SMALL:
                    size = WindowSize.SMALL;
                    break;
                case PreviewSize.NORMAL:
                    size = WindowSize.DEFAULT;
                    break;
                default:
                    throw new IllegalArgumentException("Bad size index:" + sizeIndex);
            }
            CameraPreviewService.this.showPreview(size);
        }

        @Override
        public void hide() {
            CameraPreviewService.this.hidePreview();
        }

        @Override
        public boolean isShowing() {
            return CameraPreviewService.this.isShowing();
        }

        @Override
        public void setSize(int sizeIndex) {
            WindowSize size;
            switch (sizeIndex) {
                case PreviewSize.LARGE:
                    size = WindowSize.LARGE;
                    break;
                case PreviewSize.SMALL:
                    size = WindowSize.SMALL;
                    break;
                case PreviewSize.NORMAL:
                    size = WindowSize.DEFAULT;
                    break;
                default:
                    throw new IllegalArgumentException("Bad size index:" + sizeIndex);
            }
            CameraPreviewService.this.setSize(size);
        }
    }
}
