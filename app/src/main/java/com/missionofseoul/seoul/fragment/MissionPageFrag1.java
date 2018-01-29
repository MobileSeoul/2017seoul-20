package com.missionofseoul.seoul.fragment;

import android.app.ProgressDialog;
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
import com.missionofseoul.seoul.adapter.MissionRecyclerAdapter;
import com.missionofseoul.seoul.model.MissionBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by hyunho on 2016-10-25.
 */
//여행의 미션 프레그먼트(미션)
public class MissionPageFrag1 extends Fragment {

    public ArrayList<MissionBean> missions = new ArrayList<>();

    RecyclerView recyclerView;
    MissionRecyclerAdapter recyclerAdapter;

    ProgressDialog dialog;

    View view;


    MissionLoadThread missionLoadThread;

    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;

    SwipeRefreshLayout mSwipeRefreshLayout;

    int isCompleted;

    int count; // 미션테이블 개수

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGlideRequestManager = Glide.with(this);


    }// ************************** end of onCreate() Fragment *********************************

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {

        if (view == null) { //fragment가 처음에만 만들어지게 하기(fragment객체가 있으면 다시만들지 않음 )?   //왜 이렇게 했지??
            view = inflater.inflate(R.layout.frag_missionpage1, container, false);
            recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
            recyclerAdapter = new MissionRecyclerAdapter(inflater, missions, mGlideRequestManager);
            recyclerView.setAdapter(recyclerAdapter);

            mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.layout_swipe_frag);
        }
        //리스트 불러올 때 프로그레스다이알로그 만들기
        dialog = new ProgressDialog(getContext());
        dialog.setMessage("리스트를 불러오는 중입니다.");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        //미션을 가져오는 쓰레드
        missionLoadThread = new MissionLoadThread();
        missionLoadThread.start();

        //당겼다 새로고침
        mSwipeRefreshLayout.setOnRefreshListener(listener);

        return view;

    }// ******************************** end onCreateView() ***************************************

    //당겨서 새로고침 리스너
    SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {

            ((MainActivity) getActivity()).loadFragment();

            // 새로고침 완료
            mSwipeRefreshLayout.setRefreshing(false);

        }
    };//end of 당겨서 새로고침

    //미션을 가져오는 쓰레드
    class MissionLoadThread extends Thread {
        @Override
        public void run() {

            missions.clear();

            //missionTable의 개수
            String countTableUrl = G.domain + "php/count_table.php";
            try {
                URL url = new URL(countTableUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setDoInput(true);

                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader reader = new BufferedReader(isr);

                StringBuffer buffer = new StringBuffer();

                String line = reader.readLine();

                while (true) {
                    buffer.append(line);
                    line = reader.readLine();
                    if (line == null) break;
                    buffer.append("\n");
                }

                count = Integer.parseInt(buffer.toString()); //미션테이블 개수

                if (G.totalMission == 0) { //맨 처음에만 미션DB개수 구하기, 전체미션개수  전역변수에 대입하기
                    G.missionDBNum = count;
                    G.totalMission = count * 5; //총미션개수 미션 수행률에 쓸 전역변수
                }

                //미션숨김 확인하기
                G.HiddenList = G.getStringArrayPref(getContext(), "Hidden");//

                if (G.HiddenList.size() == 0) {//숨긴 미션이 없을 때

                    for (int i = 1; i <= count; i++) {
                        String missionTableUrl = G.domain + "php/mission/m" + i + ".php";
                        missionTable(missionTableUrl, i);
                    }

                } else {//숨긴 미션이 있을 때

                    int[] intArr = new int[G.HiddenList.size()];
                    for (int i = 0; i < G.HiddenList.size(); i++) {
                        intArr[i] = Integer.parseInt(G.HiddenList.get(i));
                    }
                    Loop1:
                    for (int i = 1; i <= count; i++) {
                        //미션숨기기능 추가시 여기서 숨긴 미션 테이블가 일치할시 건너뛰게 하면 될것 같음!!
                        for (int k = 0; k < intArr.length; k++) {
                            if (intArr[k] == i) continue Loop1;
                        }
                        String missionTableUrl = G.domain + "php/mission/m" + i + ".php";
                        missionTable(missionTableUrl, i);
                    }//end of Loop1

                }//end of if ~ else


            } catch (Exception e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        }//end of run()

    }//end of inner Thread class

    //MissionLoadThread 쓰레드에서 호출하는 메서드
    public void missionTable(String severUrl, int i) { //미션리스트 가져오는 메서드
        //int i는 미션의 기준값이 될 번호

        try {
            URL url = new URL(severUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            String id = "id=" + G.mem_id;

            OutputStream os = connection.getOutputStream();
            os.write(id.getBytes());

            os.flush();
            os.close();

            //서버로부터 오는 echo를 읽어오기
            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
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

            for (String row : rows) {
                String mainimg = row.split("&")[0];
                String imgbtn1 = row.split("&")[1];
                String imgbtn2 = row.split("&")[2];
                String imgbtn3 = row.split("&")[3];
                String imgbtn4 = row.split("&")[4];
                String imgbtn5 = row.split("&")[5];
                String subtitle = row.split("&")[6];
                String maintitle = row.split("&")[7];
                String level = row.split("&")[8];
                String level_star = row.split("&")[9]; //2016-12-01 level_star int값으로 변경

                //Mission Completed  스템프를 붙이기 위한 서버에서 check1~5 컬럼 가져오기
                int check1 = Integer.parseInt(row.split("&")[10]);
                int check2 = Integer.parseInt(row.split("&")[11]);
                int check3 = Integer.parseInt(row.split("&")[12]);
                int check4 = Integer.parseInt(row.split("&")[13]);
                int check5 = Integer.parseInt(row.split("&")[14]);

                isCompleted = check1 + check2 + check3 + check4 + check5; //더해서 5가 나오면 미션완료

                String location = row.split("&")[15];

                //17/02/14 newicon 추가
                String newIcon1 = row.split("&")[16];
                String newIcon2 = row.split("&")[17];
                String newIcon3 = row.split("&")[18];
                String newIcon4 = row.split("&")[19];
                String newIcon5 = row.split("&")[20];

                if (G.range == 0) { //기본정렬

                    missions.add(new MissionBean(G.domain + mainimg, R.drawable.stamp_256, G.domain + imgbtn1, G.domain + imgbtn2, G.domain + imgbtn3,
                            G.domain + imgbtn4, G.domain + imgbtn5, subtitle, maintitle, level, level_star, i, isCompleted, location
                            , newIcon1, newIcon2, newIcon3, newIcon4, newIcon5)); //i는 기준값이 될 번호  // newicon추가

                } else if (G.range == 1) { //난이도 낮은 순서정렬

                    // 미션 난이도 별로 미션 정렬 주석처리
                    //추후에 정렬방법을 1.난이도 낮은 순서, 2.높은 순서 별 미션정렬
                    int num = Integer.parseInt(level_star.trim());
                    int j = 0;
                    for (j = 0; j < missions.size(); j++) { //레벨값에 따른 미션 정렬
                        if (num < Integer.parseInt(missions.get(j).getLevel_star())) break;
                        //num값(현재의 level_star값)이 level_star   부등호 '>' 이면, 레벨이 높은 순서 , '<'레벨이 낮은 순서로 정렬
                    }

                    missions.add(j, new MissionBean(G.domain + mainimg, R.drawable.stamp_256, G.domain + imgbtn1, G.domain + imgbtn2, G.domain + imgbtn3,
                            G.domain + imgbtn4, G.domain + imgbtn5, subtitle, maintitle, level, level_star, i, isCompleted, location
                            , newIcon1, newIcon2, newIcon3, newIcon4, newIcon5)); //i는 기준값이 될 번호  // newicon추가

                } else if (G.range == 2) { //난이도 높은 순서 정렬

                    // 미션 난이도 별로 미션 정렬 주석처리
                    //추후에 정렬방법을 1.난이도 낮은 순서, 2.높은 순서 별 미션정렬
                    int num = Integer.parseInt(level_star.trim());
                    int j = 0;
                    for (j = 0; j < missions.size(); j++) { //레벨값에 따른 미션 정렬
                        if (num > Integer.parseInt(missions.get(j).getLevel_star())) break;
                        //num값(현재의 level_star값)이 level_star   부등호 '>' 이면, 레벨이 높은 순서 , '<'레벨이 낮은 순서로 정렬
                    }

                    missions.add(j, new MissionBean(G.domain + mainimg, R.drawable.stamp_256, G.domain + imgbtn1, G.domain + imgbtn2, G.domain + imgbtn3,
                            G.domain + imgbtn4, G.domain + imgbtn5, subtitle, maintitle, level, level_star, i, isCompleted, location
                            , newIcon1, newIcon2, newIcon3, newIcon4, newIcon5)); //i는 기준값이 될 번호  // newicon추가

                } else if (G.range == 3) {

                    missions.add(new MissionBean(G.domain + mainimg, R.drawable.stamp_256, G.domain + imgbtn1, G.domain + imgbtn2, G.domain + imgbtn3,
                            G.domain + imgbtn4, G.domain + imgbtn5, subtitle, maintitle, level, level_star, i, isCompleted, location
                            , newIcon1, newIcon2, newIcon3, newIcon4, newIcon5)); //i는 기준값이 될 번호  // newicon추가

                    if (missions.size() + G.HiddenList.size() == count) // 마지막 한번만 섞어 준다
                        Collections.shuffle(missions);
                }


                isCompleted = 0;

            }//for each 문 끝

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerAdapter.notifyDataSetChanged();

                }
            });//end of runOnUiThread()
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }//end of missionTable()


}//end of class
