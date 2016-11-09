package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.DropDownTileView;
import dev.nick.tiles.tile.TileListener;

class PreviewSizeDropdownTile extends SwitchCameraTile {

    private String[] mSizes = null;

    PreviewSizeDropdownTile(@NonNull Context context, TileListener listener) {
        super(context, listener);
        this.mSizes = getContext().getResources().getStringArray(R.array.preview_sizes);
        this.iconRes = R.drawable.ic_photo_size_select_small_black_24dp;
        this.titleRes = R.string.title_preview_size;
        this.summary = mSizes[SettingsProvider.get().previewSize()];
        this.tileView = new DropDownTileView(context) {
            @Override
            protected List<String> onCreateDropDownList() {
                return Arrays.asList(mSizes);
            }

            @Override
            public void onDropdownItemSelected(int position, boolean fromSpinner) {
                super.onDropdownItemSelected(position, fromSpinner);
                SettingsProvider.get().setPreviewSize(position);
                getSummaryTextView().setText(mSizes[position]);
            }
        };
    }
}