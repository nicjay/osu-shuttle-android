package edu.oregonstate.beaverbus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/*
    CLASS- MapsActivty
        Handles the overall order of events within the app lifecycle.
        Initialization > Updating > Shut Down
*/
public class MapsActivity extends FragmentActivity implements InitialNetworkRequestor.OnInitialRequestComplete, ShuttleUpdater.OnMapStateUpdate {
    private static final String TAG = "MapsActivity";

    //Map and Singleton variables
    private GoogleMap mMap;
    private MapState mMapState;
    private final LatLng MAP_CENTER = new LatLng(44.563731, -123.279534);
    private final float MAP_ZOOM_LEVEL = 14.5f;

    private static LinearLayout progressSpinner;

    //Navigation Drawer variables
    private DrawerLayout mDrawerLayout;
    private static ExpandableDrawerAdapter mAdapter;
    private ExpandableListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    //HTTP Request Interface
    private InitialNetworkRequestor initialNetworkRequestor;
    private ShuttleUpdater shuttleUpdater;

    //Various shown dialogs
    private static Dialog busInfoDialog;
    private static ProgressDialog sProgressDialog;
    private static AlertDialog networkFailureDialog;
    private static LinearLayout errorLayout;

    //TextView displayed at top of screen
    private static TextView selectedStopTitle;

    //Route Lines
    Polyline polylineWest;
    Polyline polylineNorth;
    Polyline polylineEast;


    //Options Menu
    public static Menu menuGlobal;

    private FavoriteManager favoriteManager;
    private SelectedMarkerManager selectedMarkerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "LIFECYCLE - onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //View references within main screen
        errorLayout = (LinearLayout) findViewById(R.id.error_view);
        selectedStopTitle = ((TextView)findViewById(R.id.selected_stop));
        selectedStopTitle.setVisibility(View.INVISIBLE);


        favoriteManager = new FavoriteManager(this, getApplicationContext(), selectedMarkerManager);



        //Get singleton, set context
        mMapState = MapState.get();
        mMapState.setCurrentContext(this);



        //Initialization
        setUpMapIfNeeded();



        selectedMarkerManager = new SelectedMarkerManager(this);
        mMapState.setSelectedMarkerManager(selectedMarkerManager);

        //Show Loading Dialog
        sProgressDialog = new ProgressDialog(this, R.style.CustomDialog);
        sProgressDialog.setMessage("Loading...");
        sProgressDialog.setCancelable(false);
        //sProgressDialog.show();
        //sProgressDialog = ProgressDialog.show(this, "", "Loading...", true, false);
        //sProgressDialog.setProgressStyle(R.style.CustomDialog);

        progressSpinner = (LinearLayout)findViewById(R.id.progress_spinner);
        progressSpinner.setVisibility(View.VISIBLE);


        //Start initial HTTP request
        initialNetworkRequestor = new InitialNetworkRequestor();
        initialNetworkRequestor.execute();

        //Initialize shuttleUpdater for subsequent HTTP requests
        shuttleUpdater = ShuttleUpdater.get(this);

        initNetworkFailureDialog();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "LIFECYCLE - onStart");
        super.onStart();

        initNavigationDrawer();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "LIFECYCLE - onResume");
        super.onResume();

        Log.d(TAG, "$# firstTime: " + mMapState.isFirstTime());

        if(!mMapState.isFirstTime()){
            shuttleUpdater.startShuttleUpdater();
            //mAdapter.notifyDataSetInvalidated();

            for(Shuttle shuttle : mMapState.getShuttles()){
                Log.d(TAG, "$# shuttle : " + shuttle.getName() + " = " + shuttle.isOnline() );
            }

            //For shuttle move from (0,0) to actual coords, don't animate.
            mMapState.noAnimate = true;
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "LIFECYCLE - onPause");
        super.onPause();

        shuttleUpdater.stopShuttleUpdater();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "LIFECYCLE - onStop");
        super.onStop();

        mMapState.setFirstTime(false);
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "LIFECYCLE - onRestart");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "LIFECYCLE - onDestroy");
        super.onDestroy();
        selectedMarkerManager.setSelectedMarker(null, false);
        menuGlobal = null;
        mMap = null;
        mMapState.setMap(null);
    }

    /*
    METHOD - setUpMapIfNeeded()
       If map does not already exist in mapState...
       Retrieves map from mapFragment. Initializes marker and map click listeners, sets infoAdapter, restricts map zoomLevel.
       Finally, performs initialization of key app objects.
    */
    public void setUpMapIfNeeded() {
        Log.d(TAG, "setUpMapIfNeeded");

        mMap = mMapState.getMap();
        if (mMap == null) { //Saved map is null. Recreate
            final MapFragment mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
            mapFragment.setRetainInstance(true);
            mMap = mapFragment.getMap();
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.clear();

            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MAP_CENTER, MAP_ZOOM_LEVEL));
            }

            //Set infoWindowAdapter, and onClickListeners
            mMap.setInfoWindowAdapter(new MapInfoWindowAdapter(this));
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    mMapState.animateMap(marker.getPosition());
                }
            });
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    Log.d(TAG, "!@# markerClick. Map : " + mMap);
                   return onMapMarkerClick(marker);
                }
            });

            //Deselect marker by touching anywhere else on map
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    selectedMarkerManager.onMapClick();
                    favoriteManager.setFavIcon(favoriteManager.FAV_ICON_DISABLED);
                }
            });

            //Restricts zoom level to 13 and above. Can't zoom out that far.
            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    if (cameraPosition.zoom < 13){
                        //TODO: put this back
                         mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, 13));
                    }
                }
            });

            mMapState.setMap(mMap);
            mMapState.initShuttles();
        }
    }
    /*
    METHOD - initNavigationDrawer()
       Creates all components needed for navDrawer.
       Uses mapState.getDrawerItems for row data.
       Sets custom adapter so data can be refreshed when needed.
    */
    private void initNavigationDrawer() {
        Log.d(TAG, "initNavigationDrawer");

        if (mDrawerLayout == null || mDrawerList == null) {
            //Get views
            mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
            mDrawerList = (ExpandableListView) this.findViewById(R.id.left_drawer);

            //Set onClickListeners
            mDrawerList.setOnGroupClickListener(new DrawerItemClickListener(this, mDrawerLayout, mDrawerList));
            mDrawerList.setOnChildClickListener(new DrawerItemClickListener(this, mDrawerLayout, mDrawerList));

            //NavDrawer Initialization
            mAdapter = new ExpandableDrawerAdapter(this, mMapState.getDrawerItems());
            mDrawerList.setAdapter(mAdapter);
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    //invalidateOptionsMenu();
                }
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    mAdapter.notifyDataSetInvalidated();
                    //invalidateOptionsMenu();
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
    METHOD - onPostInitialRequest()
        Callback, handles the success or failure result of the initial stop+shuttle request.
     */
    public void onPostInitialRequest(boolean success){
        Log.d(TAG, "..! ON POST INITIAL requEST");
        if (success){
            Log.d(TAG, "&& onPostInitialRequest success");
            mMapState.readConfigurationFile();
            mMapState.setStopsMarkers();
            selectedMarkerManager.setPolylines();
            shuttleUpdater.startShuttleUpdater();
        } else {
            Log.d(TAG, "onPostInitialRequest fail");
            networkFailureDialog.show();
        }
    }


    /*
    METHOD - onPostShuttleRequest()
        Callback, handles the success or failure result of a shuttle request event.
        The general updater function is called if needed.
        Error views and loading dialogs are added or removed accordingly.
     */
    public void onPostShuttleRequest(boolean success){
        Log.d(TAG, "..! ON POST SHUTTLE requEST");
        if(success){
            Log.d(TAG, "onPostShuttleRequest success");
            favoriteManager.initSavedFavorites();
            updateMap();
            if(errorLayout.getVisibility() == View.VISIBLE) {
                //Remove the error view if it was there
                Animation animation = AnimationUtils.makeOutAnimation(this, true);
                animation.setDuration(1000);
                errorLayout.startAnimation(animation);
                errorLayout.setVisibility(View.INVISIBLE);

                //Set the stop title back to normal
                RelativeLayout.LayoutParams selectedStopTitleParams = (RelativeLayout.LayoutParams)selectedStopTitle.getLayoutParams();
                //selectedStopTitleParams.setMargins(0, 15, 55, 0);
                selectedStopTitleParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                selectedStopTitleParams.addRule(RelativeLayout.BELOW, 0);
                selectedStopTitle.setLayoutParams(selectedStopTitleParams);
            }
        }else{
            Log.d(TAG, "onPostShuttleRequest fail");
            sProgressDialog.dismiss();

            hideProgressSpinner();
            if(errorLayout.getVisibility() == View.INVISIBLE && !networkFailureDialog.isShowing()) {
                //Show the error view
                Animation animationSlideLeft = AnimationUtils.makeInAnimation(this, true);
                animationSlideLeft.setDuration(1000);
                errorLayout.startAnimation(animationSlideLeft);
                errorLayout.setVisibility(View.VISIBLE);

                //Reposition stop title below error view
                RelativeLayout.LayoutParams selectedStopTitleParams = (RelativeLayout.LayoutParams)selectedStopTitle.getLayoutParams();
                //selectedStopTitleParams.setMargins(0, 90, 55, 0);
                selectedStopTitleParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                selectedStopTitleParams.addRule(RelativeLayout.BELOW, errorLayout.getId());
                selectedStopTitle.setLayoutParams(selectedStopTitleParams);

                mMapState.invalidateStopETAs();
                favoriteManager.updateFavorites();


            }
            //Clear last ETA data. Maybe not necessary.
            /*
            for (Stop stop : mMapState.getStops()){
                stop.setShuttleETA(0, -1);
                stop.setShuttleETA(1, -1);
                stop.setShuttleETA(2, -1);
                stop.setShuttleETA(3, -1);
            }
            */
        }
    }

    private void hideProgressSpinner(){
        if(progressSpinner.getVisibility() == View.VISIBLE) {
            Animation fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            progressSpinner.startAnimation(fadeOutAnimation);
            fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    progressSpinner.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    public void animateSelectedStopTitle(final String markerTitle, final Boolean slideNewTitle, Boolean fromHidden, final Boolean isShuttle, Boolean mapClick) {
        if(isShuttle == null) Log.d(TAG, "!@# NULL");
        selectedMarkerManager.animateSelectedStopTitle(markerTitle, slideNewTitle, fromHidden, isShuttle, mapClick);
    }

    public boolean onMapMarkerClick(Marker marker){
        favoriteManager.onMapClick(marker);
        return selectedMarkerManager.onMarkerClick(marker);
    }

    /*
    METHOD - updateMap()
       Callback from shuttleUpdater. Called each time shuttleLocs and routeEst are fetched.
       Sets positions and visibility of each shuttleMarker.
       Updates shuttle & stop objects with latest routeEstimates.
    */
    public void updateMap(){
        Log.d(TAG, "updateMap");
        favoriteManager.updateFavorites();
        boolean updateDrawer = false;
        for (Shuttle shuttle : mMapState.getShuttles()) {
            if (!shuttle.isOnline()) {
                Log.d(TAG, "$# " + shuttle.getName() + " . " + shuttle.isOnline());
                if(shuttle.getMarker().isVisible()) {
                    shuttle.getMarker().setVisible(false);
                    updateDrawer = true;
                }
            } else {
                if (shuttle.getLatLng() != shuttle.getMarker().getPosition()) {
                    if (shuttle.getLatLng() != new LatLng(0,0)) {
                        Log.d(TAG, "Starting position. WITHOUT AIM");
                        shuttle.updateMarker(!mMapState.noAnimate);
                    }
                    else{


                        shuttle.updateMarkerWithoutAnim();
                    }
                }
                if(!shuttle.getMarker().isVisible()) {
                    shuttle.getMarker().setVisible(true);
                    updateDrawer = true;
                }
            }
        }
        if(updateDrawer){
            mAdapter.notifyDataSetInvalidated();
        }
        if(mMapState.noAnimate){
            mMapState.noAnimate = false;
        }
        //Only executes if any StopIndex array is null (first run)
        mMapState.initStopsArrays();

        //Only executes if drawerItems is null (first run)
        if(mMapState.initDrawerItems()){
            mAdapter.notifyDataSetInvalidated();
        }

        sProgressDialog.dismiss();
        hideProgressSpinner();
    }




    public void initNetworkFailureDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Network unavailable")
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
                    }
                })
                .setCancelable(false);
        networkFailureDialog = alertDialogBuilder.create();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        //Navigation drawer "option"
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        //Regular options menu contents
        switch (item.getItemId()) {
            case R.id.toggle_stops:
                toggleStopsVisibility(item);
                return true;
            //TODO: create information window
            case R.id.clear_favorites:
                favoriteManager.clearFavorites();
                return true;
            case R.id.view_info:
                showBusInfoDialog(item);
                return true;
            case R.id.rate_app:
                final String myappPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + myappPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + myappPackageName)));
                }
                    return true;
            case R.id.add_favorite:
                favoriteManager.addFavorite();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menuGlobal = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");
        createBusInfoDialog();
        if (mMapState != null){
            if (mMapState.isStopsVisible()) menu.findItem(R.id.toggle_stops).setTitle("Hide Stops");
            else menu.findItem(R.id.toggle_stops).setTitle("Show Stops");
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

    public void showBusInfoDialog(MenuItem item){
        if (busInfoDialog != null) {
            busInfoDialog.show();
        }

    }

    public void createBusInfoDialog(){



        AlertDialog.Builder busInfoDialogBuilder = new AlertDialog.Builder(this, R.style.BusInfoDialogTheme);


        TextView title = new TextView(getApplicationContext());
        title.setText("Beaver Bus");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(getResources().getColor(R.color.OSU_orange));
        title.setPadding(0, 24, 0, 24);


        busInfoDialogBuilder.setMessage("Hours of Operation:\n7:00 AM to 7:00 PM\n\nEstimated loop times:\n5 to 14 minutes")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setCustomTitle(title);
        busInfoDialog = busInfoDialogBuilder.create();

    }

    public boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return(cm.getActiveNetworkInfo() != null);
    }

}