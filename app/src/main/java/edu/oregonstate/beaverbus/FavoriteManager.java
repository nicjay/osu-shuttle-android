package edu.oregonstate.beaverbus;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/**
 * Created by sellersk on 11/17/2014.
 */
public class FavoriteManager {
    private static final String TAG = FavoriteManager.class.getSimpleName();

    //Favorites view variables
    private static ArrayList<FavoriteStopRow> favoriteStopRows;
    private static LinearLayout favoritesView;
    public final int FAV_ICON_DISABLED = 0;
    public final int FAV_ICON_EMPTY = 1;
    public final int FAV_ICON_FILLED = 2;
    private int favIconState = FAV_ICON_DISABLED;
    private Context mContext;
    private static MapState mMapState;
    private MapsActivity mActivity;

    public void clearFavorites(){
        favoriteStopRows.clear();
        favoritesView.removeAllViews();
        saveFavoriteStopRows();
        if (mMapState.getSelectedStopMarker() != null) setFavIcon(FAV_ICON_EMPTY);
    }

    public void initSavedFavorites(){
        if(favoritesView.getChildCount() == 0) loadFavoriteStopRows();
    }


    public void updateFavorites(){
        for (int i = 0; i < favoriteStopRows.size(); i++){
            FavoriteStopRow row = favoriteStopRows.get(i);
            int[] shuttleETAs = row.getFavStopObj().getShuttleETAs();

            TextView[] textViews = row.getFavStopETAs();

            //TODO: fails sometimes. shuttleETAs[j] is null
            for (int j = 0; j < shuttleETAs.length; j++){
                if(shuttleETAs[j] == -1) continue;
                textViews[j].setText(String.valueOf(shuttleETAs[j]));
            }
        }
    }

    public FavoriteManager(MapsActivity activity, Context context) {
        mContext = context;
        favoritesView = (LinearLayout)activity.findViewById(R.id.favorites_view);
        favoriteStopRows = new ArrayList<FavoriteStopRow>();
        mMapState = mMapState.get();
        mActivity = activity;
    }

    public void onMapClick(Marker marker){
        boolean favorite = false;
        if (!marker.isFlat()) {
            for (int i = 0; i < favoriteStopRows.size(); i++){
                if(favoriteStopRows.get(i).getFavStopObj().getMarker().equals(marker)){
                    favorite = true;
                    break;
                }
            }
            if(favorite) setFavIcon(FAV_ICON_FILLED);
            else setFavIcon(FAV_ICON_EMPTY);

        } else setFavIcon(FAV_ICON_DISABLED);
    }

    public void addFavorite(){
        if (favIconState == FAV_ICON_DISABLED) Toast.makeText(mContext, mContext.getResources().getString(R.string.disabled_favorites), Toast.LENGTH_SHORT).show();
        if (mMapState.getSelectedStopMarker() != null){ //Stop must be selected
            ArrayList<Stop> mStops = mMapState.getStops();
            LatLng newFavCoords = mMapState.getSelectedStopMarker().getPosition();

            outerloop:
            for (Stop stop : mStops) {
                if (stop.getLatLng().equals(newFavCoords)) { //Found stopObj
                    for (int j = 0; j < favoriteStopRows.size(); j++){
                        if (favoriteStopRows.get(j).getFavStopObj().equals(stop)){ //If stopObj is already in favorites, remove
                            favoritesView.removeView(favoriteStopRows.get(j).getFavRow());
                            favoriteStopRows.remove(j);
                            setFavIcon(FAV_ICON_EMPTY);
                            saveFavoriteStopRows();
                            break outerloop;
                        }
                    }
                    if (favoriteStopRows.size() >= 3){
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.max_favorites), Toast.LENGTH_SHORT).show();
                    } else {
                        setFavIcon(FAV_ICON_FILLED);
                        addFavoriteRow(stop, true);
                    }
                    break;
                }
            }
        }
    }
    //Adds new favorite row view for given stopObj. Called on add new favorite, and to load saved favorites.
    public void addFavoriteRow(Stop newFavoriteStop, boolean save){
        final Stop newStop = newFavoriteStop;

        FavoriteStopRow newFavoriteStopRow = new FavoriteStopRow();
        View newFavRow = mActivity.getLayoutInflater().inflate(R.layout.favorite_row, null, false);
        LinearLayout timesView = (LinearLayout)newFavRow.findViewById(R.id.favorite_times_layout);

        TextView favStopName = (TextView)newFavRow.findViewById(R.id.favorite_stop_name);
        favStopName.setText(newStop.getName());

        newFavRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.onMapMarkerClick(newStop.getMarker());
                //newFavoriteStop.getMarker().showInfoWindow();
            }
        });
        newFavoriteStopRow.setFavRow(newFavRow);
        newFavoriteStopRow.setFavStopName(favStopName);
        newFavoriteStopRow.setFavStopObj(newStop);

//        if (favoriteStopRows.size() == 0) { //If first row, set no margin on bottom. Default bottom-margin in xml of rowView
//            LinearLayout.LayoutParams nameParams = (LinearLayout.LayoutParams) favStopName.getLayoutParams();
//            nameParams.setMargins(0, 0, 0, 0);
//            favStopName.setLayoutParams(nameParams);
//
//            LinearLayout.LayoutParams timeParams = (LinearLayout.LayoutParams) timesView.getLayoutParams();
//            timeParams.setMargins(0, 0, 0, 0);
//            timesView.setLayoutParams(timeParams);
//        }
//
//        if(favoriteStopRows.size() > 0){
//            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)newFavoriteStopRow.getFavRow().getLayoutParams();
//            favStopName.setBackgroundResource(R.drawable.favorite_name_border_no_top);
//            timesView.setBackgroundResource(R.drawable.favorite_times_border_no_top);
//            Log.d(TAG, "Setting to no Top");
//        }

        for (int i = 0; i < 4; i++){ //For each ETA in shuttleETAs
            if (newStop.getShuttleETA(i) == -1) continue;

            View newTime = mActivity.getLayoutInflater().inflate(R.layout.favorite_row_eta, null, false);
            View stopSquare = (View)newTime.findViewById(R.id.favorite_info_stop_square);
            TextView stopETA = (TextView)newTime.findViewById(R.id.favorite_info_stop_eta);

            stopETA.setText(Integer.toString(newStop.getShuttleETA(i)));

            int etaColor;
            switch (i){
                case 0:
                    etaColor = mContext.getResources().getColor(R.color.shuttle_green);
                    break;
                case 1:
                    etaColor = mContext.getResources().getColor(R.color.shuttle_orange);
                    break;
                case 2:
                    etaColor = mContext.getResources().getColor(R.color.shuttle_orange);
                    break;
                case 3:
                    etaColor = mContext.getResources().getColor(R.color.shuttle_purple);
                    break;
                default:
                    etaColor = mContext.getResources().getColor(R.color.OSU_black);

            }
            stopSquare.setBackgroundColor(etaColor);
            timesView.addView(newTime);

            //Set textView of ETA in favObj for updating
            newFavoriteStopRow.setFavStopETA(stopETA, i);
        }
        favoritesView.addView(newFavRow);
        favoriteStopRows.add(newFavoriteStopRow);

        if(save) saveFavoriteStopRows(); //Save on newFavorite click. However, when loading initially, save after all have been loaded
    }

    private void setFavoritesViewDrawables(){
        for (FavoriteStopRow row : favoriteStopRows){
            if (favoriteStopRows.indexOf(row) == 0){
                row.getFavRow().findViewById(R.id.favorite_times_layout).setBackgroundResource(R.drawable.favorite_times_border);
                row.getFavRow().findViewById(R.id.favorite_stop_name).setBackgroundResource(R.drawable.favorite_name_border);
            }else{
                row.getFavRow().findViewById(R.id.favorite_times_layout).setBackgroundResource(R.drawable.favorite_times_border_no_top);
                row.getFavRow().findViewById(R.id.favorite_stop_name).setBackgroundResource(R.drawable.favorite_name_border_no_top);
            }

        }
    }

    public void saveFavoriteStopRows(){
        setFavoritesViewDrawables();
        SharedPreferences sharedPreferences = mActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();

        String savedString = "";
        for(int i = 0; i < favoriteStopRows.size(); i++){
            if(i != 0) savedString = savedString.concat("_");
            FavoriteStopRow favoriteStopRow = favoriteStopRows.get(i);
            savedString = savedString.concat(favoriteStopRow.getFavStopObj().getName());
        }
        prefsEditor.putString("BeaverBusFavorites", savedString);
        prefsEditor.commit();
    }

    public void loadFavoriteStopRows(){
        SharedPreferences sharedPreferences = mActivity.getPreferences(Context.MODE_PRIVATE);
        String favorites = sharedPreferences.getString("BeaverBusFavorites", "");
        String[] favoriteStopNames = favorites.split("_");

        ArrayList<Stop> stops = mMapState.getStops();
        for (int i = 0; i < favoriteStopNames.length; i++){ //For each favorite
            for (int j = 0; j < stops.size(); j++){
                if (favoriteStopNames[i].equals(stops.get(j).getName())){ //Find stopObj
                    addFavoriteRow(stops.get(j), false);    //Add saved favorite
                    break;
                }
            }
        }
        if(favoriteStopNames.length > 0) saveFavoriteStopRows();
    }

    public void setFavIcon(int newState){
        MenuItem item = mActivity.menuGlobal.getItem(0);
        favIconState = newState;
        switch (newState){
            case FAV_ICON_DISABLED:
                item.setIcon(R.drawable.favorite_star_disabled);
                break;
            case FAV_ICON_EMPTY:
                item.setIcon(R.drawable.favorite_star_empty);
                break;
            case FAV_ICON_FILLED:
                item.setIcon(R.drawable.favorite_star_filled);
                break;
        }
    }


}
