package com.missionofseoul.seoul.intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.missionofseoul.seoul.Fcm;
import com.missionofseoul.seoul.FindPWActivity;
import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.MainActivity;
import com.missionofseoul.seoul.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    EditText edit_id;
    EditText edit_pw;

    TextView validate_text;

    //fcm
    Fcm fcm = new Fcm();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_login);

        //actionBar custom하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.login_abs_layout);

        edit_id = (EditText) findViewById(R.id.edit_id);
        edit_pw = (EditText) findViewById(R.id.edit_pw);
        validate_text = (TextView) findViewById(R.id.validate_text);

    }//*********************************************// end of onCreat() *******************************************//

    //ActionBar 뒤로가기 버튼 눌렀을 때 발동.
    public void backBtn(View v) {
        Intent intent = new Intent(this, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_transparent);
        finish();
    }//end of backBtn()

    //닫기버튼 눌렀을 메인화면으로 이동하기
    public void closeBtn(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//activity삭제
        startActivity(intent);
        finish();
    }

    //로그인버튼 눌렀을 때
    public void clickLogin(View v) {
        new Thread() {//로그인 눌렀을 시 login.php과 통신
            @Override
            public void run() {
                final String mem_id = edit_id.getText().toString().trim();
                final String mem_pw = edit_pw.getText().toString().trim();

                String serverUrl = G.domain + "php/login.php";
                try {

                    URL url = new URL(serverUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);


                    String data = "mem_id=" + mem_id + "&mem_pw=" + mem_pw;

                    OutputStream os = connection.getOutputStream();
                    os.write(data.getBytes());

                    os.flush();
                    os.close();//서버로 데이타 보내기 끝;

                    //서버로부터 오는 echo를 읽어오기
                    InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader reader = new BufferedReader(isr);

                    final StringBuffer buffer = new StringBuffer();

                    String line = reader.readLine();

                    while (line != null) {
                        buffer.append(line + "\n");
                        line = reader.readLine();
                    }
                    final String result = buffer.toString().trim();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            validate_text.setVisibility(View.VISIBLE);
                           /* validate_text.setText(result);*/

                            if (result.equals("1")) {//아이디 비번일치
                                //validate_text.setText("로그인 성공");

                                //로그인성공시 sharedPreference에 아이디 저장,
                                SharedPreferences preferences = getSharedPreferences("AutoLogin", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("Mem_id", mem_id);
                                editor.commit();

                                String mem_id = preferences.getString("Mem_id", "empty");

                                //글로벌 전역변수 설정
                                G.isLogin = true;
                                G.mem_id = mem_id;

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//activity삭제
                                startActivity(intent);

                                //fcm
                                FirebaseMessaging.getInstance().subscribeToTopic("news");
                                String token = FirebaseInstanceId.getInstance().getToken();
                                fcm.sendRegistrationToServer(token);

                                finish();

                            } else if (result.equals("3")) { //비번 불일치
                                validate_text.setText("비밀번호가 일치하지 않습니다.");
                            } else if (result.equals("4")) {// 가입된 아이디 없음
                                validate_text.setText("이메일을 다시 한 번 확인해주세요");
                            } else if (result.equals("2")) {//탈퇴한 회원
                                validate_text.setText("탈퇴한 아이디입니다.");
                            }
                        }//end of run
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }//end of clickLogin

    public void lostPW(View v) {

        Intent intent = new Intent(this, FindPWActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);

    }//end of lostPW

    public void clickIsMember(View v) {
        Intent intent = new Intent(this, MemberJoinActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);
        finish();
    }//end of clickIsMember

}//end of class
