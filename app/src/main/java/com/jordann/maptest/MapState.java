package com.jordann.maptest;

import java.util.ArrayList;

/**
 * Created by jordan_n on 8/22/2014.
 */
public class MapState {
    private Shuttle[] mShuttles;
    private ArrayList<Stop> mStops;
    private static MapState sMapState;

    private ArrayList<DrawerItem> mDrawerItems;

    private MapState(){
        mDrawerItems = new ArrayList<DrawerItem>();
        mShuttles = new Shuttle[4];
        mStops = new ArrayList<Stop>();
    }

    public static MapState get(){
        if (sMapState == null){
            sMapState = new MapState();
        }
        return sMapState;
    }



    public Shuttle[] getShuttles() {
        return mShuttles;
    }


    public void setShuttle(int index, Shuttle shuttle) {
        mShuttles[index] = shuttle;
    }

    public ArrayList<Stop> getStops() {
        return mStops;
    }

    public void setStops(ArrayList<Stop> stops) {
        mStops = stops;
    }

    public ArrayList<DrawerItem> getDrawerItems() {
        return mDrawerItems;
    }

    public void setDrawerItems(ArrayList<DrawerItem> drawerItems) {
        mDrawerItems = drawerItems;
    }
}
