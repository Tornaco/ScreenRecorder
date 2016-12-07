package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import java.util.Arrays;
import java.util.List;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.cast.ValidResolutions;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.DropDownTileView;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.TileListener;

class ResolutionsTile extends QuickTile {

    private String[] mResDescs;

    ResolutionsTile(@NonNull Context context, TileListener listener) {

        super(context, listener);
        this.iconRes = R.drawable.ic_play_circle_filled_black_24dp;
        this.titleRes = R.string.title_high_res;

        mResDescs = ValidResolutions.DESC;
        mResDescs[ValidResolutions.INDEX_MASK_AUTO] = context.getString(R.string.summary_res_auto);

        this.summary = mResDescs[SettingsProvider.get().resolutionIndex()];

        this.tileView = new DropDownTileView(context) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setSelectedItem(SettingsProvider.get().resolutionIndex(), false);
            }

            @Override
            protected List<String> onCreateDropDownList() {
                return Arrays.asList(mResDescs);
            }

            @Override
            protected void onItemSelected(int position) {
                super.onItemSelected(position);
                SettingsProvider.get().setResolutionIndex(position);
                getSummaryTextView().setText(mResDescs[position]);
            }
        };
    }
}