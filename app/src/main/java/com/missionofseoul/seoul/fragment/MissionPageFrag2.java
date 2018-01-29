package com.missionofseoul.seoul.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.missionofseoul.seoul.MainActivity;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.adapter.CultureAdapter;
import com.missionofseoul.seoul.model.CulturePojo;
import com.missionofseoul.seoul.service.APIService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hyunho on 2016-10-25.
 */
//여행캐스트 프레그먼트
public class MissionPageFrag2 extends Fragment implements CultureAdapter.LoadMoreListener {

    public RequestManager mGlideRequestManager;
    SwipeRefreshLayout mSwipeRefreshLayout;

    private String key = "504a45547a79616e363549584c5155";

    //
    RecyclerView recyclerView;
    CultureAdapter cultureAdapter;
    GridLayoutManager mGridLayoutManager;
    List<CulturePojo.Row> list;
    //
    private static final String SEOUL_API_URL = "http://openapi.seoul.go.kr:8088/";
    private Retrofit apiRetrofit = null;
    private Gson apiGson = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGlideRequestManager = Glide.with(this); //API glide수정

    }//************************************************************* end of onCreat(); **********************************************//

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_missionpage2, container, false);

        //


        recyclerView = (RecyclerView) view.findViewById(R.id.recylerView);
        mGridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(mGridLayoutManager);

        cultureAdapter = new CultureAdapter(inflater, mGlideRequestManager, this);
        cultureAdapter.setGridLayoutManager(mGridLayoutManager);
        cultureAdapter.setRecyclerView(recyclerView);
        recyclerView.setAdapter(cultureAdapter);


        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.layout_swipe_frag2);
        //당겼다 새로고침
        mSwipeRefreshLayout.setOnRefreshListener(listener);

        APIService service = getApiClient().create(APIService.class);
        Call<CulturePojo> call = service.loadCultureApi(key, 1, 40);

        call.enqueue(new Callback<CulturePojo>() {
            @Override
            public void onResponse(Call<CulturePojo> call, Response<CulturePojo> response) {
                try {
                    list = response.body().getSearchConcertDetailService().getRow();
                    if (list.size() != 0) {
                        cultureAdapter.addAll(list);
                    }

                } catch (NullPointerException e) {
                    Toast.makeText(getContext(), "서버가 불안정 합니다.\n다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CulturePojo> call, Throwable t) {
            }
        });

        return view;
    }//************************************* end of onCreateView *********************************************//

    @Override
    public void onStart() {
        super.onStart();


    }//end of onStart()

    //당겨서 새로고침 리스너
    SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {

            ((MainActivity) getActivity()).loadFragment();

            // 새로고침 완료
            mSwipeRefreshLayout.setRefreshing(false);

        }
    };//end of 당겨서 새로고침

    //
    @Override
    public void onMyLoadMore(int currentPage) {

        cultureAdapter.setProgressMore(true);

        int begin = (40 * currentPage) + 1;
        int end = 40 * (currentPage + 1);
        APIService service = getApiClient().create(APIService.class);
        Call<CulturePojo> call = service.loadCultureApi(key, begin, end);

        call.enqueue(new Callback<CulturePojo>() {
            @Override
            public void onResponse(Call<CulturePojo> call, Response<CulturePojo> response) {

                try {
                    list.clear();
                    list = response.body().getSearchConcertDetailService().getRow();
                    if (list.size() != 0) {
                        cultureAdapter.setProgressMore(false);
                        cultureAdapter.addItemMore(list);
                        cultureAdapter.setMoreLoading(false);
                    }

                } catch (NullPointerException e) {
                    cultureAdapter.setProgressMore(false);
                    Toast.makeText(getContext(), "서버가 불안정 합니다.\n다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CulturePojo> call, Throwable t) {
                cultureAdapter.setProgressMore(false);

            }
        });
    }//end of onMyLoadMore()

    private Retrofit getApiClient() {

        //timeout 설정
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        if (apiRetrofit == null) {
            apiGson = new GsonBuilder().setLenient().create();

            apiRetrofit = new Retrofit.Builder()
                    .baseUrl(SEOUL_API_URL)
                    .addConverterFactory(GsonConverterFactory.create(apiGson))
                    .client(okHttpClient)
                    .build();
        }

        return apiRetrofit;
    }//end of getClient()


}//end of fragment cclass
