package com.jordann.maptest;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by sellersk on 8/27/2014.
 */
public class ExpandableDrawerAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "ExpandableDrawerAdapter";

    private final Context mContext;
    private ArrayList<DrawerItem> mDrawerItems;
    private static  MapState sMapState;

    public ExpandableDrawerAdapter(Context context, ArrayList<DrawerItem> drawerItems) {
        super();
        mContext = context;
        mDrawerItems = drawerItems;
        sMapState = MapState.get();
    }

    @Override
    public int getGroupCount() {
        return 9;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        switch (groupPosition){
            case 6:
                return 1;
            case 7:
                return 1;
            case 8:
                return 1;
            default:
                return 0;
        }
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

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_section_header, null);

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
            groupTitle.setText("OBJ");
        }else {
            switch (groupPosition){
                case 5:
                    sectionTitle.setText("Stops");
                    convertView.setEnabled(false);
                    convertView.setOnClickListener(null);
                    break;
                case 6:
                    groupTitle.setText("North");
                    imageView.setImageResource(imageViewResId);
                    break;
                case 7:
                    groupTitle.setText("West");
                    imageView.setImageResource(imageViewResId);
                    break;
                case 8:
                    groupTitle.setText("East");
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
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_list_item, null);
        }
        TextView childTitle = (TextView)convertView.findViewById(R.id.drawer_list_item_title);
        switch (groupPosition){
            case 6:
                childTitle.setText("North child");
                convertView.setBackgroundColor(0xFF123123);
                break;
            case 7:
                childTitle.setText("West child");
                break;
            case 8:
                childTitle.setText("East child");
                break;
            default:

        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;

    }



}
