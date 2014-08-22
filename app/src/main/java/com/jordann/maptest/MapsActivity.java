package com.jordann.maptest;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity {

    private static final String TAG = "MapsActivity";
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private GoogleMap mMap = null; // Might be null if Google Play services APK is not available.

    private ShuttleUpdater shuttleUpdater;
    private String[] mTestArray;

    //private SupportMapFragment mMapFragment;
    private MapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpMapIfNeeded();


        mTestArray = new String[]{"TEST1", "Test2", "Test3"};
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);


        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mTestArray));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
                Log.d(TAG, "DRAWER isAvailable: " + isAvailable);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()'

            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        //ASYNC Requests
        shuttleUpdater = new ShuttleUpdater();


    }

    private void setUpMapIfNeeded() {
        if(mMap == null){
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

            if(mMap != null){
                setUpMap();
            }

        }
    }


    private void setUpMap() {
        //LatLngBounds bounds = new LatLngBounds(new LatLng(44.556911, -123.289607), new LatLng(44.568181, -123.267406));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.563731, -123.279534), 14.5f));

        mMap.setMyLocationEnabled(true);

        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        /*




         */

    }

    @Override
    protected void onPause() {
        super.onPause();
        shuttleUpdater.stopShuttleUpdater();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        shuttleUpdater.startShuttleUpdater();
        Log.d(TAG, "----------RESUME----------\n----------RESUME----------\n----------RESUME----------\n");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        Log.d(TAG, "drawerOpen bool: " + drawerOpen);

        return super.onPrepareOptionsMenu(menu);
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mTestArray[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

}
