package edu.oregonstate.beaverbus;

import java.util.ArrayList;

/*
  Created by jordan_n on 8/22/2014.
 */
public class DrawerItem {
    private static final String TAG = "DrawerItem";

    private int mTypeId;    //Differentiates between: list header, child, expandable
    private String mTitle;

    private Shuttle mShuttle;   //List items 1-4 represent the 4 shuttle objects
    private ArrayList<Integer> mStopsIndex; //Stop objects for each route. Indices 6-8

    private boolean mRowEnabled;    //Currently unused. TODO: disabled when shuttle offline

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
