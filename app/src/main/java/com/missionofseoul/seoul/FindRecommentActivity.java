package com.missionofseoul.seoul;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.missionofseoul.seoul.adapter.FindRecommentAdapter;
import com.missionofseoul.seoul.model.CastCommentBean;
import com.missionofseoul.seoul.model.CastRecommBeen;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class FindRecommentActivity extends AppCompatActivity {

    TextView abs_title;

    ArrayList<CastCommentBean> commentBeens;
    CastCommentBean commentBean;

    ArrayList<CastRecommBeen> recommBeens = new ArrayList<>();
    CastRecommBeen recommBeen;

    int positon;
    int comm_no;

    //맨위의 댓글 comment의 글 멤버변수
    ImageView profile_img;
    TextView nick_text;
    TextView comment_textarea;
    TextView date_text;
    TextView recomment_num;

    //댓글edit
    EditText comment_edittext;

    ListView listview_findrecomment;
    FindRecommentAdapter recommentAdapter;

    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;

    //
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
        setContentView(R.layout.activity_find_recomment);

        mGlideRequestManager = Glide.with(this);

        commentBeens = (ArrayList<CastCommentBean>) getIntent().getSerializableExtra("commentBeen");
        positon = getIntent().getIntExtra("position", 0);

        commentBean = commentBeens.get(positon);
        comm_no = commentBean.getCommentNo(); //findcomment의 기준값

        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        //액션바 타이틀
        abs_title = (TextView) findViewById(R.id.abs_title);
        abs_title.setText("댓글");
        //comment 댓글영역 객체 생성
        profile_img = (ImageView) findViewById(R.id.profile_img);
        nick_text = (TextView) findViewById(R.id.nick_text);
        comment_textarea = (TextView) findViewById(R.id.comment_textarea);
        date_text = (TextView) findViewById(R.id.date_text);
        recomment_num = (TextView) findViewById(R.id.recomment_num_text);

        mGlideRequestManager.load(G.domain + commentBean.getProfileImg()).bitmapTransform(new CropCircleTransformation(this))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(profile_img);
        nick_text.setText(commentBean.getNickName());
        comment_textarea.setText(commentBean.getCommentArea());
        recomment_num.setText("댓글 " + commentBean.getRecommentNum() + "개");

        String stirngDate = commentBean.getDate();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date parseDate = format.parse(stirngDate);
            String mag = DateParse.formatTimeString(parseDate);
            date_text.setText("" + mag);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //
        comment_edittext = (EditText) findViewById(R.id.comment_edittext);

        listview_findrecomment = (ListView) findViewById(R.id.listview_findrecomment);

        //개인 프로필 이동 리스너
        listview_findrecomment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                recommBeen = recommBeens.get(position);
                String id = recommBeen.getRecomm_mem_id();
                int isPrivate = recommBeen.getIsPrivate();

                if (G.mem_id.equals(id)) {
                    Toast.makeText(FindRecommentActivity.this, "본인 프로필은 내 활동에서\n확인하실 수 있습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(FindRecommentActivity.this, AnotherUserActivity.class);
                    intent.putExtra("ID", id);
                    intent.putExtra("IsPrivate", isPrivate);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                    startActivity(intent);
                }

            }
        });

        //recomment 댓글 가져오기
        new Thread() {
            @Override
            public void run() {
                String serverUrl = G.domain + "php/findfriend/load_recomment.php";

                try {
                    URL url = new URL(serverUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);

                    String data = "comm_no=" + comm_no;
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
                    final String[] rows = buffer.toString().split(";");

                    //기존의 데이터랑 겹칠 수 있으므로 모든 데이터를 삭제시킨다.
                    recommBeens.clear();

                    for (String row : rows) {
                        String profileImg = row.split("&")[0];
                        String nickName = row.split("&")[1];
                        String recommArea = row.split("&")[2];
                        String date = row.split("&")[3];
                        int recomm_no = Integer.parseInt(row.split("&")[4]);
                        String recomm_mem_id = row.split("&")[5];
                        int isPrivate = Integer.parseInt(row.split("&")[6]);

                        recommBeens.add(new CastRecommBeen(profileImg, nickName, recommArea, date, recomm_no, recomm_mem_id, isPrivate));

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recommentAdapter = new FindRecommentAdapter(recommBeens, getLayoutInflater(), mGlideRequestManager);
                            listview_findrecomment.setAdapter(recommentAdapter);
                            setListViewHeightBasedOnChildren(listview_findrecomment);// Scrollview에 가려진 listView Height 지정해주기
                            recommentAdapter.notifyDataSetChanged();
                            recomment_num.setText("댓글 " + recommBeens.size() + "개");
                        }
                    });
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }//end of run()
        }.start();  //recomment 댓글 가져오기 끝


    }//******************************************** end of onCreate() ***************************************//

    //댓글 전송버튼
    public void recommBtn(View v) {

        new Thread() {
            @Override
            public void run() {

                String recomment = comment_edittext.getText().toString().trim();
                if (recomment.equals("")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(FindRecommentActivity.this, "댓글을 입력해 주세요!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else { //review_edittext의 내용을 가지고 와서 서버db에 저장시키기

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog = new ProgressDialog(FindRecommentActivity.this);
                            dialog.setMessage("댓글을 저장 중입니다.");
                            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.show();
                        }
                    });

                    String serverUrl = G.domain + "php/findfriend/write_recomment.php";

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
                        dos.writeBytes("Content-Disposition: form-data; name=\"comm_no\"\r\n\r\n" + comm_no);

                        dos.writeBytes("\r\n--*****\r\n");
                        dos.writeBytes("Content-Disposition: form-data; name=\"recomment\"\r\n\r\n" + URLEncoder.encode(recomment, "utf-8"));

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
                        final String result = buffer.toString().trim();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.equals("success")) {
                                    Toast.makeText(FindRecommentActivity.this, "댓글이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                                    comment_edittext.setText("");//전송완료 후 edittext 초기화
                                    //soft키보드 감추는 코드
                                    InputMethodManager immhide = (InputMethodManager) getSystemService(CastDetailActivity.INPUT_METHOD_SERVICE);
                                    immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                    //activity refresh 시켜주기!!
                                    finish();
                                    startActivity(getIntent());
                                } else {
                                    Toast.makeText(FindRecommentActivity.this, "댓글 저장에 실패했습니다\n네트워크가 불안정합니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }//end of if ~ esle 빈문자열이지 체크 if문

            }//end of run
        }.start();

    }//end recommBtn() //댓글 전송 버튼

    //리스트뷰 Height 결정시켜주는 메서드
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 150; // << 이 값으로 마지막 리스트뷰의 BOTTOM을 띄울수 있다.
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }//end of setListViewHeightBasedOnChildren // listview Hieght 결정

    //프로필 클릭하면개인 프로필로이동
    public void clickProfile(View v) {
        String id = commentBean.getComm_mem_id();
        int isPrivate = commentBean.getIsPrivate();

        if (G.mem_id.equals(id)) {
            Toast.makeText(this, "본인 프로필은 내 활동에서\n확인하실 수 있습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, AnotherUserActivity.class);
            intent.putExtra("ID", id);
            intent.putExtra("IsPrivate", isPrivate);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
            startActivity(intent);
        }
    }

    //ActionBar LeftBtn
    public void backBtn(View v) {
        finish();
    }


}//end of class
