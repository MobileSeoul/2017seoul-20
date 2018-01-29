package com.missionofseoul.seoul.model;

import java.io.Serializable;

/**
 * Created by hyunho on 2016-12-08.
 */

public class FindFriendBean implements Serializable {

    String profile;
    String nickName;
    Integer isPrivate;
    Integer find_no;
    String id;
    String continent;
    String nation;
    String title;
    String contents;
    String date;
    Integer views;
    Integer comment_num;


    public FindFriendBean(String profile, String nickName, Integer isPrivate, Integer find_no, String id,
                          String continent, String nation, String title, String contents, String date, Integer views, Integer comment_num) {
        this.profile = profile;
        this.nickName = nickName;
        this.isPrivate = isPrivate;
        this.find_no = find_no;
        this.id = id;
        this.continent = continent;
        this.nation = nation;
        this.title = title;
        this.contents = contents;
        this.date = date;
        this.views = views;
        this.comment_num = comment_num;
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

    public Integer getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Integer isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Integer getFind_no() {
        return find_no;
    }

    public void setFind_no(Integer find_no) {
        this.find_no = find_no;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Integer getComment_num() {
        return comment_num;
    }

    public void setComment_num(Integer comment_num) {
        this.comment_num = comment_num;
    }
}//end of class




