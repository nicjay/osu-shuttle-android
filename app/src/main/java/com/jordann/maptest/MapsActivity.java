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
import android.widget.ExpandableListView;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements ShuttleUpdater.OnMapStateUpdate {


    private static final String TAG = "MapsActivity";

    private static final String mStopsUrl = "http://www.osushuttles.com/Services/JSONPRelay.svc/GetRoutesForMapWithSchedule";


    private static final String KEY_FIRST_TIME = "first_time";

    private DrawerLayout mDrawerLayout;
    private ExpandableDrawerAdapter mAdapter;
    //private ListView mDrawerList;
    private ExpandableListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean firstTime = true;

    private GoogleMap mMap = null; // Might be null if Google Play services APK is not available.
    private MapState mMapState;

    private ShuttleUpdater shuttleUpdater;

    Bundle savedInstanceState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.savedInstanceState = savedInstanceState;
        if(savedInstanceState != null){
            firstTime = savedInstanceState.getBoolean(KEY_FIRST_TIME);
        }

        mMapState = MapState.get();
        setUpMapIfNeeded();

        Log.d(TAG, "onCreate getStops : " + mMapState.getStops());
        if (mMapState.getStops() == null){
            InitialStopsTask stopsTask = new InitialStopsTask(mStopsUrl);
            stopsTask.execute();
        }

        mMapState.initShuttles();


        //ASYNC Requests
        shuttleUpdater = ShuttleUpdater.get(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        shuttleUpdater.stopShuttleUpdater();
    }

    @Override
    protected void onResume() {
        super.onResume();
       // setUpStops();
        shuttleUpdater.startShuttleUpdater();
       // setUpMapIfNeeded();

        Log.d(TAG, "onResume");
    }

    /*
    private void setUpStops(){
        ArrayList<Stop> stops = new ArrayList<Stop>();
        stops.add(new Stop(44.55832, -123.28162, "Reser Stadium", new int[]{-1,-1,-1,-1}));
        stops.add(new Stop(44.560524,-123.282411, "Ralph Miller Way", new int[]{-1,-1,-1,-1}));
        stops.add(new Stop(44.56344,-123.27964, "Dixon Rec Center", new int[]{-1,-1,-1,-1}));
        mMapState.setStops(stops);

    }
    */

    private void initNavigationDrawer(){
        if(mDrawerLayout == null && mDrawerList == null) {
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer);


            // set a custom shadow that overlays the main content when the drawer opens
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            // set up the drawer's list view with items and click listener
            //mAdapter = new DrawerAdapter(this, mMapState.getDrawerItems());


            mDrawerList.setOnGroupClickListener(new DrawerItemClickListener());
            mDrawerList.setOnChildClickListener(new DrawerItemClickListener());

            ExpandableListView listView = (ExpandableListView) findViewById(R.id.left_drawer);


            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    mDrawerLayout,         /* DrawerLayout object */
                    R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                    R.string.drawer_open,  /* "open drawer" description for accessibility */
                    R.string.drawer_close  /* "close drawer" description for accessibility */
            ) {
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    //getActionBar().setTitle(mTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    //getActionBar().setTitle(mDrawerTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()'

                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
            mDrawerToggle.syncState();
        }

    }

    public void updateMap() {
        Log.d(TAG, "UPDATE MAP : " + mMapState.getMap());
        //TODO: shadow view and show ActivityIndicator until this happens

      //  ArrayList<Shuttle> shuttles = mMapState.getShuttles();

        //Update current marker list
        ArrayList<Shuttle> shuttles = mMapState.getShuttles();
        Log.d(TAG, "Shuttles: " + shuttles);
        //for(int i = 0, len = shuttles.size(); i < len; i++){
        //Shuttle shuttle = shuttles.get(i);
        for(Shuttle shuttle : mMapState.getShuttles()) {
            Log.d(TAG, "Shuttle onlineBool: " + shuttle.isOnline());
            if (!shuttle.isOnline()) {
                shuttle.getMarker().setVisible(false);
                //TODO: see if really set
            } else {
                shuttle.getMarker().setVisible(true);

            }

            if (shuttle.getLatLng() != shuttle.getMarker().getPosition()) {
                shuttle.updateMarker();
            }
            //Update InfoWindow RouteEstimates
        }
        mMapState.initStops();
        initNavigationDrawer();
        if (mMapState.initDrawerItems()) {
            mAdapter = new ExpandableDrawerAdapter(this, mMapState.getDrawerItems());
            mDrawerList.setAdapter(mAdapter);

        }
        mAdapter.notifyDataSetChanged();

    }


    private void setUpMapIfNeeded() {
        //mMapState.clearShuttleMarkerArrayList();
        //mMap = mMapState.getMap();

        Log.d(TAG, "setUpMapIfNeeded");
        if(mMap == null) {
            MapFragment mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
            mapFragment.setRetainInstance(true);

            mMap = mapFragment.getMap();
            mMap.clear();

            mMapState.setMap(mMap);


            if(mMap != null){
                setUpRouteLines();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.563731, -123.279534), 14.5f));
                mMap.setMyLocationEnabled(true);

            }
            mMap.setInfoWindowAdapter(new MapInfoWindowAdapter(this));
            mMapState.setMap(mMap);
        }


    }


    public void setUpRouteLines(){

        // Instantiates a new Polyline object and adds points to define a rectangle


        //NORTH ROUTE
        PolylineOptions rectOptionsNorth = new PolylineOptions()
                .add(new LatLng(44.566792,-123.289718))
                .add(new LatLng(44.566783,-123.284842))
                .add(new LatLng(44.566799,-123.284738))
                .add(new LatLng(44.566798,-123.284360))
                .add(new LatLng(44.567408,-123.284354))
                .add(new LatLng(44.567685,-123.284553))
                .add(new LatLng(44.567904,-123.284555))
                .add(new LatLng(44.567957,-123.279962))
                .add(new LatLng(44.566784,-123.279930))
                .add(new LatLng(44.566765,-123.272398))
                .add(new LatLng(44.565833,-123.272961))
                .add(new LatLng(44.564669,-123.274050))
                .add(new LatLng(44.564643,-123.275300))
                .add(new LatLng(44.564635,-123.279935))
                .add(new LatLng(44.564650,-123.284575))
                .add(new LatLng(44.564590,-123.289720))
                .add(new LatLng(44.566792,-123.289718));


        // Get back the mutable Polyline
        Polyline polylineNorth = mMap.addPolyline(rectOptionsNorth);
        polylineNorth.setColor(0xBD70A800);


        //SOUTH ROUTE
        PolylineOptions rectOptionsEast = new PolylineOptions()
                .add(new LatLng(44.564507,-123.274058))
                .add(new LatLng(44.564489,-123.275318))
                .add(new LatLng(44.564495,-123.280051))
                .add(new LatLng(44.564158,-123.280016))
                .add(new LatLng(44.563829,-123.279917))
                .add(new LatLng(44.563401,-123.279700))
                .add(new LatLng(44.563371,-123.279686))
                .add(new LatLng(44.561972,-123.279700))
                .add(new LatLng(44.560713,-123.279700))
                .add(new LatLng(44.560713,-123.281585))
                .add(new LatLng(44.560538,-123.282356))
                .add(new LatLng(44.559992,-123.282962))
                .add(new LatLng(44.559296,-123.283010))
                .add(new LatLng(44.558409,-123.281948))
                .add(new LatLng(44.558455,-123.280609))
                .add(new LatLng(44.559033,-123.279740))
                .add(new LatLng(44.557859,-123.279679))
                .add(new LatLng(44.559460,-123.276646))
                .add(new LatLng(44.559873,-123.273996))
                .add(new LatLng(44.561578,-123.274318))
                .add(new LatLng(44.562113,-123.274114))
                .add(new LatLng(44.564507,-123.274058));

        Polyline polylineEast = mMap.addPolyline(rectOptionsEast);
        polylineEast.setColor(0xBDE0AA0F);

        //EAST ROUTE
        PolylineOptions rectOptionsWest = new PolylineOptions()
                .add(new LatLng(44.558993,-123.279550))
                .add(new LatLng(44.561972,-123.279550))
                .add(new LatLng(44.563391,-123.279526))
                .add(new LatLng(44.563401,-123.279520))
                .add(new LatLng(44.563829,-123.279737))
                .add(new LatLng(44.564158,-123.279826))
                .add(new LatLng(44.564495,-123.279901))
                .add(new LatLng(44.564500,-123.284775))
                .add(new LatLng(44.562234,-123.284775))
                .add(new LatLng(44.561965,-123.284625))
                .add(new LatLng(44.560529,-123.284625))
                .add(new LatLng(44.560538,-123.282576))
                .add(new LatLng(44.560012,-123.283142))
                .add(new LatLng(44.559246,-123.283160))
                .add(new LatLng(44.558254,-123.281967))
                .add(new LatLng(44.558305,-123.280559))
                .add(new LatLng(44.558993,-123.279550));

        Polyline polylineWest = mMap.addPolyline(rectOptionsWest);
        polylineWest.setColor(0xBDAA66CD);



    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_FIRST_TIME, firstTime);


        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onDestroy() {
        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(KEY_FIRST_TIME)){
                savedInstanceState.remove(KEY_FIRST_TIME);
            }
        }

        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        mDrawerToggle.syncState();
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
        return super.onPrepareOptionsMenu(menu);
    }

/*

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
*/

    private class DrawerItemClickListener implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {

        @Override
        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

            selectItemGroup(groupPosition);

            return false;
        }

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            selectItemChild(groupPosition, childPosition);
            return false;
        }

    }







    private void selectItemGroup(int groupPosition) {
        // update the main content by replacing fragments
        ArrayList<DrawerItem> drawerItems = mMapState.getDrawerItems();
        // update selected item and title, then close the drawer
        //mDrawerList.setItemChecked(groupPosition, true);
       // setTitle(drawerItems.get(groupPosition).getTitle());

        switch (drawerItems.get(groupPosition).getTypeId()){
            case 0://Section

                break;
            case 1://Shuttle
                mMapState.animateMap(drawerItems.get(groupPosition).getShuttle().getLatLng());
                Log.d(TAG, "Marker: " + drawerItems.get(groupPosition).getShuttle().getMarker());
                drawerItems.get(groupPosition).getShuttle().getMarker().showInfoWindow();
               // mMapState.showInfoWindow(groupPosition);
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case 2://Route

                break;
            default:

        }
    }
    private void selectItemChild(int groupPosition, int childPosition){
        ArrayList<DrawerItem> drawerItems = mMapState.getDrawerItems();
        ArrayList<Stop> stops = mMapState.getStops();

        Log.d(TAG, "ch "+ childPosition +" : stopsIndex for drawerItem#" + groupPosition + " is " + drawerItems.get(groupPosition).getStopsIndex() );
        int index = drawerItems.get(groupPosition).getStopsIndex().get(childPosition);
        mMapState.animateMap(stops.get(index).getLatLng());
        //mMapState.showInfoWindow(position);
        stops.get(index).getMarker().showInfoWindow();
        mDrawerLayout.closeDrawer(mDrawerList);
    }
}
