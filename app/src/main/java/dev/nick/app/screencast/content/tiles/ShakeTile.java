package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import dev.nick.tiles.tile.TileListener;

public class ShakeTile extends QuickTile {

    public ShakeTile(@NonNull Context context, TileListener listener) {
        super(context, listener);

        this.titleRes = R.string.title_shake_action;
        this.iconRes = R.drawable.ic_close_white_24dp;

        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().shakeAction());
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                SettingsProvider.get().setShakeAction(checked);
            }
        };
    }
}
