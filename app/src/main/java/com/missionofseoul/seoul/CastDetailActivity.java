package com.missionofseoul.seoul;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.missionofseoul.seoul.adapter.CastCommentAdapter;
import com.missionofseoul.seoul.intro.LoginActivity;
import com.missionofseoul.seoul.model.CastBean;
import com.missionofseoul.seoul.model.CastCommentBean;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class CastDetailActivity extends AppCompatActivity {
    ScrollView scrollView;
    TextView cast_maintitle;
    TextView cast_date;
    TextView cast_likes;
    TextView top_comment_num; //17/03/21추가 상단 커맨트 개수

    //댓글쓰기 멤버변수
    EditText comment_edittext;

    ArrayList<String> arrayList = new ArrayList<>(); // cast DB의 contents 컬럼의 값들을 split해서 저장 시킬 arrayList

    ArrayList<CastCommentBean> commentBeen = new ArrayList<>(); //리스트뷰에 붙을 것

    ListView listview_castcomment;
    CastCommentAdapter castCommentAdapter;

    TextView comment_num;//댓글 총 개수 구해주는

    ProgressDialog dialog;

    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;

    ImageView icon_heart;
    ImageView icon_solidheart;

    ArrayList<CastBean> castBeens;
    CastBean castBean;
    int cast_no; //db cast테이블에서 기준값으로 사용 될 값
    int pos;

    //
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);

        }

        setContentView(R.layout.activity_cast_deatil);

        mGlideRequestManager = Glide.with(this);

        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.castdetail_abs_layout);

        icon_heart = (ImageView) findViewById(R.id.icon_heart);
        icon_solidheart = (ImageView) findViewById(R.id.icon_solidheart);


        castBeens = (ArrayList<CastBean>) getIntent().getSerializableExtra("castBeens");
        pos = getIntent().getIntExtra("pos", 0);

        castBean = castBeens.get(pos);
        cast_no = castBean.getCast_no();


        scrollView = (ScrollView) findViewById(R.id.cast_scrollView);
        cast_maintitle = (TextView) findViewById(R.id.cast_maintitle);
        cast_date = (TextView) findViewById(R.id.cast_date);
        cast_likes = (TextView) findViewById(R.id.cast_likes);
        top_comment_num = (TextView) findViewById(R.id.top_comment_num); //17/03/21 추가


        comment_edittext = (EditText) findViewById(R.id.comment_edittext);

        comment_num = (TextView) findViewById(R.id.comment_num);//댓글 총 개수 구해주는

        //캐스트 내용 붙이기
        cast_maintitle.setText(castBean.getCast_title());
        cast_date.setText(castBean.getCast_date());
        cast_likes.setText(castBean.getCast_likes());

        //웹뷰를 붙이기
        webView = (WebView) findViewById(R.id.webview);
        //웹뷰안에서 웹페이지가 열리도록 설정.
        //이 설정을 안하며 웹뷰는 로드된 웹페이지를 새로운 창으로 오픈함.
        //webView.setWebViewClient(new WebViewClient());

        //자바스크립트로 실행되는 alert()같은 브라우저의 별도의 필요한. 요청은.
        //아래설정이 없으면 실행되지 못함.
        webView.setWebChromeClient(new WebChromeClient());
        //로드된 페이지의 자바스크립트를 사용할 수 있도록 허용.
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //웹뷰에게 웹페이지와 연동작업을 수행하는 인터페이스 객체 설정.
        //webView.addJavascriptInterface(new WebAppInterface(), "Droid");
        webView.loadUrl(G.domain + castBean.getCast_contents());

        //댓글 부분 리스트뷰 붙이기
        listview_castcomment = (ListView) findViewById(R.id.listview_castcomment);

        listview_castcomment.setOnItemClickListener(new AdapterView.OnItemClickListener() {//댓글을 누르면 발동
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //댓글 항목을 클릭하면 castrecommentActivity로 이동
                Intent intent1 = new Intent(CastDetailActivity.this, CastRecommentActivity.class);
                intent1.putExtra("commentBeen", commentBeen); //ArrayList<commentBeen>에는 서버에서 가져온 값들이 다 저장되어있다.
                intent1.putExtra("position", position);//리스트뷰 클릭한 위치
                startActivity(intent1);

                // Toast.makeText(CastDetailActivity.this, comm_no + "", Toast.LENGTH_SHORT).show();
            }
        });//댓글을 클릭하면 상세 댓글로

        //자신이 작성한 댓글을 삭제하기 위한 롱클릭 리스너 달기
        listview_castcomment.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {


                if (G.mem_id.equals(commentBeen.get(position).getComm_mem_id())) {//아이디가 같으면

                    if (commentBeen.get(position).getRecommentNum() == 0) {//댓글이 0개
                        new AlertDialog.Builder(CastDetailActivity.this).setMessage("정말 삭제하시겠습니까?")
                                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //서버에 접속해 comm_no 기준으로 삭제하기
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                String serverUrl = G.domain + "php/cast/del_comment.php";

                                                try {
                                                    URL url = new URL(serverUrl);

                                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                                    connection.setRequestMethod("POST");
                                                    connection.setUseCaches(false);
                                                    connection.setDoInput(true);
                                                    connection.setDoOutput(true);

                                                    String data = "comm_no=" + commentBeen.get(position).getCommentNo();
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
                                                                Toast.makeText(CastDetailActivity.this, "삭제했습니다.", Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(CastDetailActivity.this, "댓글이 달린 글은 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                } //end of if(해당 댓글 리스트에 position의 아이디와 내 아이디가 같으면 발동


                return true;

            }
        });//리스트의 롱클릭리스너로 자신이 쓴 글 삭제 시키기


        //캐스트 상세페이지에 들어와서,좋아요 눌렀는지 확인
        if (G.isLogin == true) {//로그인 상태

            new Thread() {
                @Override
                public void run() {
                    String serverUrl = G.domain + "php/cast/check_likes.php";

                    try {
                        URL url = new URL(serverUrl);

                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setUseCaches(false);
                        connection.setDoInput(true);
                        connection.setDoOutput(true);

                        String data = "cast_no=" + cast_no + "&id=" + G.mem_id;
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

        }//end of if else //캐스트 상세페이지에 들어오면, 로그인 확인 후 좋아요 확인하기

        //광고
        AdView adView = (AdView) findViewById(R.id.adview);
        //광고요청자 객체 생성
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);


    }//************************************************* end of onCreate() **********************************************************//

    //댓글의 댓글에서 뒤로 넘어 오면 바로 적용 시키기 위해서
    @Override
    protected void onResume() {
        super.onResume();

        //서버에서 댓글 가져오기
        new Thread() {
            @Override
            public void run() {
                String serverUrl = G.domain + "php/cast/load_comment.php";

                try {
                    URL url = new URL(serverUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);

                    String data = "cast_no=" + cast_no;
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
                    commentBeen.clear();

                    for (String row : rows) {
                        String profileImg = row.split("&")[0];
                        String nickName = row.split("&")[1];
                        String commentArea = row.split("&")[2];
                        String date = row.split("&")[3];
                        int recommentNum = Integer.parseInt(row.split("&")[4]);
                        int commentNo = Integer.parseInt(row.split("&")[5]);
                        String comm_mem_id = row.split("&")[6];
                        int isPrivate = Integer.parseInt(row.split("&")[7]);

                        commentBeen.add(new CastCommentBean(profileImg, nickName, commentArea, date, recommentNum, commentNo, comm_mem_id, isPrivate));

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            castCommentAdapter = new CastCommentAdapter(commentBeen, getLayoutInflater(), mGlideRequestManager);
                            listview_castcomment.setAdapter(castCommentAdapter);
                            setListViewHeightBasedOnChildren(listview_castcomment); // Scrollview에 가려진 listView Height 지정해주기
                            castCommentAdapter.notifyDataSetChanged();
                            comment_num.setText(commentBeen.size() + "");

                            top_comment_num.setText(commentBeen.size() + ""); // 17/03/21추가
                        }
                    });
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start(); //서버에서 댓글 가져오기

    }//************************** end of onResume *************************************//

    //댓글 전송버튼을 클릭시 발동
    public void commentBtn(View v) {

        if (G.isLogin == false) {//비로그인 상태라면
            new AlertDialog.Builder(this).setMessage("로그인이 필요합니다. 로그인 하시겠습니까?").setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //로그인 페이지로 이동
                    Intent intent = new Intent(CastDetailActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
                    finish();
                }
            }).setNegativeButton("다음에", null).create().show();
        } else {//로그인 상태

            new Thread() {
                @Override
                public void run() {
                    String comment = comment_edittext.getText().toString().trim();
                    if (comment.equals("")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CastDetailActivity.this, "댓글을 입력해 주세요!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else { //review_edittext의 내용을 가지고 와서 서버db에 저장시키기

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog = new ProgressDialog(CastDetailActivity.this);
                                dialog.setMessage("댓글을 저장 중입니다.");
                                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.show();
                            }
                        });

                        String serverUrl = G.domain + "php/cast/write_comment.php";

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
                            dos.writeBytes("Content-Disposition: form-data; name=\"cast_no\"\r\n\r\n" + cast_no);

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
                                        Toast.makeText(CastDetailActivity.this, "댓글이 저장되었습니다.", Toast.LENGTH_SHORT).show();
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

        }//로그인 상태에 if else 문 끝
    }//end of commentBtn //댓글작성 서버에 전송 쓰레드

    //leftbtn 종료
    public void backBtn(View v) {
        finish();
    }

    //캐스트 좋아요 하트 눌르기
    public void heartBtn(View v) {

        if (G.isLogin == false) {//비로그인 상태라면
            new AlertDialog.Builder(this).setMessage("로그인이 필요합니다. 로그인 하시겠습니까?").setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //로그인 페이지로 이동
                    Intent intent = new Intent(CastDetailActivity.this, LoginActivity.class);
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

                //서버에 접속 cast_likes insert cast db cast_likes update +1
                new Thread() {
                    @Override
                    public void run() {
                        String serverUrl = G.domain + "php/cast/selectlike.php";

                        try {
                            URL url = new URL(serverUrl);

                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setUseCaches(false);
                            connection.setDoInput(true);
                            connection.setDoOutput(true);

                            String data = "cast_no=" + cast_no + "&id=" + G.mem_id;
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
                                        Toast.makeText(CastDetailActivity.this, "좋아요", Toast.LENGTH_SHORT).show();
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
                        String serverUrl = G.domain + "php/cast/unselectlike.php";

                        try {
                            URL url = new URL(serverUrl);

                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setUseCaches(false);
                            connection.setDoInput(true);
                            connection.setDoOutput(true);

                            String data = "cast_no=" + cast_no + "&id=" + G.mem_id;
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
                                        Toast.makeText(CastDetailActivity.this, "좋아요 취소", Toast.LENGTH_SHORT).show();
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
    }//end of heartbtn

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


}//end of class

