package com.missionofseoul.seoul.model;

import java.io.Serializable;

/**
 * Created by hyunho on 2016-12-11.
 */

public class NoticeBean implements Serializable {

    int notice_no;
    String title;
    String date;
    int comment_num;
    String nUrl;
    int label = 0;
    String mainimg;


    public NoticeBean(int notice_no, String title, String date, int comment_num, String nUrl) {
        this.notice_no = notice_no;
        this.title = title;
        this.date = date;
        this.comment_num = comment_num;
        this.nUrl = nUrl;
    }

    //공지인지 이벤트인지 분간하기 위한 생성자
    public NoticeBean(int notice_no, String title, String date, int comment_num, String nUrl, int label) {
        this.notice_no = notice_no;
        this.title = title;
        this.date = date;
        this.comment_num = comment_num;
        this.nUrl = nUrl;
        this.label = label;
    }

    //리싸이클러뷰에서 가져올 때
    public NoticeBean(int notice_no, String title, String date, int comment_num, String nUrl, String mainimg) {
        this.notice_no = notice_no;
        this.title = title;
        this.date = date;
        this.comment_num = comment_num;
        this.nUrl = nUrl;
        this.mainimg = mainimg;
    }


    public int getNotice_no() {
        return notice_no;
    }

    public void setNotice_no(int notice_no) {
        this.notice_no = notice_no;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getComment_num() {
        return comment_num;
    }

    public void setComment_num(int comment_num) {
        this.comment_num = comment_num;
    }

    public String getnUrl() {
        return nUrl;
    }

    public void setnUrl(String nUrl) {
        this.nUrl = nUrl;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public String getMainimg() {
        return mainimg;
    }

    public void setMainimg(String mainimg) {
        this.mainimg = mainimg;
    }
}

