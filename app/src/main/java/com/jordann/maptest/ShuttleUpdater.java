package com.jordann.maptest;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Random;

/*
  Created by jordan_n on 8/21/2014.
*/
public class ShuttleUpdater {
    private static final String TAG = "ShuttleUpdater";

    private static ShuttleUpdater sShuttleUpdater;
    private static MapState sMapState;
    private OnMapStateUpdate listener;

    private Handler mHandler;
    private boolean isBusy = false;
    private boolean stop = true;
    private Runnable r;
    private static final long ASYNC_DELAY = 5000;
    private String urlShuttlePoints = "http://portal.campusops.oregonstate.edu/files/shuttle/GetMapVehiclePoints.txt";

    private static final int NORTH_ROUTE_ID = 3;
    private static final int WEST_ROUTE_ID = 2;
    private static final int EAST_ROUTE_ID = 1;

    public interface OnMapStateUpdate{
        void updateMap();
    }


    private ShuttleUpdater(OnMapStateUpdate listener) {
        super();
        this.listener = listener;
        sMapState = MapState.get();
        mHandler = new Handler();
    }

    public static ShuttleUpdater get(OnMapStateUpdate listener){
        if(sShuttleUpdater == null){
            sShuttleUpdater = new ShuttleUpdater(listener);
        }
        return sShuttleUpdater;
    }

    private void startHandler(){
        r = new Runnable() {
            @Override
            public void run() {
                if(!isBusy) new pollNewDataTask(urlShuttlePoints).execute();
                if(!stop) startHandler();
            }
        };
        mHandler.postDelayed(r, ASYNC_DELAY);
    }

    public void stopShuttleUpdater(){
        stop = true;
        mHandler.removeCallbacks(r);
    }

    public void startShuttleUpdater(){
        stop = false;
        new pollNewDataTask(urlShuttlePoints).execute();
        startHandler();
    }

    private class pollNewDataTask extends AsyncTask<String, Void, Void>{
        private static final String TAG = "pollNewDataTask";

        private String url;
        private JSONArray JSONShuttles;
        private JSONArray[] jStopsArray;

        public pollNewDataTask(String url) {
            super();
            this.url = url;
        }

        @Override
        protected Void doInBackground(String... params) {
            JSONGetter getter = new JSONGetter();
            JSONShuttles = getter.getJSONFromUrl(url);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            parseJSON();
            listener.updateMap();
        }

        //TODO: reduce number of string conversions here and JSONGetter
       private void parseJSON(){
            Gson gson = new Gson();
            boolean[] onlineStates = {false,false,false,false};
            if(JSONShuttles != null) {
                for (int i = 0; i < JSONShuttles.length(); i++) {
                    String json = null;
                    try {
                        JSONObject rawJson = JSONShuttles.getJSONObject(i);
                        json = rawJson.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Shuttle shuttle = gson.fromJson(json, Shuttle.class);
                    shuttle.setOnline(true);

                    switch (shuttle.getRouteID()) {
                        case NORTH_ROUTE_ID:
                            sMapState.setShuttle(0, shuttle);
                            onlineStates[0] = true;
                            break;
                        case WEST_ROUTE_ID:  //Double route
                            if (sMapState.getShuttles().get(1).getVehicleId() == shuttle.getVehicleId()) {
                                sMapState.setShuttle(1, shuttle);
                                onlineStates[1] = true;
                            } else if (sMapState.getShuttles().get(2).getVehicleId() == shuttle.getVehicleId()) {
                                sMapState.setShuttle(2, shuttle);
                                onlineStates[2] = true;
                            } else if (!sMapState.getShuttles().get(1).isOnline()) {
                                sMapState.setShuttle(1, shuttle);
                                onlineStates[1] = true;
                            } else if (!sMapState.getShuttles().get(2).isOnline()) {
                                sMapState.setShuttle(2, shuttle);
                                onlineStates[2] = true;
                            }
                            break;
                        case EAST_ROUTE_ID:
                            sMapState.setShuttle(3, shuttle);
                            onlineStates[3] = true;
                            break;
                    }
                }
            }
            for (int i = 0; i < 4; i++) {
               Shuttle shuttle = sMapState.getShuttles().get(i);
               if (!onlineStates[i]) shuttle.setOnline(false);
               else shuttle.setOnline(true);
            }


            //Test Times
            ArrayList<Stop> stops = sMapState.getStops();
            Random random = new Random();
            int maxNum = 600;
            for (int i = 0, len = stops.size(); i < len; i++){
                stops.get(i).setShuttleETAs(new int[]{random.nextInt(maxNum),random.nextInt(maxNum),random.nextInt(maxNum),random.nextInt(maxNum)});
            }
       }
    }
}