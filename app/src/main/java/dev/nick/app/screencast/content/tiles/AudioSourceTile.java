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

class AudioSourceTile extends QuickTile {

    private String[] mSources = null;

    AudioSourceTile(@NonNull Context context, final TileListener listener) {
        super(context, listener);

        this.iconRes = R.drawable.ic_speaker_black_24dp;

        this.mSources = getContext().getResources().getStringArray(R.array.audio_source);

        this.tileView = new DropDownTileView(context) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setSelectedItem(SettingsProvider.get().audioSource(), false);
            }

            @Override
            protected List<String> onCreateDropDownList() {
                return Arrays.asList(mSources);
            }

            @Override
            protected void onItemSelected(int position) {
                super.onItemSelected(position);
                int previous = SettingsProvider.get().audioSource();
                boolean changed = position != previous;
                SettingsProvider.get().setAudioSource(position);
                getSummaryTextView().setText(mSources[position]);
                if (changed) {
                    listener.onTileClick(AudioSourceTile.this);
                }
            }
        };
        this.titleRes = R.string.title_audio_source;
        this.summary = mSources[SettingsProvider.get().audioSource()];
    }
}
