package com.jordann.maptest;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;

/*
  Created by jordan_n on 8/22/2014.
 */
public class MapState {
    private static final String TAG = "MapState";

    private static MapState sMapState;
    private static GoogleMap mMap;

    private static ArrayList<Shuttle> mShuttles;
    private static ArrayList<Stop> mStops;
    private boolean mStopsVisible;

    private Marker mSelectedStopMarker;

    private static ArrayList<Integer> mNorthStopIndex;
    private static ArrayList<Integer> mWestStopIndex;
    private static ArrayList<Integer> mEastStopIndex;

    private static ArrayList<DrawerItem> mDrawerItems;

    private static Context sCurrentContext;

    private static double PORTRAIT_OFFSET;
    private static double LANDSCAPE_OFFSET;
    private static final int CAMERA_ANIMATION_SPEED = 600;

    private static final float CAMERA_TILT = 0;
    private static final float CAMERA_BEARING = 0;

    private MapState(){
        mDrawerItems = new ArrayList<DrawerItem>();
        mStopsVisible = true;
    }

    public static MapState get(){
        if (sMapState == null){
            Log.d(TAG, "mapState null, creating new...");
            sMapState = new MapState();

            Log.d(TAG, "getshuttles: " +mShuttles);
        }
        return sMapState;
    }

    public static MapState get(Context context){
        sCurrentContext = context;
        if (sMapState == null){
            Log.d(TAG, "mapState null, creating new...");
            sMapState = new MapState();

            Log.d(TAG, "getshuttles: " +mShuttles);
        }
        return sMapState;
    }

    public void initShuttles(){
       // if (mShuttles == null){
            mShuttles = new ArrayList<Shuttle>();
            LatLng initLatLng = new LatLng(0,0);

            Shuttle newShuttle = new Shuttle("North", false);
            newShuttle.setMarker(mMap.addMarker(new MarkerOptions().position(initLatLng).title("Init Shuttle").icon(BitmapDescriptorFactory.fromResource(R.drawable.shuttle_green)).flat(true).anchor(0.5f, 0.5f)));
            mShuttles.add(newShuttle);

            newShuttle = new Shuttle("West 1", false);
            newShuttle.setMarker(mMap.addMarker(new MarkerOptions().position(initLatLng).title("Init Shuttle").icon(BitmapDescriptorFactory.fromResource(R.drawable.shuttle_purple)).flat(true).anchor(0.5f, 0.5f)));
            mShuttles.add(newShuttle);

            newShuttle = new Shuttle("West 2", false);
            newShuttle.setMarker(mMap.addMarker(new MarkerOptions().position(initLatLng).title("Init Shuttle").icon(BitmapDescriptorFactory.fromResource(R.drawable.shuttle_purple)).flat(true).anchor(0.5f, 0.5f)));
            mShuttles.add(newShuttle);

            newShuttle = new Shuttle("East", false);
            newShuttle.setMarker(mMap.addMarker(new MarkerOptions().position(initLatLng).title("Init Shuttle").icon(BitmapDescriptorFactory.fromResource(R.drawable.shuttle_orange)).flat(true).anchor(0.5f, 0.5f)));
            mShuttles.add(newShuttle);
      //  }
    }

    public void setShuttle(int index, Shuttle shuttle){
        mShuttles.get(index).updateAll(shuttle);
    }

    public void animateMap(LatLng latLng){
        Display display = ((WindowManager) sCurrentContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();

        Log.d(TAG, "zoom level: "+mMap.getCameraPosition().zoom);
        getInfoWindowOffset(mMap.getCameraPosition().zoom);

        LatLng newPosition = new LatLng(latLng.latitude + PORTRAIT_OFFSET, latLng.longitude);

        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
           newPosition = new LatLng(latLng.latitude + LANDSCAPE_OFFSET, latLng.longitude);
        }

        CameraPosition cameraPosition = new CameraPosition(newPosition, mMap.getCameraPosition().zoom, CAMERA_TILT, CAMERA_BEARING);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), CAMERA_ANIMATION_SPEED, null);
    }

    public int getInfoWindowOffset(float zoom){
        int offset = 0;

        if (zoom >= 13 && zoom < 14){
            PORTRAIT_OFFSET = .002;
            LANDSCAPE_OFFSET = .0059;
        }
        else if (zoom >= 14 && zoom < 16){
            PORTRAIT_OFFSET = .002;
            LANDSCAPE_OFFSET = .0029;
        }
        else if (zoom >= 16 && zoom < 17){
            PORTRAIT_OFFSET = .0008;
            LANDSCAPE_OFFSET = .00105;
        }
        else if (zoom >= 17 && zoom < 18){
            PORTRAIT_OFFSET = .0006;
            LANDSCAPE_OFFSET = .00060;
        }
        else if (zoom >= 18) {
            PORTRAIT_OFFSET = 0;
            LANDSCAPE_OFFSET = 0;
        }
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
        mStops = stops;
    }

    public void setStopsMarkers(){
        if (mStopsVisible){
            for (Stop stop : mStops) {
                stop.setMarker(mMap.addMarker(new MarkerOptions().position(stop.getLatLng()).title(stop.getName())));
            }
        } else {
            for (Stop stop : mStops) {
                stop.setMarker(mMap.addMarker(new MarkerOptions().position(stop.getLatLng()).title(stop.getName()).visible(false)));
            }
        }

    }

    public void initStopsArrays() {
        if (mNorthStopIndex == null && mWestStopIndex == null && mEastStopIndex == null) {
            Log.d(TAG, "initStopsArrays null. recreating...");
            mNorthStopIndex = new ArrayList<Integer>();
            mWestStopIndex = new ArrayList<Integer>();
            mEastStopIndex = new ArrayList<Integer>();
            for (Stop stop : mStops) {
                int[] shuttleETAs = stop.getShuttleETAs();
                int index = mStops.indexOf(stop);
                for (int i = 0; i < 4; i++) {
                    if (/*shuttleETAs[i] != -1*/true) {
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
        mDrawerItems = drawerItems;
    }

    public boolean initDrawerItems() {
        if (mDrawerItems.size() == 0 || mDrawerItems == null) {
            Log.d(TAG, "mDrawerItems null");


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
        mSelectedStopMarker = selectedStopMarker;
    }
}
