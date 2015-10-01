package com.cosmosound.app.ui;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.Toast;
import com.cosmosound.app.adapters.DrawerNavigationAdapter;
import com.cosmosound.app.R;


public class CosmoSoundPlaylistActivity extends AppCompatActivity {

    private boolean doubleBackToQuitPressedOnce = false;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DrawerLayout drawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    private String menuTitles[] = {"Play all", "Play from source", "Exit"};
    private int icons[] = {R.mipmap.ic_action_playall, R.mipmap.ic_action_add,
            android.R.drawable.ic_menu_close_clear_cancel};

    private String name = "CosmoSound player";

    private CosmoSoundPlaylistFragment playlistFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracklist);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new DrawerNavigationAdapter(menuTitles, icons, name, this);

        mRecyclerView.setAdapter(mAdapter);

        final GestureDetector mGestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        return true;
                    }
                });

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());

                if (child != null && mGestureDetector.onTouchEvent(e)) {
                    drawerLayout.closeDrawers();
                    handleDrawerItems(rv, e, child);
                    return true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }
        });

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerLayout.bringToFront();
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_closed) {

            @Override
            public void onDrawerOpened(View drawerView) { }

            @Override
            public void onDrawerClosed(View drawerView) { }
        };

        drawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        if (getSupportFragmentManager() != null) {
            playlistFragment = new CosmoSoundPlaylistFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.playlist_container, playlistFragment)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.doubleBackToQuitPressedOnce = false;
    }

    private void handleDrawerItems(RecyclerView rv, MotionEvent e, View child) {
        switch (rv.getChildPosition(child)) {
            case 1:
                playlistFragment.loadAllTracks();
                break;
            case 2:
                Intent intent = new Intent(this, SourceBrowserActivity.class);
                startActivityForResult(intent, 1);
                break;
            case 3:
                playlistFragment.getService().stopService(playlistFragment.getLaunchIntent());
                System.exit(0);
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cstrack_list, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        //playlistFragment.getService().stopService(playlistFragment.getLaunchIntent());
        //if (doubleBackToQuitPressedOnce) {
        //    super.onBackPressed();
        //    return;
        //}
        //this.doubleBackToQuitPressedOnce = true;
        Toast.makeText(this, "Choose \"Exit\" option in the main menu to quit!", Toast.LENGTH_SHORT).show();
    }
}
