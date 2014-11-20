package edu.oregonstate.beaverbus;

import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

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

    //Stops and Shuttles
    private static ArrayList<Shuttle> mShuttles;
    private static ArrayList<Stop> mStops;
    private boolean mStopsVisible;

    //Currently selected Stop
    private Marker mSelectedStopMarker;
    public boolean showSelectedInfoWindow = false;
    private int[] mSelectedStopMarkerTimes;

    //Stop indices of mStops for Stops of each route
    private static ArrayList<Integer> mNorthStopIndex;
    private static ArrayList<Integer> mWestStopIndex;
    private static ArrayList<Integer> mEastStopIndex;

    //Navigation Drawer items
    private static ArrayList<DrawerItem> mDrawerItems;

    private static Context sCurrentContext;

    private static final int CAMERA_ANIMATION_SPEED = 700;
    private static final float CAMERA_TILT = 0;
    private static final float CAMERA_BEARING = 0;

    private boolean stopsTaskStatus;

    public boolean noAnimate = false;
    private boolean stopMarkersBordered = true;

    //Allows fast lookup of stopObjects for Estimate parsing
    private HashMap<Integer, Stop> mStopsMap;

    public HashMap<Integer, Stop> northMap = new HashMap<Integer, Stop>();
    public HashMap<Integer, Stop> westMap = new HashMap<Integer, Stop>();
    public HashMap<Integer, Stop> eastMap = new HashMap<Integer, Stop>();


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

    public HashMap<Integer, Stop> getStopsMap() {
        return mStopsMap;
    }

    public void setStopsMap(HashMap<Integer, Stop> stopsMap) {
        mStopsMap = stopsMap;
    }

    //Creates shuttle objects and their markers, initializing them to (0,0) on the map.
    public void initShuttles(){
        Log.d(TAG_DB, "initShuttles");
        mShuttles = new ArrayList<Shuttle>();
        LatLng initLatLng = new LatLng(0,0);

        Shuttle newShuttle = new Shuttle("North", false);
        newShuttle.setMarker(mMap.addMarker(new MarkerOptions().alpha(.85f).position(initLatLng).title("North").icon(BitmapDescriptorFactory.fromResource(R.drawable.shut_green_marker_m)).flat(true).anchor(0.5f, 0.5f).infoWindowAnchor(.5f, .5f)));
        newShuttle.setColorID(R.color.shuttle_green);
        mShuttles.add(newShuttle);

        newShuttle = new Shuttle("West 1", false);
        newShuttle.setMarker(mMap.addMarker(new MarkerOptions().alpha(.85f).position(initLatLng).title("West 1").icon(BitmapDescriptorFactory.fromResource(R.drawable.shut_orange_marker_m)).flat(true).anchor(0.5f, 0.5f).infoWindowAnchor(.5f, .5f)));
        newShuttle.setColorID(R.color.shuttle_orange);
        mShuttles.add(newShuttle);

        newShuttle = new Shuttle("West 2", false);
        newShuttle.setMarker(mMap.addMarker(new MarkerOptions().alpha(.85f).position(initLatLng).title("West 2").icon(BitmapDescriptorFactory.fromResource(R.drawable.shut_orange_marker_m)).flat(true).anchor(0.5f, 0.5f).infoWindowAnchor(.5f, .5f)));
        newShuttle.setColorID(R.color.shuttle_orange);
        mShuttles.add(newShuttle);

        newShuttle = new Shuttle("East", false);
        newShuttle.setMarker(mMap.addMarker(new MarkerOptions().alpha(.85f).position(initLatLng).title("East").icon(BitmapDescriptorFactory.fromResource(R.drawable.shut_purple_marker_m)).flat(true).anchor(.5f, .5f).infoWindowAnchor(.5f, .5f)));
        newShuttle.setColorID(R.color.shuttle_purple);
        mShuttles.add(newShuttle);

        if(mDrawerItems != null && mDrawerItems.size() != 0){
            //mDrawerItems[1] through mDrawerItems[4] are shuttles. Update new markers
            for(int i = 1; i < 5; i++){
                mDrawerItems.get(i).setShuttle(mShuttles.get(i-1));
            }

        }

        Shuttle shut1 = sMapState.getShuttles().get(1);
        Shuttle shut2 = sMapState.getShuttles().get(2);
        Log.d(TAG, "~~~! shut1" + shut1.getName() + shut1.isOnline() + " shut2" + shut2.getName() + shut2.isOnline());
    }

    public void setShuttle(int index, Shuttle shuttle){
        //Log.d(TAG, "~~~! setShuttle index: " + index);
        if(shuttle.isOnline()) Log.d(TAG, "~~~! " + index + " --> SET TO ONLINE");
        else Log.d(TAG, "~~~! " + index + " --> SET TO OFFLINE");
        mShuttles.get(index).updateAll(shuttle);
    }

    //Called on marker selection. Moves camera, accounting for infoWindow height and display orientation.
    public void animateMap(LatLng latLng){
        //CameraPosition cameraPosition = new CameraPosition(newPosition, mMap.getCameraPosition().zoom, CAMERA_TILT, CAMERA_BEARING);
        CameraPosition cameraPosition = new CameraPosition(latLng, mMap.getCameraPosition().zoom, mMap.getCameraPosition().tilt, mMap.getCameraPosition().bearing);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), CAMERA_ANIMATION_SPEED, null);
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
            //TODO: fix stop.getName() in this next line. Only uses first stored stopName
            stop.setMarker(mMap.addMarker(new MarkerOptions().position(stop.getLatLng()).infoWindowAnchor(.5f, .25f).title(stop.getName()).visible(mStopsVisible).alpha(0.7f).anchor(.5f, .5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_dot_plus))));
        }
    }

    public void toggleStopMarkerBorders(){
        BitmapDescriptor newIcon;
        if(stopMarkersBordered) newIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker_dot_empty);
        else newIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker_dot_plus);
        stopMarkersBordered = !stopMarkersBordered;

        for(Stop stop : mStops){
            stop.getMarker().setIcon(newIcon);
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
                    if (shuttleETAs[i] != -1) {
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

    public void setDrawerShuttleStatus(Shuttle shuttle, boolean newStatus){
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

    public void refreshSelectedStopMarker(){
        if(mSelectedStopMarker != null && showSelectedInfoWindow) mSelectedStopMarker.showInfoWindow();
    }

    public boolean getSelectedStopMarkerVisibility() {
        return mSelectedStopMarker.isVisible();
    }

    public Marker getSelectedStopMarker(){
        return mSelectedStopMarker;
    }


    public void setSelectedStopMarker(Marker selectedStopMarker, boolean showInfoWindow) {
        if(mSelectedStopMarker != null && !mSelectedStopMarker.isFlat()) mSelectedStopMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_dot_plus));
        if(selectedStopMarker!= null && showInfoWindow)selectedStopMarker.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.marker_dot_empty)));

        showSelectedInfoWindow = showInfoWindow;
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

    public HashMap<Integer, Stop> getNorthMap() {
        return northMap;
    }

    public void setNorthMap(HashMap<Integer, Stop> northMap) {
        this.northMap = northMap;
    }

    public HashMap<Integer, Stop> getEastMap() {
        return eastMap;
    }

    public void setEastMap(HashMap<Integer, Stop> eastMap) {
        this.eastMap = eastMap;
    }

    public HashMap<Integer, Stop> getWestMap() {
        return westMap;
    }

    public void setWestMap(HashMap<Integer, Stop> westMap) {
        this.westMap = westMap;
    }
}
