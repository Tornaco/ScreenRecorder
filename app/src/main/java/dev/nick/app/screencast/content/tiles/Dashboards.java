package dev.nick.app.screencast.content.tiles;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.app.Factory;
import dev.nick.app.screencast.cast.CasterAudioSource;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.logger.LoggerManager;
import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.TileListener;

public class Dashboards extends DashboardFragment implements TileListener {

    private TextView mSummView;

    @Override
    protected void onCreateDashCategories(List<Category> categories) {
        super.onCreateDashCategories(categories);
        Category audio = new Category() {
            @Override
            public void onSummaryViewAttached(TextView view) {
                super.onSummaryViewAttached(view);
                mSummView = view;
            }

            @Override
            public void onNoRemindClick() {
                super.onNoRemindClick();
                SettingsProvider.get().setAudioSourceNoRemind(true);
            }
        };
        audio.titleRes = R.string.category_audio;
        audio.summaryRes = !SettingsProvider.get().audioSourceNoRemind()
                && (SettingsProvider.get().audioSource() == CasterAudioSource.DEFAULT
                || SettingsProvider.get().audioSource()
                == CasterAudioSource.R_SUBMIX) ? R.string.audio_xopsed_desc : 0;
        audio.addTile(new WithAudioTile(getContext(), this));
        audio.addTile(new AudioSourceTile(getContext(), this));
        Category camera = new Category();
        camera.titleRes = R.string.category_camera;
        camera.addTile(new WithCameraTile(getContext(), this));
        camera.addTile(new PreviewSizeDropdownTile(getContext(), this));
        camera.addTile(new SwitchCameraTile(getContext(), this));
        Category video = new Category();
        video.titleRes = R.string.category_video;
        video.addTile(new ResolutionsTile(getContext(), this));
        video.addTile(new OrientationTile(getContext(), this));
        Category access = new Category();
        access.titleRes = R.string.category_accessibility;
        access.addTile(new SoundEffectTile(getContext(), this));
        access.addTile(new ShakeTile(getContext(), this));
        access.addTile(new ShowTouchTile(getContext(), this));
        access.addTile(new DelayTile(getContext(), this));
        access.addTile(new ShowCDTile(getContext(), this));
        access.addTile(new AutoHideTile(getContext(), this));
        Category others = new Category();
        others.titleRes = R.string.category_others;
        if (Factory.get().integratedAD()) others.addTile(new WithADTile(getContext(), this));
        others.addTile(new StorageTile(getContext(), this));
        others.addTile(new LicenseTile(getContext(), this));
        others.addTile(new AuthorInfoTile(getContext(), this));
        categories.add(audio);
        categories.add(video);
        categories.add(camera);
        categories.add(access);
        categories.add(others);
    }

    @Override
    public void onTileClick(@NonNull QuickTile tile) {
        LoggerManager.getLogger(getClass()).debug("OnTileClick:" + tile);
        // Nothing.
        if (!SettingsProvider.get().audioSourceNoRemind() && mSummView != null && tile instanceof AudioSourceTile) {
            mSummView.setText(R.string.audio_xopsed_desc);
            mSummView.setVisibility(View.VISIBLE);
        }
    }
}