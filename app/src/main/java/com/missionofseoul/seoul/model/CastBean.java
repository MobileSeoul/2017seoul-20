package com.missionofseoul.seoul.model;


import java.io.Serializable;

/**
 * Created by hyunho on 2016-11-24.
 */

public class CastBean implements Serializable {
    //DB cast 테이블 컬럼명이랑 동일하게 작성

    int cast_no;
    String cast_mainimg;
    String cast_title;
    String cast_date;
    String cast_likes;
    String cast_contents;


    public CastBean(int cast_no, String cast_mainimg, String cast_title, String cast_date, String cast_likes, String cast_contents) {
        this.cast_no = cast_no;
        this.cast_mainimg = cast_mainimg;
        this.cast_title = cast_title;
        this.cast_date = cast_date;
        this.cast_likes = cast_likes;

        this.cast_contents = cast_contents;
    }

    public int getCast_no() {
        return cast_no;
    }

    public void setCast_no(int cast_no) {
        this.cast_no = cast_no;
    }

    public String getCast_mainimg() {
        return cast_mainimg;
    }

    public void setCast_mainimg(String cast_mainimg) {
        this.cast_mainimg = cast_mainimg;
    }

    public String getCast_title() {
        return cast_title;
    }

    public void setCast_title(String cast_title) {
        this.cast_title = cast_title;
    }

    public String getCast_date() {
        return cast_date;
    }

    public void setCast_date(String cast_date) {
        this.cast_date = cast_date;
    }

    public String getCast_likes() {
        return cast_likes;
    }

    public void setCast_likes(String cast_likes) {
        this.cast_likes = cast_likes;
    }

    public String getCast_contents() {
        return cast_contents;
    }

    public void setCast_contents(String cast_contents) {
        this.cast_contents = cast_contents;
    }
}
