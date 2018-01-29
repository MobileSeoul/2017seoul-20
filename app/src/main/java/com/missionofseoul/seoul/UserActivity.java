package com.missionofseoul.seoul;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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

public class UserActivity extends AppCompatActivity {

    //BottomNavigation icon 변수
    ImageView home_icon;
    ImageView search_icon;
    ImageView user_icon;
    ImageView thebogi_icon;

    //Actionbartext
    TextView titletext;

    //email 인증 레이아웃
    RelativeLayout layout_email;
    TextView email_address;

    //멤버프로필 영역
    ImageView mProfile;
    TextView mMissionrate; //미션수행률
    TextView mTalk_num;//여행톡 작성 개수 보여주는 곳
    TextView mNick;
    TextView mAgeNsex; //나이랑 성별 보여주는 곳
    TextView mIntroduce; //자기소개
    TextView mPoint;//포인트 17/02/22추가

    String code;//16자리 난수를 받을 코드

    AlertDialog confirmDialog;
    RelativeLayout layout; // 다이얼로그 붙일 레이아웃

    //다이얼로그 레이아웃 변수
    EditText confirm_edit;
    Button positive;
    Button negative;

    UploadConfirm confirmThread;//이메일 확인 후 서버업로드

    ProgressDialog dialog;

    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;


    //서버에서 가져올 값을 받을 변수
    String profileAddr;
    String nick;
    String gender;
    String birth;
    int missionComplete;
    String talkNum;
    String introduce;
    int point; //17/02/22추가 포인트

    //
    String birthYear = "";
    String userGender;

    //gridview 변수설정
    ExpandableHeightGridView gridView;
    GridAdapter gridAdapter;

    //17/02/25수정 미션이미지 설명 담을 리스트
    ArrayList<MImageBean> mImageBeens = new ArrayList<>();//17/02/25 수정 미션 이미제 텍스트 붙이기

    //프로필 클릭하면 전체화면으로 보여줄 레이이아수
    RelativeLayout fullScreen;
    ImageView fullimg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_user);

        mGlideRequestManager = Glide.with(this);

        //BottomNavigation icon 객체생성
        home_icon = (ImageView) findViewById(R.id.home_icon);
        search_icon = (ImageView) findViewById(R.id.search_icon);
        user_icon = (ImageView) findViewById(R.id.user_icon);
        thebogi_icon = (ImageView) findViewById(R.id.thebogi_icon);

        user_icon.setImageResource(R.drawable.user_icon_over);




        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.bottom_abs_layout);

        titletext = (TextView) findViewById(R.id.actionbar_text);
        titletext.setText("내 활동"); //Actionbar title를 더보기로

        //이메일 레이아웃 객체 생성
        layout_email = (RelativeLayout) findViewById(R.id.layout_email);//이메일인증코드보내는 레이아웃 영역
        email_address = (TextView) findViewById(R.id.email_address);//사용자 이메일 주소 넣을 곳
        email_address.setText(G.mem_id);//사용자 이메일 주소 보여주기


        //프로필 영역  객체 생성
        mProfile = (ImageView) findViewById(R.id.profile);
        mMissionrate = (TextView) findViewById(R.id.missionrate);
        mTalk_num = (TextView) findViewById(R.id.talk_num);
        mNick = (TextView) findViewById(R.id.nick);
        mAgeNsex = (TextView) findViewById(R.id.ageNsex);
        mIntroduce = (TextView) findViewById(R.id.introduce);
        mPoint = (TextView) findViewById(R.id.point); //포인트 17/02/22 추가

        if (G.isConfirm == true) {//이메일 인증을 받았으면 인증 레이아웃 없애기
            layout_email.setVisibility(View.GONE);
        }


        gridView = (ExpandableHeightGridView) findViewById(R.id.gridview);
        gridView.setExpanded(true);
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

                    String data = "id=" + G.mem_id + "&dbnum=" + G.missionDBNum; //G.missionDBNum은 미션 테이블개수
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

        //프로필 이미지 클릭시 전체화면으로 보여주 객체
        fullScreen = (RelativeLayout) findViewById(R.id.layout_fullscreen);
        fullimg = (ImageView) findViewById(R.id.fullimg);

    }//****************************************************** end of onCreate() **************************************//

    //프로필을 하고 뒤로 돌아왔을 때 바로 변경시켜주기 위해서 onResume에서 실행
    @Override
    protected void onResume() {
        super.onResume();

        //서버에서 회원정보 가져오기.
        new Thread() {
            @Override
            public void run() {
                //프로필사진,닉네임,자기소개,미션완료(missioncomplete) 개수,

                String serverUrl = G.domain + "php/useractivity/loadmember.php";

                try {
                    URL url = new URL(serverUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    String data = "id=" + G.mem_id;
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

                    //읽어온 데이터 문자열에서 db의 row별로 배열로 구분해서 저장
                    String[] row = buffer.toString().split("&");

                    profileAddr = row[0];
                    nick = row[1];
                    gender = row[2];
                    birth = row[3];
                    missionComplete = Integer.parseInt(row[4]);
                    introduce = row[5];
                    talkNum = row[6];
                    point = Integer.parseInt(row[7]); //17/02/22추가

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mGlideRequestManager.load(G.domain + profileAddr).bitmapTransform(new CropCircleTransformation(UserActivity.this))
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
                            }//end of 자기소개

                            //포인트 17/02/22수정
                            DecimalFormat commas = new DecimalFormat("#,###");
                            String resultPoint = (String) commas.format(point);
                            mPoint.setText(resultPoint+"P");

                        }//end of run
                    });//end of runOnUiThread
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }//end of run
        }.start();//서버에서 회원정보 가져오기 끝

    }//***************************************************** end of onResume ****************************************

    //이이메일증레이아웃의 닫기 버튼
    public void closeBtn(View v) {
        //닫기버튼을 누르면 이메일인증 레이아웃 사라짐.
        layout_email.setVisibility(View.GONE);
    }

    //이메일 인증 보내기 버튼 클릭
    public void confirmEmail(View v) {
        code = G.randCode();//16자리램덤코드 생성

        //이메일인증번호 입력할 다이얼로그
        LayoutInflater inflater = getLayoutInflater();
        layout = (RelativeLayout) inflater.inflate(R.layout.confirm_dialog, null);//null은 아직 아무 곳에도 안붙인다.
        confirm_edit = (EditText) layout.findViewById(R.id.confirm_edit);//인증메일이 보내지면 나오는 다이얼로그의 에디트
        positive = (Button) layout.findViewById(R.id.positive);//확인버튼
        negative = (Button) layout.findViewById(R.id.negative);//취소버튼

        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
        builder.setTitle("이메일 주소 확인");
        builder.setView(layout);
        confirmDialog = builder.create();
        confirmDialog.setCancelable(false); //back버튼으로도 취소가 불가능!!
        confirmDialog.setCanceledOnTouchOutside(false);
        confirmDialog.show();

        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (confirm_edit.getText().toString().trim().equals(code)) {

                    dialog = new ProgressDialog(UserActivity.this);
                    dialog.setMessage("인증번호 확인 중입니다.");
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();

                    //서버에 아이디값으로 인증확인 업데이트하기
                    confirmThread = new UploadConfirm();
                    confirmThread.start();

                } else {
                    Toast.makeText(UserActivity.this, "인증번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    confirm_edit.setText("");
                }

            }
        });

        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDialog.dismiss();
            }
        });

        //16자리인증코드 회원 이메일로 보내기
        new Thread() {
            @Override
            public void run() {

                String serverUrl = G.domain + "php/mail/confirm_email.php";

                try {
                    URL url = new URL(serverUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    String data = "id=" + G.mem_id + "&code=" + code; //+ "&nick=" + G.mem_id;
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UserActivity.this, "인증코드가 이메일로 발송 되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }//end of run()
        }.start();
    }//end of email //16자리인증코드 회원 이메일로 보내기 끝

    //프로필 편집 누르면
    public void editProfileBtn(View v) {

        Intent intent = new Intent(UserActivity.this, EditProfileActivity.class);
        intent.putExtra("Profile", profileAddr);
        intent.putExtra("Nick", nick);
        intent.putExtra("Gender", gender);
        intent.putExtra("Birth", birth);
        intent.putExtra("Introduce", introduce);
        startActivity(intent);

    }//프로필 편집 버튼 끝

    //프로필 이미지 클릭하면 발동
    public void clickProfile(View v) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//statusbar hidden
        fullScreen.setVisibility(View.VISIBLE);
        mGlideRequestManager.load(G.domain + profileAddr).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(fullimg);//프로필 이미지

    }//end of clickProfile\

    public void closeFullScreen(View v) {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//statusbar show
        ActionBar actionBar = getSupportActionBar();
        actionBar.show();//액션바 다시 보이게
        fullScreen.setVisibility(View.GONE);
    }

    //미션수행률을 누르면 발동
    public void clickRank(View v) {
        Intent intent = new Intent(UserActivity.this, RankingActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
    }//end of clickRank()

    //내 여행톡 개수 누르면
    public void travelTalk(View v) {

        if (Integer.parseInt(talkNum) == 0) {//작성한 여행톡이 없으면
            Toast.makeText(this, "작성하신 여행톡이 없습니다.\n 여행톡을 작성해 주세요 :)", Toast.LENGTH_SHORT).show();
        } else {
            //여행톡 누르면 자신이 쓴 여행톡 보여주는 엑티비로 이동
            Intent intent = new Intent(UserActivity.this, MyTravelTalkActivity.class);
            intent.putExtra("Nick", nick);
            startActivity(intent);
            overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
        }

    }//end of travelTalk

    //bottom navigation
    public void bottomBtn(View v) {
        switch (v.getId()) {
            case R.id.layout_home:
                //Main화면으로 이동
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                break;
            case R.id.layout_search://내주변
                Intent intent1 = new Intent(this, MapsActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                startActivity(intent1);
                overridePendingTransition(0, 0);//<< activity전환시 깜빡임을 없애 주었다
                finish();
                break;
            case R.id.layout_user:
                //자기 자신 activity는 아무것도 안함
                break;
            case R.id.layou_thebogi:
                //thebogi화면으로 이동
                Intent intent2 = new Intent(this, ThebogiActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                startActivity(intent2);
                overridePendingTransition(0, 0);

                finish();
                break;
        }
    }//end of bttomBtn

    //컴펌받은 이메일 서버에 업로드시키는 쓰레드
    class UploadConfirm extends Thread {
        @Override
        public void run() {

            String serverUrl = G.domain + "php/mail/upload_confirm.php";

            try {
                URL url = new URL(serverUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                String data = "id=" + G.mem_id; //+ "&nick=" + G.mem_id;
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
                final String result = buffer.toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.equals("success")) {
                            Toast.makeText(UserActivity.this, "인증이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                            G.isConfirm = true; // 인증확인
                            //인증확인후 다이얼로그 없애기
                            confirmDialog.dismiss();
                            dialog.dismiss();
                            //activity refresh 시켜주기!!
                            finish();
                            startActivity(getIntent());
                        }
                    }
                });
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }//end of run
    }//end of inner class 컴펌받은 이메일 서버에 업로드시키는 쓰레드 끝

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
