package com.missionofseoul.seoul.intro;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class MemberJoinActivity extends AppCompatActivity {
    EditText edit_id;
    EditText edit_nick;
    EditText edit_pw;
    EditText edit_pwchk;
    TextView validate_idtext;
    TextView validate_nicktext;
    TextView validate_pwtext;
    TextView validate_pwchktext;

    ToggleButton toggleButton;

    //id중복체크할.php서버주소
    String serverUrl = G.domain + "php/validate_id.php";
    //아이디 중복체크할 쓰레드
    IDThread idThread;
    Boolean dbIDResult;

    //nick중복체크할.php서버주소
    String serverUrlNick = G.domain + "php/validate_nick.php";
    //닉네임 중복체크할 쓰레드
    NickThread nickThread;
    Boolean dbNickResult;

    //다음버튼 누를 시 유효성검사 되었는지 체크
    Boolean check1 = false, check2 = false, check3 = false, check4 = false, check5 = false;
    //member.db에 추가시킬 서버 주소
    String serverJoin = G.domain + "php/member_join.php";

    //약관보기 레이아웃 변수
    RelativeLayout layout_terms;

    //약관텍스트뷰들 변수
    TextView termstext1;
    TextView termstext2;
    // TextView termstext3; //위치기반 서비스
    ScrollView scrollview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }

        setContentView(R.layout.activity_member_join);

        //약관보기 레이아웃 객체 생성
        layout_terms = (RelativeLayout) findViewById(R.id.layout_terms);
        //약관텍스뷰들 객체생성
        termstext1 = (TextView) findViewById(R.id.termstext1);
        termstext2 = (TextView) findViewById(R.id.termstext2);
        // termstext3 = (TextView) findViewById(R.id.termstext3); //위치기반서비스

        //ActionBar에  레이아웃 바꾸기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.mem_abs_layout);

        edit_id = (EditText) findViewById(R.id.edit_id);
        edit_nick = (EditText) findViewById(R.id.edit_nick);
        edit_pw = (EditText) findViewById(R.id.edit_pw);
        edit_pwchk = (EditText) findViewById(R.id.edit_pwchk);
        validate_idtext = (TextView) findViewById(R.id.validate_idtext);
        validate_nicktext = (TextView) findViewById(R.id.validate_nicktext);
        validate_pwtext = (TextView) findViewById(R.id.validate_pwtext);
        validate_pwchktext = (TextView) findViewById(R.id.validate_pwchktext);

        scrollview = (ScrollView) findViewById(R.id.scrollview);

        //아이디 유효성 검사시작
        edit_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                edit_id.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext));

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                idThread = new IDThread();
                idThread.start();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });//end of edittext id 유효성 체크

        //닉네임 유효성검사 시작
        edit_nick.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                edit_nick.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nickThread = new NickThread();
                nickThread.start();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });//end of edittext nickmane 유효성검사

        //패스워드 유효성 검사 시작
        edit_pw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                edit_pw.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edit_pw.setBackgroundDrawable(getResources().getDrawable(R.drawable.focusout_edittext));
                validate_pwtext.setVisibility(View.VISIBLE);
                //비밀번호 유효성 검사
                if (edit_pw.getText().toString().trim().length() >= 8) {
                    boolean result = Pattern.matches("^(?=.*[a-zA-Z]+)(?=.*[!@#$%^*+=-]|.*[0-9]+).{8,20}$", edit_pw.getText().toString().trim());
                    if (result) {
                        /*  validate_pwtext.setTextColor(0xff18ffff);
                        validate_pwtext.setText("");*/
                        validate_pwtext.setVisibility(View.GONE);
                        check3 = true;

                    } else {
                        validate_pwtext.setTextColor(0xffff0000);
                        validate_pwtext.setText("영문/숫자조합 8~20자로 입력해주세요");
                    }
                } else {
                    validate_pwtext.setTextColor(0xffff0000);
                    validate_pwtext.setText("비밀번호가 너무 짧습니다. 8자 이상 입력해주세요.");
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });//패스워드 유효성 검사 끝

        //비밀번호 입력 후 포커스 아웃시 밑으로 스크롤 이동
        edit_pw.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                } else {
                    scrollview.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollview.smoothScrollBy(0, 200);
                        }
                    });
                }
            }
        });//스크롤 이동 끝


        //비밀번호 체크 유효성 검사
        edit_pwchk.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                edit_pwchk.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext));

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edit_pwchk.setBackgroundDrawable(getResources().getDrawable(R.drawable.focusout_edittext));
                validate_pwchktext.setVisibility(View.VISIBLE);
                if (!edit_pw.getText().toString().trim().equals(edit_pwchk.getText().toString().trim())) {
                    validate_pwchktext.setTextColor(0xffff0000);
                    validate_pwchktext.setText("입력된 비밀번호와 일치하지 않습니다.");
                    check4 = false;
                } else {
                    // validate_pwchktext.setTextColor(0xff18ffff);
                    validate_pwchktext.setText("");
                    validate_pwchktext.setVisibility(View.GONE);
                    check4 = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        toggleButton = (ToggleButton) findViewById(R.id.toggle_btn);

        //toggleButton클릭시 이미지 변경시키기
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleButton.isChecked()) {
                    toggleButton.setBackgroundDrawable(getResources().
                            getDrawable(R.drawable.toggle_on_back));
                    check5 = true;
                } else {
                    toggleButton.setBackgroundDrawable(getResources().
                            getDrawable(R.drawable.toggle_off_back));
                    check5 = false;
                }
            }
        });


    }//end of onCreate()

    /*//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////*/


    //ActionBar 뒤로가기 버튼 눌렀을 때 발동.
    public void backBtn(View v) {
        Intent intent = new Intent(this, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_transparent);
        finish();

    }//end of backBtn

    //이메일 유효성 검사
    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }//end of isValidEmail


    //아이디 중복체크할 쓰레드
    class IDThread extends Thread {

        @Override
        public void run() {

            final String id = edit_id.getText().toString().trim();

            try {

                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);


                String data = "id=" + id;

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

                while (line != null) {
                    buffer.append(line + "\n");
                    line = reader.readLine();

                }
                //서버php에서 결과값으로 넘어온 것을 파싱해 블리언 값 주기
                final String result = buffer.toString().trim();
                if (result.equals("success")) {
                    dbIDResult = true;
                } else {
                    dbIDResult = false;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(MemberJoin2Activity.this, id + "," + result, Toast.LENGTH_SHORT).show();
                        edit_id.setBackgroundDrawable(getResources().getDrawable(R.drawable.focusout_edittext));
                        validate_idtext.setVisibility(View.VISIBLE);
                        Boolean result = isValidEmail(edit_id.getText().toString());//trim()은 안해줌, 공백오류 잡기
                        if (result) {
                            if (dbIDResult) {
                                validate_idtext.setTextColor(0xff18ffff);
                                validate_idtext.setText("사용가능한 이메일입니다.");
                                check1 = true;
                            } else {
                                validate_idtext.setTextColor(0xffff0000);
                                validate_idtext.setText("이미 사용중인 아이디입니다.");
                                check1 = false;
                            }
                        } else {
                            validate_idtext.setTextColor(0xffff0000);
                            validate_idtext.setText("이메일 형식이 유효하지 않습니다.");
                            check1 = false;
                        }//end of inner if

                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }


        }//end of run()
    }//end of IDThread class

    //nickname 유효성검사 쓰레드
    class NickThread extends Thread {
        @Override
        public void run() {

            final String nick = edit_nick.getText().toString().trim();

            try {

                URL url = new URL(serverUrlNick);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);


                String data = "nick=" + nick;

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

                while (line != null) {
                    buffer.append(line + "\n");
                    line = reader.readLine();

                }
                //서버php에서 결과값으로 넘어온 것을 파싱해 블리언 값 주기
                final String result = buffer.toString().trim();
                if (result.equals("success")) {
                    dbNickResult = true;
                } else {
                    dbNickResult = false;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //  Toast.makeText(MemberJoin2Activity.this, nick + "," + result, Toast.LENGTH_SHORT).show();
                        edit_nick.setBackgroundDrawable(getResources().getDrawable(R.drawable.focusout_edittext));
                        validate_nicktext.setVisibility(View.VISIBLE);
                        //글자수 유효성 검사
                        int length = edit_nick.getText().toString().trim().length(); //공백은 글자수로 안쳐줌

                        if (length >= 2) {
                            if (dbNickResult) {
                                validate_nicktext.setTextColor(0xff18ffff);
                                validate_nicktext.setText("사용가능한 닉네임입니다.");
                                check2 = true;
                            } else {
                                validate_nicktext.setTextColor(0xffff0000);
                                validate_nicktext.setText("이미 사용중인 닉네임입니다.");
                                check2 = false;
                            }
                        } else {
                            validate_nicktext.setTextColor(0xffff0000);
                            validate_nicktext.setText("닉네임은 2자 이상 되어야합니다.");
                            check2 = false;
                        }

                    }
                });//end of runOnUiThread

            } catch (Exception e) {
                e.printStackTrace();
            }

        }//end of run()
    }//end of nickThread class

    //**************** 다음버튼 누르면 발생 **********************//
    public void clickNext(View v) {
        if (check1 && check2 && check3 && check4 && check5) {
            if (edit_pw.getText().toString().trim().equals(edit_pwchk.getText().toString().trim())) { //비밀번호 다시 확인
                new Thread() {//회원가입 memeber_join.php과 통신
                    @Override
                    public void run() {

                        final String mem_id = edit_id.getText().toString().trim();
                        final String mem_nick = edit_nick.getText().toString().trim();
                        final String mem_pw = edit_pw.getText().toString().trim();

                        try {

                            URL url = new URL(serverJoin);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setDoOutput(true);
                            connection.setDoInput(true);
                            connection.setUseCaches(false);


                            String data = "mem_id=" + mem_id + "&mem_nick=" + mem_nick + "&mem_pw=" + mem_pw;

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
                                    if (result.equals("success")) {//회원저장 성공시 로그인 페이지로 이동
                                        Toast.makeText(MemberJoinActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MemberJoinActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);
                                        finish();
                                    } else {
                                        Toast.makeText(MemberJoinActivity.this, "회원가입 실패!!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } catch (Exception e) {

                        }
                    }
                }.start();
            } else {//비밀번호 일치 않을 경우
                Toast.makeText(this, "비밀번호가 일치 하지 않습니다.", Toast.LENGTH_SHORT).show();
            }


        } else {
            Toast.makeText(this, "모든 양식을 알맞게 작성하셔야 합니다.", Toast.LENGTH_SHORT).show();
        }
    }//****************  다음버튼 누르면 발생 끝 *****************************//


    //약관보기 글씨 누르면 발생
    public void clickTerm(View v) {
        hideSoftKeyboard(v);

        //약관보기 할때 애니메션주기
        Animation animation = new AlphaAnimation(0, 1);
        animation.setDuration(1000);
        layout_terms.setVisibility(View.VISIBLE);
        layout_terms.setAnimation(animation);

        //res폴더 안에 있는 terms.xml파일을 읽어와서 분석(parse)하기.
        //res폴더 창고관리자 객체 소환하기.
        Resources res = getResources();

        //창고관리자로부터 파서객체 얻어오기.
        XmlResourceParser xrp = res.getXml(R.xml.terms);

        StringBuffer buffer = new StringBuffer();
        /*StringBuffer buffer2 = new StringBuffer();*/
        String name;
        String msg;

        try {
            xrp.next();
            int eventType = xrp.getEventType();

            while (eventType != XmlResourceParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlResourceParser.START_DOCUMENT:
                        break;
                    case XmlResourceParser.START_TAG:
                        name = xrp.getName();
                        if (name.equals("no1")) {
                            xrp.next();
                            msg = xrp.getText();
                            buffer.append(msg);
                            termstext1.setText(buffer.toString());
                            buffer.setLength(0);//StringBuffer 객체 초기화하기
                        } else if (name.equals("no2")) {
                            xrp.next();
                            msg = xrp.getText();
                            buffer.append(msg);
                            termstext2.setText(buffer.toString());
                            buffer.setLength(0);//StringBuffer 객체 초기화하기
                        } /*else if (name.equals("no3")) { //위치기반 서비스
                            xrp.next();
                            msg = xrp.getText();
                            buffer.append(msg);
                            termstext3.setText(buffer.toString());
                            buffer.setLength(0);//StringBuffer 객체 초기화하기
                        }*/
                        break;
                    case XmlResourceParser.TEXT:
                        break;
                    case XmlResourceParser.END_TAG:
                        break;
                    case XmlResourceParser.END_DOCUMENT:
                        break;
                }//switch

                eventType = xrp.next();
                //  eventType = xrp.getEventType();
            }//end of while

          /*  termstext1.setText(buffer1.toString());*/
          /*  termstext2.setText(buffer2.toString());*/

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }//end of clickTerm()

    //약관 닫기 버튼 누르면 발생
    public void clickCloseTerms(View v) {
        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(500);
        layout_terms.setVisibility(View.GONE);
        layout_terms.setAnimation(animation);
    }//end of 약관닫기버튼

    //소프트 키보드 감추는 메서드
    protected void hideSoftKeyboard(View view) {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }//end fo hideSoftKeyboard

    //뒤로가기 버튼을 눌렀을 때
    @Override
    public void onBackPressed() {

        if (layout_terms.getVisibility() == View.VISIBLE) { //이용약관이 보여질때
            Animation animation = new AlphaAnimation(1, 0);
            animation.setDuration(500);
            layout_terms.setVisibility(View.GONE);
            layout_terms.setAnimation(animation);

        } else {

            super.onBackPressed();
        }

    }//end of onBackPressed()


}//end of MainActivityClass
