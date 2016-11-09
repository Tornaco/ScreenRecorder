package dev.nick.app.screencast.cast;

import android.media.projection.MediaProjection;

public interface IScreencaster {
    boolean start(MediaProjection projection, boolean withAudio);

    void stop();

    boolean isCasting();

    void watch(ICastWatcher watcher);

    void unWatch(ICastWatcher watcher);

    interface ICastWatcher {
        void onStartCasting();

        void onStopCasting();
    }
}
