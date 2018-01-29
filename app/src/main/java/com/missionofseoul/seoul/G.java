package com.missionofseoul.seoul;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by hyunho on 2016-12-22.
 */

public class G {
    //Global 변수를 만들기 위한 클래스

    //도메인 주소
    public static String domain = "http://mottestserver.dothome.co.kr/";
    public static boolean isLogin = false; //로그인을 했는 지 묻는 변수
    public static String mem_id = "empty";
    public static boolean isConfirm = false; //이메일 컴펌을 받았는지 확인하는 변수
    public static int totalMission = 0; //전체미션개수
    public static int missionDBNum = 0; //미션개수
    public static int range = 0; //미션정렬 방법 1 - 기본정렬, 2 - 난이도 낮은정렬 , 3 - 난이도 높은정렬
    public static int new_Notice = 0; //새로운 공지가 생기면 new icon 붙이기 위한 글로벌 변수 // 17/02/22 추가

    //미션숨김
    public static ArrayList<String> missionPosList = new ArrayList<>(); // 미션숨길레이아웃번호 저장할 배열
    public static ArrayList<String> HiddenList = new ArrayList<>(); //히든미션 저장할 ArrayList

    //난수로 랜덤코드 만들어 내는 메서드
    public static String randCode() {
        Random rnd = new Random();
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < 16; i++) {
            if (rnd.nextBoolean()) {
                buf.append((char) ((int) (rnd.nextInt(26)) + 97));
            } else {
                buf.append((rnd.nextInt(10)));
            }
        }


        return buf.toString();
    }

    //미션 숨김 json으로 저장할 메서드
    public static void setStringArrayPref(Context context, String key, ArrayList<String> values) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        JSONArray a = new JSONArray();

        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.apply();

    }//setStringArrayPref


    public static ArrayList<String> getStringArrayPref(Context context, String key) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);

        ArrayList<String> urls = new ArrayList<String>();

        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;


    }//end of getStringArrayPref


}//end of Class G

