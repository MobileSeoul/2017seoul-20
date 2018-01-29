package com.missionofseoul.seoul;

import android.app.ProgressDialog;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class SearchTalkActivity extends AppCompatActivity {
    //액션바 멤버변수
    EditText search_edit;
    TextView search_text;

    RecyclerView recyclerView;
    TravelTalkRecyclerAdapter travelTalkRecyclerAdapter;

    ArrayList<TravelTalkBean> travelTalkBeens = new ArrayList<>();

    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;

    SearchThread searchThread;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_search_talk);

        mGlideRequestManager = Glide.with(this);

        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.search_abs_layout);

        //액션바 변수객체생성
        search_edit = (EditText) findViewById(R.id.search_edit);
        search_text = (TextView) findViewById(R.id.search_text);

        search_edit.setHint("검색하실 여행톡 내용을 입력해주세요");

        //
        recyclerView = (RecyclerView) findViewById(R.id.recylerview);
        travelTalkRecyclerAdapter = new TravelTalkRecyclerAdapter(travelTalkBeens, getLayoutInflater(), mGlideRequestManager);
        recyclerView.setAdapter(travelTalkRecyclerAdapter);



    }//end of onCreate()

    //검색을 누르면 발동
    public void clickSearch(View v) {

        String mission = search_edit.getText().toString().trim();

        if (mission.equals("")) {
            Toast.makeText(this, "검색어를 입력해 주세요", Toast.LENGTH_SHORT).show();
        } else {
            //서버에서 으로 검색 후 리스트뷰로 화면에 뿌다
            searchThread = new SearchThread();
            searchThread.start();
        }

    }// end of 검색버튼

    //leftBtn
    public void backBtn(View v) {
        finish();
    }


    class SearchThread extends Thread {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog = new ProgressDialog(SearchTalkActivity.this);
                    dialog.setMessage("리스트를 불러오고 있습니다.");
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
            });

            String edittext = search_edit.getText().toString().trim();
            String serverUrl = G.domain + "php/traveltalk/search_talk.php";

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);

                String data = "text=" + edittext;
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

                if (buffer.toString().equals("null")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SearchTalkActivity.this, "해당 검색어에 맞는 여행톡이 없습니다", Toast.LENGTH_SHORT).show();
                            //soft키보드 감추는 코드
                            InputMethodManager immhide = (InputMethodManager) getSystemService(CastDetailActivity.INPUT_METHOD_SERVICE);
                            immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        }
                    });
                } else {

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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            travelTalkRecyclerAdapter.notifyDataSetChanged();

                            //soft키보드 감추는 코드
                            InputMethodManager immhide = (InputMethodManager) getSystemService(CastDetailActivity.INPUT_METHOD_SERVICE);
                            immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        }
                    });

                }//end of if

            } catch (Exception e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        }//end of run
    }//end of inner Thread class



}//end of class
