package com.missionofseoul.seoul.model;

/**
 * Created by hyunho on 2017-03-15.
 */

public class CloseMissionBean {

    float distansBetween;
    double latitude;
    double logitude;
    String locationName;

    public CloseMissionBean(float distansBetween, double latitude, double logitude, String locationName) {
        this.distansBetween = distansBetween;
        this.latitude = latitude;
        this.logitude = logitude;
        this.locationName = locationName;
    }

    public float getDistansBetween() {
        return distansBetween;
    }

    public void setDistansBetween(float distansBetween) {
        this.distansBetween = distansBetween;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLogitude() {
        return logitude;
    }

    public void setLogitude(double logitude) {
        this.logitude = logitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }


}//end of class
