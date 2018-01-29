package com.missionofseoul.seoul.model;

import java.io.Serializable;

/**
 * Created by user on 2017-08-01.
 */

public class MSG implements Serializable {

    private Integer status;
    private String message;

    /**
     * No args constructor for use in serialization
     */
    public MSG() {

    }

    /**
     * @param message
     * @param status
     */
    public MSG(Integer status, String message) {
        super();
        this.status = status;
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
