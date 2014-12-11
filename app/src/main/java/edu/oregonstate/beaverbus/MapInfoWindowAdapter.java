package edu.oregonstate.beaverbus;

import android.content.Context;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/*
  Created by sellersk on 8/26/2014.

  Returns custom infoWindow for a stop.
  Displays ETA times of stopObj obtained from marker.
*/
public class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final static String TAG = "MapInfoWindowAdapter";

    private static MapState sMapState;
    private final Context mContext;
    private final LayoutInflater inflater;

    public MapInfoWindowAdapter(final Context context) {
        sMapState = MapState.get();
        mContext = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        //Find stopObj that matches marker
        for (Stop stop : sMapState.getStops()){
            if (stop.getMarker().equals(marker)){ //Found stopObj

                //Initialized to false. If any ETA exists, it is set to true.
                //Allows "Offline" to be written.
                boolean shuttleShown = false;

                View stopView = inflater.inflate(R.layout.info_stop_new, null);
                LinearLayout container = (LinearLayout)stopView.findViewById(R.id.eta_container);

                int[] shuttleEtas = stop.getShuttleETAs();
                for(int i = 0; i < shuttleEtas.length; i++){
                    int eta = shuttleEtas[i];
                    if(eta != -1){
                        shuttleShown = true;

                        View stopSection = inflater.inflate(R.layout.info_stop_section, null);
                        View square = stopSection.findViewById(R.id.info_stop_square);
                        TextView etaText = (TextView)stopSection.findViewById(R.id.info_stop_eta);
                        //etaText.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));

                        //Set square color based on ETA index
                        switch (i){
                            case 0:
                                square.setBackgroundColor(mContext.getResources().getColor(R.color.shuttle_green));
                                break;
                            case 1:
                                square.setBackgroundColor(mContext.getResources().getColor(R.color.shuttle_orange));
                                break;
                            case 2:
                                square.setBackgroundColor(mContext.getResources().getColor(R.color.shuttle_orange));
                                break;
                            case 3:
                                square.setBackgroundColor(mContext.getResources().getColor(R.color.shuttle_purple));
                                break;
                        }
                        etaText.setText(""+eta);
                        container.addView(stopSection);
                    }
                }
                if(!shuttleShown){ //Write "Offline" in place of 0 ETA times
                    TextView textView = new TextView(mContext);
                    textView.setText(R.string.offline);
                    textView.setTextColor(mContext.getResources().getColor(R.color.Favorite_Offline_Text));
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                    container.addView(textView);
                }

                //Set custom image bottom pointer
                ImageView imageView = new ImageView(mContext);
                imageView.setImageResource(R.drawable.info_window_bottom_triangle);

                return stopView;
            }
        }
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
