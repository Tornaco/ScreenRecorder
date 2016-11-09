package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;

import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import dev.nick.tiles.tile.TileListener;

public class HeadlessTile2 extends QuickTile {
    public HeadlessTile2(@NonNull Context context, TileListener listener) {
        super(context, listener);
        this.title = getClass().getSimpleName();
        this.summary = "Summary here";
        this.tileView = new QuickTileView(context, this);
    }
}
