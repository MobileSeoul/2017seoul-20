package com.missionofseoul.seoul.model;

import java.io.Serializable;

/**
 * Created by hyunho on 2017-02-26.
 */

public class MImageBean implements Serializable {

    String img;
    String text;

    public MImageBean(String img, String text) {
        this.img = img;
        this.text = text;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


}




