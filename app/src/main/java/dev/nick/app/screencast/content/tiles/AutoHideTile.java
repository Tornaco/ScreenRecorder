package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import dev.nick.tiles.tile.TileListener;

public class AutoHideTile extends QuickTile {

    public AutoHideTile(@NonNull Context context, TileListener listener) {
        super(context, listener);
        this.iconRes = R.drawable.ic_visibility_off_black_24dp;
        this.tileView = new SwitchTileView(context) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().hideAppWhenStart());
            }

            @Override
            protected void onCheckChanged(final boolean checked) {
                super.onCheckChanged(checked);
                SettingsProvider.get().setHideAppWhenStart(checked);
            }
        };
        this.titleRes = R.string.title_auto_hide;
        this.summaryRes = R.string.summary_auto_hide;
    }
}
