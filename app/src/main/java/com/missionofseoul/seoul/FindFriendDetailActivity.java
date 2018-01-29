package com.missionofseoul.seoul;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.missionofseoul.seoul.adapter.FindCommentAdapter;
import com.missionofseoul.seoul.intro.LoginActivity;
import com.missionofseoul.seoul.model.CastCommentBean;
import com.missionofseoul.seoul.model.FindFriendBean;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class FindFriendDetailActivity extends AppCompatActivity {

    //액션바
    TextView abs_title;
    TextView save_text;

    List<FindFriendBean> friendBeens;
    FindFriendBean friendBean;
    int positon;

    int find_no; //findfriend db의 고유번호

    //layout 멤버변수 설정
    TextView find_title;
    TextView find_date;
    TextView find_views;
    TextView find_comm_num;
    TextView find_contents;
    ImageView profile;
    TextView nick;
    TextView email;

    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;

    //댓글쓰기 멤버변수
    EditText comment_edittext;
    TextView comment_num;

    //댓글 리스트뷰 달기
    ListView listview_findcomment;
    FindCommentAdapter findCommentAdapter;
    ArrayList<CastCommentBean> commentBeens = new ArrayList<>(); //모든 댓글 UI가 같기때문에 하나로 쓴다.

    ProgressDialog dialog;

    //개인 프로필 화면 공개/비공개 확인
    int isPrivate;
    String userId;

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
        setContentView(R.layout.activity_find_friend_detail);

        mGlideRequestManager = Glide.with(this);

        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        //액션바 타이틀
        abs_title = (TextView) findViewById(R.id.abs_title);
        abs_title.setText("여행친구찾기");
        save_text = (TextView) findViewById(R.id.save_text);
        save_text.setVisibility(View.VISIBLE);
        save_text.setText("신고");

        //MissionPageFrag3에서 가져오는 값들
        friendBeens = (List<FindFriendBean>) getIntent().getSerializableExtra("friendBeens");
        positon = getIntent().getIntExtra("position", 0);

        friendBean = friendBeens.get(positon);
        find_no = friendBean.getFind_no();

        //layout 객체생성
        find_title = (TextView) findViewById(R.id.find_title);
        find_date = (TextView) findViewById(R.id.find_date);
        find_views = (TextView) findViewById(R.id.find_views);
        find_comm_num = (TextView) findViewById(R.id.find_comment_mum);
        find_contents = (TextView) findViewById(R.id.find_contents);
        profile = (ImageView) findViewById(R.id.profile_img);
        nick = (TextView) findViewById(R.id.nick_text);
        email = (TextView) findViewById(R.id.email_text);

        find_title.setText(friendBean.getTitle());
        find_date.setText(friendBean.getDate());
        find_views.setText(friendBean.getViews() + "");
        // find_comm_num.setText(friendBean.getComment_num() + "");
        find_contents.setText(friendBean.getContents());
        mGlideRequestManager.load(G.domain + friendBean.getProfile()).bitmapTransform(new CropCircleTransformation(this))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(profile);
        nick.setText(friendBean.getNickName());
        email.setText(friendBean.getId());

        //댓글쓰기
        comment_edittext = (EditText) findViewById(R.id.comment_edittext);
        comment_num = (TextView) findViewById(R.id.comment_num);//댓글 개수

        // 댓글 리스트뷰 달기
        listview_findcomment = (ListView) findViewById(R.id.listview_findcomment);

        //댓글 클릭시 상세댓글로
        listview_findcomment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //댓글 항목을 클릭하면 recommentActivity로 이동
                Intent intent1 = new Intent(FindFriendDetailActivity.this, FindRecommentActivity.class);
                intent1.putExtra("commentBeen", commentBeens); //ArrayList<commentBeen>에는 서버에서 가져온 값들이 다 저장되어있다.
                intent1.putExtra("position", position);//리스트뷰 클릭한 위치
                intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                startActivity(intent1);
            }
        });

        //서버에 접속해서 views를 +1를 해준다.
        new Thread() {
            @Override
            public void run() {
                String serverUrl = G.domain + "php/findfriend/update_views.php";
                try {
                    URL url = new URL(serverUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    String data = "find_no=" + find_no;
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

                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();//서버에 접속해서 views +1

        //광고
        AdView adView = (AdView) findViewById(R.id.adview);
        //광고요청자 객체 생성
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }//********************************** end of onCreat() *************************************//

    @Override
    protected void onResume() {
        super.onResume();

        //서버에서 댓글 가져오기
        new Thread() {
            @Override
            public void run() {
                String serverUrl = G.domain + "php/findfriend/load_comment.php";

                try {
                    URL url = new URL(serverUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);

                    String data = "find_no=" + find_no;
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
                    commentBeens.clear();

                    for (String row : rows) {
                        String profileImg = row.split("&")[0];
                        String nickName = row.split("&")[1];
                        String commentArea = row.split("&")[2];
                        String date = row.split("&")[3];
                        int recommentNum = Integer.parseInt(row.split("&")[4]);
                        int commentNo = Integer.parseInt(row.split("&")[5]);
                        String comm_mem_id = row.split("&")[6];
                        int isPrivate = Integer.parseInt(row.split("&")[7]);

                        commentBeens.add(new CastCommentBean(profileImg, nickName, commentArea, date, recommentNum, commentNo, comm_mem_id, isPrivate));

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findCommentAdapter = new FindCommentAdapter(commentBeens, getLayoutInflater(), mGlideRequestManager);
                            listview_findcomment.setAdapter(findCommentAdapter);
                            setListViewHeightBasedOnChildren(listview_findcomment); // Scrollview에 가려진 listView Height 지정해주기
                            findCommentAdapter.notifyDataSetChanged();
                            comment_num.setText(commentBeens.size() + "");//댓글 영역 댓글개수 표신
                            find_comm_num.setText(commentBeens.size() + ""); //타이틀영역 댓글개수 표시
                        }
                    });
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start(); //서버에서 댓글 가져오기
    }

    //댓글 전송버튼을 클릭시 발동
    public void commentBtn(View v) {

        new Thread() {
            @Override
            public void run() {
                String comment = comment_edittext.getText().toString().trim();
                if (comment.equals("")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(FindFriendDetailActivity.this, "댓글을 입력해 주세요!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else { //review_edittext의 내용을 가지고 와서 서버db에 저장시키기

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog = new ProgressDialog(FindFriendDetailActivity.this);
                            dialog.setMessage("댓글을 저장 중입니다.");
                            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.show();
                        }
                    });

                    String serverUrl = G.domain + "php/findfriend/write_comment.php";

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
                        dos.writeBytes("Content-Disposition: form-data; name=\"find_no\"\r\n\r\n" + find_no);

                        dos.writeBytes("\r\n--*****\r\n");
                        dos.writeBytes("Content-Disposition: form-data; name=\"comment\"\r\n\r\n" + URLEncoder.encode(comment, "utf-8"));

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

                                    //댓글이 저장 되면 여행톡을 작성한 사람에게 push 메시지 보내기
                                    fcm.sendFindFriendFcm(find_no);

                                    Toast.makeText(FindFriendDetailActivity.this, "댓글이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                                    comment_edittext.setText("");//전송완료 후 edittext 초기화
                                    //soft키보드 감추는 코드
                                    InputMethodManager immhide = (InputMethodManager) getSystemService(CastDetailActivity.INPUT_METHOD_SERVICE);
                                    immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                    //activity refresh 시켜주기!!
                                    finish();
                                    startActivity(getIntent());
                                } else {
                                    Toast.makeText(FindFriendDetailActivity.this, "댓글 저장에 실패했습니다\n네트워크가 불안정합니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }//end of if ~ esle 빈문자열이지 체크 if문

            }//end of run
        }.start();

    }//end of commentBtn

    // ScrollView에 가려진 리스트뷰 Height 결정시켜주는 메서드
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

    //프로필 이미지 클릭하면 다른 사용자 개인프로필 화면으로  이동
    public void clickProfile(View v) {
        userId = friendBean.getId();
        isPrivate = friendBean.getIsPrivate();

        if (G.mem_id.equals(userId)) {
            Toast.makeText(this, "본인 프로필은 내 활동에서 확인하실 수 있습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, AnotherUserActivity.class);
            intent.putExtra("ID", userId);
            intent.putExtra("IsPrivate", isPrivate);
            startActivity(intent);

        }

    }

    //이메일 누르면 발동 추후 다시 확인
    public void clickEmail(View v) {

        SharedPreferences preferences = getSharedPreferences("AutoLogin", MODE_PRIVATE);
        String mem_id = preferences.getString("Mem_id", "empty");

        Intent it = new Intent(Intent.ACTION_SEND);
        it.putExtra(Intent.EXTRA_EMAIL, mem_id);
        //it.putExtra(Intent.EXTRA_TEXT, "The email body text");
        it.setType("text/plain");
        startActivity(Intent.createChooser(it, friendBean.getId()));

    }//end of clickEmail

    //액션바의 저장버튼 클릭 여기에서 !!신고!!버튼
    public void clickSave(View v) {
        //신고버튼을 누르면 해당글이 기준값으로 신고 DB에 저장

        if (G.isLogin == true) { //로그인상태라면
            new AlertDialog.Builder(this).setTitle("글 신고").setMessage("해당 글을 신고 하시겠습니까?").setPositiveButton("신고", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //신고 하는 로직 서버에 신고하는 해당글의 고유번호와 타이틀  신고자 id를 입력
                    new Thread() {
                        @Override
                        public void run() {
                            String serverUrl = G.domain + "php/findfriend/findreport.php";
                            try {
                                URL url = new URL(serverUrl);

                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setUseCaches(false);
                                connection.setDoInput(true);
                                connection.setDoOutput(true);

                                String data = "find_no=" + find_no + "&title=" + friendBean.getTitle() + "&id=" + G.mem_id;
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

                                final String result = buffer.toString().trim();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (result.equals("success")) {
                                            Toast.makeText(FindFriendDetailActivity.this, "해당 글이 신고 되었습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                is.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();//친구찾기 글 신고

                }
            }).setNegativeButton("취소", null).create().show();
        } else {//비로그인 상태라면
            new AlertDialog.Builder(this).setMessage("로그인이 필요합니다. 로그인 하시겠습니까?").setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //로그인 페이지로 이동
                    Intent intent = new Intent(FindFriendDetailActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
                    finish();
                }
            }).setNegativeButton("다음에", null).create().show();
        }

    }//신고버튼 끝


    //leftbtn 클릭시 발동
    public void backBtn(View v) {
        finish();
    }

}//end of class
