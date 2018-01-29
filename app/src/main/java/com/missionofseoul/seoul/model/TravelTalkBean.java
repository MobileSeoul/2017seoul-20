package com.missionofseoul.seoul.model;

import java.io.Serializable;

/**
 * Created by hyunho on 2016-12-13.
 */

public class TravelTalkBean implements Serializable {

    String id;
    String profile;
    String nickName;
    int isPrivate;
    int talk_no;
    String talk_text;
    String date;
    int likes_num;
    int comment_num;
    String img0;
    String img1;
    String img2;
    String img3;
    String img4;

    public TravelTalkBean(String id, String profile, String nickName, int isPrivate, int talk_no, String talk_text, String date, int likes_num, int comment_num,
                          String img0, String img1, String img2, String img3, String img4) {
        this.id = id;
        this.profile = profile;
        this.nickName = nickName;
        this.isPrivate = isPrivate;
        this.talk_no = talk_no;
        this.talk_text = talk_text;
        this.date = date;
        this.likes_num = likes_num;
        this.comment_num = comment_num;
        this.img0 = img0;
        this.img1 = img1;
        this.img2 = img2;
        this.img3 = img3;
        this.img4 = img4;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(int isPrivate) {
        this.isPrivate = isPrivate;
    }

    public int getTalk_no() {
        return talk_no;
    }

    public void setTalk_no(int talk_no) {
        this.talk_no = talk_no;
    }

    public String getTalk_text() {
        return talk_text;
    }

    public void setTalk_text(String talk_text) {
        this.talk_text = talk_text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getLikes_num() {
        return likes_num;
    }

    public void setLikes_num(int likes_num) {
        this.likes_num = likes_num;
    }

    public int getComment_num() {
        return comment_num;
    }

    public void setComment_num(int comment_num) {
        this.comment_num = comment_num;
    }

    public String getImg0() {
        return img0;
    }

    public void setImg0(String img0) {
        this.img0 = img0;
    }

    public String getImg1() {
        return img1;
    }

    public void setImg1(String img1) {
        this.img1 = img1;
    }

    public String getImg2() {
        return img2;
    }

    public void setImg2(String img2) {
        this.img2 = img2;
    }

    public String getImg3() {
        return img3;
    }

    public void setImg3(String img3) {
        this.img3 = img3;
    }

    public String getImg4() {
        return img4;
    }

    public void setImg4(String img4) {
        this.img4 = img4;
    }
}//end of class
