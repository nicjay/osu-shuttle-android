package edu.oregonstate.beaverbus;

import android.content.Context;
import android.util.Log;
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
    private static  MapState sMapState;

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

            TextView sectionTitle = (TextView)sectionView.findViewById(R.id.drawer_section_header_title);
            sectionTitle.setText(mDrawerItems.get(groupPosition).getTitle());

            ImageView sectionIcon = (ImageView)sectionView.findViewById(R.id.drawer_section_icon);

            //Two different icons: stop and shuttle, 0 and 5.
            if(groupPosition == 0) {
                sectionIcon.setImageResource(R.drawable.shuttle_grey);
            }else{
                sectionIcon.setImageResource(R.drawable.map_marker_icon);
                sectionIcon.getLayoutParams().height = 25;
                //sectionIcon.setPadding(3, 3, 3, 3);
            }
            return sectionView;
        } else {    //ELSE it's a child

            itemView = inflater.inflate(R.layout.drawer_item, parent, false);
            TextView itemTitle = (TextView)itemView.findViewById(R.id.drawer_item_title);

            if (groupPosition < 5){ //Indices 1-4: shuttle  //TODO: set title on click
                Shuttle shuttle = mDrawerItems.get(groupPosition).getShuttle();
                itemTitle.setText(shuttle.getName());

                //Create colored route square
                View square = new View(mContext);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(18, 18);
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                layoutParams.setMargins(16, 0, 0, 0);
                square.setId(R.id.drawer_item_square);
                switch (groupPosition){
                    case 1:
                        square.setBackgroundColor(0xFF70A800);  //North
                        break;
                    case 2:
                        square.setBackgroundColor(0xFFE0AA0F);  //West 1
                        break;
                    case 3:
                        square.setBackgroundColor(0xFFE0AA0F);  //West 2
                        break;
                    case 4:
                        square.setBackgroundColor(0xFFAA66CD);  //East
                }
                square.setLayoutParams(layoutParams);

                //Set square to leftOf Title
                RelativeLayout.LayoutParams LL = (RelativeLayout.LayoutParams)itemTitle.getLayoutParams();
                LL.addRule(RelativeLayout.RIGHT_OF, R.id.drawer_item_square);
                itemTitle.setLayoutParams(LL);

                //Add square to view
                ((RelativeLayout)itemView.findViewById(R.id.drawer_item_relative_layout)).addView(square);

                //Disable offline shuttles
                if(!mDrawerItems.get(groupPosition).isRowEnabled()){
                    itemView.setEnabled(false);
                    //TODO: gray out offline shuttle
                }
            } else {    //Expandable Route Headers

                String stopTitle = mDrawerItems.get(groupPosition).getTitle();
                ImageView imageView = (ImageView)itemView.findViewById(R.id.drawer_item_carrot);
                imageView.setVisibility(View.VISIBLE);

                //Set correct icon based on state of expanded list header
                if(isExpanded){
                    imageView.setImageResource(R.drawable.ic_action_collapse);
                }else{
                    imageView.setImageResource(R.drawable.ic_action_expand);
                }

                itemTitle.setText(stopTitle);
            }
            return itemView;
        }
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_child_item, parent, false);
        }
        TextView childTitle = (TextView)convertView.findViewById(R.id.drawer_list_item_title); //Title
        switch (groupPosition){ //TODO: add in new stopNames once finalized
            case NORTH:
                //childTitle.setText("N-stop " + (childPosition+1));
                childTitle.setText(sMapState.getNorthMap().get(childPosition).getName());
                break;
            case WEST:
                childTitle.setText(sMapState.getWestMap().get(childPosition).getName());
                break;
            case EAST:
                childTitle.setText(sMapState.getEastMap().get(childPosition).getName());
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
                return sMapState.getNorthMap().size();
            case WEST:
                return sMapState.getWestMap().size();
            case EAST:
                return sMapState.getEastMap().size();
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
