package com.jordann.maptest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

/**
 * Created by sellersk on 9/11/2014.
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


//    class ShuttleEta
//    {
//        String name;
//        int time;
//    }

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








/*
        for(int i = 0, len = stops.size(); i < len; i++){
            Stop stop = stops.get(i);
            int[] eta = stop.getShuttleETAs();
            if(i < 3){
                if(eta[0] > northMax) northMax = eta[0];
                shuttleNorthDict.put(stop.getName(), stop.getShuttleETA(0));
                if(eta[1] > west1Max) west1Max = eta[1];
                shuttleWest1Dict.put(stop.getName(), stop.getShuttleETA(1));
                if(eta[2] > west2Max) west2Max = eta[2];
                shuttleWest2Dict.put(stop.getName(), stop.getShuttleETA(2));
                if(eta[3] > eastMax) eastMax = eta[3];
                shuttleEastDict.put(stop.getName(), stop.getShuttleETA(3));
            }else{
                if(eta[0] < northMax){
                    shuttleNorthDict.remove(northMax);
                    shuttleNorthDict.put(stop.getName(), stop.getShuttleETA(0));
                    northMax = stop.getShuttleETA(0);
                    Iterator<Integer> iter = shuttleNorthDict.values().iterator();
                    while(iter.hasNext()){
                        if(shuttleNorthDict.get(iter) > northMax) northMax = shuttleNorthDict.get(iter);
                    }
                }else if(eta[1] < west1Max){
                    shuttleNorthDict.remove(northMax);
                    shuttleNorthDict.put(stop.getName(), stop.getShuttleETA(0));
                    northMax = stop.getShuttleETA(0);
                    Iterator<Integer> iter = shuttleNorthDict.values().iterator();
                    while(iter.hasNext()){
                        if(shuttleNorthDict.get(iter) > northMax) northMax = shuttleNorthDict.get(iter);
                    }
                }else if(eta[2] < west2Max){
                    shuttleNorthDict.remove(northMax);
                    shuttleNorthDict.put(stop.getName(), stop.getShuttleETA(0));
                    northMax = stop.getShuttleETA(0);
                    Iterator<Integer> iter = shuttleNorthDict.values().iterator();
                    while(iter.hasNext()){
                        if(shuttleNorthDict.get(iter) > northMax) northMax = shuttleNorthDict.get(iter);
                    }
                }else if(eta[4] < northMax){
                    shuttleNorthDict.remove(northMax);
                    shuttleNorthDict.put(stop.getName(), stop.getShuttleETA(0));
                    northMax = stop.getShuttleETA(0);
                    Iterator<Integer> iter = shuttleNorthDict.values().iterator();
                    while(iter.hasNext()){
                        if(shuttleNorthDict.get(iter) > northMax) northMax = shuttleNorthDict.get(iter);
                    }
                }
            }


        }
*/
        /*
        ArrayList<String[]> sortedStopsWithTimes0 = new ArrayList<String[]>();
        ArrayList<String[]> sortedStopsWithTimes1 = new ArrayList<String[]>();
        ArrayList<String[]> sortedStopsWithTimes2 = new ArrayList<String[]>();
        ArrayList<String[]> sortedStopsWithTimes3 = new ArrayList<String[]>();

            for (int i = 0, len = stops.size(); i < len; i++) {
                Stop stop = stops.get(i);
                stop.setShuttleETAs(new int[]{random.nextInt(maxNum), random.nextInt(maxNum), random.nextInt(maxNum), random.nextInt(maxNum)});


                String[] stopWithTime = new String[2];

                stopWithTime[0] = stop.getName();
                stopWithTime[1] = String.valueOf(stop.getShuttleETA(0));
                sortedStopsWithTimes0.add(stopWithTime);

                stopWithTime = new String[2];
                stopWithTime[0] = stop.getName();
                stopWithTime[1] = String.valueOf(stop.getShuttleETA(1));
                sortedStopsWithTimes1.add(stopWithTime);

                stopWithTime = new String[2];
                stopWithTime[0] = stop.getName();
                stopWithTime[1] = String.valueOf(stop.getShuttleETA(2));
                sortedStopsWithTimes2.add(stopWithTime);

                stopWithTime = new String[2];
                stopWithTime[0] = stop.getName();
                stopWithTime[1] = String.valueOf(stop.getShuttleETA(3));
                sortedStopsWithTimes3.add(stopWithTime);
            }

        Collections.sort(sortedStopsWithTimes0,new Comparator<String[]>() {
            @Override
            public int compare(String[] lhs, String[] rhs) {
                int a = Integer.parseInt(lhs[1]);
                int b = Integer.parseInt(rhs[1]);

                if (a < b) return -1;
                if (a > b) return 1;
                return 0;
            }
        });

        for (int i = 0; i < sortedStopsWithTimes0.size();i++) {
            Log.d(TAG, "sortedStops0 name: " + sortedStopsWithTimes0.get(i)[0] + "; ETA: "+sortedStopsWithTimes0.get(i)[1]);
        }

        */


          /* stops = sMapState.getStops();
           for(int i = 0; i < stops.size(); i++) {
               //Log.d(TAG, "1stopShuttleETAS: " + stops.get(i).getShuttleETAs()[0] + " , " + stops.get(i).getShuttleETAs()[1] + " , " + stops.get(i).getShuttleETAs()[2] + " , " + stops.get(i).getShuttleETAs()[3] + " , ");
           }*/

}
