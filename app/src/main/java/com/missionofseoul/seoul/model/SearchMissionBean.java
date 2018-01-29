package com.missionofseoul.seoul.model;

/**
 * Created by hyunho on 2017-01-19.
 */

public class SearchMissionBean {

    String mission_img;
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

    public SearchMissionBean(String mission_img, String btn_galleryimg1, String btn_galleryimg2, String btn_galleryimg3, String btn_galleryimg4, String btn_galleryimg5,
                             String sub_title, String main_title, String level_text, String level_star, int layoutPos) {
        this.mission_img = mission_img;
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
    }

    public String getMission_img() {
        return mission_img;
    }

    public void setMission_img(String mission_img) {
        this.mission_img = mission_img;
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
}
