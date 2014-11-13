package edu.oregonstate.beaverbus;

import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/*
  Created by sellersk on 8/28/2014.
 */
public class DrawerItemClickListener implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {
    private static final String TAG = "DrawerItemClickListener";

    private static MapState sMapState;  //Reference to singleton
    private DrawerLayout mDrawerLayout;
    private ExpandableListView mDrawerList;

    private static final int NORTH = 6, WEST = 7, EAST = 8; //Indices for expandable list headers

    public DrawerItemClickListener(DrawerLayout layout, ExpandableListView listView) {
        super();
        sMapState = MapState.get();
        mDrawerLayout = layout;
        mDrawerList = listView;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        selectItemGroup(groupPosition); //Call helper function
        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        selectItemChild(groupPosition, childPosition); //Call helper function
        return false;
    }


    private void selectItemGroup(int groupPosition) {
        ArrayList<DrawerItem> drawerItems = sMapState.getDrawerItems();

        if (drawerItems.get(groupPosition).getTypeId() == 1){
            //Jump to shuttle location, hide Nav drawer
            sMapState.animateMap(drawerItems.get(groupPosition).getShuttle().getLatLng());

            if (sMapState.getSelectedStopMarker() != null) { //If a stop is currently selected
                if (!sMapState.isStopsVisible()){
                    sMapState.getSelectedStopMarker().setVisible(false);
                }
                sMapState.getSelectedStopMarker().hideInfoWindow();
                sMapState.setSelectedStopMarker(null, false);
            }
            mDrawerLayout.closeDrawer(mDrawerList);
        }

    }

    private void selectItemChild(int groupPosition, int childPosition){
        if (sMapState.getSelectedStopMarker() != null && !sMapState.isStopsVisible()){
            sMapState.setSelectedStopMarkerVisibility(false);
        }

        Marker marker;
        switch (groupPosition) {    //Switch to find which stopsIndex map to search in for childPosition
            case NORTH:
                marker = sMapState.getNorthMap().get(childPosition+1).getMarker();
                break;
            case WEST:
                marker = sMapState.getWestMap().get(childPosition+1).getMarker();
                break;
            case EAST:
                marker = sMapState.getEastMap().get(childPosition+1).getMarker();
                break;
            default:
                marker = null;
        }
        if(marker != null) {
            sMapState.animateMap(marker.getPosition());
            sMapState.setSelectedStopMarker(marker, true);
            marker.setVisible(true);
            marker.showInfoWindow();
        }

        mDrawerLayout.closeDrawer(mDrawerList);
    }
}