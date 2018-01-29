package com.missionofseoul.seoul;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.missionofseoul.seoul.intro.IntroActivity;
import com.missionofseoul.seoul.intro.LoginActivity;
import com.missionofseoul.seoul.thebogi.MissionExplainActivity;
import com.missionofseoul.seoul.thebogi.NoticeActivity;
import com.missionofseoul.seoul.thebogi.TermsActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ThebogiActivity extends AppCompatActivity {

    //BottomNavigation icon 변수
    ImageView home_icon;
    ImageView search_icon;
    ImageView user_icon;
    ImageView thebogi_icon;

    //Actionbartext
    TextView titletext;

    String version;

    LinearLayout layout_checklogin;
    LinearLayout layout_login;
    LinearLayout layout_logout;
    MainActivity mainActivity;

    ProgressDialog dialog;

    //
    Switch switch1;
    IsPrivateThread isPrivateThread;
    int mPrivate = 0;

    //미션숨김버튼
    Button hidden_button;

    //미션정렬
    Button range_button;
    AlertDialog alertDialog;
    int tmp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_thebogi);
        /*******************************************************************/
        mainActivity = MainActivity.mainActivity; // 로그아웃시 메인엑티비티 종료 시키려고

        layout_checklogin = (LinearLayout) findViewById(R.id.layout_checklogin);
        layout_login = (LinearLayout) findViewById(R.id.layout_login);
        layout_logout = (LinearLayout) findViewById(R.id.layout_logout);

        hidden_button = (Button) findViewById(R.id.hidden_button);//미션숨김버튼
        range_button = (Button) findViewById(R.id.range_button);//미션정렬버튼

        if (G.isLogin == true) {//로그인했다면

            layout_checklogin.setVisibility(View.VISIBLE);
            layout_login.setVisibility(View.GONE);
            layout_logout.setVisibility(View.VISIBLE);

            //미션숨기버튼 활성화/비활성화
            if (G.HiddenList.size() == 0) {
                hidden_button.setEnabled(false);
            } else {
                hidden_button.setEnabled(true);
            }

            //서버에서 프로필 공개 여부 가져오기
            new Thread() {
                @Override
                public void run() {

                    String serverUrl = G.domain + "php/thebogi/load_private.php";

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

                        final int result = Integer.parseInt(buffer.toString().trim());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result == 1) {
                                    isPrivate(1);
                                } else if (result == 2) {
                                    isPrivate(2);
                                }
                            }
                        });
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();//서버에서 프로필 공개 여부 가져오기 끝
        } else {//비로그인 상태라면
            //비로그인 상태면 미션숨기버튼 / 미션정렬버튼 비활성화
            hidden_button.setEnabled(false);
            range_button.setEnabled(false);
        }

        //BottomNavigation icon 객체생성
        home_icon = (ImageView) findViewById(R.id.home_icon);
        search_icon = (ImageView) findViewById(R.id.search_icon);
        user_icon = (ImageView) findViewById(R.id.user_icon);
        thebogi_icon = (ImageView) findViewById(R.id.thebogi_icon);


        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.bottom_abs_layout);

        titletext = (TextView) findViewById(R.id.actionbar_text);
        titletext.setText("더보기"); //Actionbar title를 더보기로

        thebogi_icon.setImageResource(R.drawable.thebogi_icon_over);

       /* //새로운 공지 있으면 new icon 띄우기 // 17/02/22 추가
        new_notice = (ImageView) findViewById(R.id.new_notice);
        new_noticetext = (TextView) findViewById(R.id.new_noticetext);*/


    }//***************************************end of onCreate()  ***********************************************//

    @Override
    protected void onResume() {
        super.onResume();

        //새로운 공지 있으면 new icon 띄우기 // 17/02/22 추가
    /*    SharedPreferences pre = getSharedPreferences("NewNotice", MODE_PRIVATE);
        int newNotice = pre.getInt("NoticeNum", 0);
        if (G.new_Notice != newNotice) {
            new_notice.setVisibility(View.VISIBLE);
            new_noticetext.setVisibility(View.VISIBLE);
        } else {
            new_notice.setVisibility(View.GONE);
            new_noticetext.setVisibility(View.GONE);

        }*/

    }

    //프로필 공개 여부
    public void isPrivate(int num) {
        if (num == 1) {
            switch1 = (Switch) findViewById(R.id.switch1);
            switch1.setChecked(true);
            switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        //서버에 member DB private update 1
                        mPrivate = 1;
                        isPrivateThread = new IsPrivateThread();
                        isPrivateThread.start();
                    } else {
                        //서버에 ember DB private update 2
                        mPrivate = 2;
                        isPrivateThread = new IsPrivateThread();
                        isPrivateThread.start();
                    }
                }
            });

        } else if (num == 2) {
            switch1 = (Switch) findViewById(R.id.switch1);
            switch1.setChecked(false);
            switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        //서버에 member DB private update 1
                        mPrivate = 1;
                        isPrivateThread = new IsPrivateThread();
                        isPrivateThread.start();
                    } else {
                        //서버에 ember DB private update 2
                        mPrivate = 2;
                        isPrivateThread = new IsPrivateThread();
                        isPrivateThread.start();
                    }
                }
            });
        }
    }//end isPrivate()

    //공지사항버튼 누르면 발생
    public void noticeBtn(View v) {
        //  Toast.makeText(this, "준비중입니다.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, NoticeActivity.class);
        startActivity(intent);

    }//end of 공지사항


    //버전정보 누르면 발생
    public void versionBtn(View v) {
        //현재 버젼정보 알아내기 // build.gradle에서 versionName "1.0" 을 가져온다. 추후 1.0을 버젼별로 도시이름과 매칭시킨다.
        try {
            PackageInfo i = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            version = i.versionName;
            Toast.makeText(this, "현재버전 : v" + version, Toast.LENGTH_SHORT).show();
        } catch (PackageManager.NameNotFoundException e) {

        }

    }//end of 버전정보


    //사용방법
    public void infoBtn(View v) {
        //  Toast.makeText(this, "준비중입니다.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MissionExplainActivity.class);
        startActivity(intent);
    }//end of 사용방법

    //랭킹순위
    public void rankBtn(View v) {
        Intent intent = new Intent(this, RankingActivity.class);
        startActivity(intent);
    }


    //사용자 권한 설정
    public void permissionBtn(View v) {

        //이 앱에 대한 사용자가 권한을 on/off 할 수 있는 system setting으로 이동
        Uri uri = Uri.fromParts("package", "com.missionofseoul.seoul", null);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
        startActivity(intent);

    }//end of 사용자 권한 설정

    //미션정렬 버튼
    public void rangeMission(View v) {
        //버튼 클릭시 다이얼로그 띄움
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("미션정렬설정");
        LayoutInflater inflater = getLayoutInflater();
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.range_dialog, null);//null은 아직 아무 곳에도 안붙인다.

        RadioGroup rg_range = (RadioGroup) layout.findViewById(R.id.rg_range); //라디오그룹객체생성
        RadioButton rb_normal = (RadioButton) layout.findViewById(R.id.rb_normal);
        RadioButton rb_row = (RadioButton) layout.findViewById(R.id.rb_row);
        RadioButton rb_high = (RadioButton) layout.findViewById(R.id.rb_high);
        //2017-02-14 추가
        RadioButton rb_shuffle = (RadioButton) layout.findViewById(R.id.rb_shuffle); //무작위 정렬

        if (G.range == 0) {
            rb_normal.setChecked(true);
        } else if (G.range == 1) {
            rb_row.setChecked(true);
        } else if (G.range == 2) {
            rb_high.setChecked(true);
        } else if (G.range == 3) {
            rb_shuffle.setChecked(true);
        }
        //라디오 그룹 변경 리스너
        rg_range.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch (i) {
                    case R.id.rb_normal:
                        tmp = 0;
                        break;
                    case R.id.rb_row:
                        tmp = 1;
                        break;
                    case R.id.rb_high:
                        tmp = 2;
                        break;
                    case R.id.rb_shuffle:
                        tmp = 3;
                        break;
                }
            }
        });
        builder.setView(layout);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                SharedPreferences preferences = getSharedPreferences("MissionRange", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("Range", tmp);
                editor.apply();

                G.range = preferences.getInt("Range", 0); // 글로벌 변수에 저장 미션정렬방법 저장
                alertDialog.dismiss();

                mainActivity.finish();

                Intent intent = new Intent(ThebogiActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                startActivity(intent);

                finish();
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        //  ******** 공통부분 *******

        alertDialog = builder.create();
        alertDialog.setCancelable(false); //back버튼으로도 취소가 불가능!!
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }//end of 미션정렬

    //미션 숨김 취소
    public void isHiddenMission(View v) {
        new AlertDialog.Builder(this).setMessage("미션 숨김을 취소하시겠습니까?").setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ThebogiActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove("Hidden");
                editor.apply();

                mainActivity.finish();

                Intent intent = new Intent(ThebogiActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                startActivity(intent);

                finish();
            }
        }).setNegativeButton("아니요", null).create().show();

    }//end of isHiddenMission

    //약관들 관련 버튼 누르면 발생
    public void termsBtn(View v) {
        switch (v.getId()) {
            case R.id.use_term: //서비스이용약관
                Intent intent = new Intent(this, TermsActivity.class);
                intent.putExtra("terms", "no1");
                startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 아래서 위로
                break;
            case R.id.personinfo_term://개인정보처리방침
                Intent intent1 = new Intent(this, TermsActivity.class);
                intent1.putExtra("terms", "no2");
                startActivity(intent1);
                overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 아래서 위로
                break;
        /*    case R.id.location_term://위치정보 이용약관
                Intent intent2 = new Intent(this, TermsActivity.class);
                intent2.putExtra("terms", "no3");
                startActivity(intent2);
                overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 아래서 위로

                break;*/
        }
    }//end of terms 약관들

    //고객센터 버튼
    public void csBtn(View v) {
        //  Toast.makeText(this, "준비중입니다.", Toast.LENGTH_SHORT).show();
        Intent it = new Intent(Intent.ACTION_SEND);
        String[] tos = {"missionoftrip@gmail.com"}; //보내는 사람
        //    String[] ccs = {"missionoftrip@gmail.com"};  //참조부분
        it.putExtra(Intent.EXTRA_EMAIL, tos);
        // it.putExtra(Intent.EXTRA_CC, ccs);
        it.putExtra(Intent.EXTRA_SUBJECT, "여행의 미션 고객센터");
        it.setType("message/rfc822");
        startActivity(Intent.createChooser(it, "Choose Email Client"));

    }//end of 고객센터

    //로그인 버튼을 누르면 발생
    public void loginBtn(View v) {

        Intent intent = new Intent(ThebogiActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
        finish();
    }

    //로그아웃 버튼을 누르면 발생
    public void logoutBtn(View v) {

        new AlertDialog.Builder(this).setMessage("로그아웃 하시겠습니까?").setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //SharedPreferenceds 삭제 / 삭제하면 로그아웃 시킴!!
                SharedPreferences preferences = getSharedPreferences("AutoLogin", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove("Mem_id");
                editor.commit();

                //로그아웃시 미션숨김 다 삭제
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ThebogiActivity.this);
                SharedPreferences.Editor editor1 = prefs.edit();
                editor1.remove("Hidden");
                editor1.apply();

                //로그아웃시 미션정렬 삭제
                SharedPreferences preferences02 = getSharedPreferences("MissionRange", MODE_PRIVATE);
                SharedPreferences.Editor editor02 = preferences02.edit();
                editor02.remove("Range");
                editor02.apply();

                //글로벌 전역변수 초기화
                G.isLogin = false;
                G.mem_id = "empty";
                G.isConfirm = false;

                //메인엑티비트 종료 후
                mainActivity.finish();

                Intent intent = new Intent(ThebogiActivity.this, IntroActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
                finish();

            }
        }).setNegativeButton("아니오", null).create().show();


    }//end of logout()

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
            case R.id.layout_search://내 주변
                Intent intent1 = new Intent(this, MapsActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                startActivity(intent1);
                overridePendingTransition(0, 0);//<< activity전환시 깜빡임을 없애 주었다
                finish();
                break;
            case R.id.layout_user://내 활동 버튼
                //Activity이동 전 로그인 했는지 물어보기
                if (G.isLogin == true) {//로그인했다면
                    //user화면으로 이동
                    Intent intent2 = new Intent(this, UserActivity.class);
                    intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                    startActivity(intent2);
                    overridePendingTransition(0, 0); // << activity전환시 깜빡임을 없애 주었다.
                    finish();
                } else { //비로그인 상태라면,
                    new AlertDialog.Builder(this).setMessage("로그인이 필요합니다. 로그인 하시겠습니까?").setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //로그인 페이지로 이동
                            Intent intent = new Intent(ThebogiActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                            startActivity(intent);
                            overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
                            finish();
                        }
                    }).setNegativeButton("다음에", null).create().show();
                }//end if ~ else //로그인여부 묻고 userActivity로 이동
                break;
            case R.id.layou_thebogi: //더 보기(설정)버튼
                //자기 자신 activity는 아무것도 안함
                break;
        }
    }//end of bttomBtn

    //서버에 private 컬럼에 프로필 공개 여부 업데이트
    class IsPrivateThread extends Thread {
        @Override
        public void run() {

            String serverUrl = G.domain + "php/thebogi/update_private.php";

            try {
                URL url = new URL(serverUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                String data = "id=" + G.mem_id + "&isPrivate=" + mPrivate;
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (buffer.toString().equals("success")) {
                            if (mPrivate == 1) { // 프로필을 공개로 전환
                                Toast.makeText(ThebogiActivity.this, "프로필이 공개되었습니다.", Toast.LENGTH_SHORT).show();
                                switch1.setChecked(true);

                            } else if (mPrivate == 2) {
                                Toast.makeText(ThebogiActivity.this, "프로필이 비공개되었습니다", Toast.LENGTH_SHORT).show();
                                switch1.setChecked(false);
                            }
                        }//end of if
                    }
                });
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }//end of run
    }//end Thread class 서버에 private 컬럼에 프로필 공개 여부 업데이트 끝


}//end of class
