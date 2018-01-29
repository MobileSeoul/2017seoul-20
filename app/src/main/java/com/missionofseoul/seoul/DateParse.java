package com.missionofseoul.seoul;

import java.util.Date;

/**
 * Created by hyunho on 2016-12-04.
 */

public class DateParse {
    
    private static class TIME_MAXIMUM {
        public static final int SEC = 60;
        public static final int MIN = 60;
        public static final int HOUR = 24;
        public static final int DAY = 30;
        public static final int MONTH = 12;
    }

    public static String formatTimeString(Date tempDate) {

        long curTime = System.currentTimeMillis();
        long regTime = tempDate.getTime();
        long diffTime = (curTime - regTime) / 1000;

        String msg = null;
        if (diffTime < TIME_MAXIMUM.SEC) {
            // sec
            msg = "방금 전";
        } else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN) {
            // min
            msg = diffTime + "분 전";
        } else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR) {
            // hour
            msg = (diffTime) + "시간 전";
        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
            // day
            msg = (diffTime) + "일 전";
        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
            // day
            msg = (diffTime) + "달 전";
        } else {
            msg = (diffTime) + "년 전";
        }

        return msg;
    }//end of formatTimeString



    public static String newIconParse(Date iconDate) {
        long curTime = System.currentTimeMillis();
        long regTime = iconDate.getTime();
        long diffTime = (curTime - regTime) / 1000;

        String msg = null;

        diffTime /= TIME_MAXIMUM.SEC;
        diffTime /= TIME_MAXIMUM.MIN;
        diffTime /= TIME_MAXIMUM.HOUR;

        msg = diffTime + "";

        return msg;

    }

}
