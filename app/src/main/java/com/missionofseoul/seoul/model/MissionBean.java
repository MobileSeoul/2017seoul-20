package com.missionofseoul.seoul.model;

/**
 * Created by hyunho on 2016-10-25.
 */

public class MissionBean {

    String mission_img;
    int stamp;
    String btn_galleryimg1;
    String btn_galleryimg2;
    String btn_galleryimg3;
    String btn_galleryimg4;
    String btn_galleryimg5;
    String sub_title;
    String main_title;
    String level_text;
    String level_star;
    int layoutPos; //미션 테이블의 기준 값
    int isCompleted; // check1~5 컬럼값을 더해서 5가되는지 확인 5면(미션성공)
    String location;

    //newicon 붙일 변수 생성 17/02/14
    String newIcon1;
    String newIcon2;
    String newIcon3;
    String newIcon4;
    String newIcon5;

    public MissionBean(String mission_img, int stamp, String btn_galleryimg1, String btn_galleryimg2, String btn_galleryimg3, String btn_galleryimg4, String btn_galleryimg5,
                       String sub_title, String main_title, String level_text, String level_star, int layoutPos, int isCompleted, String location,
                       String newIcon1, String newIcon2, String newIcon3, String newIcon4, String newIcon5) {

        this.mission_img = mission_img;
        this.stamp = stamp;
        this.btn_galleryimg1 = btn_galleryimg1;
        this.btn_galleryimg2 = btn_galleryimg2;
        this.btn_galleryimg3 = btn_galleryimg3;
        this.btn_galleryimg4 = btn_galleryimg4;
        this.btn_galleryimg5 = btn_galleryimg5;
        this.sub_title = sub_title;
        this.main_title = main_title;
        this.level_text = level_text;
        this.level_star = level_star;
        this.layoutPos = layoutPos;
        this.isCompleted = isCompleted;
        this.location = location;
        this.newIcon1 = newIcon1;
        this.newIcon2 = newIcon2;
        this.newIcon3 = newIcon3;
        this.newIcon4 = newIcon4;
        this.newIcon5 = newIcon5;
    }//newicon 추가한 생성자 17/02/14 생성


    public String getMission_img() {
        return mission_img;
    }

    public void setMission_img(String mission_img) {
        this.mission_img = mission_img;
    }

    public int getStamp() {
        return stamp;
    }

    public void setStamp(int stamp) {
        this.stamp = stamp;
    }

    public String getBtn_galleryimg1() {
        return btn_galleryimg1;
    }

    public void setBtn_galleryimg1(String btn_galleryimg1) {
        this.btn_galleryimg1 = btn_galleryimg1;
    }

    public String getBtn_galleryimg2() {
        return btn_galleryimg2;
    }

    public void setBtn_galleryimg2(String btn_galleryimg2) {
        this.btn_galleryimg2 = btn_galleryimg2;
    }

    public String getBtn_galleryimg3() {
        return btn_galleryimg3;
    }

    public void setBtn_galleryimg3(String btn_galleryimg3) {
        this.btn_galleryimg3 = btn_galleryimg3;
    }

    public String getBtn_galleryimg4() {
        return btn_galleryimg4;
    }

    public void setBtn_galleryimg4(String btn_galleryimg4) {
        this.btn_galleryimg4 = btn_galleryimg4;
    }

    public String getBtn_galleryimg5() {
        return btn_galleryimg5;
    }

    public void setBtn_galleryimg5(String btn_galleryimg5) {
        this.btn_galleryimg5 = btn_galleryimg5;
    }

    public String getSub_title() {
        return sub_title;
    }

    public void setSub_title(String sub_title) {
        this.sub_title = sub_title;
    }

    public String getMain_title() {
        return main_title;
    }

    public void setMain_title(String main_title) {
        this.main_title = main_title;
    }

    public String getLevel_text() {
        return level_text;
    }

    public void setLevel_text(String level_text) {
        this.level_text = level_text;
    }

    public String getLevel_star() {
        return level_star;
    }

    public void setLevel_star(String level_star) {
        this.level_star = level_star;
    }

    public int getLayoutPos() {
        return layoutPos;
    }

    public void setLayoutPos(int layoutPos) {
        this.layoutPos = layoutPos;
    }

    public int getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(int isCompleted) {
        this.isCompleted = isCompleted;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNewIcon1() {
        return newIcon1;
    }

    public void setNewIcon1(String newIcon1) {
        this.newIcon1 = newIcon1;
    }

    public String getNewIcon2() {
        return newIcon2;
    }

    public void setNewIcon2(String newIcon2) {
        this.newIcon2 = newIcon2;
    }

    public String getNewIcon3() {
        return newIcon3;
    }

    public void setNewIcon3(String newIcon3) {
        this.newIcon3 = newIcon3;
    }

    public String getNewIcon4() {
        return newIcon4;
    }

    public void setNewIcon4(String newIcon4) {
        this.newIcon4 = newIcon4;
    }

    public String getNewIcon5() {
        return newIcon5;
    }

    public void setNewIcon5(String newIcon5) {
        this.newIcon5 = newIcon5;
    }
}


