package com.missionofseoul.seoul;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.missionofseoul.seoul.intro.LoginActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class FindPWActivity extends AppCompatActivity {
    //Actionbartext
    TextView abs_title;

    //
    IDThread idThread;
    Boolean isIDCheck = false;

    //
    EditText id_check, code_check, edit_pw, edit_pwchk;
    TextView validate_idtext, validate_codetext, validate_pwtext, validate_pwchktext;

    //
    String randCode;

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

        setContentView(R.layout.activity_find_pw);

        //ActionBar 커스텀하기      //emulator api 16에서 custom error
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        //액션바 타이틀
        abs_title = (TextView) findViewById(R.id.abs_title);
        abs_title.setText("비밀번호찾기");

        //Edit text 객체생성
        id_check = (EditText) findViewById(R.id.id_check);
        code_check = (EditText) findViewById(R.id.code);
        edit_pw = (EditText) findViewById(R.id.edit_pw);
        edit_pwchk = (EditText) findViewById(R.id.edit_pwchk);
        //처음에 비밀번호 입력칸 비활성화 상태 만들기
        //코드 입력후 활성화로 변경
        edit_pw.setEnabled(false);
        edit_pwchk.setEnabled(false);

        validate_idtext = (TextView) findViewById(R.id.validate_idtext);
        validate_codetext = (TextView) findViewById(R.id.validate_codetext);
        validate_pwtext = (TextView) findViewById(R.id.validate_pwtext);
        validate_pwchktext = (TextView) findViewById(R.id.validate_pwchktext);

        //가입된 이메일 확인 리스너
        id_check.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                id_check.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext));

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                idThread = new IDThread();
                idThread.start();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //이메일 인증코드 확인 리스너
        code_check.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                code_check.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext));
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (code_check.getText().toString().equals(randCode)) {
                    code_check.setBackgroundDrawable(getResources().getDrawable(R.drawable.focusout_edittext));
                    validate_codetext.setVisibility(View.VISIBLE);
                    validate_codetext.setTextColor(0xff18ffff);
                    validate_codetext.setText("인증코드가 일치합니다. 변경하실 비밀번호를 입력해 주세요.");
                    edit_pw.setEnabled(true);
                    edit_pwchk.setEnabled(true);
                } else {
                    validate_codetext.setTextColor(0xffff0000);
                    validate_codetext.setText("인증코드가 일치하지 않습니다. 다시 입력해 주세요.");
                    edit_pw.setEnabled(false);
                    edit_pwchk.setEnabled(false);
                }
            }
        });


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
                } else {
                    // validate_pwchktext.setTextColor(0xff18ffff);
                    validate_pwchktext.setText("");
                    validate_pwchktext.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }//***************************************end of onCreate() ************************************************//

    //이메일 전송 버튼을 누르면 이메일이로 16자리 코드가 나라감
    public void clickBtn(View v) {

        if (isIDCheck == true) {
            randCode = G.randCode();//16자리램덤코드 생성

            dialog = new ProgressDialog(FindPWActivity.this);
            dialog.setMessage("인증번호 확인 중입니다.");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            new Thread() {
                @Override
                public void run() {

                    String id = id_check.getText().toString().trim();
                    String serverUrl = G.domain + "php/mail/find_pw.php";

                    try {
                        URL url = new URL(serverUrl);

                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setUseCaches(false);
                        connection.setDoInput(true);
                        connection.setDoOutput(true);

                        String data = "id=" + id + "&code=" + randCode; //+ "&nick=" + G.mem_id;
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
                                Toast.makeText(FindPWActivity.this, "인증코드가 이메일로 발송되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });

                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }//end of run()
            }.start();

        } else {
            Toast.makeText(this, "가입하신 이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show();

        }
    }

    //비밀번호 변경 버튼 누르면 서버에 비밀번호 업데이트 시키기
    public void clickChagePW(View v) {
        //비밀번호 다시 확인
        if (edit_pw.getText().toString().trim().equals(edit_pwchk.getText().toString().trim())
                && !edit_pw.getText().toString().equals("")) {
            dialog = new ProgressDialog(FindPWActivity.this);
            dialog.setMessage("비밀번호 변경 중입니다.");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            new Thread() {
                @Override
                public void run() {

                    String id = id_check.getText().toString().trim();
                    String pw = edit_pw.getText().toString().trim();
                    String serverUrl = G.domain + "php/findpw/update_pw.php";

                    try {

                        URL url = new URL(serverUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.setUseCaches(false);


                        String data = "id=" + id + "&pw=" + pw;

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
                                    Toast.makeText(FindPWActivity.this, "비밀번호 변경에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(FindPWActivity.this, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);
                                    finish();
                                } else {
                                    Toast.makeText(FindPWActivity.this, "비밀번호 변경에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } catch (Exception e) {
                    }
                    dialog.dismiss();
                }
            }.start();
        } else {//비밀번호 일치 않을 경우
            Toast.makeText(this, "비밀번호가 일치하지 않습니다. 비밀번호를 다시 한 번 확인해 주세요.", Toast.LENGTH_SHORT).show();
        }


    }//end of clickChagePW() 비밀번호 변경 버튼을 클릭하면 서버에 비밀번호 업데이트 끝ㄴ

    //********************************************************************************************************************//
//아이디 중복체크할 쓰레드
    class IDThread extends Thread {

        @Override
        public void run() {

            String id = id_check.getText().toString().trim();
            String serverUrl = G.domain + "php/findpw/id_check.php";
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

                StringBuffer buffer = new StringBuffer();

                String line = reader.readLine();

                while (line != null) {
                    buffer.append(line + "\n");
                    line = reader.readLine();

                }
                //서버php에서 결과값으로 넘어온 것을 파싱해 블리언 값 주기
                final String result = buffer.toString().trim();


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        id_check.setBackgroundDrawable(getResources().getDrawable(R.drawable.focusout_edittext));
                        validate_idtext.setVisibility(View.VISIBLE);

                        if (result.equals("success")) {
                            validate_idtext.setTextColor(0xff18ffff);
                            validate_idtext.setText("가입된 이메일입니다. 전송 버튼을 클릭해 주세요.");
                            isIDCheck = true;
                        } else {
                            validate_idtext.setTextColor(0xffff0000);
                            validate_idtext.setText("이메일을 찾지 못하였습니다. 다시 입력해 주세요.");
                            isIDCheck = false;

                        }

                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }


        }//end of run()
    }//end of IDThread class


    //액션바의 leftBtn 클릭
    public void backBtn(View v) {
        finish();
    }
}
