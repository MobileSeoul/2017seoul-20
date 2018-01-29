package com.missionofseoul.seoul.model;

/**
 * Created by hyunho on 2017-02-11.
 */

public class RankingBean {

    String id; //아이디
    String nickName; // 닉네임
    String profile; //프로필
    int missionComplete; // 미션수행률
    int isPrivate; //프로필 공개여부

    public RankingBean(String id, String nickName, String profile, int missionComplete, int isPrivate) {
        this.id = id;
        this.nickName = nickName;
        this.profile = profile;
        this.missionComplete = missionComplete;
        this.isPrivate = isPrivate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public int getMissionComplete() {
        return missionComplete;
    }

    public void setMissionComplete(int missionComplete) {
        this.missionComplete = missionComplete;
    }

    public int getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(int isPrivate) {
        this.isPrivate = isPrivate;
    }


}//end of class
