package com.missionofseoul.seoul.model;

/**
 * Created by hyunho on 2017-03-15.
 */

public class MarkerBean {
    String mainTitle;
    String location;
    String search;
    int num;

    public MarkerBean(String mainTitle, String location, String search, int num) {
        this.mainTitle = mainTitle;
        this.location = location;
        this.search = search;
        this.num = num;
    }

    public String getMainTitle() {
        return mainTitle;
    }

    public void setMainTitle(String mainTitle) {
        this.mainTitle = mainTitle;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

}//end of class
