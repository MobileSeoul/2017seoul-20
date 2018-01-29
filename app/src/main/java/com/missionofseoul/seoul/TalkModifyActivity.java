package com.missionofseoul.seoul;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TalkModifyActivity extends AppCompatActivity {
    public RequestManager mGlideRequestManager;

    //액션바 멤버변수
    TextView abs_title;
    TextView save_text;

    TextView talk_text;

    //
    String talkText;
    int talkNo;

    String modityText;

    MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_talk_modify);

        mGlideRequestManager = Glide.with(this);
        mainActivity = MainActivity.mainActivity;

        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);


        //액션바 타이틀
        abs_title = (TextView) findViewById(R.id.abs_title);
        abs_title.setText("글수정");
        save_text = (TextView) findViewById(R.id.save_text);
        save_text.setVisibility(View.VISIBLE);
        save_text.setText("수정");

        Intent intent = getIntent();
        talkNo = intent.getIntExtra("TalkNo", 0);
        talkText = intent.getStringExtra("TalkText");


        talk_text = (TextView) findViewById(R.id.talk_text);
        talk_text.setText(talkText);

    }//************************************************* end of onCreate() *****************************************

    //액션바의 저장버튼 클릭 여기에서 !!수정!!버튼
    public void clickSave(View v) {
        modityText = talk_text.getText().toString().trim();
        if (modityText.equals(talkText)) {
            Toast.makeText(this, "변경된 내용이 없습니다. 글을 수정해 주세요.", Toast.LENGTH_SHORT).show();
        } else if (modityText.equals("")) {
            Toast.makeText(this, "수정한 내용이 없습니다. 글을 수정해 주세요. ", Toast.LENGTH_SHORT).show();
        } else {
            //수정된 글을 서버에 update 시키기
            new Thread() {
                @Override
                public void run() {
                    String serverUrl = G.domain + "php/traveltalk/modify_mytalk.php";//글러벌 변수로 domain 설정
                    try {
                        URL url = new URL(serverUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.setUseCaches(false);


                        //파일전송의 헤더영역 속성 설정(한글교안 헤더부분 파란색 부분)
                        connection.setRequestProperty("Connection", "Keep-Alive");
                        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=*****");//boundary - 데이터 바디의 파일의 구분자역할

                        //파일전송의 바디영역에 들어가 data 작성 및 Output
                        OutputStream os = connection.getOutputStream();
                        DataOutputStream dos = new DataOutputStream(os); //DataOutputStream은 자동으로 utf-8로 전송이 된다. 한글 깨짐 생각안해도 됨.
                        dos.writeBytes("\r\n--*****\r\n");
                        dos.writeBytes("Content-Disposition: form-data; name=\"talk_no\"\r\n\r\n" + talkNo);

                        dos.writeBytes("\r\n--*****\r\n");
                        dos.writeBytes("Content-Disposition: form-data; name=\"modityText\"\r\n\r\n" + URLEncoder.encode(modityText, "utf-8"));

                        dos.flush();

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

                        if (buffer.toString().equals("success")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TalkModifyActivity.this, "수정완료", Toast.LENGTH_SHORT).show();

                                    TalkDetailActivity talkDetailActivity = (TalkDetailActivity) TalkDetailActivity.talkDetailActivity;
                                    talkDetailActivity.finish();
                                    mainActivity.loadFragment();
                                    finish();
                                }
                            });
                        }//end of if

                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        }//end of if else

    }//end of clickSave()

    //액션바의 leftBtn 클릭
    public void backBtn(View v) {
        finish();
    }//end of backBtn


}
