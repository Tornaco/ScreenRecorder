package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import dev.nick.app.screencast.R;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import dev.nick.tiles.tile.TileListener;

public class AudioSourceDescTile extends QuickTile {

    public AudioSourceDescTile(@NonNull Context context, TileListener listener) {
        super(context, listener);

        this.summaryRes = R.string.audio_xopsed_desc;

        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
            }
        };
    }
}
