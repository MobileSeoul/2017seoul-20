package com.missionofseoul.seoul;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class FindFrinedWriteActivity extends AppCompatActivity {

    //액션바 멤버변수
    TextView abs_title;
    TextView save_text;

    //spinner멤버변수
    Spinner continent_spinner;
    Spinner nation_spinner;
    ArrayAdapter continetn_adapter;
    ArrayAdapter nation_adapter;

    EditText title_edit;
    EditText contents_edit;

    //spinner 대륙과 나라 선택 저장변수
    String continent;
    String nation;
    //edittext의 제목과 내용을 저장변수
    String title;
    String contents;

    ProgressDialog dialog;

    //
    MainActivity mainActivity;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_find_frined_write);

        //17/03/17 추가 친구찾기 글쓰면 새로고침
        mainActivity = MainActivity.mainActivity;

        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        //액션바 타이틀
        abs_title = (TextView) findViewById(R.id.abs_title);
        abs_title.setText("글쓰기");
        save_text = (TextView) findViewById(R.id.save_text);
        save_text.setVisibility(View.VISIBLE);

        //spinner객체생성
        continent_spinner = (Spinner) findViewById(R.id.continent_spinner);
        nation_spinner = (Spinner) findViewById(R.id.nation_spinner);

        continetn_adapter = ArrayAdapter.createFromResource(this, R.array.continent, android.R.layout.simple_spinner_item);
        continent_spinner.setAdapter(continetn_adapter);

        continent_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                switch (position) {
                    case 0: //아시아
                        TextView text = (TextView) view;
                        continent = text.getText().toString().trim();
                        nation_adapter = ArrayAdapter.createFromResource(FindFrinedWriteActivity.this, R.array.aisa, android.R.layout.simple_spinner_item);
                        nation_spinner.setAdapter(nation_adapter);

                        break;
                    case 1://유럽

                        TextView text1 = (TextView) view;
                        continent = text1.getText().toString().trim();
                        nation_adapter = ArrayAdapter.createFromResource(FindFrinedWriteActivity.this, R.array.europe, android.R.layout.simple_spinner_item);
                        nation_spinner.setAdapter(nation_adapter);

                        break;
                    case 2://아메리카
                        TextView text2 = (TextView) view;
                        continent = text2.getText().toString().trim();
                        nation_adapter = ArrayAdapter.createFromResource(FindFrinedWriteActivity.this, R.array.america, android.R.layout.simple_spinner_item);
                        nation_spinner.setAdapter(nation_adapter);
                        break;
                    case 3://오세아니아
                        TextView text3 = (TextView) view;
                        continent = text3.getText().toString().trim();
                        nation_adapter = ArrayAdapter.createFromResource(FindFrinedWriteActivity.this, R.array.oceania, android.R.layout.simple_spinner_item);
                        nation_spinner.setAdapter(nation_adapter);
                        break;
                    case 4://아프리카
                        TextView text4 = (TextView) view;
                        continent = text4.getText().toString().trim();
                        nation_adapter = ArrayAdapter.createFromResource(FindFrinedWriteActivity.this, R.array.africa, android.R.layout.simple_spinner_item);
                        nation_spinner.setAdapter(nation_adapter);
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        nation_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TextView text = (TextView) view;
                nation = text.getText().toString().trim();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //edittext 객체생성
        title_edit = (EditText) findViewById(R.id.title_edit);
        contents_edit = (EditText) findViewById(R.id.contents_edit);
    }//end of onCreate()

    //액션바의 저장버튼 클릭
    public void clickSave(View v) {

        title = title_edit.getText().toString(); //게시물 제목
        contents = contents_edit.getText().toString(); //게시물 내용

        if (!title.equals("") && !contents.equals("")) {//제목과 내용이 비어있지 않으면 저장실행
            //저장버튼을 누르면 서버에 저장
            new Thread() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog = new ProgressDialog(FindFrinedWriteActivity.this);
                            dialog.setMessage("게시물을 저장 중입니다.");
                            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.show();
                        }
                    });

                    String serverUrl = G.domain + "php/findfriend/write_findfriend.php";

                    URL url = null;
                    try {
                        url = new URL(serverUrl);
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
                        dos.writeBytes("Content-Disposition: form-data; name=\"id\"\r\n\r\n" + G.mem_id);

                        dos.writeBytes("\r\n--*****\r\n");
                        dos.writeBytes("Content-Disposition: form-data; name=\"continent\"\r\n\r\n" + URLEncoder.encode(continent, "utf-8"));

                        dos.writeBytes("\r\n--*****\r\n");
                        dos.writeBytes("Content-Disposition: form-data; name=\"nation\"\r\n\r\n" +  URLEncoder.encode(nation, "utf-8"));

                        dos.writeBytes("\r\n--*****\r\n");
                        dos.writeBytes("Content-Disposition: form-data; name=\"title\"\r\n\r\n" + URLEncoder.encode(title, "utf-8"));

                        dos.writeBytes("\r\n--*****\r\n");
                        dos.writeBytes("Content-Disposition: form-data; name=\"contents\"\r\n\r\n" + URLEncoder.encode(contents, "utf-8"));

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
                        final String result = buffer.toString();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.equals("success")) {
                                    Toast.makeText(FindFrinedWriteActivity.this, "글저장성공", Toast.LENGTH_SHORT).show();

                                    InputMethodManager immhide = (InputMethodManager) getSystemService(CastDetailActivity.INPUT_METHOD_SERVICE);
                                    immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                                    //activity refresh 시켜주기!!
                                    finish();
                                    startActivity(getIntent());
                                    //
                                    mainActivity.loadFragment();
                                } else {
                                    Toast.makeText(FindFrinedWriteActivity.this, "실패", Toast.LENGTH_SHORT).show();

                                }

                            }
                        });
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }//end of run
            }.start();//친구찾기게시물 서버에 저장하기

        } else {
            Toast.makeText(this, "제목과 내용이 모두 입력되어야 합니다.", Toast.LENGTH_SHORT).show();
        }


    }//end of clickSave // 액션바 저장버튼 누르면 글저장

    //leftbtn 누르면 발동
    public void backBtn(View v) {
        finish();
    }

    @Override
    public void onBackPressed() {

        title = title_edit.getText().toString(); //게시물 제목
        contents = contents_edit.getText().toString(); //게시물 내용

        if (!title.equals("") || !contents.equals("")) {
            new AlertDialog.Builder(this).setMessage("페이지를 나가시면 작성하신 글은 저장되지 않습니다.").setPositiveButton("나가기", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).setNegativeButton("취소", null).create().show();
        } else {
            finish();
        }
    }
}//end of class
