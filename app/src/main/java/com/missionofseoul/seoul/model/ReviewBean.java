package com.missionofseoul.seoul.model;

import java.io.Serializable;

/**
 * Created by hyunho on 2016-11-16.
 */

public class ReviewBean implements Serializable {
    //Serializable 데이터를 직렬화를 시켜야 fragment에서 intent로 값을 넘길 때, array를 담아서 넘길 수 있다.

    String profileImg;
    String nickName;
    String textArea;
    String date;
    String id;
    int isPrivate;

    public ReviewBean(String profileImg, String nickName, String textArea, String date, String id, int isPrivate) {
        this.profileImg = profileImg;
        this.nickName = nickName;
        this.textArea = textArea;
        this.date = date;
        this.id = id;
        this.isPrivate = isPrivate;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getTextArea() {
        return textArea;
    }

    public void setTextArea(String textArea) {
        this.textArea = textArea;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(int isPrivate) {
        this.isPrivate = isPrivate;
    }
}
