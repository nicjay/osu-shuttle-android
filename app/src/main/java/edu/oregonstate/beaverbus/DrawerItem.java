package edu.oregonstate.beaverbus;

import java.util.ArrayList;

/*
  Created by jordan_n on 8/22/2014.
 */
public class DrawerItem {
    private static final String TAG = "DrawerItem";

    private int mTypeId;
    private String mTitle;

    private Shuttle mShuttle;
    private ArrayList<Integer> mStopsIndex;

    private boolean mRowEnabled;

    //Section constructor
    public DrawerItem(int typeId, String title) {
        mTypeId = typeId;
        mTitle = title;
    }

    //Shuttle Row constructor
    public DrawerItem(int typeId, String title, Shuttle shuttle, boolean rowEnabled) {
        mTypeId = typeId;
        mTitle = title;
        mShuttle = shuttle;
        mRowEnabled = rowEnabled;
    }

    //Route Row constructor
    public DrawerItem(int typeId, String title, ArrayList<Integer> stopsIndex) {
        mTypeId = typeId;
        mTitle = title;
        mStopsIndex = stopsIndex;
    }

    public void setShuttle(Shuttle shuttle) {
        mShuttle = shuttle;
    }

    public int getTypeId() {
        return mTypeId;
    }

    public String getTitle() {
        return mTitle;
    }

    public Shuttle getShuttle() {
        return mShuttle;
    }

    public ArrayList<Integer> getStopsIndex() {
        return mStopsIndex;
    }

    public boolean isRowEnabled() {
        return mRowEnabled;
    }

    public void setRowEnabled(boolean rowEnabled) {
        mRowEnabled = rowEnabled;
    }
}
