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

public class MapsActivity extends FragmentActivity implements ShuttleUpdater.OnMapStateUpdate {

    private static final String TAG = "MapsActivity";
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean firstTime = true;


    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private GoogleMap mMap = null; // Might be null if Google Play services APK is not available.
    MapState mapState;

    private ShuttleUpdater shuttleUpdater;

    //Shuttle Markers
    private Marker[] shuttleMarkers;
    private Marker shuttleMarker0;
    private Marker shuttleMarker1;
    private Marker shuttleMarker2;
    private Marker shuttleMarker3;

    //private SupportMapFragment mMapFragment;
    private MapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapState = MapState.get();

        shuttleMarkers = new Marker[4];
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
        mDrawerList.setAdapter(new DrawerAdapter(this, mapState.getDrawerItems()));
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
        /*
        Route 2:  Bus 6  &  Bus 7
        Key: "Bus 6" : shuttleMarker1
        Key: "Bus 7  : shuttleMarker2
        ---------------------------------
         Key: "Bus 6" : false
        Key: "Bus 7  : false

        if(dict.contains(shuttle.getName())
            dict[shuttle.getName()].setPosition(LatLng);
            //dict["Bus 6"].setPosition...
            boolDict[shuttle.getName()] = true;
        else
            dict.add(shuttle.getName() : new ShuttleMarkerOptions(...))


        if(boolDict[...] == false)
            ....Visible = false;

         */

        Shuttle[] shuttles = mapState.getShuttles();

        for (Shuttle shuttle : shuttles){
            switch(shuttle.getRouteID()){
                case 1:

                    break;
                case 2:

                    break;
                case 3:

                    break;
                default:
                    Log.d(TAG, "-------DEFAULT SWITCH-------");
            }
        }

        //Update Shuttle Markers

        for(int i = 0; i < shuttles.length; i++){
            shuttleMarkers[i].setPosition(new LatLng(shuttles[i].getLatitude(), shuttles[i].getLongitude()));
        }

        if(firstTime){
            Log.d(TAG, "setDrawerItems() CALLED");
            setDrawerItems();
            mDrawerList.setAdapter(new DrawerAdapter(this, mapState.getDrawerItems()));
            firstTime = false;
        }
        //Update InfoWindow RouteEstimates

    }


    private void setUpMapIfNeeded() {
        if(mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            mapState.setMap(mMap);

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

        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        /*

                VehicleId[ 4, 2, 1, 7]
                DictBoolSet initalize to false each time.

                for(Shuttle shuttle: Shuttles){
                    if(VehicleId.contains(shuttle.vehId))
                        dict[shuttle.vehId].setPosition(...)
                        DictBoolSet.add[VehId, true];
                    else
                        dict.add(vehId, new ShuttleMarker)
                        VehicleId.add(vehId);
                        DictBoolSet[VehId] = true;

                }

                for(Key in DictBoolSet){
                    if(DictBoolSet[Key] == false)
                        dict(Key).setVisible = false;


         */

        //Add Shuttle Markers
        Shuttle[] shuttles = mapState.getShuttles();
        for(int i = 0; i < shuttles.length; i++){
            shuttleMarkers[i] = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("booya"));
        }

    }

    private void setDrawerItems() {
        ArrayList<DrawerItem> drawerItems = new ArrayList<DrawerItem>();
        Shuttle[] shuttles = mapState.getShuttles();
        int i;

        //"Shuttles" section header
        drawerItems.add(new DrawerItem(0, "Shuttles"));

        //Shuttle items
        for(i = 0; i < 4; i ++){
            drawerItems.add(new DrawerItem(1, shuttles[i].getName(), shuttleMarkers[i]));
        }

        //"Stops" section header
        drawerItems.add(new DrawerItem(0, "Stops"));

        //Stop items   //TODO Add Stop Items
        for(i = 0; i < 8; i++){

            drawerItems.add(new DrawerItem(2, "Test Stop#" + i, shuttleMarkers[i%2]));
        }

        mapState.setDrawerItems(drawerItems);
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
        ArrayList<DrawerItem> drawerItems = mapState.getDrawerItems();
        // update selected item and title, then close the drawer
        Log.d(TAG, "selectItem[ " + position + " ]");
        mDrawerList.setItemChecked(position, true);
        setTitle(drawerItems.get(position).getTitle());

        switch (drawerItems.get(position).getTypeId()){
            case 0:

                break;
            case 1:
                mMap.animateCamera(CameraUpdateFactory.newLatLng(drawerItems.get(position).getLatLng()));
                drawerItems.get(position).getMarker().showInfoWindow();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 2:
                mMap.animateCamera(CameraUpdateFactory.newLatLng(drawerItems.get(position).getLatLng()));
                drawerItems.get(position).getMarker().showInfoWindow();
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            default:
                Log.d(TAG, "selectItem getTypeID DEFAULT CASE");


        }


    }

}
