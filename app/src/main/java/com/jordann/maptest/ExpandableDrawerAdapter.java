package com.jordann.maptest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

/*
  Created by sellersk on 8/27/2014.
*/
public class ExpandableDrawerAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "ExpandableDrawerAdapter";

    private final Context mContext;
    private ArrayList<DrawerItem> mDrawerItems;
    private static  MapState sMapState;

    private static final int NORTH = 6, WEST = 7, EAST = 8;

    public ExpandableDrawerAdapter(Context context, ArrayList<DrawerItem> drawerItems) {
        super();
        mContext = context;
        mDrawerItems = drawerItems;
        sMapState = MapState.get();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_section_header, parent, false);
        }
        TextView sectionTitle = (TextView)convertView.findViewById(R.id.drawer_section_header_title);
        TextView groupTitle = (TextView)convertView.findViewById(R.id.drawer_group_item_title);
        ImageView imageView = (ImageView)convertView.findViewById(R.id.drawer_group_image_view);

        int imageViewResId;
        if(isExpanded){
            imageViewResId = R.drawable.ic_action_collapse;
        }else{
            imageViewResId = R.drawable.ic_action_expand;
        }

        if(groupPosition == 0){ //"Shuttles" header
            sectionTitle.setText("Shuttles");
            convertView.setEnabled(false);
            convertView.setOnClickListener(null);
        }else if(groupPosition < 5){ //Shuttle Names... "West A", "North"
            Shuttle shuttle = mDrawerItems.get(groupPosition).getShuttle();
            groupTitle.setText(shuttle.getName());
            if(!shuttle.isOnline()){
                convertView.setEnabled(false);
                //TODO: gray out to show disabled
            }
        }else {
            String title = mDrawerItems.get(groupPosition).getTitle();
            groupTitle.setText("");
            sectionTitle.setText("");
            imageView.setVisibility(View.VISIBLE);
            switch (groupPosition){
                case 5:
                    sectionTitle.setText(title);
                    imageView.setVisibility(View.INVISIBLE);
                    convertView.setEnabled(false);
                    convertView.setOnClickListener(null);
                    break;
                case NORTH:
                    groupTitle.setText(title);
                    imageView.setImageResource(imageViewResId);
                    break;
                case WEST:
                    groupTitle.setText(title);
                    imageView.setImageResource(imageViewResId);
                    break;
                case EAST:
                    groupTitle.setText(title);
                    imageView.setImageResource(imageViewResId);
                    break;
                default:
                    groupTitle.setText("DEFAULT");
            }
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Log.d(TAG, "getChildView");
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
        }
        TextView childTitle = (TextView)convertView.findViewById(R.id.drawer_list_item_title);
        int stopsIndex = mDrawerItems.get(groupPosition).getStopsIndex().get(childPosition);
        String title = sMapState.getStops().get(stopsIndex).getName();
        switch (groupPosition){
            case NORTH:
                childTitle.setText(title);
                break;
            case WEST:
                childTitle.setText(title);
                break;
            case EAST:
                childTitle.setText(title);
                break;
            default:
                childTitle.setText("DEFAULT");
        }
        return convertView;
    }

    @Override
    public int getGroupCount() {
        return sMapState.getDrawerItems().size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        switch (groupPosition){
            case NORTH:
                return mDrawerItems.get(groupPosition).getStopsIndex().size();
            case WEST:
                return mDrawerItems.get(groupPosition).getStopsIndex().size();
            case EAST:
                return mDrawerItems.get(groupPosition).getStopsIndex().size();
            default:
                return 0;
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
