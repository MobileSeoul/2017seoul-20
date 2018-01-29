package com.missionofseoul.seoul.service;


import com.missionofseoul.seoul.model.CulturePojo;
import com.missionofseoul.seoul.model.FindFriendBean;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by user on 2017-05-16.
 */

public interface APIService {

    @GET("php/findfriend/loadFindfriend.php")
    Call<List<FindFriendBean>> loadFindFriend(@Query("Continent") String continent);

    @GET("/{apiKey}/json/SearchConcertDetailService/{begin}/{end}/")
    Call<CulturePojo> loadCultureApi(@Path("apiKey") String apiKey, @Path("begin") int begin, @Path("end") int end);
}//end of APIService class
