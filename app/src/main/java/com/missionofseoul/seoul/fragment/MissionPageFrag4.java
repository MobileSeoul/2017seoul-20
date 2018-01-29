package com.missionofseoul.seoul.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.github.clans.fab.FloatingActionButton;
import com.missionofseoul.seoul.EndlessRecyclerOnScrollListener;
import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.MainActivity;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.SearchTalkActivity;
import com.missionofseoul.seoul.TalkWriteActivity;
import com.missionofseoul.seoul.adapter.TravelTalkRecyclerAdapter;
import com.missionofseoul.seoul.intro.LoginActivity;
import com.missionofseoul.seoul.model.TravelTalkBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by hyunho on 2016-12-13.
 */

public class MissionPageFrag4 extends Fragment {

    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;
    SwipeRefreshLayout mSwipeRefreshLayout;

    FloatingActionButton fabSearch;
    FloatingActionButton fabWrite;

    RecyclerView recyclerView;
    TravelTalkRecyclerAdapter travelTalkRecyclerAdapter;

    ArrayList<TravelTalkBean> travelTalkBeens = new ArrayList<>();

    //recyclerview 무한 스크롤 offset
    LinearLayoutManager linearLayoutManager;
    int page;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGlideRequestManager = Glide.with(this);

    }//*******************************************end of onCreate()*******************************


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_missionpage4, container, false);
        //당겨서 새로고침
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.layout_swipe_frag);
        mSwipeRefreshLayout.setOnRefreshListener(listener);

        //floation액션버튼 객체생성
        fabSearch = (FloatingActionButton) view.findViewById(R.id.fab_search);
        fabWrite = (FloatingActionButton) view.findViewById(R.id.fab_write);
        //floationbutton 클릭 리스너
        fabSearch.setOnClickListener(searchListener); //검색버튼 추후 넣기
        fabWrite.setOnClickListener(writeListener);


        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        travelTalkRecyclerAdapter = new TravelTalkRecyclerAdapter(travelTalkBeens, inflater, mGlideRequestManager);
        recyclerView.setAdapter(travelTalkRecyclerAdapter);

        //
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        travelTalkBeens.clear(); //17/02/22추가

        //DB에서 traveltalk 가져오기
        new Thread() {
            @Override
            public void run() {
                String serverUrl = G.domain + "php/traveltalk/loadtalk.php";

                try {
                    URL url = new URL(serverUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    String data = "current_page=" + page;
                    OutputStream os = connection.getOutputStream();
                    os.write(data.getBytes());

                    os.flush();
                    os.close();

                    InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");
                    BufferedReader reader = new BufferedReader(isr);

                    final StringBuffer buffer = new StringBuffer();

                    String line = reader.readLine();

                    while (true) {
                        buffer.append(line);
                        line = reader.readLine();
                        if (line == null) break;
                        buffer.append("\n");
                    }

                    travelTalkBeens.clear(); //기존에 있던 데이터들은 삭제시킨다.

                    //읽어온 데이터 문자열에서 db의 row별로 배열로 구분해서 저장
                    String[] rows = buffer.toString().split(";");
                    String[] row = null;

                    String id = null;
                    String profile = null;
                    String nick = null;
                    int isPrivate = 0;
                    int talk_no = 0;
                    String talk_text = null;
                    String date = null;
                    int like_num = 0;
                    int comment_num = 0;

                    String img0 = null;
                    String img1 = null;
                    String img2 = null;
                    String img3 = null;
                    String img4 = null;

                    for (int i = 0; i < rows.length; i++) {

                        row = rows[i].split("&");

                        id = row[0];
                        profile = row[1];
                        nick = row[2];
                        isPrivate = Integer.parseInt(row[3]);
                        talk_no = Integer.parseInt(row[4]);
                        talk_text = row[5];
                        date = row[6];
                        like_num = Integer.parseInt(row[7]);
                        comment_num = Integer.parseInt(row[8]);
                        img0 = row[9];
                        img1 = row[10];
                        img2 = row[11];
                        img3 = row[12];
                        img4 = row[13];

                        travelTalkBeens.add(new TravelTalkBean(id, profile, nick, isPrivate, talk_no,
                                talk_text, date, like_num, comment_num, img0, img1, img2, img3, img4));

                    }//end of for
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            travelTalkRecyclerAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }//end of run
        }.start();

        /////////////////////////////////////////////////////////////////////
        recyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                // do something...
                //서버에서 데이타 가져오기
                page = current_page;

                new Thread() {
                    @Override
                    public void run() {
                        String serverUrl = G.domain + "php/traveltalk/loadtalk_offset.php";

                        try {
                            URL url = new URL(serverUrl);

                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setUseCaches(false);
                            connection.setDoInput(true);
                            connection.setDoOutput(true);

                            String data = "current_page=" + page;
                            OutputStream os = connection.getOutputStream();
                            os.write(data.getBytes());

                            os.flush();
                            os.close();

                            InputStream is = connection.getInputStream();
                            InputStreamReader isr = new InputStreamReader(is, "utf-8");
                            BufferedReader reader = new BufferedReader(isr);

                            final StringBuffer buffer = new StringBuffer();

                            String line = reader.readLine();

                            while (true) {
                                buffer.append(line);
                                line = reader.readLine();
                                if (line == null) break;
                                buffer.append("\n");
                            }

                            //읽어온 데이터 문자열에서 db의 row별로 배열로 구분해서 저장
                            String[] rows = buffer.toString().split(";");
                            String[] row = null;

                            String id = null;
                            String profile = null;
                            String nick = null;
                            int isPrivate = 0;
                            int talk_no = 0;
                            String talk_text = null;
                            String date = null;
                            int like_num = 0;
                            int comment_num = 0;

                            String img0 = null;
                            String img1 = null;
                            String img2 = null;
                            String img3 = null;
                            String img4 = null;

                            for (int i = 0; i < rows.length; i++) {

                                row = rows[i].split("&");

                                id = row[0];
                                profile = row[1];
                                nick = row[2];
                                isPrivate = Integer.parseInt(row[3]);
                                talk_no = Integer.parseInt(row[4]);
                                talk_text = row[5];
                                date = row[6];
                                like_num = Integer.parseInt(row[7]);
                                comment_num = Integer.parseInt(row[8]);
                                img0 = row[9];
                                img1 = row[10];
                                img2 = row[11];
                                img3 = row[12];
                                img4 = row[13];

                                travelTalkBeens.add(new TravelTalkBean(id, profile, nick, isPrivate, talk_no, talk_text, date, like_num, comment_num, img0, img1, img2, img3, img4));

                            }//end of for
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    travelTalkRecyclerAdapter.notifyDataSetChanged();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }//end of run
                }.start();

            }
        });

        return view;
    }//********************************************end of onCreateView()************************************


    //당겨서 새로고침 리스너
    SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {

            ((MainActivity) getActivity()).loadFragment();

            // 새로고침 완료
            mSwipeRefreshLayout.setRefreshing(false);

        }
    };//end of 당겨서 새로고침

    //검색버튼 누르면 발동 리스너
    FloatingActionButton.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getContext(), SearchTalkActivity.class);
            getActivity().startActivity(intent);
        }
    };

    //쓰기 버튼 누르면 발동 리스너
    FloatingActionButton.OnClickListener writeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (G.isLogin == false) {//비회원
                new AlertDialog.Builder(getActivity()).setMessage("로그인이 필요합니다. 로그인 하시겠습니까?").setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //로그인 페이지로 이동
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
                        getActivity().finish();
                    }
                }).setNegativeButton("다음에", null).create().show();
            } else { //로그인 회원 글쓰기 activity로 이동
                Intent intent = new Intent(getActivity(), TalkWriteActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
            }

        }
    };//end of floatinbutton 글쓰기 버튼 누르면 발동되는 리스너 끙

}//end of fragment
