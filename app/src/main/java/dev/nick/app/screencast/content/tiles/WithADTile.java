package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import dev.nick.tiles.tile.TileListener;

class WithADTile extends QuickTile {

    WithADTile(@NonNull Context context, TileListener listener) {
        super(context, listener);
        this.iconRes = R.drawable.ic_block_black_24dp;
        this.tileView = new SwitchTileView(context) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().showAD());
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                SettingsProvider.get().setShowAD(checked);
                update();
                setChecked(SettingsProvider.get().showAD());
            }
        };
        this.titleRes = R.string.title_with_ad;
        this.summaryRes = R.string.summary_with_ad;
        update();
    }

    private void update() {
        getTileView().getSummaryTextView().setText(SettingsProvider.get().showAD() ? R.string.summary_add_on : R.string.summary_add_off);
    }
}
