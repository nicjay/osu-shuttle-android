package com.jordann.maptest;

import android.nfc.Tag;
import android.util.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
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

    private static ArrayList<Integer> mNorthStopIndex;
    private static ArrayList<Integer> mWestStopIndex;
    private static ArrayList<Integer> mEastStopIndex;

    private static ArrayList<DrawerItem> mDrawerItems;

    private MapState(){
        mDrawerItems = new ArrayList<DrawerItem>();
    }

    public static MapState get(){
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

            Shuttle newShuttle = new Shuttle("North", false);
            newShuttle.setMarker(mMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("Init Shuttle")));
            mShuttles.add(newShuttle);

            newShuttle = new Shuttle("West #1", false);
            newShuttle.setMarker(mMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("Init Shuttle")));
            mShuttles.add(newShuttle);

            newShuttle = new Shuttle("West #2", false);
            newShuttle.setMarker(mMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("Init Shuttle")));
            mShuttles.add(newShuttle);

            newShuttle = new Shuttle("East", false);
            newShuttle.setMarker(mMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("Init Shuttle")));
            mShuttles.add(newShuttle);
      //  }
    }

    public void setShuttle(int index, Shuttle shuttle){
        mShuttles.get(index).updateAll(shuttle);
    }

    public void animateMap(LatLng latLng){
        CameraPosition cameraPosition = new CameraPosition(latLng, mMap.getCameraPosition().zoom, 0, 0);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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
        for (Stop stop : mStops) {
            stop.setMarker(mMap.addMarker(new MarkerOptions().position(stop.getLatLng()).title(stop.getName())));
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
        mDrawerItems = drawerItems;
    }

    public boolean initDrawerItems() {
        if (mDrawerItems.size() == 0) {
            Log.d(TAG, "mDrawerItems null");


            mDrawerItems.add(new DrawerItem(0, "Shuttles"));
            mDrawerItems.add(new DrawerItem(1, "North", mShuttles.get(0)));
            mDrawerItems.add(new DrawerItem(1, "West #1", mShuttles.get(1)));
            mDrawerItems.add(new DrawerItem(1, "West #2", mShuttles.get(2)));
            mDrawerItems.add(new DrawerItem(1, "East", mShuttles.get(3)));
            mDrawerItems.add(new DrawerItem(0, "Stops"));
            mDrawerItems.add(new DrawerItem(2, "North1", mNorthStopIndex));
            mDrawerItems.add(new DrawerItem(2, "West1", mWestStopIndex));
            mDrawerItems.add(new DrawerItem(2, "East", mEastStopIndex));

            return true;
        }
        return false;
    }


    public void destroyMapState(){
        sMapState = null;

    }


}
