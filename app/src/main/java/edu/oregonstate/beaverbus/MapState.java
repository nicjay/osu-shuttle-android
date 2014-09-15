package edu.oregonstate.beaverbus;

import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by jordan_n on 8/22/2014.
 *
 * ClASS - MapState
 * Maintains most persistent data, such as shuttles, stops, and drawer items, to be used between classes and the lifecycle of the app.
 */
public class MapState {

    private static final String TAG = "MapState";
    private static final String TAG_DB = "SpecialTag";

    private static MapState sMapState;
    private static GoogleMap mMap;

    private static ArrayList<Shuttle> mShuttles;
    private static ArrayList<Stop> mStops;
    private boolean mStopsVisible;

    private Marker mSelectedStopMarker;
    private int[] mSelectedStopMarkerTimes;

    private static ArrayList<Integer> mNorthStopIndex;
    private static ArrayList<Integer> mWestStopIndex;
    private static ArrayList<Integer> mEastStopIndex;

    private static ArrayList<DrawerItem> mDrawerItems;

    private static Context sCurrentContext;

    private static final int CAMERA_ANIMATION_SPEED = 700;

    private static final float CAMERA_TILT = 0;
    private static final float CAMERA_BEARING = 0;

    private boolean stopsTaskStatus;


    public boolean isStopsTaskStatus() {
        return stopsTaskStatus;
    }

    public void setStopsTaskStatus(boolean stopsTaskStatus) {
        this.stopsTaskStatus = stopsTaskStatus;
    }

    private MapState(){
        mDrawerItems = new ArrayList<DrawerItem>();
        mStopsVisible = true;
        stopsTaskStatus = true;
    }

    public static MapState get(){
        if (sMapState == null){
            sMapState = new MapState();
        }
        return sMapState;
    }

    public static void setCurrentContext(Context currentContext) {
        sCurrentContext = currentContext;
    }

    public static Context getCurrentContext() {
        return sCurrentContext;
    }

    //Creates shuttle objects and their markers, initializing them to (0,0) on the map.
    public void initShuttles(){
        Log.d(TAG_DB, "initShuttles");
        mShuttles = new ArrayList<Shuttle>();
        LatLng initLatLng = new LatLng(0,0);

        Shuttle newShuttle = new Shuttle("North", false);
        newShuttle.setMarker(mMap.addMarker(new MarkerOptions().position(initLatLng).title("Init Shuttle").icon(BitmapDescriptorFactory.fromResource(R.drawable.shuttle_green)).flat(true).anchor(0.5f, 0.5f).infoWindowAnchor(.5f, .5f)));
        newShuttle.setColorID(R.color.shuttle_green);
        //Log.d(TAG, "Shuttle color: " + newShuttle.getColorID());
        mShuttles.add(newShuttle);

        newShuttle = new Shuttle("West 1", false);
        newShuttle.setMarker(mMap.addMarker(new MarkerOptions().position(initLatLng).title("Init Shuttle").icon(BitmapDescriptorFactory.fromResource(R.drawable.shuttle_purple)).flat(true).anchor(0.5f, 0.5f).infoWindowAnchor(.5f, .5f)));
        newShuttle.setColorID(R.color.shuttle_purple);
        //Log.d(TAG, "Shuttle color: " + newShuttle.getColorID());
        mShuttles.add(newShuttle);

        newShuttle = new Shuttle("West 2", false);
        newShuttle.setMarker(mMap.addMarker(new MarkerOptions().position(initLatLng).title("Init Shuttle").icon(BitmapDescriptorFactory.fromResource(R.drawable.shuttle_purple)).flat(true).anchor(0.5f, 0.5f).infoWindowAnchor(.5f, .5f)));
        newShuttle.setColorID(R.color.shuttle_purple);
        //Log.d(TAG, "Shuttle color: " + newShuttle.getColorID());
        mShuttles.add(newShuttle);

        newShuttle = new Shuttle("East", false);
        newShuttle.setMarker(mMap.addMarker(new MarkerOptions().position(initLatLng).title("Init Shuttle").icon(BitmapDescriptorFactory.fromResource(R.drawable.shuttle_orange)).flat(true).anchor(.5f, .5f).infoWindowAnchor(.5f, .5f)));
        newShuttle.setColorID(R.color.shuttle_orange);
        //Log.d(TAG, "Shuttle color: " + newShuttle.getColorID());
        mShuttles.add(newShuttle);

        if(mDrawerItems != null && mDrawerItems.size() != 0){
            //mDrawerItems[1] through mDrawerItems[4] are shuttles. Update new markers
            for(int i = 1; i < 5; i++){
                mDrawerItems.get(i).setShuttle(mShuttles.get(i-1));
            }

        }
    }

    public void setShuttle(int index, Shuttle shuttle){
        mShuttles.get(index).updateAll(shuttle);
    }

    //Called on marker selection. Moves camera, accounting for infoWindow height and display orientation.
    public void animateMap(LatLng latLng){
        Display display = ((WindowManager) sCurrentContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();

        //[0] = portraitOffset  ,   [1] = landscapeOffset
        double[] orientationOffset = getInfoWindowOffset(mMap.getCameraPosition().zoom);

        LatLng newPosition = new LatLng(latLng.latitude + orientationOffset[0], latLng.longitude);

        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
           newPosition = new LatLng(latLng.latitude + orientationOffset[1], latLng.longitude);
        }

        //CameraPosition cameraPosition = new CameraPosition(newPosition, mMap.getCameraPosition().zoom, CAMERA_TILT, CAMERA_BEARING);
        CameraPosition cameraPosition = new CameraPosition(newPosition, mMap.getCameraPosition().zoom, mMap.getCameraPosition().tilt, mMap.getCameraPosition().bearing);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), CAMERA_ANIMATION_SPEED, null);
    }

    public double[] getInfoWindowOffset(float zoom){
        double[] offset = new double[2];
        double portraitOffset = 0;
        double landscapeOffset = 0;

        if (zoom >= 13 && zoom < 14){
            portraitOffset = .002;
            landscapeOffset = .0059;
        }
        else if (zoom >= 14 && zoom < 16){
            portraitOffset = .002;
            landscapeOffset = .0029;
        }
        else if (zoom >= 16 && zoom < 17){
            portraitOffset = .0008;
            landscapeOffset = .00105;
        }
        else if (zoom >= 17 && zoom < 18){
            portraitOffset = .0006;
            landscapeOffset = .00060;
        }

        offset[0] = portraitOffset;
        offset[1] = landscapeOffset;
        return offset;
    }

    public GoogleMap getMap() {
        return mMap;
    }

    public void setMap(GoogleMap map) {
        mMap = map;
    }

    public ArrayList<Shuttle> getShuttles() {
        return mShuttles;
    }

    public void setShuttles(ArrayList<Shuttle> shuttles) {
        mShuttles = shuttles;
    }

    public ArrayList<Stop> getStops() {
        return mStops;
    }

    public void setStops(ArrayList<Stop> stops){
        Log.d(TAG_DB, "setStops");
        mStops = stops;
    }

    public void setStopsMarkers(){
        Log.d(TAG_DB, "setStopMarkers");
        for (Stop stop : mStops) {
            stop.setMarker(mMap.addMarker(new MarkerOptions().position(stop.getLatLng()).title(stop.getName()).visible(mStopsVisible)));
        }
    }

    //Uses shuttleETAs to determine which stops belong to each route.
    public void initStopsArrays() {
        Log.d(TAG_DB, "initStopsArrays");
        if (mNorthStopIndex == null || mWestStopIndex == null || mEastStopIndex == null) {
            mNorthStopIndex = new ArrayList<Integer>();
            mWestStopIndex = new ArrayList<Integer>();
            mEastStopIndex = new ArrayList<Integer>();

            for (Stop stop : mStops) {
                //Log.d(TAG, "stopShuttleETAS: " + stops.get(i).getShuttleETAs()[0] + " , "+ stops.get(i).getShuttleETAs()[1] + " , "+ stops.get(i).getShuttleETAs()[2] + " , "+ stops.get(i).getShuttleETAs()[3] + " , ");
                int[] shuttleETAs = stop.getShuttleETAs();
                int index = mStops.indexOf(stop);
                //TODO: input shuttleETAs
                for (int i = 0; i < 4; i++) {
                    if (true/*shuttleETAs[i] != -1*/) {
                        switch (i) {
                            case 0:
                                mNorthStopIndex.add(index);
                                break;
                            case 1:
                                mWestStopIndex.add(index);
                                break;
                            case 3:
                                mEastStopIndex.add(index);
                                break;
                            default:

                        }
                    }
                }
            }
        }
    }

    public ArrayList<DrawerItem> getDrawerItems() {
        return mDrawerItems;
    }

    public void setDrawerItems(ArrayList<DrawerItem> drawerItems){
        Log.d(TAG_DB, "setDrawerItems");
        mDrawerItems = drawerItems;
    }

    public boolean initDrawerItems() {
        Log.d(TAG_DB, "initDrawerItems");
        Log.d(TAG, "mDrawerItems: "+mDrawerItems+" ; ");
        if (mDrawerItems.size() == 0 || mDrawerItems == null) {
            mDrawerItems.add(new DrawerItem(0, "Buses"));
            mDrawerItems.add(new DrawerItem(1, "North", mShuttles.get(0), true));
            mDrawerItems.add(new DrawerItem(1, "West 1", mShuttles.get(1), true));
            mDrawerItems.add(new DrawerItem(1, "West 2", mShuttles.get(2), true));
            mDrawerItems.add(new DrawerItem(1, "East", mShuttles.get(3), true));
            mDrawerItems.add(new DrawerItem(0, "Stops"));
            mDrawerItems.add(new DrawerItem(2, "North Route", mNorthStopIndex));
            mDrawerItems.add(new DrawerItem(2, "West Route", mWestStopIndex));
            mDrawerItems.add(new DrawerItem(2, "East Route", mEastStopIndex));
            return true;
        }
        return false;
    }


    public boolean isStopsVisible() {
        return mStopsVisible;
    }

    public void setStopsVisible(boolean stopsVisible) {
        mStopsVisible = stopsVisible;
    }


    public void setSelectedStopMarkerVisibility(boolean isVisible) {
        mSelectedStopMarker.setVisible(isVisible);
    }

    public boolean getSelectedStopMarkerVisibility() {
        return mSelectedStopMarker.isVisible();
    }

    public Marker getSelectedStopMarker(){
        return mSelectedStopMarker;
    }


    public void setSelectedStopMarker(Marker selectedStopMarker) {
        if (selectedStopMarker != null) {
            for(Stop stop: mStops){
                if(stop.getMarker().equals(selectedStopMarker)){
                    mSelectedStopMarkerTimes = stop.getShuttleETAs();
                }
            }
            mSelectedStopMarker = selectedStopMarker;

        } else {
            mSelectedStopMarkerTimes = null;
            mSelectedStopMarker = null;
        }
    }

    public int[] getSelectedStopMarkerTimes() {
        return mSelectedStopMarkerTimes;
    }


}
