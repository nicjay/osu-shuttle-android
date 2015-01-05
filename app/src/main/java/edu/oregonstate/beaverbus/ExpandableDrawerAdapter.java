package edu.oregonstate.beaverbus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/*
  Created by sellersk on 8/27/2014.
*/
public class ExpandableDrawerAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "ExpandableDrawerAdapter";

    private final Context mContext;
    private ArrayList<DrawerItem> mDrawerItems;
    private static MapState sMapState;

    private static final int NORTH = 6, WEST = 7, EAST = 8; //Indices for expandable list headers

    public ExpandableDrawerAdapter(Context context, ArrayList<DrawerItem> drawerItems) {
        super();
        mContext = context;
        mDrawerItems = drawerItems;
        sMapState = MapState.get();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View sectionView;   //Route Header. e.g. North, West, East
        View itemView;      //Stop or Shuttle

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (groupPosition == 0 || groupPosition == 5) { //Header index

            sectionView = inflater.inflate(R.layout.drawer_section_header, parent, false);
            sectionView.setEnabled(false);
            sectionView.setOnClickListener(null);

            TextView sectionTitle = (TextView) sectionView.findViewById(R.id.drawer_section_header_title);
            sectionTitle.setText(mDrawerItems.get(groupPosition).getTitle());

            ImageView sectionIcon = (ImageView) sectionView.findViewById(R.id.drawer_section_icon);

            //Two different icons: stop and shuttle, 0 and 5.
            if (groupPosition == 0) {
                sectionIcon.setImageResource(R.drawable.nav_drawer_bus_icon);
            } else {
                sectionIcon.setImageResource(R.drawable.nav_drawer_map_marker_icon);
                sectionIcon.getLayoutParams().height = 28;
            }
            return sectionView;
        } else {    //ELSE it's a child

            itemView = inflater.inflate(R.layout.drawer_item, parent, false);
            TextView itemTitle = (TextView) itemView.findViewById(R.id.drawer_item_title);
            TextView itemOfflineText = (TextView) itemView.findViewById(R.id.drawer_item_offline_text);

            if (groupPosition < 5) { //Indices 1-4: shuttle
                Shuttle shuttle = mDrawerItems.get(groupPosition).getShuttle();
                itemTitle.setText(shuttle.getName());

                //Create colored route square
                View square = new View(mContext);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(18, 18);
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                layoutParams.setMargins(16, 0, 0, 0);
                square.setId(R.id.drawer_item_square);
                switch (groupPosition) {
                    case 1:
                        //North
                        square.setBackgroundResource(R.color.shuttle_green);
                        break;
                    case 2:
                        //West 1
                        square.setBackgroundResource(R.color.shuttle_orange);
                        break;
                    case 3:
                        //West 2
                        square.setBackgroundResource(R.color.shuttle_orange);
                        break;
                    case 4:
                        //East
                        square.setBackgroundResource(R.color.shuttle_purple);
                }
                square.setLayoutParams(layoutParams);

                //Set title to right of square
                RelativeLayout.LayoutParams LL = (RelativeLayout.LayoutParams) itemTitle.getLayoutParams();
                LL.addRule(RelativeLayout.RIGHT_OF, R.id.drawer_item_square);
                itemTitle.setLayoutParams(LL);

                //Add square to view
                ((RelativeLayout) itemView.findViewById(R.id.drawer_item_relative_layout)).addView(square);

                //Disable offline shuttles (show "OFFLINE" text)
                if (!shuttle.isOnline()) {
                    itemView.setEnabled(false);
                    itemTitle.setTextColor(mContext.getResources().getColor(R.color.Navigation_Drawer_Shuttle_Disabled_Text));
                    itemOfflineText.setVisibility(View.VISIBLE);
                } else {
                    itemView.setEnabled(true);
                }

            } else {    //Expandable Route Headers
                String stopTitle = mDrawerItems.get(groupPosition).getTitle();
                ImageView imageView = (ImageView) itemView.findViewById(R.id.drawer_item_carrot);
                imageView.setVisibility(View.VISIBLE);

                //Set correct icon based on state of expanded list header
                if (isExpanded) {
                    imageView.setImageResource(R.drawable.ic_action_collapse);
                } else {
                    imageView.setImageResource(R.drawable.ic_action_expand);
                }

                itemTitle.setText(stopTitle);
            }

            return itemView;
        }
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_child_item, parent, false);
        }
        TextView childTitle = (TextView) convertView.findViewById(R.id.drawer_list_item_title);
        switch (groupPosition) {
            case NORTH:
                childTitle.setText(sMapState.getNorthStops().get(childPosition).getName());
                break;
            case WEST:
                childTitle.setText(sMapState.getWestStops().get(childPosition).getName());
                break;
            case EAST:
                childTitle.setText(sMapState.getEastStops().get(childPosition).getName());
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
        switch (groupPosition) {
            case NORTH:
                return sMapState.getNorthStops().size();
            case WEST:
                return sMapState.getWestStops().size();
            case EAST:
                return sMapState.getEastStops().size();
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
