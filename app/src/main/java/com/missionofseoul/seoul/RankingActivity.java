package com.missionofseoul.seoul;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.missionofseoul.seoul.adapter.RankingAdapter;
import com.missionofseoul.seoul.model.RankingBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RankingActivity extends AppCompatActivity {

    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;

    //액션바 멤버변수
    TextView abs_title;

    ListView listView;
    RankingAdapter rankingAdapter;
    ArrayList<RankingBean> rankingBeens = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_ranking);

        mGlideRequestManager = Glide.with(this);

        //ActionBar 커스텀하기      //emulator api 16에서 custom error
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        //액션바 타이틀
        abs_title = (TextView) findViewById(R.id.abs_title);
        abs_title.setText("랭킹순위");

        listView = (ListView) findViewById(R.id.listview);
        rankingAdapter = new RankingAdapter(rankingBeens, getLayoutInflater(), mGlideRequestManager);
        listView.setAdapter(rankingAdapter);

        //랭킹을 클릭하면
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                String id = rankingBeens.get(position).getId();
                int isPrivate = rankingBeens.get(position).getIsPrivate();
                //아이디 값을 가지고 프로필 클릭한 유저의 화면을 이동
                if (G.mem_id.equals(id)) {//내 아이디와 클릭한 아이디가 같은면 토스트
                    Toast.makeText(RankingActivity.this, "본인 프로필은 내 활동에서\n확인하실 수 있습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(RankingActivity.this, AnotherUserActivity.class);
                    intent.putExtra("ID", id);
                    intent.putExtra("IsPrivate", isPrivate);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                    startActivity(intent);
                }
            }
        });

        new Thread() {
            @Override
            public void run() {
                String serverUrl = G.domain + "php/useractivity/ranking.php";

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

                    //기존의 데이터랑 겹칠 수 있으므로 모든 데이터를 삭제시킨다.
                    rankingBeens.clear();
                    String[] row = null;

                    for (int i = 0; i < rows.length; i++) {

                        row = rows[i].split("&");

                        String id = row[0];
                        String nick = row[1];
                        String profile = row[2];
                        int missionComplete = Integer.parseInt(row[3]);
                        int isPrivate = Integer.parseInt(row[4]);

                        rankingBeens.add(new RankingBean(id, nick, profile, missionComplete, isPrivate));
                    }


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rankingAdapter.notifyDataSetChanged();
                        }
                    });

                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }//end of run
        }.start(); // 랭킹가져오는 쓰레드 끝

    }//end of onCreate()

    //액션바의 뒤로가기 버튼
    public void backBtn(View v) {
        finish();
    }

}//end of class
