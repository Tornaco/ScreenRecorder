package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import dev.nick.tiles.tile.TileListener;

class StorageTile extends QuickTile {

    private static final File RECORDINGS_DIR = new File(Environment.getExternalStorageDirectory().getPath(), SettingsProvider.STORAGE_FOLDER_NAME);

    StorageTile(@NonNull Context context, TileListener listener) {
        super(context, listener);
        this.titleRes = R.string.title_storage;
        this.summary = getContext().getString(R.string.summary_storage, RECORDINGS_DIR.getPath());
        this.iconRes = R.drawable.ic_folder_open_black_24dp;
        this.tileView = new QuickTileView(context, this);
    }
}
