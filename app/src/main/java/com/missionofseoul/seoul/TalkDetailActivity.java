package com.missionofseoul.seoul;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.clans.fab.FloatingActionMenu;
import com.missionofseoul.seoul.adapter.FindCommentAdapter;
import com.missionofseoul.seoul.intro.LoginActivity;
import com.missionofseoul.seoul.model.CastCommentBean;
import com.missionofseoul.seoul.model.TravelTalkBean;

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

public class TalkDetailActivity extends AppCompatActivity {

    public RequestManager mGlideRequestManager;

    //액션바 멤버변수
    TextView abs_title;
    TextView save_text;

    //상세페이지에 보여줄 여행톡 내용들
    ArrayList<TravelTalkBean> travelTalkBeens;
    TravelTalkBean talkBean;
    int pos;   //여행톡 리스트에서 눌려진 번째 값
    int talk_no; //여행톡의 내용을 가져올 기준값,

    //layout 멤버변수 설정
    ImageView profile;
    TextView nick;
    TextView date;
    TextView contents_text;
    LinearLayout layout_inflateimg;
    TextView likes_num;
    TextView comment_num;

    ImageView icon_heart;
    ImageView icon_solidheart;
    ScrollView scrollView;

    //댓글 영역
    ListView listview_talkcomment;
    ArrayList<CastCommentBean> commentBeens = new ArrayList<>(); //모든 댓글 UI가 같기때문에 하나로 쓴다.
    FindCommentAdapter findCommentAdapter; //adapter 재활용

    LinearLayout layout_dummy;
    //댓글 멤버변수
    EditText comment_edittext;

    ProgressDialog dialog;

    //글작성자와 보는 사람이 같으면 보여줄 액션버튼
    FloatingActionMenu fab_menu;

    //TalkDetailActivity 전역변수만들기
    public static TalkDetailActivity talkDetailActivity;

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
        setContentView(R.layout.activity_talk_detail);

        talkDetailActivity = TalkDetailActivity.this;

        mGlideRequestManager = Glide.with(this);

        //ActionBar 커스텀하기      //emulator api 16에서 custom error
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        //액션바 타이틀
        abs_title = (TextView) findViewById(R.id.abs_title);
        abs_title.setText("여행톡");
        save_text = (TextView) findViewById(R.id.save_text);
        save_text.setVisibility(View.VISIBLE);
        save_text.setText("신고");

        //
        travelTalkBeens = (ArrayList<TravelTalkBean>) getIntent().getSerializableExtra("travelTalkBeens");
        pos = getIntent().getIntExtra("pos", 0);

        talkBean = travelTalkBeens.get(pos);
        talk_no = talkBean.getTalk_no();

        //layout 객체 생성
        profile = (ImageView) findViewById(R.id.profile);
        nick = (TextView) findViewById(R.id.nick);
        date = (TextView) findViewById(R.id.date);
        contents_text = (TextView) findViewById(R.id.contents_text);
        layout_inflateimg = (LinearLayout) findViewById(R.id.layout_inflateimg);
        likes_num = (TextView) findViewById(R.id.likes_num);
        comment_num = (TextView) findViewById(R.id.comment_num);

        // 좋아요
        icon_heart = (ImageView) findViewById(R.id.icon_heart);
        icon_solidheart = (ImageView) findViewById(R.id.icon_solidheart);
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        //
        mGlideRequestManager.load(G.domain + talkBean.getProfile()).bitmapTransform(new CropCircleTransformation(this))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(profile);
        nick.setText(talkBean.getNickName());

        //SNS식 날짜변형
        String snsDate = talkBean.getDate();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date parseDate = format.parse(snsDate);
            String mag = DateParse.formatTimeString(parseDate);
            date.setText(mag);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        contents_text.setText(talkBean.getTalk_text());
        comment_num.setText(talkBean.getComment_num() + "");

        String[] img = {talkBean.getImg0(), talkBean.getImg1(), talkBean.getImg2(), talkBean.getImg3(), talkBean.getImg4()};
        //이미지 붙이기
        for (int i = 0; i < 5; i++) {
            View imageLayout = getLayoutInflater().inflate(R.layout.item_cast_contents, null);
            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.cast_contents_img);
            mGlideRequestManager.load(G.domain + img[i]).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);
            layout_inflateimg.addView(imageLayout);//LinearLayout에 이미지뷰 붙이기

        }

        layout_dummy = (LinearLayout) findViewById(R.id.layout_dummy);


        //댓글 layout
        comment_edittext = (EditText) findViewById(R.id.comment_edittext);
        listview_talkcomment = (ListView) findViewById(R.id.listview_talkcomment);

        //댓글리스트를 누르면 recomment로 넘어감
        listview_talkcomment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //recommentActivity로 이동
                Intent intent1 = new Intent(TalkDetailActivity.this, TalkRecommentActivity.class);
                intent1.putExtra("commentBeen", commentBeens); //ArrayList<commentBeen>에는 서버에서 가져온 값들이 다 저장되어있다.
                intent1.putExtra("position", position);//리스트뷰 클릭한 위치
                startActivity(intent1);
            }
        });

        //자신이 작성한 댓글을 삭제하기 위한 롱클릭 리스너 달기
        listview_talkcomment.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {

                if (G.mem_id.equals(commentBeens.get(position).getComm_mem_id())) { // 아이디가 같으면

                    if (commentBeens.get(position).getRecommentNum() == 0) {//댓글이 0개
                        new AlertDialog.Builder(TalkDetailActivity.this).setMessage("정말 삭제하시겠습니까?")
                                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //서버에 접속해 comm_no 기준으로 삭제하기
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                String serverUrl = G.domain + "php/traveltalk/del_comment.php";

                                                try {
                                                    URL url = new URL(serverUrl);

                                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                                    connection.setRequestMethod("POST");
                                                    connection.setUseCaches(false);
                                                    connection.setDoInput(true);
                                                    connection.setDoOutput(true);

                                                    String data = "comm_no=" + commentBeens.get(position).getCommentNo() + "&talk_no=" + talk_no;
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
                                                                Toast.makeText(TalkDetailActivity.this, "삭제했습니다.", Toast.LENGTH_SHORT).show();

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
                                            }//emd of run()
                                        }.start();

                                    }//삭제 버튼 눌렀을 때 발동 끝

                                }).setNegativeButton("취소", null).create().show(); //취소버튼

                    } else {//아이디 일치하지만 댓글이 0이 아닐 때
                        //댓글이 있으면 삭제할 수 없기
                        Toast.makeText(TalkDetailActivity.this, "댓글이 달린 글은 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                }//end of if(해당 댓글 리스트에 position의 아이디와 내 아이디가 같으면 발동

                return true;//중요!!  true로 해야 뒤로 넘어가지 않아 롱클릭만 적용되고 클릭이벤트는 발생 되지 않는다
            }
        }); //댓글 삭제를 위한 롱클릭 리스너 끝


        //상세페이지에 들어와서,좋아요 눌렀는지 확인
        if (G.isLogin == true) {//로그인 상태
            new Thread() {
                @Override
                public void run() {
                    String serverUrl = G.domain + "php/traveltalk/check_likes.php";

                    try {
                        URL url = new URL(serverUrl);

                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setUseCaches(false);
                        connection.setDoInput(true);
                        connection.setDoOutput(true);

                        String data = "talk_no=" + talk_no + "&id=" + G.mem_id;
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
                                if (result.equals("selected")) {//좋아요를 선택했다면
                                    icon_heart.setVisibility(View.INVISIBLE); //빈하트 안보이게
                                    icon_solidheart.setVisibility(View.VISIBLE); // 하트 보이게
                                }
                            }
                        });
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }//end of run()
            }.start();

        }//end of if else // 상세페이지에 들어오면, 로그인 확인 후 좋아요 확인하기

        //좋아요 개수 따로 가져오기
        new Thread() {
            @Override
            public void run() {
                String serverUrl = G.domain + "php/traveltalk/likes_num.php";
                try {
                    URL url = new URL(serverUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    String data = "talk_no=" + talk_no;
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
                            likes_num.setText(result);
                        }
                    });
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();//좋아요 개수 가져오기


        fab_menu = (FloatingActionMenu) findViewById(R.id.fab_menu);

        // 여행톡 상세 페이지를 보는 사람과 글쓴 사람이 같으면 foltionactionButton을 보여주기
        if (G.mem_id.equals(talkBean.getId())) {
            fab_menu.setVisibility(View.VISIBLE);
        }


    }//****************************************************** end of onCreate() **********************************************//

    @Override
    protected void onResume() {
        super.onResume();

        //서버에서 댓글 가져오기
        new Thread() {
            @Override
            public void run() {
                String serverUrl = G.domain + "php/traveltalk/load_comment.php";//글러벌 변수로 domain 설정
                try {
                    URL url = new URL(serverUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);

                    String data = "talk_no=" + talk_no;
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
                            listview_talkcomment.setAdapter(findCommentAdapter);
                            setListViewHeightBasedOnChildren(listview_talkcomment); // Scrollview에 가려진 listView Height 지정해주기
                            findCommentAdapter.notifyDataSetChanged();
                            comment_num.setText(commentBeens.size() + "");// 댓글개수 표시
                            //댓글이 하나라도 달리면 "댓글을 달아보세요" 텍스트 지우기
                            if (commentBeens.size() > 0) {
                                layout_dummy.setVisibility(View.GONE);
                            }
                        }
                    });
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    //좋아요(하트) 아이콘 버튼을 클릭
    public void clickLike(View v) {

        if (G.isLogin == false) {//비로그인 상태라면
            new AlertDialog.Builder(this).setMessage("로그인이 필요합니다. 로그인 하시겠습니까?").setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //로그인 페이지로 이동
                    Intent intent = new Intent(TalkDetailActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
                    finish();
                }
            }).setNegativeButton("다음에", null).create().show();
        } else {//로그인 상태
            if (icon_solidheart.getVisibility() == View.INVISIBLE) {//좋아요 안누른 상태에서 누름

                icon_heart.setVisibility(View.INVISIBLE);
                icon_solidheart.setVisibility(View.VISIBLE);

                //서버에 접속 talk_likes insert cast db cast_likes update +1
                new Thread() {
                    @Override
                    public void run() {
                        String serverUrl = G.domain + "php/traveltalk/selectlike.php";

                        try {
                            URL url = new URL(serverUrl);

                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setUseCaches(false);
                            connection.setDoInput(true);
                            connection.setDoOutput(true);

                            String data = "talk_no=" + talk_no + "&id=" + G.mem_id;
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
                                        Toast.makeText(TalkDetailActivity.this, "좋아요", Toast.LENGTH_SHORT).show();

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
                    }//end of run()
                }.start();//end of thread 좋아요 누름

            } else { //좋아요 누름 상태에서 다시 누름
                icon_solidheart.setVisibility(View.INVISIBLE);
                icon_heart.setVisibility(View.VISIBLE);

                //서버 접속 castlikes del 한 후 cast_likes update -1;
                new Thread() {
                    @Override
                    public void run() {
                        String serverUrl = G.domain + "php/traveltalk/unselectlike.php";

                        try {
                            URL url = new URL(serverUrl);

                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setUseCaches(false);
                            connection.setDoInput(true);
                            connection.setDoOutput(true);

                            String data = "talk_no=" + talk_no + "&id=" + G.mem_id;
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
                                        Toast.makeText(TalkDetailActivity.this, "좋아요 취소", Toast.LENGTH_SHORT).show();
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
                    }//end of run()
                }.start();
            }//end of if else 좋아요 누른 상태에서 다시 누름(좋아요취소)
        }//end of if else (로그인 상태)
    }//end of clickLike (좋아요 선택 취소 버튼)

    //댓글 전송 버튼 클릭
    public void commentBtn(View v) {
        if (G.isLogin == false) { //비로그인상태라면
            new AlertDialog.Builder(this).setMessage("로그인이 필요합니다. 로그인 하시겠습니까?").setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //로그인 페이지로 이동
                    Intent intent = new Intent(TalkDetailActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
                    finish();
                }
            }).setNegativeButton("다음에", null).create().show();

        } else {//로그인 상태라면
            new Thread() {
                @Override
                public void run() {

                    String comment = comment_edittext.getText().toString().trim();

                    if (comment.equals("")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(TalkDetailActivity.this, "댓글을 입력해 주세요!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else { //review_edittext의 내용을 가지고 와서 서버db에 저장시키기

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog = new ProgressDialog(TalkDetailActivity.this);
                                dialog.setMessage("댓글을 저장 중입니다.");
                                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.show();
                            }
                        });

                        String serverUrl = G.domain + "php/traveltalk/write_comment.php";

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
                            dos.writeBytes("Content-Disposition: form-data; name=\"talk_no\"\r\n\r\n" + talk_no);

                            dos.writeBytes("\r\n--*****\r\n");
                            dos.writeBytes("Content-Disposition: form-data; name=\"comment\"\r\n\r\n" + URLEncoder.encode(comment, "utf-8"));

                            dos.flush();

                            //서버로부터 오는 echo를 읽어오기
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

                                        //댓글이 저장 되면 여행톡을 작성한 사람에게 push 메시지 보내기
                                        //여행톡 작성자를 알아야 하기 때문에 기준값 talk_no를 가져간다.
                                        fcm.sendTravelTalkFcm(talk_no);

                                        Toast.makeText(TalkDetailActivity.this, "댓글이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                                        comment_edittext.setText("");//전송완료 후 edittext 초기화
                                        //soft키보드 감추는 코드
                                        InputMethodManager immhide = (InputMethodManager) getSystemService(CastDetailActivity.INPUT_METHOD_SERVICE);
                                        immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

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
                        dialog.dismiss();
                    }//end of if ~ esle 빈문자열이지 체크 if문

                }//end of run
            }.start();
        }//end of if 로그인인지 아닌지 확인 if

    }//댓글 전송번튼 끝


    //액션바의 저장버튼 클릭 여기에서 !!신고!!버튼
    public void clickSave(View v) {
        //신고버튼을 누르면 해당글이 기준값으로 신고 DB에 저장

        if (G.isLogin == true) { //로그인상태라면
            new AlertDialog.Builder(this).setTitle("글 신고").setMessage("해당 글을 신고 하시겠습니까?").setPositiveButton("신고", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //신고 하는 로직

                }
            }).setNegativeButton("취소", null).create().show();
        } else {//비로그인 상태라면
            new AlertDialog.Builder(this).setMessage("로그인이 필요합니다. 로그인 하시겠습니까?").setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //로그인 페이지로 이동
                    Intent intent = new Intent(TalkDetailActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
                    finish();
                }
            }).setNegativeButton("다음에", null).create().show();
        }

    }//신고버튼 끝

    //액션바의 leftBtn 클릭
    public void backBtn(View v) {
        finish();
    }


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

    //플로팅액션 버튼 삭제 버튼을 클릭하면 댓글이 없으면 삭제
    public void clickFabDel(View v) {
        if (talkBean.getComment_num() == 0) {
            new AlertDialog.Builder(TalkDetailActivity.this).setMessage("정말 삭제하시겠습니까?")
                    .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //댓글 개수가 0이면 삭제
                            new Thread() {
                                @Override
                                public void run() {
                                    String serverUrl = G.domain + "php/traveltalk/del_mytalk.php";//글러벌 변수로 domain 설정
                                    try {
                                        URL url = new URL(serverUrl);
                                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                        connection.setRequestMethod("POST");
                                        connection.setDoOutput(true);
                                        connection.setDoInput(true);
                                        connection.setUseCaches(false);

                                        String data = "talk_no=" + talk_no + "&id=" + G.mem_id;
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
                                                    Toast.makeText(TalkDetailActivity.this, "글삭제", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            });
                                        }
                                        is.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();

                        }//삭제 버튼 눌렀을 때 발동 끝
                    }).setNegativeButton("취소", null).create().show(); //취소버튼
        } else {
            Toast.makeText(this, "댓글이 달린 글은 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }//end of clickFabDel() // 여행톡 삭제

    //글수정 버튼을 누르면 수정 activity로 이동
    public void clickFabRewrite(View v) {
        //   Toast.makeText(this, "글수정테스트", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, TalkModifyActivity.class);
        intent.putExtra("TalkNo", talk_no);
        intent.putExtra("TalkText", talkBean.getTalk_text());
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로


    }//end of clickFabRewrite()

    //프로필 누르면 개인 프로필로 넘어간다.
    public void clickProfile(View v) {

        String id = talkBean.getId();
        int isPrivate = talkBean.getIsPrivate();
        if (G.mem_id.equals(id)) {
            Toast.makeText(this, "본인 프로필은 내 활동에서\n확인하실 수 있습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, AnotherUserActivity.class);
            intent.putExtra("ID", id);
            intent.putExtra("IsPrivate", isPrivate);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
            startActivity(intent);
        }

    }//end of clickProfile

}//end of TalkDetailActivity
