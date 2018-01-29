package com.missionofseoul.seoul.model;

import java.io.Serializable;

/**
 * Created by hyunho on 2017-01-08.
 */

public class MissionProfileBean implements Serializable {
    String id;
    String profile;
    int isPrivate;
    String nick;
    String imgAddr;

    public MissionProfileBean(String id, String profile, int isPrivate, String nick, String imgAddr) {
        this.id = id;
        this.profile = profile;
        this.isPrivate = isPrivate;
        this.nick = nick;
        this.imgAddr = imgAddr;
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

    public int getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(int isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getImgAddr() {
        return imgAddr;
    }

    public void setImgAddr(String imgAddr) {
        this.imgAddr = imgAddr;
    }
}
