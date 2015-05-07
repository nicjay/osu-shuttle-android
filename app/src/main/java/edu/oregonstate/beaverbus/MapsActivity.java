package edu.oregonstate.beaverbus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/*
*    CLASS- MapsActivity
*        Handles the overall order of events within the app lifecycle.
*        Initialization >> Updating >> Shut Down
*/
public class MapsActivity extends FragmentActivity implements InitialNetworkRequestor.OnInitialRequestComplete, ShuttleUpdater.OnMapStateUpdate {
    private static final String TAG = "MapsActivity";

    //Map and Singleton variables
    private GoogleMap mMap;
    private MapState mMapState;
    private final LatLng MAP_CENTER = new LatLng(44.562231, -123.281114);
    private final float MAP_ZOOM_LEVEL = 14.5f;

    private FavoriteManager favoriteManager;
    private SelectedMarkerManager selectedMarkerManager;

    //Loading overlay
    private static LinearLayout progressSpinner;

    //TextView displayed at top of screen
    private static TextView selectedStopTitle;

    //Navigation Drawer variables
    private DrawerLayout mDrawerLayout;
    private static ExpandableDrawerAdapter mAdapter;
    private ExpandableListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    //Options Menu
    public static Menu menuGlobal;

    //Various shown dialogs
    private static Dialog busInfoDialog;
    private static AlertDialog networkFailureDialog;
    private static LinearLayout errorLayout;

    //HTTP Request Interface
    private InitialNetworkRequestor initialNetworkRequestor;
    private ShuttleUpdater shuttleUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "LIFECYCLE - onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //View references within main screen
        errorLayout = (LinearLayout) findViewById(R.id.error_view);
        selectedStopTitle = ((TextView) findViewById(R.id.selected_stop));
        selectedStopTitle.setVisibility(View.INVISIBLE);

        favoriteManager = new FavoriteManager(this, getApplicationContext(), selectedMarkerManager);

        //Get singleton, set context
        mMapState = MapState.get();
        mMapState.setCurrentContext(this);
        mMapState.readConfigurationFile(true); //Read only urls from configuration file

        //Initialization
        setUpMapIfNeeded();

        selectedMarkerManager = new SelectedMarkerManager(this);
        mMapState.setSelectedMarkerManager(selectedMarkerManager);

        //Show Loading Dialog
        progressSpinner = (LinearLayout) findViewById(R.id.progress_spinner);
        progressSpinner.setVisibility(View.VISIBLE);

        //Start initial HTTP request
        initialNetworkRequestor = new InitialNetworkRequestor();
        initialNetworkRequestor.execute();

        //Initialize shuttleUpdater for subsequent HTTP requests
        shuttleUpdater = ShuttleUpdater.get(this);

        //Create dialog to display if HTTP fails
        initNetworkFailureDialog();
    }

    @Override
    protected void onStart() {
        //Log.d(TAG, "LIFECYCLE - onStart");
        super.onStart();
        initNavigationDrawer();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "LIFECYCLE - onResume");
        super.onResume();
        if(networkFailureDialog != null) {
            if (networkFailureDialog.isShowing()) {
                Log.d(TAG, "progressSpinner vis: " + progressSpinner.getVisibility());
                progressSpinner.setVisibility(View.VISIBLE);

                Log.d(TAG, "POST progressSpinner vis: " + progressSpinner.getVisibility());
                WindowManager.LayoutParams lp = networkFailureDialog.getWindow().getAttributes();
                Log.d(TAG, "OLD Dim AMount: " + lp.dimAmount);
                lp.dimAmount = 0.8f;
                networkFailureDialog.getWindow().setAttributes(lp);
                Log.d(TAG, "New Dim AMount: " + lp.dimAmount);
            }
        }

        if (!mMapState.isFirstTime()) {
            shuttleUpdater.startShuttleUpdater();
            //For shuttle move from (0,0) to actual coords, don't animate.
            mMapState.noAnimate = true;
        }
    }

    @Override
    protected void onPause() {
        //Log.d(TAG, "LIFECYCLE - onPause");
        super.onPause();
        shuttleUpdater.stopShuttleUpdater();
    }

    @Override
    protected void onStop() {
        //Log.d(TAG, "LIFECYCLE - onStop");
        super.onStop();
        mMapState.setFirstTime(false);
    }

    @Override
    protected void onRestart() {
        //Log.d(TAG, "LIFECYCLE - onRestart");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        //Log.d(TAG, "LIFECYCLE - onDestroy");
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
    private void setUpMapIfNeeded() {
        //Log.d(TAG, "setUpMapIfNeeded");

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
                    if (cameraPosition.zoom < 13) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, 13));
                    }
                }
            });

            mMapState.setMap(mMap);
            mMapState.initShuttles(); //Init shuttle markers
        }
    }

    /*
    METHOD - initNavigationDrawer()
       Creates all components needed for navDrawer.
       Uses mapState.getDrawerItems for row data.
       Sets custom adapter so data can be refreshed when needed.
    */
    private void initNavigationDrawer() {
        //Log.d(TAG, "initNavigationDrawer");

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
                }

                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    mAdapter.notifyDataSetInvalidated();
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
            //Enable and show navigation drawer icon
            try {
                getActionBar().setDisplayHomeAsUpEnabled(true);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
            getActionBar().setHomeButtonEnabled(true);
        }
    }


    /*
    METHOD - onPostInitialRequest()
        Callback, handles the success or failure result of the initial stop+shuttle request.
     */
    public void onPostInitialRequest(boolean success) {
        if (success) {
            mMapState.readConfigurationFile(false); //Read remainder of configuration file. (URLs are read in onCreate using same function)
            mMapState.setStopsMarkers();
            selectedMarkerManager.setPolylines();
            shuttleUpdater.startShuttleUpdater();
            networkFailureDialog.dismiss();
        } else {
            networkFailureDialog.show();
        }
    }


    /*
    METHOD - onPostShuttleRequest()
        Callback, handles the success or failure result of a shuttle request event.
        The general updater function is called if needed.
        Error views and loading dialogs are added or removed accordingly.
     */
    public void onPostShuttleRequest(boolean success) {
        if (success) {
            favoriteManager.initSavedFavorites();
            updateMap();
            if (errorLayout.getVisibility() == View.VISIBLE) {
                //Remove the error view if it was there
                Animation animation = AnimationUtils.makeOutAnimation(this, true);
                animation.setDuration(1000);
                errorLayout.startAnimation(animation);
                errorLayout.setVisibility(View.INVISIBLE);

                //Set the stop title back to normal
                RelativeLayout.LayoutParams selectedStopTitleParams = (RelativeLayout.LayoutParams) selectedStopTitle.getLayoutParams();
                selectedStopTitleParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                selectedStopTitleParams.addRule(RelativeLayout.BELOW, 0);
                selectedStopTitle.setLayoutParams(selectedStopTitleParams);
            }
        } else {
            hideProgressSpinner();
            if (errorLayout.getVisibility() == View.INVISIBLE && !networkFailureDialog.isShowing()) {
                //Show the error view
                Animation animationSlideLeft = AnimationUtils.makeInAnimation(this, true);
                animationSlideLeft.setDuration(1000);
                errorLayout.startAnimation(animationSlideLeft);
                errorLayout.setVisibility(View.VISIBLE);

                //Reposition stop title below error view
                RelativeLayout.LayoutParams selectedStopTitleParams = (RelativeLayout.LayoutParams) selectedStopTitle.getLayoutParams();
                selectedStopTitleParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                selectedStopTitleParams.addRule(RelativeLayout.BELOW, errorLayout.getId());
                selectedStopTitle.setLayoutParams(selectedStopTitleParams);

                mMapState.invalidateStopETAs();
                favoriteManager.updateFavorites();
            }
        }
    }

    private void hideProgressSpinner() {
        if (progressSpinner.getVisibility() == View.VISIBLE) {
            Animation fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            progressSpinner.startAnimation(fadeOutAnimation);
            fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    progressSpinner.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }
    }

    public void animateSelectedStopTitle(final String markerTitle, final Boolean isShuttle, Boolean mapClick) {
        selectedMarkerManager.animateSelectedStopTitle(markerTitle, isShuttle, mapClick);
    }

    public boolean onMapMarkerClick(Marker marker) {
        favoriteManager.onMapClick(marker);
        return selectedMarkerManager.onMarkerClick(marker);
    }

    /*
    METHOD - updateMap()
       Callback from shuttleUpdater. Called each time shuttleLocs and routeEst are fetched.
       Sets positions and visibility of each shuttleMarker.
       Updates shuttle & stop objects with latest routeEstimates.
    */
    private void updateMap() {
        //Log.d(TAG, "updateMap");
        favoriteManager.updateFavorites();
        boolean updateDrawer = false;
        for (Shuttle shuttle : mMapState.getShuttles()) {
            if (!shuttle.isOnline()) {
                if (shuttle.getMarker().isVisible()) {
                    shuttle.getMarker().setVisible(false);
                    updateDrawer = true;
                }
            } else {
                if (shuttle.getLatLng() != shuttle.getMarker().getPosition()) {
                    if (shuttle.getLatLng() != new LatLng(0, 0)) {
                        shuttle.updateMarker(!mMapState.noAnimate);
                    } else {
                        shuttle.updateMarkerWithoutAnim();
                    }
                }
                if (!shuttle.getMarker().isVisible()) {
                    shuttle.getMarker().setVisible(true);
                    updateDrawer = true;
                }
            }
        }
        if (mMapState.noAnimate) {
            mMapState.noAnimate = false;
        }
        //Only executes if any StopIndex array is null (first run)
        mMapState.initStopsArrays();

        //Only executes if drawerItems is null (first run)
        if (mMapState.initDrawerItems() || updateDrawer) {
            mAdapter.notifyDataSetInvalidated();
        }
        hideProgressSpinner();
    }


    private void initNetworkFailureDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.network_failure_dialog, null);
        alertDialogBuilder.setView(dialogView);

        //Get 'EXIT' and 'TRY AGAIN' buttons, and set their OnClickListeners
        Button exitButton = (Button)dialogView.findViewById(R.id.network_dialog_exit_button);
        Button tryAgainButton = (Button)dialogView.findViewById(R.id.network_dialog_try_again_button);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialNetworkRequestor = new InitialNetworkRequestor();
                initialNetworkRequestor.execute();
                networkFailureDialog.dismiss();

            }
        });

        alertDialogBuilder.setCancelable(false);
        networkFailureDialog = alertDialogBuilder.create();
        networkFailureDialog.getWindow().setDimAmount(0f);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Navigation drawer "option"
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        //Regular options menu contents
        switch (item.getItemId()) {
            case R.id.toggle_stops:
                toggleStopsVisibility(item);
                return true;
            case R.id.clear_favorites:
                favoriteManager.clearFavorites();
                return true;
            case R.id.view_info:
                showBusInfoDialog(item);
                return true;
            case R.id.rate_app:
                //TODO 12/11/2014: fix link to GooglePlay for rating the app
                final String packageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
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
        createBusInfoDialog();
        if (mMapState != null) {
            if (mMapState.isStopsVisible()) menu.findItem(R.id.toggle_stops).setTitle(R.string.hide_stops);
            else menu.findItem(R.id.toggle_stops).setTitle(R.string.show_stops);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void toggleStopsVisibility(MenuItem item) {
        boolean stopsVisible = mMapState.isStopsVisible();

        if (stopsVisible){
            item.setTitle(R.string.hide_stops);
            selectedMarkerManager.onMapClick(); //Hide "selected stop" title if shown
        }
        else item.setTitle(R.string.show_stops);

        for (Stop stop : mMapState.getStops()) {
            stop.getMarker().setVisible(!stopsVisible);
        }
        mMapState.setStopsVisible(!stopsVisible);


    }

    private void showBusInfoDialog(MenuItem item) {
        if (busInfoDialog != null) {
            busInfoDialog.show();
        }
    }

    private void createBusInfoDialog() {
        AlertDialog.Builder busInfoDialogBuilder = new AlertDialog.Builder(this, R.style.BusInfoDialogTheme);
        TextView title = new TextView(getApplicationContext());
        title.setText(R.string.app_name);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(getResources().getColor(R.color.OSU_orange));
        title.setPadding(0, 24, 0, 24);


        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = (View) inflater.inflate(R.layout.custom_bus_info_dialog, null);

        TextView messageTextView = (TextView) layout.findViewById(R.id.bus_info_message);
        TextView noteTextView = (TextView)layout.findViewById(R.id.bus_info_note);

        messageTextView.setText(R.string.bus_info_message);
        noteTextView.setText(R.string.bus_info_note);

//        busInfoDialogBuilder.setMessage(R.string.bus_info_message)
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                    }
//                }).setCustomTitle(title);
        busInfoDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setCustomTitle(title);

        busInfoDialogBuilder.setView(layout);

        busInfoDialog = busInfoDialogBuilder.create();
    }
}