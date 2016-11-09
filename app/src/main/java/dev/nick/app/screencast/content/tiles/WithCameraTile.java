package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.app.screencast.cast.IScreencaster;
import dev.nick.app.screencast.R;
import dev.nick.app.screencast.cast.ScreencastServiceProxy;
import dev.nick.app.screencast.camera.CameraPreviewServiceProxy;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.SwitchTileView;
import dev.nick.tiles.tile.TileListener;

public class WithCameraTile extends SwitchCameraTile {

    boolean mRecording;

    public WithCameraTile(@NonNull Context context, TileListener listener) {
        super(context, listener);
        this.iconRes = R.drawable.ic_camera_alt_black_24dp;
        ScreencastServiceProxy.watch(getContext().getApplicationContext(), new IScreencaster.ICastWatcher() {
            @Override
            public void onStartCasting() {
                mRecording = true;
            }

            @Override
            public void onStopCasting() {
                mRecording = false;
            }
        });
        this.tileView = new SwitchTileView(context) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().withCamera());
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                SettingsProvider.get().setWithCamera(checked);
                if (mRecording) {
                    if (checked)
                        CameraPreviewServiceProxy.show(getContext(), SettingsProvider.get().previewSize());
                    else
                        CameraPreviewServiceProxy.hide(getContext());
                }
            }
        };
        this.titleRes = R.string.title_with_camera;
        this.summaryRes = R.string.summary_camera;
    }
}
