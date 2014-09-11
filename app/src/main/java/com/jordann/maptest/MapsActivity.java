package com.jordann.maptest;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.nfc.Tag;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements InitialNetworkRequestor.OnInitialRequestComplete, ShuttleUpdater.OnMapStateUpdate {
    private static final String TAG = "MapsActivity", TAG_PD = "ProgressDialog";
    private static final String TAG_DB = "SpecialTag";

    private static final String sStopsUrl = "http://www.osushuttles.com/Services/JSONPRelay.svc/GetRoutesForMapWithSchedule";

    //Navigation Drawer variables
    private DrawerLayout mDrawerLayout;
    private ExpandableDrawerAdapter mAdapter;
    private ExpandableListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    //Map variables
    private GoogleMap mMap;
    private MapState mMapState;
    private final LatLng MAP_CENTER = new LatLng(44.563731, -123.279534);
    private final float MAP_ZOOM_LEVEL = 14.5f;

    //Repeating Async task class
    private ShuttleUpdater shuttleUpdater;

    //Async loading dialog. onStart: created  ,  updateMap: dismissed
    private static ProgressDialog sProgressDialog;

    private boolean networkAvailable = false;

    private InitialNetworkRequestor initialNetworkRequestor;

    private boolean firstTime = true;
    private static boolean errorShown = false;
    private static LinearLayout errorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "LIFECYCLE - onCreate");

        setContentView(R.layout.activity_main);

        errorLayout = (LinearLayout) findViewById(R.id.error_view);


        /*SpannableString s = new SpannableString("Beaver Bus Tracker");
        s.setSpan(new TypefaceSpan(this, "Gudea-Bold.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Update the action bar title with the TypefaceSpan instance
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(s);*/

        //Get the singleton MapState variable



        mMapState = MapState.get();
        mMapState.setCurrentContext(this);


        setUpMapIfNeeded();

        shuttleUpdater = ShuttleUpdater.get(this);


        sProgressDialog = ProgressDialog.show(this, "", "Loading...", true, false);


        initialNetworkRequestor = new InitialNetworkRequestor();
        initialNetworkRequestor.execute();

/*
        //getStops returns null on first run. onDestroy into another onCreate and getStops is no longer null. setUpMapIfNeeded is called in both cases.
        if(networkAvailable) {
            if (mMapState.getStops() == null) {
                //Async task that fetches stops data
                InitialStopsTask stopsTask = new InitialStopsTask(sStopsUrl, this);
                stopsTask.execute();
            } else {
                onPostStopsTask();
            }
        }


        //Async task that fetches Shuttle positions and estimates on interval
        shuttleUpdater = ShuttleUpdater.get(this);
*/
    }

    public void onPostInitialRequest(boolean success){
        if (success){
            sProgressDialog.dismiss();
            mMapState.setStopsMarkers();
            shuttleUpdater.startShuttleUpdater();
        } else {
            showNoConnectionDialog();
        }

    }

    public void onPostShuttleRequest(boolean success){
        Log.d(TAG, "onPostShuttleRequest");

        if(success){
            Log.d(TAG, "onPostShuttleRequest success");
            updateMap();
            if(errorLayout.getVisibility() == View.VISIBLE) {
                Log.d(TAG, "View was visible, setting slide out + invis");
                Animation animation = AnimationUtils.makeOutAnimation(this, true);
                animation.setDuration(1000);
                errorLayout.startAnimation(animation);
                errorLayout.setVisibility(View.INVISIBLE);
            }
            errorShown = false;
        }else{
            Log.d(TAG, "onPostShuttleRequest fail");
            sProgressDialog.dismiss();
            if(errorLayout.getVisibility() == View.INVISIBLE) {
                Log.d(TAG, "Error not shown, setting to visible");
                //Animation animationSlideLeft = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
                Animation animationSlideLeft = AnimationUtils.makeInAnimation(this, true);
                animationSlideLeft.setDuration(1000);

                errorLayout.startAnimation(animationSlideLeft);
                errorLayout.setVisibility(View.VISIBLE);
                Log.d(TAG, "visibility: "+errorLayout.getVisibility());
                errorShown = true;
            }
        }

    }


    /*
    public void onPostStopsTask(){

        if(networkAvailable) {
            shuttleUpdater.startShuttleUpdater();
        }else{
            sProgressDialog.dismiss();
            showNoConnectionDialog();
            //setUpMapIfNeeded();
        }
    }
*/
    /*
    public void retryConnection(){
        Log.d(TAG, "retryConnection");
        if(isNetworkAvailable()){
            networkAvailable = true;
            if (mMapState.getStops() == null) {
                //Async task that fetches stops data
                InitialStopsTask stopsTask = new InitialStopsTask(sStopsUrl, this);
                stopsTask.execute();
            } else {
                onPostStopsTask();
            }
            //shuttleUpdater.startShuttleUpdater();
        }else{
            networkAvailable = false;
            showNoConnectionDialog();
        }
    }
*/
    public void showNoConnectionDialog(){
        Log.d(TAG, "showNoConnectionDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Network unavailable")
                .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        initialNetworkRequestor = new InitialNetworkRequestor();
                        initialNetworkRequestor.execute();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        Log.d(TAG, "TEST NO LINE");
                    }
                })
                .setCancelable(false)
                .create();
        builder.show();
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "LIFECYCLE - onStart");
        initNavigationDrawer();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "LIFECYCLE - onPause");
        //if(networkAvailable) {
            shuttleUpdater.stopShuttleUpdater();
        //}
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "LIFECYCLE - onResume");


        if(!firstTime){
            shuttleUpdater.startShuttleUpdater();
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "LIFECYCLE - onStop");
        firstTime = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "LIFECYCLE - onRestart");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LIFECYCLE - onDestroy");

        mMap = null;
        mMapState.setMap(null);

        //mMapState.setDrawerItems(new ArrayList<DrawerItem>());
    }

    /*
    FUNCTION - updateMap()
       Callback from shuttleUpdater. Called each time shuttleLocs and routeEst are fetched.
       Sets positions and visibility of each shuttleMarker.
       Updates shuttle & stop objects with latest routeEstimates.
       Lastly, notifies navigationDrawer of these changes.
    */
    public void updateMap(){
        Log.d(TAG, "updateMap");
        Log.d(TAG_DB, "updateMap");
        //Update current marker list
        for (Shuttle shuttle : mMapState.getShuttles()) {
            //Log.d(TAG, "Shuttle status : " + shuttle.isOnline());
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
        mAdapter.notifyDataSetInvalidated();
        //mMapState.getAdapter().notifyDataSetInvalidated();
        if(mMapState.initDrawerItems()){

        }

        //Notify navigation drawer of data change, remove ProgressDialog, update complete


        sProgressDialog.dismiss();
    }

/*
    private void initNavigationDrawer() {
        Log.d(TAG, "initNavigationDrawer");
        Log.d(TAG_DB, "initNavigationDrawer");

//        mDrawerList = mMapState.getDrawerList();
//        mDrawerLayout = mMapState.getDrawerLayout();
//        mAdapter = mMapState.getAdapter();

        if (mDrawerLayout == null || mDrawerList == null) {
            Log.d(TAG, "initNavigationDrawer init");
            mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);

            mDrawerList = (ExpandableListView) this.findViewById(R.id.left_drawer);

            mDrawerList.setOnGroupClickListener(new DrawerItemClickListener(mDrawerLayout, mDrawerList));
            mDrawerList.setOnChildClickListener(new DrawerItemClickListener(mDrawerLayout, mDrawerList));



            Log.d(TAG, "LIST init adpater:" + mDrawerList.getExpandableListAdapter());
            Log.d(TAG, "init adpater:" + mAdapter);
            mAdapter = new ExpandableDrawerAdapter(this, mMapState.getDrawerItems());
            Log.d(TAG, "post adpater:" + mAdapter);
            mDrawerList.setAdapter(mAdapter);

            Log.d(TAG, "LIST post adpater:" + mDrawerList.getExpandableListAdapter());

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    invalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    invalidateOptionsMenu();
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();


            //Enable and show navigation drawer icon
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
            Log.d(TAG, "if groupCount: " + mAdapter.getGroupCount());
        }

//        mMapState.setDrawerList(mDrawerList);
//        mMapState.setDrawerLayout(mDrawerLayout);
//        mMapState.setAdapter(mAdapter);

    }

*/

    /*
    FUNCTION - initNavigationDrawer()
       Creates all components needed for navDrawer.
       Uses mapState.getDrawerItems for row data.
       Sets custom adapter so data can be refreshed when needed.
    */


    private void initNavigationDrawer() {
        Log.d(TAG, "initNavigationDrawer");
        Log.d(TAG_DB, "initNavigationDrawer");
        if (mDrawerLayout == null || mDrawerList == null) {
            Log.d(TAG, "initNavigationDrawer init");
            mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
            mDrawerList = (ExpandableListView) this.findViewById(R.id.left_drawer);

            mDrawerList.setOnGroupClickListener(new DrawerItemClickListener(mDrawerLayout, mDrawerList));
            mDrawerList.setOnChildClickListener(new DrawerItemClickListener(mDrawerLayout, mDrawerList));



            Log.d(TAG, "LIST init adpater:" + mDrawerList.getExpandableListAdapter());
            Log.d(TAG, "init adpater:" + mAdapter);
            mAdapter = new ExpandableDrawerAdapter(this, mMapState.getDrawerItems());
            Log.d(TAG, "post adpater:" + mAdapter);
            mDrawerList.setAdapter(mAdapter);

            Log.d(TAG, "LIST post adpater:" + mDrawerList.getExpandableListAdapter());

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    invalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    invalidateOptionsMenu();
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();


            //Enable and show navigation drawer icon
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
    }

    /*
    FUNCTION - setUpMapIfNeeded()
       If map does not already exist in mapState...
       Retrieves map from mapFragment. Initializes marker and map click listeners, sets infoAdapter, restricts map zoomLevel.
       Finally, performs initialization of key app objects.
    */
    public void setUpMapIfNeeded() {
        Log.d(TAG, "setUpMapIfNeeded...");
        Log.d(TAG_DB, "setUpMapIfNeeded");


        mMap = mMapState.getMap();
        if (mMap == null) {
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
                    if (mMapState.getSelectedStopMarker() != null && !mMapState.isStopsVisible()){
                        mMapState.setSelectedStopMarkerVisibility(false);
                        mMapState.setSelectedStopMarker(null);
                    }
                }
            });

            //Restricts zoom level to 13 and above. Can't zoom out that far.
            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    if (cameraPosition.zoom < 13){
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, 13));
                    }
                }
            });

            mMapState.setMap(mMap);

            mMapState.initShuttles();



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
                toggleStopsVisibility(item);
                return true;
            //TODO: create information window
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
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

        if (stopsVisible) item.setTitle("Hide Stops");
        else item.setTitle("Show Stops");

        for (Stop stop : mMapState.getStops()){
            stop.getMarker().setVisible(!stopsVisible);
        }
        mMapState.setStopsVisible(!stopsVisible);
    }


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


    public boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return(cm.getActiveNetworkInfo() != null);
    }

}