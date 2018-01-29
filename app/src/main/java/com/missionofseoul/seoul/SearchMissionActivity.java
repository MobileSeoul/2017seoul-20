package com.missionofseoul.seoul;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.missionofseoul.seoul.adapter.SearchMissionAdapter;
import com.missionofseoul.seoul.intro.LoginActivity;
import com.missionofseoul.seoul.model.SearchMissionBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SearchMissionActivity extends AppCompatActivity {

    //BottomNavigation icon 변수
    ImageView home_icon;
    ImageView search_icon;
    ImageView user_icon;
    ImageView thebogi_icon;

    //액션바 멤버변수
    EditText search_edit;
    TextView search_text;
    //인기검색어 영역
    LinearLayout layout_linear;


    SearchThread searchThread;
    ProgressDialog dialog;

    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;

    RecyclerView recylerview;
    ArrayList<SearchMissionBean> missions = new ArrayList<>();
    SearchMissionAdapter searchMissionAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);

        }
        setContentView(R.layout.activity_search_mission);

        mGlideRequestManager = Glide.with(this);

        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.search_abs_layout);
        //액션바 변수객체생성
        search_edit = (EditText) findViewById(R.id.search_edit);
        search_text = (TextView) findViewById(R.id.search_text);

        search_edit.setHint("미션 지명을 입력해 주세요(ex 경복궁...");

        layout_linear = (LinearLayout) findViewById(R.id.layout_linear); //인기검색어영역
        recylerview = (RecyclerView) findViewById(R.id.recylerview); //미션을 붙일


        //액션바 검색어 에티더리스너
        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //검색어가 없으면 다시 레이아웃을 보여주기
                if (search_edit.length() == 0) {
                    recylerview.setVisibility(View.INVISIBLE);
                    layout_linear.setVisibility(View.VISIBLE);

                    //리스트뷰 감추는 코드 추가
                }
            }
        });

        //BottomNavigation icon 객체생성
        home_icon = (ImageView) findViewById(R.id.home_icon);
        search_icon = (ImageView) findViewById(R.id.search_icon);
        user_icon = (ImageView) findViewById(R.id.user_icon);
        thebogi_icon = (ImageView) findViewById(R.id.thebogi_icon);

        search_icon.setImageResource(R.drawable.search_icon_over);



    }//****************************************** end of onCreate() ***************************

    //검색을 누르면 발동
    public void clickSearch(View v) {

        String mission = search_edit.getText().toString().trim();

        if (mission.equals("")) {
            Toast.makeText(this, "검색어를 입력해 주세요", Toast.LENGTH_SHORT).show();
        } else {//검색어가 있다면
            //인기검색어 레이아웃 감추기
            layout_linear.setVisibility(View.GONE);

            //서버에서 nation으로 검색 후 리스트뷰로 화면에 뿌려준다
            searchThread = new SearchThread();
            searchThread.start();
        }
    }

    //인기검색어 클릭하면 edittext에 넣기
    public void clickText(View v) {
        TextView textView = (TextView) v;
        String text = textView.getText().toString();
        search_edit.setText(text);

    }//end of clickText 인기검색어 텍스트 누르면 발동

    //leftBtn
    public void backBtn(View v) {
        finish();
    }

    //BottomNavigationBar
    public void bottomBtn(View v) {
        switch (v.getId()) {
            case R.id.layout_home: //홈버튼
                //Main화면으로 이동
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                break;
            case R.id.layout_search: //미션검색
                //자기 자신 activity는 아무것도 안함
                break;
            case R.id.layout_user://내 활동 버튼
                //Activity이동 전 로그인 했는지 물어보기
                if (G.isLogin == true) {//로그인했다면
                    //user화면으로 이동
                    Intent intent1 = new Intent(this, UserActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                    startActivity(intent1);
                    overridePendingTransition(0, 0); // << activity전환시 깜빡임을 없애 주었다.

                    finish();
                } else { //비로그인 상태라면,
                    new AlertDialog.Builder(this).setMessage("로그인이 필요합니다. 로그인 하시겠습니까?").setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //로그인 페이지로 이동
                            Intent intent = new Intent(SearchMissionActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                            startActivity(intent);
                            overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
                            finish();
                        }
                    }).setNegativeButton("다음에", null).create().show();

                }//end if ~ else //로그인여부 묻고 userActivity로 이동

                break;
            case R.id.layou_thebogi: //더 보기(설정)버튼
                //thebogi화면으로 이동
                Intent intent2 = new Intent(this, ThebogiActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                startActivity(intent2);
                overridePendingTransition(0, 0);

                finish();
                break;
        }
    }//end of bttomBtn


    class SearchThread extends Thread {
        @Override
        public void run() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    dialog = new ProgressDialog(SearchMissionActivity.this);
                    dialog.setMessage("리스트를 불러오고 있습니다.");
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
            });

            String edittext = search_edit.getText().toString().trim();
            String serverUrl = G.domain + "php/mission/search_mission.php";

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);

                String data = "mission=" + edittext + "&dbmum=" + G.missionDBNum;
                OutputStream os = connection.getOutputStream();
                os.write(data.getBytes());

                os.flush();
                os.close();

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
                missions.clear();

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
                    String level_star = row.split("&")[9];
                    int num = Integer.parseInt(row.split("&")[10]);

                    missions.add(new SearchMissionBean(mainimg, imgbtn1, imgbtn2, imgbtn3, imgbtn4, imgbtn5, subtitle, maintitle, level, level_star, num));

                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recylerview.setVisibility(View.VISIBLE);
                        searchMissionAdapter = new SearchMissionAdapter(getLayoutInflater(), missions, mGlideRequestManager);
                        recylerview.setAdapter(searchMissionAdapter);
                        searchMissionAdapter.notifyDataSetChanged();
                        //soft키보드 감추는 코드
                        InputMethodManager immhide = (InputMethodManager) getSystemService(CastDetailActivity.INPUT_METHOD_SERVICE);
                        immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    }
                });

                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            dialog.dismiss();

        }//end of run()
    }//end inner Thread class 검색 쓰레드

}
