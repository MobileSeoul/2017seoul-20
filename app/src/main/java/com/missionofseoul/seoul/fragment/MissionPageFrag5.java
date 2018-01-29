package com.missionofseoul.seoul.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.MainActivity;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.adapter.EventRecyclerAdapter;
import com.missionofseoul.seoul.model.NoticeBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by hyunho on 2017-02-22.
 */

public class MissionPageFrag5 extends Fragment {
    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;
    SwipeRefreshLayout mSwipeRefreshLayout;


    RecyclerView recyclerView;
    EventRecyclerAdapter eventRecyclerAdapter;
    ArrayList<NoticeBean> noticeBeens = new ArrayList<>();


    @Override

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGlideRequestManager = Glide.with(this);


    }//********************************* end of onCreate *********************************

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_missionpage5, container, false);

        //당겨서 새로고침
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.layout_swipe_frag);
        mSwipeRefreshLayout.setOnRefreshListener(listener);

        //
        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        eventRecyclerAdapter = new EventRecyclerAdapter(noticeBeens, inflater, mGlideRequestManager);
        recyclerView.setAdapter(eventRecyclerAdapter);

        //공지사항 가져오는 쓰레드
        new Thread() {
            @Override
            public void run() {

                String serverUrl = G.domain + "php/notice/event.php";

                try {
                    URL url = new URL(serverUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);


                    //서버로부터 오는 echo를 읽어오기
                    InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader reader = new BufferedReader(isr);

                    StringBuffer buffer = new StringBuffer();

                    String line = reader.readLine();

                    while (true) {
                        buffer.append(line);
                        line = reader.readLine();
                        if (line == null) break;
                        buffer.append("\n");
                    }

                    //읽어온 데이터 문자열에서 db의 row별로 배열로 구분해서 저장
                    String[] rows = buffer.toString().split(";");

                    //기존의 데이터랑 겹칠 수 있으므로 모든 데이터를 삭제시킨다.
                    noticeBeens.clear();

                    for (String row : rows) {
                        int notice_no = Integer.parseInt(row.split("&")[0]);
                        String title = row.split("&")[1];
                        String date = row.split("&")[2];
                        int comment_num = Integer.parseInt(row.split("&")[3]);
                        String nUrl = row.split("&")[4];
                        String mainimg = row.split("&")[5];

                        noticeBeens.add(new NoticeBean(notice_no, title, date, comment_num, nUrl, mainimg));

                    }

                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }//end of run()
        }.start(); //공지사항 가져오는 쓰레드

        return view;
    }//*************************************** end of onCreateView ************************************* //


    //당겨서 새로고침 리스너
    SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {

            ((MainActivity) getActivity()).loadFragment();

            // 새로고침 완료
            mSwipeRefreshLayout.setRefreshing(false);

        }
    };//end of 당겨서 새로고침


}

