package edu.oregonstate.beaverbus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/*
  Created by sellersk on 9/11/2014.
  TODO: unneeded. merge with ShuttleUpdater to handle firstTime

  CLASS - InitialNetworkRequestor
  Pulls stop, shuttle, and ETA JSON URLS a single time to determine if error in JSON or network.
  Determines online states of shuttles.
 */
public class InitialNetworkRequestor extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "InitialNetworkRequestor";

    private static MapState sMapState;
    private static String stopUrl;
    private static String shuttleUrl;

    private static final int NORTH_ROUTE_ID = 7;
    private static final int WEST_ROUTE_ID = 9;
    private static final int EAST_ROUTE_ID = 8;

    private ArrayList<Stop> northStops = new ArrayList<Stop>();
    private ArrayList<Stop> westStops = new ArrayList<Stop>();
    private ArrayList<Stop> eastStops = new ArrayList<Stop>();


    //Interface to handle callback of 'network request' in MapsActivity
    private OnInitialRequestComplete listener;

    public interface OnInitialRequestComplete {
        void onPostInitialRequest(boolean success);
    }

    public InitialNetworkRequestor() {
        sMapState = MapState.get();
        listener = (OnInitialRequestComplete) sMapState.getCurrentContext();

        stopUrl = sMapState.getStopLocationsUrl();
        shuttleUrl = sMapState.getShuttleLocationsUrl();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        //False return at any point indicates a network or JSON error
        if (!isNetworkAvailable()) return false;

        //Get stops information
        JSONGetter stopGetter = new JSONGetter();
        JSONArray jStopsArray = stopGetter.getJSONFromUrl(stopUrl);
        if (jStopsArray == null) return false;

        //Get shuttle locations
        JSONGetter shuttleGetter = new JSONGetter();
        JSONArray jShuttlesArray = shuttleGetter.getJSONFromUrl(shuttleUrl);
        if (jShuttlesArray == null) return false;

        return parseJSON(jStopsArray, jShuttlesArray);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        //Callback in MapsActivity
        listener.onPostInitialRequest(success);
    }

    private boolean parseJSON(JSONArray jStopsArray, JSONArray jShuttlesArray) {
        ArrayList<Stop> stops = new ArrayList<Stop>();
        ArrayList<LatLng> seenLatLngs = new ArrayList<LatLng>();

        HashMap<Integer, Stop> stopsMap = new HashMap<Integer, Stop>();

        try {
            for (int i = 0, len = jStopsArray.length(); i < len; i++) {
                //Get JSONObject
                JSONObject stopJson = jStopsArray.getJSONObject(i);

                //Get stop info
                LatLng latLng = new LatLng(stopJson.getDouble("Latitude"), stopJson.getDouble("Longitude"));
                int routeId = stopJson.getInt("RouteID");
                int stopId = stopJson.getInt("RouteStopID");
                String stopName = stopJson.getString("Description");

                // JSON returns duplicate stop LatLngs for each Route they are on.
                if (!seenLatLngs.contains(latLng)) { //New stop object must be made.

                    Stop stop = new Stop(latLng, stopName, routeId, stopId, new int[]{-1, -1, -1, -1});

                    stopsMap.put(stopId, stop);
                    seenLatLngs.add(latLng);
                    stops.add(stop);

                    addStopToRouteMap(routeId, stop);

                } else { //Find existing stop object and add (routeId, stopId) to it
                    for (Stop existingStop : stops) {
                        if (existingStop.areLatLngEqual(latLng)) { //Found matching existing stop

                            existingStop.addServicedRoute(routeId);
                            existingStop.addStopId(stopId);
                            stopsMap.put(stopId, existingStop);

                            addStopToRouteMap(routeId, existingStop);
                            break;
                        }
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        sMapState.setStopsMap(stopsMap);

        //Parse shuttle JSON
        ArrayList<Shuttle> shuttles = sMapState.getShuttles();
        Gson gson = new Gson();
        boolean[] onlineStates = {false, false, false, false};
        if (jShuttlesArray != null) {
            for (int i = 0; i < jShuttlesArray.length(); i++) {
                String json = null;

                try {
                    json = jShuttlesArray.getJSONObject(i).toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }

                Shuttle shuttle = gson.fromJson(json, Shuttle.class); //Extract shuttleObject from JSON

                shuttle.setOnline(true);

                switch (shuttle.getRouteID()) {
                    case NORTH_ROUTE_ID:
                        sMapState.setShuttle(0, shuttle);
                        onlineStates[0] = true;
                        break;
                    case WEST_ROUTE_ID:  //Double WEST route
                        //If index 1 has vehicleId
                        if (shuttles.get(1).getVehicleId() == shuttle.getVehicleId()) {
                            sMapState.setShuttle(1, shuttle);
                            onlineStates[1] = true;
                        }
                        //If index 2 has vehicleId
                        else if (shuttles.get(2).getVehicleId() == shuttle.getVehicleId()) {
                            sMapState.setShuttle(2, shuttle);
                            onlineStates[2] = true;
                        }
                        //If index 1 is offline, set anew
                        else if (!shuttles.get(1).isOnline()) {
                            sMapState.setShuttle(1, shuttle);
                            onlineStates[1] = true;
                        }
                        //If index 2 is offline, set anew
                        else if (!shuttles.get(2).isOnline()) {
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
        //Sets online/offline status for each shuttleObj, based on whether it was just set anew
        for (Shuttle shuttle : shuttles) {
            int i = shuttles.indexOf(shuttle);
            if (!onlineStates[i]) shuttle.setOnline(false);
            else shuttle.setOnline(true);
        }

        sMapState.setStops(stops);
        sMapState.setNorthStops(northStops);
        sMapState.setWestStops(westStops);
        sMapState.setEastStops(eastStops);

        return true;
    }


    //Adds new stopNum to previously made stopObj.
    //Used to create list of stops for each route, shown in Navigation Drawer
    private void addStopToRouteMap(int routeId, Stop stop) {
        switch (routeId) {
            case NORTH_ROUTE_ID:
                northStops.add(stop);
                break;
            case WEST_ROUTE_ID:
                westStops.add(stop);
                break;
            case EAST_ROUTE_ID:
                eastStops.add(stop);
                break;
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) sMapState.getCurrentContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
    }
}
