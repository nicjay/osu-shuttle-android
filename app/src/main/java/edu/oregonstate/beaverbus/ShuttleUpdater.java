package edu.oregonstate.beaverbus;

import android.os.AsyncTask;
import android.os.Handler;

import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by jordan_n on 8/21/2014.
 *
 * CLASS - ShuttleUpdater
 * Contains repeatable logic to repeat the inner poll task
*/
public class ShuttleUpdater {
    private static final String TAG = "ShuttleUpdater";

    private static ShuttleUpdater sShuttleUpdater;
    private static MapState sMapState;
    private OnMapStateUpdate listener;
    private pollNewDataTask currentTask;

    private Handler mHandler;
    private boolean isBusy = false;
    private boolean stop = true;
    private Runnable r;
    private static final long ASYNC_DELAY = 5000;
    private String shuttleUrl = "http://portal.campusops.oregonstate.edu/files/shuttle/GetMapVehiclePoints.txt";

    private static final int NORTH_ROUTE_ID = 3;
    private static final int WEST_ROUTE_ID = 2;
    private static final int EAST_ROUTE_ID = 1;


    public interface OnMapStateUpdate{
        void onPostShuttleRequest(boolean success);
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
                if(!isBusy){
                    currentTask = new pollNewDataTask(shuttleUrl);
                    currentTask.execute();
                }
                if(!stop) startHandler();
            }
        };
        mHandler.postDelayed(r, ASYNC_DELAY);

    }

    public void stopShuttleUpdater(){
        stop = true;
        if (currentTask!=null) {
            currentTask.cancel(true);
        }
        mHandler.removeCallbacks(r);
    }

    public void startShuttleUpdater(){

        if (stop == true) {
            stop = false;
            currentTask = new pollNewDataTask(shuttleUrl);
            currentTask.execute();

            startHandler();
        }
    }

    /**
     * CLASS - pollNewDataTask
     * Pulls the shuttle JSON URL a single time.
     * Determines online states of shuttles.
     * Determines next three stops of shuttles.
     */
    private class pollNewDataTask extends AsyncTask<String, Void, Boolean>{

        private static final String TAG = "pollNewDataTask";

        private String url;
        private JSONArray JSONShuttles;

        public pollNewDataTask(String url) {
            super();
            this.url = url;
        }


        @Override
        protected Boolean doInBackground(String... params) {
            JSONGetter getter = new JSONGetter();
            JSONShuttles = getter.getJSONFromUrl(url);
            if(JSONShuttles != null){
                return true;
            }
            return false;
        }


        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            parseJSON();
            if(!stop) listener.onPostShuttleRequest(success);
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
            ArrayList<Shuttle> shuttles = sMapState.getShuttles();
            for (int i = 0; i < 4; i++) {
                Shuttle shuttle = shuttles.get(i);
                //Log.d(TAG, "SET SHUTTLE OFFLINE " + shuttle.isOnline());
                if (!onlineStates[i]){
                   shuttle.setOnline(false);

                }
                else shuttle.setOnline(true);
            }


            //Test Times
           ArrayList<Stop> stops = sMapState.getStops();
           if (stops != null) {
               Random random = new Random();
               int maxNum = 16;
               Marker marker = sMapState.getSelectedStopMarker();
               for (int i = 0, len = stops.size(); i < len; i++) {
                   Stop stop = stops.get(i);
                   int[] oldTimes = null;
                   boolean checkOld = false;

                   if(marker != null) {
                       if (sMapState.getSelectedStopMarker().equals(stop.getMarker())) {
                           oldTimes = stop.getShuttleETAs();
                           checkOld = true;
                       }
                   }

                   int num0 = random.nextInt(maxNum/2);
                   int num1 = random.nextInt((maxNum-num0)/2)+num0;
                   int num2 = random.nextInt(maxNum-num1-2)+num1;
                   int num3 = random.nextInt(maxNum-num2)+num2;

                   stop.setShuttleETAs(new int[]{num0, num1, num2, num3});

                   if(checkOld && stop.getShuttleETAs() != oldTimes){
                       sMapState.getSelectedStopMarker().showInfoWindow();
                   }

               }
           }

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


          /* stops = sMapState.getStops();
           for(int i = 0; i < stops.size(); i++) {
               //Log.d(TAG, "1stopShuttleETAS: " + stops.get(i).getShuttleETAs()[0] + " , " + stops.get(i).getShuttleETAs()[1] + " , " + stops.get(i).getShuttleETAs()[2] + " , " + stops.get(i).getShuttleETAs()[3] + " , ");
           }*/
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
    }
}