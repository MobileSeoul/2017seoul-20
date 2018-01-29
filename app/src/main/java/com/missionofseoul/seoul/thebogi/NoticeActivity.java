package com.missionofseoul.seoul.thebogi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.NoticeDeatilActivity;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.adapter.NoticeAdapter;
import com.missionofseoul.seoul.model.NoticeBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class NoticeActivity extends AppCompatActivity {
    //Actionbartext
    TextView titletext;

    //notice 공지사항
    ListView listview_notice;
    ArrayList<NoticeBean> noticeBeens = new ArrayList<>();
    NoticeAdapter noticeAdapter;

    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_notice);

        mGlideRequestManager = Glide.with(this);

        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.bottom_abs_layout);

        titletext = (TextView) findViewById(R.id.actionbar_text);
        titletext.setText("전체 공지사항");

        //리스트뷰 객체생성
        listview_notice = (ListView) findViewById(R.id.listview_notice);

        //공지사항 눌렀을때 상세페이지로 이동
        listview_notice.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                        //첫번째 notice를 누르면 sharedprefernce에 G.new_notice를 대입한다.
                        if (position == 0) { //17/02/22 추가
                            SharedPreferences preferences = getSharedPreferences("NewNotice", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("NoticeNum", G.new_Notice);
                            editor.apply();
                        }
                        //상세페이지로 이동
                        Intent intent = new Intent(NoticeActivity.this, NoticeDeatilActivity.class);
                        intent.putExtra("noticeBeens", noticeBeens);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    }
                });

        //공지사항 가져오는 쓰레드
        new Thread() {
            @Override
            public void run() {

                String serverUrl = G.domain + "php/notice/notice.php";

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
                        int label = Integer.parseInt(row.split("&")[5]);

                        noticeBeens.add(new NoticeBean(notice_no, title, date, comment_num, nUrl,label));

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            noticeAdapter = new NoticeAdapter(noticeBeens, getLayoutInflater(), mGlideRequestManager);
                            listview_notice.setAdapter(noticeAdapter);
                            noticeAdapter.notifyDataSetChanged();
                        }
                    });
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }//end of run()
        }.start(); //공지사항 가져오는 쓰레드


    }//************************************* end of onCreate() *******************************************


}
