package com.missionofseoul.seoul;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.missionofseoul.seoul.adapter.TravelTalkRecyclerAdapter;
import com.missionofseoul.seoul.model.TravelTalkBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AnotherTalkActivity extends AppCompatActivity {
    public RequestManager mGlideRequestManager;

    //Actionbartext
    TextView abs_title;
    //
    String id;
    String nick;

    RecyclerView recyclerView;
    TravelTalkRecyclerAdapter travelTalkRecyclerAdapter;
    ArrayList<TravelTalkBean> travelTalkBeens = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_another_talk);

        mGlideRequestManager = Glide.with(this);

        //UserActivity에서 가져온 값
        Intent intent = getIntent();
        nick = intent.getStringExtra("Nick");
        id = intent.getStringExtra("ID");

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        //액션바 타이틀
        abs_title = (TextView) findViewById(R.id.abs_title);
        abs_title.setText(nick + "님의 여행톡");

        recyclerView = (RecyclerView) findViewById(R.id.recylerview);
        travelTalkRecyclerAdapter = new TravelTalkRecyclerAdapter(travelTalkBeens, getLayoutInflater(), mGlideRequestManager);
        recyclerView.setAdapter(travelTalkRecyclerAdapter);

    }//end of onCreate()

    @Override
    protected void onResume() {
        super.onResume();
        //DB에서 traveltalk 가져오기
        new Thread() {
            @Override
            public void run() {
                String serverUrl = G.domain + "php/useractivity/load_mytalk.php";

                try {
                    URL url = new URL(serverUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    String data = "id=" + id;
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

                    travelTalkBeens.clear();

                    String[] rows = buffer.toString().split(";");
                    String[] row = null;

                    String userId = null;
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

                        userId = row[0];
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

                        travelTalkBeens.add(new TravelTalkBean(userId, profile, nick, isPrivate, talk_no, talk_text, date, like_num, comment_num, img0, img1, img2, img3, img4));

                    }//end of for
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            travelTalkRecyclerAdapter.notifyDataSetChanged(); // 리스트 가져오는 곳으로 이동

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }//end of run
        }.start();

    }//end of onResume)


    //액션바의 뒤로가기 버튼
    public void backBtn(View v) {
        finish();
    }
}
