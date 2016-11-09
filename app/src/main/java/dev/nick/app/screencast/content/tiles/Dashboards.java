package dev.nick.app.screencast.content.tiles;

import android.support.annotation.NonNull;

import java.util.List;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.app.Factory;
import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.TileListener;

public class Dashboards extends DashboardFragment implements TileListener {

    @Override
    protected void onCreateDashCategories(List<Category> categories) {
        super.onCreateDashCategories(categories);
        Category audio = new Category();
        audio.titleRes = R.string.category_audio;
        audio.addTile(new WithAudioTile(getContext(), this));
        Category camera = new Category();
        camera.titleRes = R.string.category_camera;
        camera.addTile(new WithCameraTile(getContext(), this));
        camera.addTile(new PreviewSizeDropdownTile(getContext(), this));
        camera.addTile(new SwitchCameraTile(getContext(), this));
        Category video = new Category();
        video.titleRes = R.string.category_video;
        video.addTile(new ResolutionsSwitchTile(getContext(), this));
        Category access = new Category();
        access.titleRes = R.string.category_accessibility;
        access.addTile(new DelayTile(getContext(), this));
        access.addTile(new ShowTouchTile(getContext(), this));
        access.addTile(new AutoHideTile(getContext(), this));
        Category others = new Category();
        others.titleRes = R.string.category_others;
        if (Factory.get().integratedAD()) others.addTile(new WithADTile(getContext(), this));
        others.addTile(new StorageTile(getContext(), this));
        others.addTile(new LicenseTile(getContext(), this));
        categories.add(audio);
        categories.add(video);
        categories.add(camera);
        categories.add(access);
        categories.add(others);
    }

    @Override
    public void onTileClick(@NonNull QuickTile tile) {
        // Nothing.
    }
}