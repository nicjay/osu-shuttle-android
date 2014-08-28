package com.jordann.maptest;

import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import java.util.ArrayList;

/*
  Created by sellersk on 8/28/2014.
 */
public class DrawerItemClickListener implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {
    private static final String TAG = "DrawerItemClickListener";

    private static MapState sMapState;
    private DrawerLayout mDrawerLayout;
    private ExpandableListView mDrawerList;

    public DrawerItemClickListener(DrawerLayout layout, ExpandableListView listView) {
        super();
        sMapState = MapState.get();
        mDrawerLayout = layout;
        mDrawerList = listView;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        selectItemGroup(groupPosition);
        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        selectItemChild(groupPosition, childPosition);
        return false;
    }

    private void selectItemGroup(int groupPosition) {
        Log.d(TAG, "selectItemGroup");
        ArrayList<DrawerItem> drawerItems = sMapState.getDrawerItems();

        if(drawerItems.get(groupPosition).getTypeId() == 1){
            sMapState.animateMap(drawerItems.get(groupPosition).getShuttle().getLatLng());
            drawerItems.get(groupPosition).getShuttle().getMarker().showInfoWindow();
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    private void selectItemChild(int groupPosition, int childPosition){
        Log.d(TAG, "selectItemChild");
        ArrayList<DrawerItem> drawerItems = sMapState.getDrawerItems();
        ArrayList<Stop> stops = sMapState.getStops();

        int index = drawerItems.get(groupPosition).getStopsIndex().get(childPosition);
        sMapState.animateMap(stops.get(index).getLatLng());
        stops.get(index).getMarker().showInfoWindow();
        mDrawerLayout.closeDrawer(mDrawerList);
    }
}