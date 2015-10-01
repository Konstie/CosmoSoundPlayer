package com.cosmosound.app.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.cosmosound.app.R;

public class SourceBrowserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filebrowser);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setCustomView(R.layout.search_filter_layout);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        if (getSupportFragmentManager() != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.filebrowser_container, new SourceBrowserFragment())
                    .commit();
        }
    }
}
