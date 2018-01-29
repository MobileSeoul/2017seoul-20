package com.missionofseoul.seoul.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by user on 2017-05-16.
 */

public class APIClient {

    public static final String BASE_URL = "http://mottestserver.dothome.co.kr";

    private static Retrofit retrofit = null;
    private static Gson gson = null;

    public static Retrofit getClient() {

        //timeout 설정
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        if (retrofit == null) {
            gson = new GsonBuilder().setLenient().create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
        }


        return retrofit;
    }//end of getClient()

}//end of APIClient class
