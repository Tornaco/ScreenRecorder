package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;

import dev.nick.app.screencast.R;
import dev.nick.tiles.tile.EditTextTileView;
import dev.nick.tiles.tile.TileListener;

public class LimitSizeTile extends SwitchCameraTile {
    public LimitSizeTile(@NonNull Context context, TileListener listener) {
        super(context, listener);
        this.iconRes = R.drawable.ic_storage_black_24dp;
        this.title = "Storage limit";
        this.summary = "No limit";
        this.tileView = new EditTextTileView(context) {
            @Override
            protected void onPositiveButtonClick() {
                super.onPositiveButtonClick();
                title = getEditText().getText().toString();
                getTitleTextView().setText(title);
            }
        };
    }
}
