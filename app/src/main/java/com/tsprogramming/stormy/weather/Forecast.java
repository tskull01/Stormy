package com.tsprogramming.stormy.weather;

/**
 * Created by Tskulley on 3/6/2018.
 */

public class Forecast {
    private Current mCurrent;
    private Hour[] mHourlyForecast;
    private Day[] mDayForecast;

    public Current getmCurrent() {
        return mCurrent;
    }

    public void setmCurrent(Current mCurrent) {
        this.mCurrent = mCurrent;
    }

    public Hour[] getmHourlyForecast() {
        return mHourlyForecast;
    }

    public void setmHourlyForecast(Hour[] mHourlyForecast) {
        this.mHourlyForecast = mHourlyForecast;
    }

    public Day[] getmDayForecast() {
        return mDayForecast;
    }

    public void setmDayForecast(Day[] mDayForecast) {
        this.mDayForecast = mDayForecast;
    }
}
