package com.jordann.maptest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by sellersk on 9/11/2014.
 */
public class InitialNetworkRequestor extends AsyncTask<Void, Void, Boolean> {

    private static MapState sMapState;
    private static final String stopUrl = "http://www.osushuttles.com/Services/JSONPRelay.svc/GetRoutesForMapWithSchedule";
    private static final String shuttleUrl = "http://portal.campusops.oregonstate.edu/files/shuttle/GetMapVehiclePoints.txt";
    private static final String etaUrl = "";

    private static final int NORTH_ROUTE_ID = 3;
    private static final int WEST_ROUTE_ID = 2;
    private static final int EAST_ROUTE_ID = 1;

    private OnInitialRequestComplete listener;

    public interface OnInitialRequestComplete{
        void onPostInitialRequest(boolean success);
    }

    public InitialNetworkRequestor(){
        sMapState = MapState.get();
        listener = (OnInitialRequestComplete)sMapState.getCurrentContext();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        if(!isNetworkAvailable()){
            return false;
        }

        JSONGetter getter = new JSONGetter();

        JSONArray jStopsArray = getter.getJSONFromUrl(stopUrl);

        if (jStopsArray == null){
            return false;
        }

        JSONArray jShuttlesArray = getter.getJSONFromUrl(shuttleUrl);

        if (jShuttlesArray == null){
            return false;
        }

        /*
        JSONArray jEtaArray = getter.getJSONFromUrl(etaUrl);

        if (jEtaArray == null){
            return false;
        }
        */

        boolean jsonSuccess = parseJSON(jStopsArray, jShuttlesArray);


        return jsonSuccess;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        listener.onPostInitialRequest(success);
        //super.onPostExecute(success);
    }




    //TODO: reduce number of string conversions here and JSONGetter
    private boolean parseJSON(JSONArray jStopsArray, JSONArray jShuttlesArray/*, JSONArray jEtaArray*/){
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
                return false;
            }
        }








        Gson gson = new Gson();
        boolean[] onlineStates = {false,false,false,false};
        if(jShuttlesArray != null) {
            for (int i = 0; i < jShuttlesArray.length(); i++) {

                String json = null;
                try {
                    JSONObject rawJson = jShuttlesArray.getJSONObject(i);
                    json = rawJson.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
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
            //Log.d(TAG, "SET SHUTTLE OFFLINE " + shuttle.isOnline());
            if (!onlineStates[i]){
                shuttle.setOnline(false);

            }
            else shuttle.setOnline(true);
        }


        //Test Times

            Random random = new Random();
            int maxNum = 600;
            for (int i = 0, len = stops.size(); i < len; i++) {
                stops.get(i).setShuttleETAs(new int[]{random.nextInt(maxNum), random.nextInt(maxNum), random.nextInt(maxNum), random.nextInt(maxNum)});

            }

        sMapState.setStops(stops);


          /* stops = sMapState.getStops();
           for(int i = 0; i < stops.size(); i++) {
               //Log.d(TAG, "1stopShuttleETAS: " + stops.get(i).getShuttleETAs()[0] + " , " + stops.get(i).getShuttleETAs()[1] + " , " + stops.get(i).getShuttleETAs()[2] + " , " + stops.get(i).getShuttleETAs()[3] + " , ");
           }*/

        return true;
    }

    public boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager)sMapState.getCurrentContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return(cm.getActiveNetworkInfo() != null);
    }




}
