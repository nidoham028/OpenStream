package com.nidoham.openstream.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.nidoham.openstream.fragments.list.FeedFragment;
import com.nidoham.openstream.fragments.list.LatestFragment;
import com.nidoham.openstream.fragments.list.SubscriptionFragment;
import com.nidoham.openstream.fragments.list.FavoriteFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.nidoham.openstream.R;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupToolbar();
        setupDrawer();
        setupTabs();

        // Load default fragment for first tab
        handleTabSelection(0);
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer);
        toolbar = findViewById(R.id.bar);
        tabLayout = findViewById(R.id.tabs);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setupDrawer() {
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_feed));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_stream));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_subscription));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_bookmark));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                handleTabSelection(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Optional: cleanup when tab unselected
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optional: handle tab reselection
            }
        });
    }

    private void handleTabSelection(int position) {
        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new FeedFragment();          // Feed tab
                break;
            case 1:
                fragment = new LatestFragment();        // Live tab -> LatestFragment
                break;
            case 2:
                fragment = new SubscriptionFragment();  // Subscription tab
                break;
            case 3:
                fragment = new FavoriteFragment();      // Bookmarks tab
                break;
            default:
                fragment = new FeedFragment();
                break;
        }

        if (fragment != null) {
            loadFragment(fragment);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.page_fragment, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ===== Toolbar Menu (Search) =====
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topbar_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setOnMenuItemClickListener(item -> {
            // Start SearchActivity when search icon is clicked
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
            overridePendingTransition(0, 0);
            return true;
        });

        return true;
    }
}