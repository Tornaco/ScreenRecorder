package dev.nick.app.screencast.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class AudioFadeLayout extends LinearLayout {

    public AudioFadeLayout(Context context) {
        super(context);
    }

    public AudioFadeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioFadeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AudioFadeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void startFading(long delay) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setAlpha(0.1f);
            }
        }, delay);
    }

    public void stopFading() {
        setAlpha(1.0f);
    }
}
