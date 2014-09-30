package edu.oregonstate.beaverbus;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
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
        listener.onPostInitialRequest(success);
    }


    //TODO: reduce number of string conversions here and JSONGetter
    private boolean parseJSON(JSONArray jStopsArray, JSONArray jShuttlesArray/*, JSONArray jEtaArray*/){
        ArrayList<Stop> stops = new ArrayList<Stop>();
        ArrayList<String> stopNames = new ArrayList<String>();

        for (int i = 0, len = jStopsArray.length(); i < len; i++){
            JSONObject route;
            try {
                route = jStopsArray.getJSONObject(i);
                JSONArray landmarks = route.getJSONArray("Stops");

                for (int j = 0; j < landmarks.length(); j++){
                    JSONObject landmark = landmarks.getJSONObject(j);
                    Log.d(TAG, "landmark is: "+landmark);
                    Log.d(TAG, "line1 is: "+landmark.getString("Line1"));
                    if (!stopNames.contains(landmark.getString("Line1"))){
                        stopNames.add(landmark.getString("Line1"));
                        Stop stop = new Stop(landmark.getDouble("Latitude"), landmark.getDouble("Longitude"), landmark.getString("Line1"), new int[]{-1,-1,-1,-1});
                        stops.add(stop);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

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
            Stop stop = stops.get(i);
            stop.setShuttleETAs(new int[]{random.nextInt(maxNum), random.nextInt(maxNum), random.nextInt(maxNum), random.nextInt(maxNum)});
            Log.d(TAG, stop.getShuttleETA(0) + " , " +stop.getShuttleETA(1) + " , " +stop.getShuttleETA(2) + " , " +stop.getShuttleETA(3) + "\n");
        }
        sMapState.setStops(stops);

        LinkedList<Shuttle.ShuttleEta> northLinkedList = new LinkedList<Shuttle.ShuttleEta>();
        LinkedList<Shuttle.ShuttleEta> west1LinkedList = new LinkedList<Shuttle.ShuttleEta>();
        LinkedList<Shuttle.ShuttleEta> west2LinkedList = new LinkedList<Shuttle.ShuttleEta>();
        LinkedList<Shuttle.ShuttleEta> eastLinkedList = new LinkedList<Shuttle.ShuttleEta>();

        //Determine the closest three stops for each shuttle
        for(int i = 0, len = stops.size(); i < len; i++) {
            Stop stop = stops.get(i);
            int[] ETAs = stop.getShuttleETAs();
            Shuttle.ShuttleEta newNorthEta = new Shuttle.ShuttleEta();
            newNorthEta.name = stop.getName();
            newNorthEta.time = ETAs[0];
            Shuttle.ShuttleEta newWest1Eta = new Shuttle.ShuttleEta();
            newWest1Eta.name = stop.getName();
            newWest1Eta.time = ETAs[1];
            Shuttle.ShuttleEta newWest2Eta = new Shuttle.ShuttleEta();
            newWest2Eta.name = stop.getName();
            newWest2Eta.time = ETAs[2];
            Shuttle.ShuttleEta newEastEta = new Shuttle.ShuttleEta();
            newEastEta.name = stop.getName();
            newEastEta.time = ETAs[3];

            northLinkedList = addEtaIfNeeded(northLinkedList, i , newNorthEta);
            west1LinkedList = addEtaIfNeeded(west1LinkedList, i , newWest1Eta);
            west2LinkedList = addEtaIfNeeded(west2LinkedList, i , newWest2Eta);
            eastLinkedList = addEtaIfNeeded(eastLinkedList, i , newEastEta);
        }
        shuttles.get(0).setUpcomingStops(northLinkedList);
        shuttles.get(1).setUpcomingStops(west1LinkedList);
        shuttles.get(2).setUpcomingStops(west2LinkedList);
        shuttles.get(3).setUpcomingStops(eastLinkedList);
//        logList(northLinkedList, "north");
//        logList(west1LinkedList, "west1");
//        logList(west2LinkedList, "west2");
//        logList(eastLinkedList, "east");

        return true;
    }

    private void logList(LinkedList<Shuttle.ShuttleEta> list, String name){
        Log.d(TAG, name + " : " + list.get(0).time + " , " + list.get(1).time + " , " + list.get(2).time + "\n");
    }

    private LinkedList<Shuttle.ShuttleEta> addEtaIfNeeded(LinkedList<Shuttle.ShuttleEta> list, int index, Shuttle.ShuttleEta newEta){
        if(newEta.time == -1) return list;
        switch (index) {
            case 0:
                list.add(newEta);
                break;
            case 1:
                if (newEta.time < list.getFirst().time)
                    list.addFirst(newEta);
                else list.add(newEta);
                break;
            case 2:
                if (newEta.time < list.getFirst().time)
                    list.addFirst(newEta);
                else if (newEta.time < list.get(1).time)
                    list.add(1, newEta);
                else list.add(newEta);
                break;
            default:
                if (newEta.time < list.getFirst().time) {
                    list.addFirst(newEta);
                    list.removeLast();
                } else if (newEta.time < list.get(1).time) {
                    list.add(1, newEta);
                    list.removeLast();
                } else if (newEta.time < list.get(2).time) {
                    list.add(2, newEta);
                    list.removeLast();
                }
        }
        return list;
    }

    public boolean isNetworkAvailable(){
        ConnectivityManager cm = (ConnectivityManager)sMapState.getCurrentContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return(cm.getActiveNetworkInfo() != null);
    }


}
