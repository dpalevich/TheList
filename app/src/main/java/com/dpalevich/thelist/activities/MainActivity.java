package com.dpalevich.thelist.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.dpalevich.thelist.R;
import com.dpalevich.thelist.fragments.BandsFragment;
import com.dpalevich.thelist.fragments.BaseFragment;
import com.dpalevich.thelist.fragments.CalendarFragment;
import com.dpalevich.thelist.fragments.FavoritesFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (null == savedInstanceState) {
            BandsFragment bandsFragment = new BandsFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.content_container, bandsFragment, bandsFragment.getClass().getName());
            ft.commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Class<? extends BaseFragment> nextFragmentClass = null;

        if (id == R.id.nav_bands) {
            nextFragmentClass = BandsFragment.class;
        } else if (id == R.id.nav_favorites) {
            nextFragmentClass = FavoritesFragment.class;
        } else if (id == R.id.nav_calendar) {
            nextFragmentClass = CalendarFragment.class;
        } else if (id == R.id.nav_update) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            dumpCurrentFragments();
        }

        if (null != nextFragmentClass) {
            navigateToFragment(nextFragmentClass);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void navigateToFragment(Class<? extends BaseFragment> nextFragmentClass) {
        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment;
        //Fragment currentFragment = getCurrentFragment(fm);

        if (null != (fragment = fm.findFragmentByTag(nextFragmentClass.getName()))) {
            if (fragment.isAdded() && fragment.isVisible()) {
                System.out.println("Already current fragment");
                return;
            }
        }

        try {
            fragment = nextFragmentClass.newInstance();
        } catch (InstantiationException|IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content_container, fragment, fragment.getClass().getName());
        ft.commit();
    }

    private static @Nullable Fragment getCurrentFragment(@NonNull FragmentManager fm) {
        List<Fragment> fragments = fm.getFragments();
        for (Fragment fragment : fragments) {
            if (null != fragment && fragment.isAdded() && fragment.isVisible()) {
                return fragment;
            }
        }
        return null;
    }

    private void dumpCurrentFragments() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (null == fragment) {
                System.out.println("null fragment");
            } else {
                System.out.println(fragment.getClass().getName() + ", added=" + fragment.isAdded() + ", visible=" + fragment.isVisible());
            }
        }
    }
}
