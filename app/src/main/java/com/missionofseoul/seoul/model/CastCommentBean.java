package com.missionofseoul.seoul.model;

import java.io.Serializable;

/**
 * Created by hyunho on 2016-12-03.
 */

public class CastCommentBean implements Serializable {

    String profileImg;//댓글 단 회원의 프로필
    String nickName; //댓글 단 회원의 닉네임
    String commentArea; //댓글쓰는 공간
    String date; //댓글 날짜
    int recommentNum; //댓글의 댓글 개수
    int commentNo;//추가 리스트뷰의 기준이 될 c_comm_no도 가져오기
    String comm_mem_id;
    int isPrivate; //개인 프로필 화면 공개여부 1은 공개 2는 비공개

    public CastCommentBean(String profileImg, String nickName, String commentArea, String date, int recommentNum, int commentNo, String comm_mem_id, int isPrivate) {
        this.profileImg = profileImg;
        this.nickName = nickName;
        this.commentArea = commentArea;
        this.date = date;
        this.recommentNum = recommentNum;
        this.commentNo = commentNo;
        this.comm_mem_id = comm_mem_id;
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

    public String getCommentArea() {
        return commentArea;
    }

    public void setCommentArea(String commentArea) {
        this.commentArea = commentArea;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getRecommentNum() {
        return recommentNum;
    }

    public void setRecommentNum(int recommentNum) {
        this.recommentNum = recommentNum;
    }

    public int getCommentNo() {
        return commentNo;
    }

    public void setCommentNo(int commentNo) {
        this.commentNo = commentNo;
    }

    public String getComm_mem_id() {
        return comm_mem_id;
    }

    public void setComm_mem_id(String comm_mem_id) {
        this.comm_mem_id = comm_mem_id;
    }

    public int getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(int isPrivate) {
        this.isPrivate = isPrivate;
    }
}
