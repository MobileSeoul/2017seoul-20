package com.missionofseoul.seoul;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.missionofseoul.seoul.adapter.GridAdapter;
import com.missionofseoul.seoul.model.MImageBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class AnotherUserActivity extends AppCompatActivity {
    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;

    //액션바 멤버변수
    TextView abs_title;

    //멤버프로필 영역
    ImageView mProfile;
    TextView mMissionrate; //미션수행률
    TextView mTalk_num;//여행톡 작성 개수 보여주는 곳
    TextView mNick;
    TextView mAgeNsex; //나이랑 성별 보여주는 곳
    TextView mIntroduce; //자기소개

    //서버에서 가져올 값을 받을 변수
    String profileAddr;
    String nick;
    String gender;
    String birth;
    int missionComplete;
    String talkNum;
    String introduce;

    String birthYear = "";
    String userGender;


    //gridview 변수설정
    ExpandableHeightGridView gridView;
    GridAdapter gridAdapter;

    //17/02/25수정 미션이미지 설명 담을 리스트
    ArrayList<MImageBean> mImageBeens = new ArrayList<>();//17/02/25 수정 미션 이미제 텍스트 붙이기

    //DetailFrag에서 intend값 받을 변수
    String id;
    int isPrivate;

    //프로필 클릭하면 전체화면으로 보여줄 레이이아수
    RelativeLayout fullScreen;
    ImageView fullimg;

    //비공개회원 화면 보여주지 않기
    RelativeLayout layout_private;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_another_user);

        mGlideRequestManager = Glide.with(this);

        Intent intent = getIntent();
        id = intent.getStringExtra("ID");
        isPrivate = intent.getIntExtra("IsPrivate", 1);

        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        abs_title = (TextView) findViewById(R.id.abs_title);

        //프로필 영역  객체 생성
        mProfile = (ImageView) findViewById(R.id.profile);
        mMissionrate = (TextView) findViewById(R.id.missionrate);
        mTalk_num = (TextView) findViewById(R.id.talk_num);
        mNick = (TextView) findViewById(R.id.nick);
        mAgeNsex = (TextView) findViewById(R.id.ageNsex);
        mIntroduce = (TextView) findViewById(R.id.introduce);

        //전체 미션 수행 이미지 보여줄
        gridView = (ExpandableHeightGridView) findViewById(R.id.gridview);
        gridView.setExpanded(true);

        //프로필 이미지 클릭시 전체화면으로 보여주 객체
        fullScreen = (RelativeLayout) findViewById(R.id.layout_fullscreen);
        fullimg = (ImageView) findViewById(R.id.fullimg);
        layout_private = (RelativeLayout) findViewById(R.id.layout_private);

        if (isPrivate == 2) {//비공개로 설정한 회원은 화면 보여주지않기
            layout_private.setVisibility(View.VISIBLE);
        }

    }//end of onCreate()

    @Override
    protected void onResume() {
        super.onResume();

        //서버에서 회원정보 가져오기.
        new Thread() {
            @Override
            public void run() {

                String serverUrl = G.domain + "php/useractivity/loadmember.php";

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

                    StringBuffer buffer = new StringBuffer();

                    String line = reader.readLine();

                    while (true) {
                        buffer.append(line);
                        line = reader.readLine();
                        if (line == null) break;
                        buffer.append("\n");
                    }

                    String[] row = buffer.toString().split("&");

                    profileAddr = row[0];
                    nick = row[1];
                    gender = row[2];
                    birth = row[3];
                    missionComplete = Integer.parseInt(row[4]);
                    introduce = row[5];
                    talkNum = row[6];

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            abs_title.setText(nick);

                            mGlideRequestManager.load(G.domain + profileAddr).bitmapTransform(new CropCircleTransformation(AnotherUserActivity.this))
                                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mProfile);//프로필 이미지
                            mNick.setText(nick);//닉네임
                            //미션수행률 계산
                            float rate = ((float) missionComplete / G.totalMission) * 100;
                            //DecimalFormat는 소수점 자리 결정 '#'은 '0'을 표시하지 않는다.
                            DecimalFormat df = new DecimalFormat("#.#");
                            String result = df.format(rate);
                            mMissionrate.setText(result + "%");

                            //나이,성별설정
                            if (!gender.equals("0") || !birth.equals("0")) { //사용자가 성별과 생년월일을 둘 중 하나라도 설정하면

                                if (birth.equals("0")) { //사용자가 생년월일설정 안했을 때
                                    birthYear = "";

                                    if (gender.equals("0")) {
                                        userGender = "";
                                    } else {
                                        switch (gender) {
                                            case "1":
                                                userGender = "Male";
                                                break;
                                            case "2":
                                                userGender = "Female";
                                                break;
                                        }
                                    }
                                    mAgeNsex.setText(userGender);
                                } else { //사용자가 생년월일을 설정했을 때
                                    Calendar c = Calendar.getInstance();
                                    String year = String.valueOf(c.get(Calendar.YEAR));
                                    birthYear = birth.substring(0, 4); //변경 String substring사용하기 4자리 숫자가져오기
                                    int age = Integer.parseInt(year) - Integer.parseInt(birthYear);
                                    birthYear = age + "";

                                    if (gender.equals("0")) {
                                        userGender = "";
                                    } else {
                                        switch (gender) {
                                            case "1":
                                                userGender = "Male";
                                                break;
                                            case "2":
                                                userGender = "Female";
                                                break;
                                        }
                                    }

                                    mAgeNsex.setText(birthYear + " , " + userGender);
                                }

                            } else if (gender.equals("0") && birth.equals("0")) {
                                mAgeNsex.setVisibility(View.GONE);
                            }

                            //여행톡 개수
                            mTalk_num.setText(talkNum);

                            //자기소개
                            if (!introduce.equals("")) {//사용자가 자기소개를 수정 했으면
                                mIntroduce.setText(introduce);
                            } else {
                                //자기소개를 수정 안했으면 아무것도 안함
                            }

                        }
                    });//end of runOnUiThread
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }//end of run
        }.start();//서버에서 회원정보 가져오기 끝

        //사용자가 업로드한 미션 이미지 가져오기
        new Thread() {
            @Override
            public void run() {

                String serverUrl = G.domain + "php/useractivity/load_missionimg.php";

                try {
                    URL url = new URL(serverUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    String data = "id=" + id + "&dbnum=" + G.missionDBNum; //다른 사용자가 수행한 미션사진,G.missionDBNum은 미션 테이블개수
                    OutputStream os = connection.getOutputStream();
                    os.write(data.getBytes());

                    os.flush();
                    os.close();

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


                    String[] rows = buffer.toString().split(";");
                    mImageBeens.clear();

                    for (String row : rows) {
                        String img = row.split("&")[0];
                        String text = row.split("&")[1];
                        mImageBeens.add(new MImageBean(img, text));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gridAdapter = new GridAdapter(mImageBeens, getLayoutInflater(), mGlideRequestManager);
                            gridView.setAdapter(gridAdapter);
                            gridAdapter.notifyDataSetChanged();
                        }
                    });
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }//end of run()
        }.start();

    }//****************************************************************end of on  Resume ***************************************************//


    //내 여행톡 개수 누르면
    public void travelTalk(View v) {

        if (Integer.parseInt(talkNum) == 0) {//작성한 여행톡이 없으면
            Toast.makeText(this, "작성한 여행톡이 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            //여행톡 누르면 자신이 쓴 여행톡 보여주는 엑티비로 이동
            Intent intent = new Intent(AnotherUserActivity.this, AnotherTalkActivity.class);
            intent.putExtra("ID", id);
            intent.putExtra("Nick", nick);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
            startActivity(intent);
            overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
        }

    }//end of travelTalk

    //프로필 이미지 클릭하면 발동
    public void clickProfile(View v) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//statusbar hidden
        fullScreen.setVisibility(View.VISIBLE);
        mGlideRequestManager.load(G.domain + profileAddr).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(fullimg);//프로필 이미지

    }//end of clickProfile

    public void closeFullScreen(View v) {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//statusbar show
        ActionBar actionBar = getSupportActionBar();
        actionBar.show();//액션바 다시 보이게
        fullScreen.setVisibility(View.GONE);
    }

    //액션바의 뒤로가기 버튼
    public void backBtn(View v) {
        finish();
    }

    //디바이스 뒤로가기 버튼
    @Override
    public void onBackPressed() {
        if (fullScreen.getVisibility() == View.VISIBLE) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//statusbar show
            ActionBar actionBar = getSupportActionBar();
            actionBar.show();//액션바 다시 보이게
            fullScreen.setVisibility(View.GONE);
        } else {
            finish();
        }
    }//end of onBackPressed

}//end of class
