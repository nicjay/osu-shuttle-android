package edu.oregonstate.beaverbus;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jordan_n on 8/22/2014.
 *
 * ClASS - MapState
 * Maintains most persistent data, such as shuttles, stops, and drawer items, to be used between classes and the lifecycle of the app.
 */
public class MapState {
    private static final String TAG = "MapState";

    private static Context sCurrentContext;
    private SelectedMarkerManager mSelectedMarkerManager;
    private static MapState sMapState;
    private static GoogleMap mMap;
    private static final int CAMERA_ANIMATION_SPEED = 700;
    private boolean firstTime;

    //Stops and Shuttles
    private static ArrayList<Shuttle> mShuttles;
    private static ArrayList<Stop> mStops;
    private HashMap<Integer, Stop> mStopsMap; //Allows fast lookup of stopObjects for Estimate parsing
    public ArrayList<Stop> northStops = new ArrayList<Stop>();
    public ArrayList<Stop> westStops = new ArrayList<Stop>();
    public ArrayList<Stop> eastStops = new ArrayList<Stop>();
    private boolean mStopsVisible;
    public boolean noAnimate = false;

    //Stop indices of mStops for Stops of each route
    private static ArrayList<Integer> mNorthStopIndex;
    private static ArrayList<Integer> mWestStopIndex;
    private static ArrayList<Integer> mEastStopIndex;

    //Navigation Drawer items
    private static ArrayList<DrawerItem> mDrawerItems;

    private Polyline northPolyline;
    private Polyline westPolyline;
    private Polyline eastPolyline;

    private String stopLocationsUrl = "";
    private String shuttleLocationsUrl = "";
    private String shuttleEstimatesUrl = "";

    public SelectedMarkerManager getSelectedMarkerManager() {
        return mSelectedMarkerManager;
    }

    public void setSelectedMarkerManager(SelectedMarkerManager selectedMarkerManager) {
        mSelectedMarkerManager = selectedMarkerManager;
    }

    private MapState() {
        mDrawerItems = new ArrayList<DrawerItem>();
        mStopsVisible = true;
        firstTime = true;
    }

    public static MapState get() {
        if (sMapState == null) {
            sMapState = new MapState();
        }
        return sMapState;
    }

    public static void setCurrentContext(Context currentContext) {
        sCurrentContext = currentContext;
    }

    public static Context getCurrentContext() {
        return sCurrentContext;
    }

    public HashMap<Integer, Stop> getStopsMap() {
        return mStopsMap;
    }

    public void setStopsMap(HashMap<Integer, Stop> stopsMap) {
        mStopsMap = stopsMap;
    }

    //Creates shuttle objects and their markers, initializing them to (0,0) on the map.
    public void initShuttles() {
        mShuttles = new ArrayList<Shuttle>();
        LatLng initLatLng = new LatLng(0, 0);

        float alphaValue = 1f;

        Shuttle newShuttle = new Shuttle("North", false);
        newShuttle.setMarker(mMap.addMarker(new MarkerOptions().alpha(alphaValue).position(initLatLng).title("North Bus").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_green_marker)).flat(true).anchor(0.5f, 0.5f).infoWindowAnchor(.5f, .5f)));
        mShuttles.add(newShuttle);

        newShuttle = new Shuttle("West 1", false);
        newShuttle.setMarker(mMap.addMarker(new MarkerOptions().alpha(alphaValue).position(initLatLng).title("West 1 Bus").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_orange_marker_dark)).flat(true).anchor(0.5f, 0.5f).infoWindowAnchor(.5f, .5f)));
        mShuttles.add(newShuttle);

        newShuttle = new Shuttle("West 2", false);
        newShuttle.setMarker(mMap.addMarker(new MarkerOptions().alpha(alphaValue).position(initLatLng).title("West 2 Bus").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_orange_marker_light)).flat(true).anchor(0.5f, 0.5f).infoWindowAnchor(.5f, .5f)));
        mShuttles.add(newShuttle);

        newShuttle = new Shuttle("East", false);
        newShuttle.setMarker(mMap.addMarker(new MarkerOptions().alpha(alphaValue).position(initLatLng).title("East Bus").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_purple_marker)).flat(true).anchor(.5f, .5f).infoWindowAnchor(.5f, .5f)));
        mShuttles.add(newShuttle);

        if (mDrawerItems != null && mDrawerItems.size() != 0) {
            //mDrawerItems[1] through mDrawerItems[4] are shuttles. Update new markers
            for (int i = 1; i < 5; i++) {
                mDrawerItems.get(i).setShuttle(mShuttles.get(i - 1));
            }
        }

    }

    public void setShuttle(int index, Shuttle shuttle) {
        mShuttles.get(index).updateAll(shuttle);
    }

    //Called on marker selection. Moves camera, accounting for infoWindow height and display orientation.
    public void animateMap(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition(latLng, mMap.getCameraPosition().zoom, mMap.getCameraPosition().tilt, mMap.getCameraPosition().bearing);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), CAMERA_ANIMATION_SPEED, null);
    }


    public void readConfigurationFile(boolean readUrls) {
        XmlResourceParser xpp = sCurrentContext.getResources().getXml(R.xml.config);

        try {
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                } else if (eventType == XmlPullParser.START_TAG) {
                    if(!readUrls) {
                        if (xpp.getName().equals("NorthRoute")) {
                            parseRoute(xpp, "North");
                        } else if (xpp.getName().equals("WestRoute")) {
                            parseRoute(xpp, "West");
                        } else if (xpp.getName().equals("EastRoute")) {
                            parseRoute(xpp, "East");
                        } else if (xpp.getName().equals("StopNames")) {
                            parseStopNames(xpp);
                        }
                    } else if (xpp.getName().equals("Urls")){
                        parseUrls(xpp);
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    // Continue
                } else if (eventType == XmlPullParser.TEXT) {
                    // Continue
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseUrls(XmlResourceParser xpp) throws  IOException, XmlPullParserException {
        while(xpp.nextTag() == XmlPullParser.START_TAG){
            if(xpp.getName().equals("StopLocations")){
                setStopLocationsUrl(xpp.nextText());
            } else if(xpp.getName().equals("ShuttleLocations")){
                setShuttleLocationsUrl(xpp.nextText());
            } else if(xpp.getName().equals("ShuttleEstimates")){
                setShuttleEstimatesUrl(xpp.nextText());
            }
        }
    }

    private void parseStopNames(XmlResourceParser xpp) throws IOException, XmlPullParserException {

        boolean set = false;

        while (xpp.nextTag() == XmlPullParser.START_TAG) {
            set = false;
            if (xpp.getName().equals("LatLng")) {
                String coords[] = xpp.nextText().split(",");
                for (Stop stop : mStops) {   //Find stopObj that matches LatLng
                    if (stop.getLatLng().latitude == Double.valueOf(coords[0]) && stop.getLatLng().longitude == Double.valueOf(coords[1])) {
                        set = true;
                        xpp.nextTag();
                        stop.setName(xpp.nextText());
                        break;
                    }
                }
                if (!set) {
                    xpp.nextTag();
                    xpp.nextText();
                }
            }
        }
        for (Stop stop : mStops){
            if(stop.getName().toCharArray()[1] == '-'){
                Log.d(TAG, "Stop: " + stop.getName() + " = " + stop.getLatLng());
            }
        }
    }

    private void parseRoute(XmlResourceParser xpp, String routeName) throws IOException, XmlPullParserException {
        PolylineOptions polylineOptions = new PolylineOptions();

        while (xpp.nextTag() == XmlPullParser.START_TAG) {
            if (xpp.getName().equals("LatLng")) {
                String coords[] = xpp.nextText().split(",");
                polylineOptions.add(new LatLng(Double.valueOf(coords[0]), Double.valueOf(coords[1])));
            } else if (xpp.getName().equals("Color")) {
                Polyline polyline = mMap.addPolyline(polylineOptions);
                String color = xpp.nextText();
                polyline.setColor(Color.parseColor(color));
                setPolyline(polyline, routeName);
            } else if (xpp.getName().equals("Width")) {
                getPolyline(routeName).setWidth(Float.valueOf(xpp.nextText()));
            }
        }
    }


    public GoogleMap getMap() {
        return mMap;
    }

    public void setMap(GoogleMap map) {
        mMap = map;
    }

    public ArrayList<Shuttle> getShuttles() {
        return mShuttles;
    }

    public void setShuttles(ArrayList<Shuttle> shuttles) {
        mShuttles = shuttles;
    }

    public ArrayList<Stop> getStops() {
        return mStops;
    }

    public void setStops(ArrayList<Stop> stops) {
        mStops = stops;
    }

    public void setStopsMarkers() {
        for (Stop stop : mStops) {
            stop.setMarker(mMap.addMarker(new MarkerOptions().position(stop.getLatLng()).infoWindowAnchor(.5f, .25f).title(stop.getName()).visible(mStopsVisible).alpha(1f).anchor(.5f, .5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_dot_plus))));
        }
    }

    //Uses shuttleETAs to determine which stops belong to each route.
    public void initStopsArrays() {
        //TODO 12/11/2014: Unsure if run when need - case shuttles offline on initial launch, index arrays will be inited to empty
        if (mNorthStopIndex == null || mWestStopIndex == null || mEastStopIndex == null) {
            mNorthStopIndex = new ArrayList<Integer>();
            mWestStopIndex = new ArrayList<Integer>();
            mEastStopIndex = new ArrayList<Integer>();

            for (Stop stop : mStops) {
                int[] shuttleETAs = stop.getShuttleETAs();
                int index = mStops.indexOf(stop);
                for (int i = 0; i < 4; i++) {
                    if (shuttleETAs[i] != -1) {
                        switch (i) {
                            case 0:
                                mNorthStopIndex.add(index);
                                break;
                            case 1:
                                mWestStopIndex.add(index);
                                break;
                            case 3:
                                mEastStopIndex.add(index);
                                break;
                            default:

                        }
                    }
                }
            }
        }
    }

    public ArrayList<DrawerItem> getDrawerItems() {
        return mDrawerItems;
    }

    public void setDrawerItems(ArrayList<DrawerItem> drawerItems) {
        mDrawerItems = drawerItems;
    }

    public boolean initDrawerItems() {
        if (mDrawerItems.size() == 0 || mDrawerItems == null) {
            mDrawerItems.add(new DrawerItem(0, "BUSES"));
            mDrawerItems.add(new DrawerItem(1, "North", mShuttles.get(0), true));
            mDrawerItems.add(new DrawerItem(1, "West 1", mShuttles.get(1), true));
            mDrawerItems.add(new DrawerItem(1, "West 2", mShuttles.get(2), true));
            mDrawerItems.add(new DrawerItem(1, "East", mShuttles.get(3), true));
            mDrawerItems.add(new DrawerItem(0, "STOPS"));
            mDrawerItems.add(new DrawerItem(2, "North Route", mNorthStopIndex));
            mDrawerItems.add(new DrawerItem(2, "West Route", mWestStopIndex));
            mDrawerItems.add(new DrawerItem(2, "East Route", mEastStopIndex));
            return true;
        }
        return false;
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }

    public void setDrawerShuttleStatus(Shuttle shuttle, boolean newStatus) {
    }

    public boolean isStopsVisible() {
        return mStopsVisible;
    }

    public void setStopsVisible(boolean stopsVisible) {
        mStopsVisible = stopsVisible;
    }

    public void refreshSelectedStopMarker() {
        mSelectedMarkerManager.refreshMarker();
    }

    public ArrayList<Stop> getNorthStops() {
        return northStops;
    }

    public void setNorthStops(ArrayList<Stop> northStops) {
        this.northStops = northStops;
    }

    public ArrayList<Stop> getWestStops() {
        return westStops;
    }

    public void setWestStops(ArrayList<Stop> westStops) {
        this.westStops = westStops;
    }

    public ArrayList<Stop> getEastStops() {
        return eastStops;
    }

    public void setEastStops(ArrayList<Stop> eastStops) {
        this.eastStops = eastStops;
    }

    public Polyline getPolyline(String route) {
        if (route.equals("North"))
            return northPolyline;
        else if (route.equals("West"))
            return westPolyline;
        else if (route.equals("East"))
            return eastPolyline;
        return null;
    }

    public void setPolyline(Polyline polyline, String route) {
        if (route.equals("North")) {
            northPolyline = polyline;
        } else if (route.equals("West")) {
            westPolyline = polyline;
        } else if (route.equals("East")) {
            eastPolyline = polyline;
        }
    }


    public void invalidateStopETAs() {
        for (Stop stop : mStops) {
            stop.invalidateShuttleETAs();
        }
    }

    public String getStopLocationsUrl() {
        return stopLocationsUrl;
    }

    public void setStopLocationsUrl(String stopLocationsUrl) {
        this.stopLocationsUrl = stopLocationsUrl;
    }

    public String getShuttleLocationsUrl() {
        return shuttleLocationsUrl;
    }

    public void setShuttleLocationsUrl(String shuttleLocationsUrl) {
        this.shuttleLocationsUrl = shuttleLocationsUrl;
    }

    public String getShuttleEstimatesUrl() {
        return shuttleEstimatesUrl;
    }

    public void setShuttleEstimatesUrl(String shuttleEstimatesUrl) {
        this.shuttleEstimatesUrl = shuttleEstimatesUrl;
    }
}
