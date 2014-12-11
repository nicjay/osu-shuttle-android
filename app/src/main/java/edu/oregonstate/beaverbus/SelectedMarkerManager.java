package edu.oregonstate.beaverbus;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

/**
 * Created by sellersk on 11/20/2014.
 */
public class SelectedMarkerManager {
    private static final String TAG = "SelectedMarkerManager";

    private MapsActivity mActivity;
    private static TextView selectedMarkerTextView;
    private static Marker selectedMarker;
    private static MapState sMapState;
    private Boolean showSelectedInfoWindow = false;

    private Polyline polylineNorth;
    private Polyline polylineWest;
    private Polyline polylineEast;

    public SelectedMarkerManager(MapsActivity activity) {
        mActivity = activity;
        sMapState = MapState.get();
        selectedMarkerTextView = ((TextView) activity.findViewById(R.id.selected_stop));
        selectedMarkerTextView.setVisibility(View.INVISIBLE);
    }


    public void onMapClick() {
        setRouteLineWidthToDefault();
        if (selectedMarker != null) {
            if (!sMapState.isStopsVisible() && showSelectedInfoWindow) {
                selectedMarker.setVisible(false);
            }
            setSelectedMarker(null, false);
        }
        animateSelectedStopTitle(null, false, true);

    }

    public void refreshMarker() {
        if (selectedMarker != null && showSelectedInfoWindow) selectedMarker.showInfoWindow();
    }

    public Boolean onMarkerClick(Marker marker) {
        Boolean isShuttle = marker.isFlat();

        setRouteLineWidthToDefault();

        if (selectedMarker != null && !sMapState.isStopsVisible() && showSelectedInfoWindow) {
            selectedMarker.setVisible(false);
        }

        sMapState.animateMap(marker.getPosition());
        if (!isShuttle) {
            marker.setVisible(true);
        }

        if (selectedMarker == null) {
            animateSelectedStopTitle(marker.getTitle(), isShuttle, false);
        } else {
            animateSelectedStopTitle(marker.getTitle(), isShuttle, false);
        }

        //If stop
        if (!isShuttle) {
            setSelectedMarker(marker, true);
            marker.showInfoWindow();
        } else {
            setSelectedMarker(marker, false);
        }

        return true;
    }

    public static Marker getSelectedMarker() {
        return selectedMarker;
    }

    public void setSelectedMarker(Marker newMarker, boolean showInfoWindow) {
        if (selectedMarker != null && !selectedMarker.isFlat())
            selectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_dot_plus));
        if (newMarker != null && showInfoWindow)
            newMarker.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.marker_dot_empty)));

        showSelectedInfoWindow = showInfoWindow;

        selectedMarker = newMarker; //Null is passed in when map is clicked
    }

    public void animateSelectedStopTitle(final String markerTitle, Boolean shuttleBool, Boolean mapClick) {
        if (shuttleBool == null) shuttleBool = false;
        final Boolean isShuttle = shuttleBool;

        final Animation fadeInAnim = AnimationUtils.makeInAnimation(mActivity.getApplicationContext(), true);
        fadeInAnim.setDuration(400);

        if (mapClick) {
            if (selectedMarkerTextView.getVisibility() == View.VISIBLE) {
                Animation fadeOutAnim = AnimationUtils.makeOutAnimation(mActivity, true);
                fadeOutAnim.setDuration(300);
                fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        selectedMarkerTextView.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                selectedMarkerTextView.startAnimation(fadeOutAnim);
            }
        } else {
            if (selectedMarkerTextView.getVisibility() == View.VISIBLE) {
                Animation fadeOutAnim = AnimationUtils.makeOutAnimation(mActivity, true);
                fadeOutAnim.setDuration(300);
                fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (markerTitle != null) {
                            setSelectedStopDrawable(markerTitle, isShuttle);
                            selectedMarkerTextView.setVisibility(View.VISIBLE);
                            selectedMarkerTextView.setText(markerTitle);
                            selectedMarkerTextView.startAnimation(fadeInAnim);
                        } else {
                            selectedMarkerTextView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                selectedMarkerTextView.startAnimation(fadeOutAnim);
            } else {
                selectedMarkerTextView.setVisibility(View.VISIBLE);
                selectedMarkerTextView.setText(markerTitle);
                selectedMarkerTextView.startAnimation(fadeInAnim);
                setSelectedStopDrawable(markerTitle, isShuttle);
            }
        }
    }


    private void setSelectedStopDrawable(String title, Boolean isShuttle) {
        if (isShuttle) {
            if (title.equals("East Bus")) {
                selectedMarkerTextView.setBackgroundResource(R.drawable.selected_stop_name_purple_bg);
                polylineEast.setWidth(20);
            } else if (title.equals("West 1 Bus") || title.equals("West 2 Bus")) {
                selectedMarkerTextView.setBackgroundResource(R.drawable.selected_stop_name_orange_bg);
                polylineWest.setWidth(20);
            } else if (title.equals("North Bus")) {
                selectedMarkerTextView.setBackgroundResource(R.drawable.selected_stop_name_green_bg);
                polylineNorth.setWidth(20);
            }
        } else {
            selectedMarkerTextView.setBackgroundResource(R.drawable.selected_stop_name_bg);
        }
        selectedMarkerTextView.setPadding(24, 12, 24, 12);
    }

    private void setRouteLineWidthToDefault() {
        if (polylineNorth.getWidth() > 10) polylineNorth.setWidth(10);
        if (polylineEast.getWidth() > 10) polylineEast.setWidth(10);
        if (polylineWest.getWidth() > 10) polylineWest.setWidth(10);
    }

    public void setPolylines() {
        polylineNorth = sMapState.getPolyline("North");
        polylineWest = sMapState.getPolyline("West");
        polylineEast = sMapState.getPolyline("East");
    }
}
