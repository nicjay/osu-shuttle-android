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
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

/**
 * Created by sellersk on 9/11/2014.
 *
 * CLASS - InitialNetworkRequestor
 * Pulls stop, shuttle, and ETA JSON URLS a single time, to determine if initial JSON or network errors.
 * Determines online states of shuttles.
 * Determines next three stops of shuttles.
 */
public class InitialNetworkRequestor extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "InitialNetworkRequestor";

    private static MapState sMapState;
    private static final String stopUrl = "http://www.osushuttles.com/Services/JSONPRelay.svc/GetStops"; //"http://www.osushuttles.com/Services/JSONPRelay.svc/GetRoutesForMapWithSchedule";
    private static final String shuttleUrl = "http://www.osushuttles.com/Services/JSONPRelay.svc/GetMapVehiclePoints"; //"http://portal.campusops.oregonstate.edu/files/shuttle/GetMapVehiclePoints.txt";
    private static final String etaUrl = "";

    private static final int NORTH_ROUTE_ID = 7;
    private static final int WEST_ROUTE_ID = 9;
    private static final int EAST_ROUTE_ID = 8;


    private HashMap<Integer, Stop> northMap;
    private HashMap<Integer, Stop> westMap = new HashMap<Integer, Stop>();
    private HashMap<Integer, Stop> eastMap = new HashMap<Integer, Stop>();



    private OnInitialRequestComplete listener;


    public interface OnInitialRequestComplete{
        void onPostInitialRequest(boolean success);
    }

    public InitialNetworkRequestor(){
        sMapState = MapState.get();
        northMap = new HashMap<Integer, Stop>();

        listener = (OnInitialRequestComplete)sMapState.getCurrentContext();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        //False return at any point indicates a network or JSON error
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
        Log.d(TAG, "Finished request!");
        listener.onPostInitialRequest(success);
    }


    //TODO: reduce number of string conversions here and JSONGetter
    private boolean parseJSON(JSONArray jStopsArray, JSONArray jShuttlesArray/*, JSONArray jEtaArray*/){
        ArrayList<Stop> stops = new ArrayList<Stop>();
        ArrayList<String> stopNames = new ArrayList<String>();
        ArrayList<LatLng> seenLatLngs = new ArrayList<LatLng>();

        HashMap<Integer, Stop> stopsMap = new HashMap<Integer, Stop>();



        try {
            for (int i = 0, len = jStopsArray.length(); i < len; i++){
                JSONObject stopJson;

                stopJson = jStopsArray.getJSONObject(i);
                LatLng latLng = new LatLng(stopJson.getDouble("Latitude"), stopJson.getDouble("Longitude"));
                int routeId = stopJson.getInt("RouteID");
                int stopId = stopJson.getInt("RouteStopID");
                String stopName = stopJson.getString("Description");
                int nameLength = stopName.length();
                int stopNum;
                Integer firstDigit;

                if(stopName.charAt(nameLength-1) != ' ') {
                    firstDigit = Character.getNumericValue(stopName.charAt(nameLength - 1));
                }else{
                    firstDigit = Character.getNumericValue(stopName.charAt(nameLength - 2));
                }

                if (firstDigit == 0) {
                    stopNum = 10;
                } else {
                    stopNum = firstDigit;
                }

                if (stopId == 77){  //Special edge case. Bad JSON
                    stopNum = 8;
                }

                if(!seenLatLngs.contains(latLng)){
                    //New stop object must be made.
                    Stop stop = new Stop(latLng, stopName, routeId, stopId, new int[]{-1, -1, -1, -1});

                    addStopToRouteMap((Integer)stopNum, routeId, stop);
                    stopsMap.put(stopId, stop);
                    seenLatLngs.add(latLng);
                    stops.add(stop);
                }else{
                    //Find existing stop object and add (routeId, stopId) to it

                    for(int j = 0; j < stops.size(); j++){
                        Stop existingStop = stops.get(j);
                        if(existingStop.areLatLngEqual(latLng)){
                            existingStop.addServicedRoute(routeId);
                            existingStop.addStopId(stopId);
                            existingStop.addStopName(stopName);
                            stopsMap.put(stopId, existingStop);
                            addStopToRouteMap((Integer)stopNum, routeId, existingStop);
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

        ArrayList<Shuttle> shuttles = sMapState.getShuttles();
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
                Log.d(TAG, "!@ " + shuttle.getName() + " / " + shuttle.getLatLng());
                shuttle.setOnline(true);

                switch (shuttle.getRouteID()) {
                    case NORTH_ROUTE_ID:
                        sMapState.setShuttle(0, shuttle);
                        onlineStates[0] = true;
                        break;
                    case WEST_ROUTE_ID:  //Double route
                        if (shuttles.get(1).getVehicleId() == shuttle.getVehicleId()) {
                            sMapState.setShuttle(1, shuttle);
                            onlineStates[1] = true;
                        } else if (shuttles.get(2).getVehicleId() == shuttle.getVehicleId()) {
                            sMapState.setShuttle(2, shuttle);
                            onlineStates[2] = true;
                        } else if (!shuttles.get(1).isOnline()) {
                            sMapState.setShuttle(1, shuttle);
                            onlineStates[1] = true;
                        } else if (!shuttles.get(2).isOnline()) {
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
            Shuttle shuttle = shuttles.get(i);
            if (!onlineStates[i]){
                shuttle.setOnline(false);
            }
            else shuttle.setOnline(true);
        }

//        //Test Times
//        Random random = new Random();
//        int maxNum = 2;
//
//        for (int i = 0, len = stops.size(); i < len; i++) {
//            Stop stop = stops.get(i);
//            stop.setShuttleETAs(new int[]{random.nextInt(maxNum), random.nextInt(maxNum), random.nextInt(maxNum), random.nextInt(maxNum)});
//        }
        sMapState.setStops(stops);
        sMapState.setNorthMap(northMap);
        sMapState.setEastMap(eastMap);
        sMapState.setWestMap(westMap);
        Log.d(TAG, "!@ sizes... " + northMap.size() + " , " + eastMap.size() + " , " + westMap.size() + "::" + sMapState.getNorthMap().get(3));

        return true;
    }

    private void addStopToRouteMap(Integer stopNum, int routeId, Stop stop){
        if(stopNum == 1){
            Log.d(TAG, "!@ YAH " + stop.getName());
        }

        switch (routeId){
            case NORTH_ROUTE_ID:
                Log.d(TAG, "Stopnum add to north: " + stopNum);
                northMap.put(stopNum, stop);
                break;
            case WEST_ROUTE_ID:
                westMap.put(stopNum, stop);
                break;
            case EAST_ROUTE_ID:
                eastMap.put(stopNum, stop);
                break;
        }

        Log.d(TAG, "!@ sizes... " + northMap.size() + " , " + eastMap.size() + " , " + westMap.size());
    }
    public boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager)sMapState.getCurrentContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return(cm.getActiveNetworkInfo() != null);
    }
}
