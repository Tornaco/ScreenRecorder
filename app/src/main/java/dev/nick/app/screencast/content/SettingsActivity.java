package dev.nick.app.screencast.content;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.content.tiles.Dashboards;

public class SettingsActivity extends TransactionSafeActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_content);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        placeFragment(R.id.container, new Dashboards(), null, false);
    }
}
