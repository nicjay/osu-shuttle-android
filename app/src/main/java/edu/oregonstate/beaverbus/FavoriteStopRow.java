package edu.oregonstate.beaverbus;

import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sellersk on 10/30/2014.
 */
public class FavoriteStopRow {

    private TextView favStopName;

    private TextView[] favStopETAs = new TextView[4];

    public Stop favStopObj;

    public TextView getFavStopName() {
        return favStopName;
    }

    public void setFavStopName(TextView favStopName) {
        this.favStopName = favStopName;
    }

    public TextView[] getFavStopETAs() {
        return favStopETAs;
    }

    public void setFavStopETAs(TextView[] favStopETAs) {
        this.favStopETAs = favStopETAs;
    }
    public void setFavStopETA(TextView favStopETA, int index) {
        favStopETAs[index] = favStopETA;
    }

    public Stop getFavStopObj() {
        return favStopObj;
    }

    public void setFavStopObj(Stop favStopObj) {
        this.favStopObj = favStopObj;
    }
}
