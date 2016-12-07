package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import java.util.Arrays;
import java.util.List;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.DropDownTileView;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.TileListener;

class OrientationTile extends QuickTile {

    private String[] mOriDesc;

    OrientationTile(@NonNull Context context, TileListener listener) {

        super(context, listener);
        this.iconRes = R.drawable.ic_screen_rotation_black_24dp;
        this.titleRes = R.string.title_orientation;

        mOriDesc = context.getResources().getStringArray(R.array.orientations);
        this.summary = mOriDesc[SettingsProvider.get().orientation()];

        this.tileView = new DropDownTileView(context) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setSelectedItem(SettingsProvider.get().orientation(), false);
            }

            @Override
            protected List<String> onCreateDropDownList() {
                return Arrays.asList(mOriDesc);
            }

            @Override
            protected void onItemSelected(int position) {
                super.onItemSelected(position);
                SettingsProvider.get().setOrientation(position);
                getSummaryTextView().setText(mOriDesc[position]);
            }
        };
    }
}