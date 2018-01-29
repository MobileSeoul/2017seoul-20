package com.missionofseoul.seoul.model;

/**
 * Created by hyunho on 2016-12-04.
 */

public class CastRecommBeen {

    String profileImg;//댓글 단 회원의 프로필
    String nickName; //댓글 단 회원의 닉네임
    String recommArea; //댓글댓글 공간
    String date; //댓글 날짜
    int recomm_no;
    String recomm_mem_id;
    int isPrivate;

    public CastRecommBeen(String profileImg, String nickName, String recommArea, String date, int recomm_no, String recomm_mem_id, int isPrivate) {
        this.profileImg = profileImg;
        this.nickName = nickName;
        this.recommArea = recommArea;
        this.date = date;
        this.recomm_no = recomm_no;
        this.recomm_mem_id = recomm_mem_id;
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

    public String getRecommArea() {
        return recommArea;
    }

    public void setRecommArea(String recommArea) {
        this.recommArea = recommArea;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getRecomm_no() {
        return recomm_no;
    }

    public void setRecomm_no(int recomm_no) {
        this.recomm_no = recomm_no;
    }

    public String getRecomm_mem_id() {
        return recomm_mem_id;
    }

    public void setRecomm_mem_id(String recomm_mem_id) {
        this.recomm_mem_id = recomm_mem_id;
    }

    public int getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(int isPrivate) {
        this.isPrivate = isPrivate;
    }
}
