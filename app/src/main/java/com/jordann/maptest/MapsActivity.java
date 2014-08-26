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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements ShuttleUpdater.OnMapStateUpdate {


    private static final String TAG = "MapsActivity";
    private DrawerLayout mDrawerLayout;
    private DrawerAdapter mAdapter;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean firstTime = true;




    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private GoogleMap mMap = null; // Might be null if Google Play services APK is not available.
    private MapState mMapState;

    private ShuttleUpdater shuttleUpdater;

    //Shuttle Markers
    private Marker[] shuttleMarkers;

    //private SupportMapFragment mMapFragment;
    private MapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapState = MapState.get();

        //shuttleMarkers = new Marker[4];


        setUpMapIfNeeded();
        setUpNavigationDrawer();

        //ASYNC Requests
        shuttleUpdater = new ShuttleUpdater(this);



    }

    private void setUpNavigationDrawer(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);


        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new DrawerAdapter(this, mMapState.getDrawerItems()));
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

    }

    public void updateMap() {
        Log.d(TAG, "UPDATE MAP");
        //TODO: shadow view and show ActivityIndicator until this happens

        ArrayList<Shuttle> shuttles = mMapState.getShuttles();

        //Update current marker list
        for(Shuttle shuttle : shuttles){

            ArrayList<ShuttleMarker> shuttleMarkerArrayList = mMapState.getShuttleMarkerList();
            boolean foundMatch = false;
            for (ShuttleMarker marker : shuttleMarkerArrayList){
                if (marker.getVehicleId() == shuttle.getVehicleId()){
                    mMapState.updateShuttleMarker(shuttleMarkerArrayList.indexOf(marker), shuttle.getLatLng());
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch){
                Log.d(TAG, "Adding new shuttleMarker");
                mMapState.addShuttleMarker(shuttle.getLatLng(), shuttle.getVehicleId());

            }
        }

        ArrayList<ShuttleMarker> shuttleMarkerArrayList = mMapState.getShuttleMarkerList();
        boolean foundMatchToRemove;
        ShuttleMarker marker1 = null;
        for (ShuttleMarker marker : shuttleMarkerArrayList){
            if(marker.getVehicleId() == 7){
                marker1 = marker;
            }
            foundMatchToRemove = true;
            for (Shuttle shuttle : shuttles){
                //Log.d(TAG, "markerVehId: " + marker.getVehicleId() +"      shuttleVehId:" + shuttle.getVehicleId());
                if (marker.getVehicleId() == shuttle.getVehicleId()){
                    foundMatchToRemove = false;
                    break;
                }
            }
            if (foundMatchToRemove){
                Log.d(TAG, "FoundMatchToRemove");
                Random random = new Random();
                if(random.nextInt(5) == 4){
                    Log.d(TAG, "RANDOM HIT 4");
                    mMapState.removeDrawerItem(marker1.getVehicleId());
                }



                mMapState.removeShuttleMarker(marker);
                mMapState.removeDrawerItem(marker.getVehicleId());
                mAdapter.notifyDataSetChanged();
            }
        }

        if(firstTime){
            Log.d(TAG, "setDrawerItems() CALLED");
            mMapState.setDrawerItems();
            mAdapter = new DrawerAdapter(this, mMapState.getDrawerItems());
            mDrawerList.setAdapter(mAdapter);
            firstTime = false;
        }else{
            mMapState.addDrawerItem();
            mAdapter.notifyDataSetChanged();
        }

        //Update InfoWindow RouteEstimates

    }


    private void setUpMapIfNeeded() {

        if(mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            //mapState.setMap(mMap);

            if(mMap != null){
               // setUpMap();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.563731, -123.279534), 14.5f));

                mMap.setMyLocationEnabled(true);

            }

        }

        mMapState.setMap(mMap);
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
        Log.d(TAG, "----------RESUME----------");
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
        ArrayList<DrawerItem> drawerItems = mMapState.getDrawerItems();
        // update selected item and title, then close the drawer
        Log.d(TAG, "selectItem[ " + position + " ]");
        mDrawerList.setItemChecked(position, true);
        setTitle(drawerItems.get(position).getTitle());

        switch (drawerItems.get(position).getTypeId()){
            case 0:

                break;
            case 1:
                Log.d(TAG, "Title: " + drawerItems.get(position).getTitle());
                mMapState.animateMap(drawerItems.get(position).getLatLng());
               // drawerItems.get(position).getMarker().showInfoWindow();
                mMapState.showInfoWindow(position);
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 2:
                mMapState.animateMap(drawerItems.get(position).getLatLng());
                //drawerItems.get(position).getMarker().showInfoWindow();
                mMapState.showInfoWindow(position);
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            default:
                Log.d(TAG, "selectItem getTypeID DEFAULT CASE");


        }


    }

}
