package com.jordann.maptest;

import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/*
  Created by sellersk on 8/28/2014.
 */
public class InitialStopsTask extends AsyncTask<String, Void, JSONArray> {
    private static final String TAG = "InitialStopsTask";

    private String url;
    private static MapState sMapState;

    public InitialStopsTask(String url){
        super();
        sMapState = MapState.get();
        this.url = url;
    }

    @Override
    protected void onPostExecute(JSONArray j) {
        super.onPostExecute(j);
        parseJSON(j);
    }

    @Override
    protected JSONArray doInBackground(String... params) {
        JSONGetter getter = new JSONGetter();
        JSONArray jStopsArray = getter.getJSONFromUrl(url);

        return jStopsArray;
    }

    private void parseJSON(JSONArray jStopsArray){
        ArrayList<Stop> stops = new ArrayList<Stop>();
        ArrayList<String> stopNames = new ArrayList<String>();

        for (int i = 0, len = jStopsArray.length(); i < len; i++){
            JSONObject route;
            try {
                route = jStopsArray.getJSONObject(i);
                JSONArray landmarks = route.getJSONArray("Landmarks");

                for (int j = 0; j < landmarks.length(); j++){
                    JSONObject landmark = landmarks.getJSONObject(j);

                    if (!stopNames.contains(landmark.getString("Label"))){
                        stopNames.add(landmark.getString("Label"));
                        Stop stop = new Stop(landmark.getDouble("Latitude"), landmark.getDouble("Longitude"), landmark.getString("Label"), new int[]{-1,-1,-1,-1});
                        stops.add(stop);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        sMapState.setStops(stops);
    }
}
