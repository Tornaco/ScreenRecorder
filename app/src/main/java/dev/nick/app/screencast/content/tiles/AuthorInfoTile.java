package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;

import dev.nick.app.screencast.R;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import dev.nick.tiles.tile.TileListener;

class AuthorInfoTile extends QuickTile {
    AuthorInfoTile(@NonNull Context context, TileListener listener) {
        super(context, listener);
        this.titleRes = R.string.title_app_info;
        this.summaryRes = R.string.app_code;
        this.iconRes = R.drawable.ic_mail_outline_black_24dp;
        this.tileView = new QuickTileView(context, this);
    }
}
