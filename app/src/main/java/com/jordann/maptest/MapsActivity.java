package com.jordann.maptest;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements ShuttleUpdater.OnMapStateUpdate, InitialStopsTask.OnStopsComplete {
    private static final String TAG = "MapsActivity";

    private static final String mStopsUrl = "http://www.osushuttles.com/Services/JSONPRelay.svc/GetRoutesForMapWithSchedule";

    //Navigation Drawer variables
    private DrawerLayout mDrawerLayout;
    private ExpandableDrawerAdapter mAdapter;
    private ExpandableListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private GoogleMap mMap;
    private MapState mMapState;
    private final LatLng MAP_CENTER = new LatLng(44.563731, -123.279534);
    private final float MAP_ZOOM_LEVEL = 14.5f;

    private ShuttleUpdater shuttleUpdater;

    private MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "LIFECYCLE - onCreate");

        setContentView(R.layout.activity_main);

        SpannableString s = new SpannableString("Beaver Bus Tracker");
        s.setSpan(new TypefaceSpan(this, "Gudea-Bold.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        // Update the action bar title with the TypefaceSpan instance
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(s);


        Log.d(TAG, "callingActivity : " + this);
        mMapState = MapState.get(this);


        if (mMapState.getStops() == null) {
            InitialStopsTask stopsTask = new InitialStopsTask(mStopsUrl, this);
            stopsTask.execute();
        } else {
            setUpMapIfNeeded();
        }
        //getActionBar().setTitle(" " +"Beaver Bus Tracker");
        // Log.d(TAG, "mMapState shuttles before: "+mMapState.getShuttles());
        //mMapState.initShuttles();
        // Log.d(TAG, "mMapState shuttles after: "+mMapState.getShuttles());
        //Asynchronous task that fetches Shuttle positions and estimates on interval
        shuttleUpdater = ShuttleUpdater.get(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);

        this.mDrawerList = (ExpandableListView) this.findViewById(R.id.left_drawer);
        Log.d(TAG, "mdrawerlayout : " + mDrawerLayout + " ; mdrawerList: " + mDrawerList);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "LIFECYCLE - onPause");
        shuttleUpdater.stopShuttleUpdater();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "LIFECYCLE - onResume");

        initNavigationDrawer();

        shuttleUpdater.startShuttleUpdater();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LIFECYCLE - onDestroy");
        mMap = null;
        mMapState.setMap(null);
        mMapState.setDrawerItems(new ArrayList<DrawerItem>());
        /*mDrawerList = null;
        mDrawerLayout = null;
        mAdapter = null;
        mDrawerToggle = null;
        finish();*/
        //  mMapState.setStops(null);
        // mMapState.destroyMapState();
        // mMapState = null;

    }

    public void updateMap() {
        Log.d(TAG, "updateMap");
        //TODO: shadow view and show ActivityIndicator until this happens
        //Update current marker list
        // ArrayList<Shuttle> shuttles = mMapState.getShuttles();
        for (Shuttle shuttle : mMapState.getShuttles()) {
            if (!shuttle.isOnline()) {
                shuttle.getMarker().setVisible(false);
            } else {
                shuttle.getMarker().setVisible(true);
            }
            if (shuttle.getLatLng() != shuttle.getMarker().getPosition()) {
                shuttle.updateMarker();
            }
            //TODO: Update InfoWindow RouteEstimates
        }
        mMapState.initStopsArrays();

        /*
        if (mMapState.initDrawerItems()) {  //If navigation drawer is newly initialized

        }
*/
        mMapState.initDrawerItems();

        mAdapter.notifyDataSetChanged();

        Log.d(TAG, "end updateMap");
    }

    private void initNavigationDrawer() {
        Log.d(TAG, "initNavigationDrawer: " + mDrawerLayout + " ; " + mDrawerList);
        if (/*mDrawerLayout == null && mDrawerList == null*/ true) {

            mDrawerList.setBackgroundColor(0xFF191b1b);   //0xFF191b1b

            Log.d(TAG, "adapter is: " + mDrawerList.getAdapter());

            Log.d(TAG, "POST Set: " + mDrawerLayout + " ; " + mDrawerList);

            //mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            mDrawerList.setOnGroupClickListener(new DrawerItemClickListener(mDrawerLayout, mDrawerList));
            mDrawerList.setOnChildClickListener(new DrawerItemClickListener(mDrawerLayout, mDrawerList));


            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    invalidateOptionsMenu();
                    Log.d(TAG, "mDrawerToggle closed: " + mDrawerToggle);
                }

                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    invalidateOptionsMenu();
                    Log.d(TAG, "mDrawerToggle open: " + mDrawerToggle);
                }
            };



            mDrawerLayout.setDrawerListener(mDrawerToggle);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);

            mAdapter = new ExpandableDrawerAdapter(this, mMapState.getDrawerItems());

            //ListAdapter mAdapterTest = new ListAdapter<DrawerItem>(this, R.layout.drawer_child_item, mMapState.getDrawerItems());
            Log.d(TAG, "DrawerItems: " + mMapState.getDrawerItems().toString());
            //mDrawerList.setAdapter(mAdapter);
            mDrawerList.setAdapter(mAdapter);
            //mDrawerList.setAdapter(mAdapterTest);

            mDrawerToggle.syncState();
        }

    }

    public void setUpMapIfNeeded() {
        Log.d(TAG, "setUpMapIfNeeded...");

        mMap = mMapState.getMap();

        Log.d(TAG, "Got the map from mapstate");

        if (mMap == null) {
            Log.d(TAG, "Needed...");
            MapFragment mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
            mapFragment.setRetainInstance(true);

            mMap = mapFragment.getMap();
            mMap.clear();

            if (mMap != null) {
                setUpRouteLines();
                mMap.setMyLocationEnabled(true);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MAP_CENTER, MAP_ZOOM_LEVEL));
            }
            mMap.setInfoWindowAdapter(new MapInfoWindowAdapter(this));
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (mMapState.getSelectedStopMarker() != null && !mMapState.isStopsVisible()){
                        mMapState.setSelectedStopMarkerVisibility(false);
                    }

                    mMapState.animateMap(marker.getPosition());
                    marker.setVisible(true);
                    marker.showInfoWindow();
                    //TODO: are we really using flat?
                    if (!marker.isFlat())
                        mMapState.setSelectedStopMarker(marker);
                    return true;
                }
            });


            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    Log.d(TAG, "outside onmapclick if : "+mMapState.getSelectedStopMarker() + "; global visibility: "+mMapState.isStopsVisible());
                    if (mMapState.getSelectedStopMarker() != null && !mMapState.isStopsVisible()){
                        //selectedStopMarker.setVisible(!selectedStopMarker.isVisible());
                        Log.d(TAG, "Got to onMapClick selectedstop");
                        mMapState.setSelectedStopMarkerVisibility(false);
                        mMapState.setSelectedStopMarker(null);
                    }
                }
            });


            mMapState.setMap(mMap);
            mMapState.initShuttles();
            mMapState.setStopsMarkers();

        }



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");


        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.toggle_stops:
                //do stuff
                toggleStopsVisibility(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");
        if (mMapState != null){
            if (mMapState.isStopsVisible())
                menu.findItem(R.id.toggle_stops).setTitle("Hide Stops");
            else
                menu.findItem(R.id.toggle_stops).setTitle("Show Stops");
        }
        return super.onPrepareOptionsMenu(menu);
    }


    public void toggleStopsVisibility(MenuItem item){
        boolean stopsVisible = mMapState.isStopsVisible();
        mMapState.setStopsVisible(!stopsVisible);

        if (mMapState.isStopsVisible()) item.setTitle("Hide Stops");
        else item.setTitle("Show Stops");

        Log.d(TAG, "map global vis was: "+stopsVisible+"; set to: "+mMapState.isStopsVisible());

        for (Stop stop : mMapState.getStops()){
            stop.getMarker().setVisible(!stopsVisible);
        }

    }

/*

  H         72x72
    M       48x48
XH          96x96
XXH         144x144
    */

    public void setUpRouteLines() {
        Log.d(TAG, "setUpRouteLines");
        //NORTH ROUTE
        PolylineOptions rectOptionsNorth = new PolylineOptions()
                .add(new LatLng(44.566792, -123.289718)).add(new LatLng(44.566783, -123.284842))
                .add(new LatLng(44.566799, -123.284738)).add(new LatLng(44.566798, -123.284360))
                .add(new LatLng(44.567408, -123.284354)).add(new LatLng(44.567685, -123.284553))
                .add(new LatLng(44.567904, -123.284555)).add(new LatLng(44.567957, -123.279962))
                .add(new LatLng(44.566784, -123.279930)).add(new LatLng(44.566765, -123.272398))
                .add(new LatLng(44.565833, -123.272961)).add(new LatLng(44.564669, -123.274050))
                .add(new LatLng(44.564643, -123.275300)).add(new LatLng(44.564635, -123.279935))
                .add(new LatLng(44.564650, -123.284575)).add(new LatLng(44.564590, -123.289720))
                .add(new LatLng(44.566792, -123.289718));
        Polyline polylineNorth = mMap.addPolyline(rectOptionsNorth);
        polylineNorth.setColor(0xBD70A800);

        //SOUTH ROUTE
        PolylineOptions rectOptionsEast = new PolylineOptions()
                .add(new LatLng(44.564507, -123.274058)).add(new LatLng(44.564489, -123.275318))
                .add(new LatLng(44.564495, -123.280051)).add(new LatLng(44.564158, -123.280016))
                .add(new LatLng(44.563829, -123.279917)).add(new LatLng(44.563401, -123.279700))
                .add(new LatLng(44.563371, -123.279686)).add(new LatLng(44.561972, -123.279700))
                .add(new LatLng(44.560713, -123.279700)).add(new LatLng(44.560713, -123.281585))
                .add(new LatLng(44.560538, -123.282356)).add(new LatLng(44.559992, -123.282962))
                .add(new LatLng(44.559296, -123.283010)).add(new LatLng(44.558409, -123.281948))
                .add(new LatLng(44.558455, -123.280609)).add(new LatLng(44.559033, -123.279740))
                .add(new LatLng(44.557859, -123.279679)).add(new LatLng(44.559460, -123.276646))
                .add(new LatLng(44.559873, -123.273996)).add(new LatLng(44.561578, -123.274318))
                .add(new LatLng(44.562113, -123.274114)).add(new LatLng(44.564507, -123.274058));
        Polyline polylineEast = mMap.addPolyline(rectOptionsEast);
        polylineEast.setColor(0xBDE0AA0F);

        //EAST ROUTE
        PolylineOptions rectOptionsWest = new PolylineOptions()
                .add(new LatLng(44.558993, -123.279550)).add(new LatLng(44.561972, -123.279550))
                .add(new LatLng(44.563391, -123.279526)).add(new LatLng(44.563401, -123.279520))
                .add(new LatLng(44.563829, -123.279737)).add(new LatLng(44.564158, -123.279826))
                .add(new LatLng(44.564495, -123.279901)).add(new LatLng(44.564500, -123.284775))
                .add(new LatLng(44.562234, -123.284775)).add(new LatLng(44.561965, -123.284625))
                .add(new LatLng(44.560529, -123.284625)).add(new LatLng(44.560538, -123.282576))
                .add(new LatLng(44.560012, -123.283142)).add(new LatLng(44.559246, -123.283160))
                .add(new LatLng(44.558254, -123.281967)).add(new LatLng(44.558305, -123.280559))
                .add(new LatLng(44.558993, -123.279550));
        Polyline polylineWest = mMap.addPolyline(rectOptionsWest);
        polylineWest.setColor(0xBDAA66CD);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }



}