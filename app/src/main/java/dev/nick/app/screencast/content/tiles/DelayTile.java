package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import dev.nick.tiles.tile.TileListener;

public class DelayTile extends QuickTile {

    public DelayTile(@NonNull Context context, TileListener listener) {
        super(context, listener);
        this.iconRes = R.drawable.ic_access_time_black_24dp;
        this.tileView = new SwitchTileView(context) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().startDelay() > 0);

                if (isChecked() && SettingsProvider.get().getAppVersionNum() < SettingsProvider.APP_VERSION_INT) {
                    SettingsProvider.get().setStartDelay(SettingsProvider.START_DELAY_DEFAULT);
                }
            }

            @Override
            protected void onCheckChanged(final boolean checked) {
                super.onCheckChanged(checked);
                SettingsProvider.get().setStartDelay(checked ? SettingsProvider.START_DELAY_DEFAULT : 0);
            }
        };
        this.titleRes = R.string.title_start_delay;
        this.summaryRes = R.string.summary_start_delay;
    }
}
