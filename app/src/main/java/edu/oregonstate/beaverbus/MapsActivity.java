package edu.oregonstate.beaverbus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
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
    private boolean firstTime = true;
    private final LatLng MAP_CENTER = new LatLng(44.563731, -123.279534);
    private final float MAP_ZOOM_LEVEL = 14.5f;



    //Navigation Drawer variables
    private DrawerLayout mDrawerLayout;
    private ExpandableDrawerAdapter mAdapter;
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

    //Options Menu
    public static Menu menuGlobal;

    private FavoriteManager favoriteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "LIFECYCLE - onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //View references within main screen
        errorLayout = (LinearLayout) findViewById(R.id.error_view);
        selectedStopTitle = ((TextView)findViewById(R.id.selected_stop));
        selectedStopTitle.setVisibility(View.INVISIBLE);

        favoriteManager = new FavoriteManager(this, getApplicationContext());


        //Get singleton, set context
        mMapState = MapState.get();
        mMapState.setCurrentContext(this);

        //Initialization
        setUpMapIfNeeded();

        //Show Loading Dialog
        sProgressDialog = new ProgressDialog(this, R.style.CustomDialog);
        sProgressDialog.setMessage("Loading...");
        sProgressDialog.show();
        //sProgressDialog = ProgressDialog.show(this, "", "Loading...", true, false);
        //sProgressDialog.setProgressStyle(R.style.CustomDialog);

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

        if(!firstTime){
            shuttleUpdater.startShuttleUpdater();
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

        firstTime = false;
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
            mMap.clear();

            if (mMap != null) {
                setUpRouteLines();
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
                   return onMapMarkerClick(marker);
                }
            });

            //Deselect marker by touching anywhere else on map
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    animateSelectedStopTitle(null, false, false, false);
                    if (mMapState.getSelectedStopMarker() != null){

                        if (!mMapState.isStopsVisible() && mMapState.showSelectedInfoWindow) {
                            mMapState.setSelectedStopMarkerVisibility(false);
                        }
                        mMapState.setSelectedStopMarker(null, false);
                    }
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

    public boolean onMapMarkerClick(Marker marker){
        Log.d(TAG, "!@# latlong  : " + marker.getPosition());

        if (mMapState.getSelectedStopMarker() != null && !mMapState.isStopsVisible() && mMapState.showSelectedInfoWindow){
            mMapState.setSelectedStopMarkerVisibility(false);
        }
        mMapState.animateMap(marker.getPosition());
        marker.setVisible(true);

        if (mMapState.getSelectedStopMarker() == null) {
            animateSelectedStopTitle(marker.getTitle(), true, true, marker.isFlat());
        } else {
            animateSelectedStopTitle(marker.getTitle(), true, false, marker.isFlat());
        }

        favoriteManager.onMapClick(marker);

        //If stop
        if (!marker.isFlat()) {

            mMapState.setSelectedStopMarker(marker, true);
            marker.showInfoWindow();
        } else {
            mMapState.animateMap(marker.getPosition());
            mMapState.setSelectedStopMarker(marker, false);
        }

        return true;
    }

    public void animateSelectedStopTitle(final String markerTitle, final Boolean slideNewTitle, Boolean fromHidden, Boolean isShuttle){
        final Animation fadeInAnim = AnimationUtils.makeInAnimation(getApplicationContext(), true);
        fadeInAnim.setDuration(400);

        if (selectedStopTitle.getVisibility() == View.INVISIBLE){
            selectedStopTitle.setVisibility(View.VISIBLE);
            selectedStopTitle.setText(markerTitle);
            selectedStopTitle.startAnimation(fadeInAnim);
        } else {
            Animation fadeOutAnim = AnimationUtils.makeOutAnimation(this, true);
            fadeOutAnim.setDuration(300);
            fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (slideNewTitle) {
                        selectedStopTitle.setVisibility(View.VISIBLE);
                        selectedStopTitle.setText(markerTitle);
                        selectedStopTitle.startAnimation(fadeInAnim);
                    }else{
                        selectedStopTitle.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            selectedStopTitle.startAnimation(fadeOutAnim);
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
    METHOD - onPostInitialRequest()
        Callback, handles the success or failure result of the initial stop+shuttle request.
     */
    public void onPostInitialRequest(boolean success){
        Log.d(TAG, "..! ON POST INITIAL requEST");
        if (success){
            Log.d(TAG, "onPostInitialRequest success");
            mMapState.setStopsMarkers();

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
                Animation animation = AnimationUtils.makeOutAnimation(this, true);
                animation.setDuration(1000);
                errorLayout.startAnimation(animation);
                errorLayout.setVisibility(View.INVISIBLE);
            }
        }else{
            Log.d(TAG, "onPostShuttleRequest fail");
            sProgressDialog.dismiss();
            if(errorLayout.getVisibility() == View.INVISIBLE && !networkFailureDialog.isShowing()) {
                Animation animationSlideLeft = AnimationUtils.makeInAnimation(this, true);
                animationSlideLeft.setDuration(1000);
                errorLayout.startAnimation(animationSlideLeft);
                errorLayout.setVisibility(View.VISIBLE);
            }
        }
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
        for (Shuttle shuttle : mMapState.getShuttles()) {
            if (!shuttle.isOnline()) {
                shuttle.getMarker().setVisible(false);
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
                shuttle.getMarker().setVisible(true);
            }
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

        busInfoDialogBuilder.setMessage("Hours of Operation:\n7:00 AM to 7:00 PM\n\nEstimated loop times:\n5 to 14 minutes")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setTitle("Beaver Bus");
        busInfoDialog = busInfoDialogBuilder.create();

    }

    public void setUpRouteLines() {
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
        polylineNorth.setWidth(10);

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
        polylineEast.setColor(0xBDAA66CD);
        polylineEast.setWidth(10);

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
        polylineWest.setColor(0xBDE0AA0F);
        polylineWest.setWidth(10);
    }


    public boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return(cm.getActiveNetworkInfo() != null);
    }

}