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
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.missionofseoul.seoul.adapter.FindCommentAdapter;
import com.missionofseoul.seoul.model.CastCommentBean;
import com.missionofseoul.seoul.model.NoticeBean;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class NoticeDeatilActivity extends AppCompatActivity {

    TextView abs_title; //액션바

    TextView notice_maintitle;
    TextView notice_date;
    TextView comment_num; //타이틀 댓글 개수 표시

    TextView comm_num; // 댓글리스트 위 댓글 표시
    //리스트뷰
    ListView listView_noticecomment;

    //댓글 전송부분
    EditText comment_edittext;

    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;

    ArrayList<NoticeBean> noticeBeens;
    NoticeBean noticeBean;
    int positon;
    int notice_no;

    ProgressDialog dialog;

    //리스트 불러오기
    ArrayList<CastCommentBean> commentBeens = new ArrayList<>(); //모든 댓글 UI가 같기때문에 하나로 쓴다.
    FindCommentAdapter findCommentAdapter; //adapter 재활용 해보자!!

    //공지를 붙일 웹뷰
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
        setContentView(R.layout.activity_notice_deatil);

        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        //액션바 타이틀
        abs_title = (TextView) findViewById(R.id.abs_title);
        abs_title.setText("공지사항");

        mGlideRequestManager = Glide.with(this);

        //MissionPageFrag3에서 가져오는 값들
        noticeBeens = (ArrayList<NoticeBean>) getIntent().getSerializableExtra("noticeBeens");
        positon = getIntent().getIntExtra("position", 0);

        noticeBean = noticeBeens.get(positon);
        notice_no = noticeBean.getNotice_no();

        notice_maintitle = (TextView) findViewById(R.id.notice_maintitle);
        notice_date = (TextView) findViewById(R.id.notice_date);
        comment_num = (TextView) findViewById(R.id.notice_comment);//타이틀 댓글 개수표시 // 서버에서 댓글 가져오면서 붙이기

        notice_maintitle.setText(noticeBean.getTitle());
        notice_date.setText(noticeBean.getDate());

        String nUrl = noticeBean.getnUrl(); //서버에서 공지가 있는 html에 주소가져오기

        //웹뷰를 붙이기
        webView = (WebView) findViewById(R.id.webview);
        //웹뷰안에서 웹페이지가 열리도록 설정.
        //이 설정을 안하며 웹뷰는 로드된 웹페이지를 새로운 창으로 오픈함.
      //  webView.setWebViewClient(new WebViewClient());

        //자바스크립트로 실행되는 alert()같은 브라우저의 별도의 필요한. 요청은.
        //아래설정이 없으면 실행되지 못함.
        webView.setWebChromeClient(new WebChromeClient());

        //로드된 페이지의 자바스크립트를 사용할 수 있도록 허용.
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //웹뷰에게 웹페이지와 연동작업을 수행하는 인터페이스 객체 설정.
        //webView.addJavascriptInterface(new WebAppInterface(), "Droid");
        webView.loadUrl(G.domain + nUrl);


        comm_num = (TextView) findViewById(R.id.comment_num);//댓글리스트 위 댓글 개수표시 //서버에서 댓글 가져오면서 붙이기

        //리스트뷰 객체생성
        listView_noticecomment = (ListView) findViewById(R.id.listview_noticecomment);

        //댓글 누르면 recomment로  넘어감
        listView_noticecomment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //댓글 항목을 클릭하면 noticerecommentActivity로 이동
                Intent intent1 = new Intent(NoticeDeatilActivity.this, NoticeRecommentActivity.class);
                intent1.putExtra("commentBeen", commentBeens); //ArrayList<commentBeen>에는 서버에서 가져온 값들이 다 저장되어있다.
                intent1.putExtra("position", position);//리스트뷰 클릭한 위치
                startActivity(intent1);
            }
        });

        //댓글 에디터
        comment_edittext = (EditText) findViewById(R.id.comment_edittext);

        //서버에서 댓글 가져오기
        new Thread() {
            @Override
            public void run() {

                String serverUrl = G.domain + "php/notice/load_comment.php";

                try {
                    URL url = new URL(serverUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);

                    String data = "notice_no=" + notice_no;
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
                            listView_noticecomment.setAdapter(findCommentAdapter);
                            setListViewHeightBasedOnChildren(listView_noticecomment); // Scrollview에 가려진 listView Height 지정해주기
                            findCommentAdapter.notifyDataSetChanged();
                            comment_num.setText(commentBeens.size() + "");//타이틀영역 댓글개수 표시
                            comm_num.setText(commentBeens.size() + ""); //댓글 영역 댓글개수 ㅂ표신

                        }
                    });
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start(); //서버에서 댓글 가져오기

    }//********************************************************* end of onCreate() ********************************************************//

    //댓글 전송글씨 누르면 발동
    public void commentBtn(View v) {

        new Thread() {
            @Override
            public void run() {
                String comment = comment_edittext.getText().toString().trim();
                if (comment.equals("")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NoticeDeatilActivity.this, "댓글을 입력해 주세요!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else { //review_edittext의 내용을 가지고 와서 서버db에 저장시키기

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog = new ProgressDialog(NoticeDeatilActivity.this);
                            dialog.setMessage("댓글을 저장 중입니다.");
                            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.show();
                        }
                    });

                    String serverUrl = G.domain + "php/notice/write_comment.php";

                    URL url = null;
                    try {
                        url = new URL(serverUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setDoOutput(true);
                        connection.setDoInput(true);

                    /*    String data = "id=" + G.mem_id + "&notice_no=" + notice_no + "&comment=" + comment;
                        OutputStream os = connection.getOutputStream();
                        os.write(data.getBytes());

                        os.flush();
                        os.close();*/

                        //파일전송의 헤더영역 속성 설정(한글교안 헤더부분 파란색 부분)
                        connection.setRequestProperty("Connection", "Keep-Alive");
                        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=*****");//boundary - 데이터 바디의 파일의 구분자역할

                        //파일전송의 바디영역에 들어가 data 작성 및 Output
                        OutputStream os = connection.getOutputStream();
                        DataOutputStream dos = new DataOutputStream(os); //DataOutputStream은 자동으로 utf-8로 전송이 된다. 한글 깨짐 생각안해도 됨.
                        dos.writeBytes("\r\n--*****\r\n");
                        dos.writeBytes("Content-Disposition: form-data; name=\"id\"\r\n\r\n" + G.mem_id);
                        dos.writeBytes("\r\n--*****\r\n");
                        dos.writeBytes("Content-Disposition: form-data; name=\"notice_no\"\r\n\r\n" + notice_no);

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
                                    Toast.makeText(NoticeDeatilActivity.this, "댓글이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                                    comment_edittext.setText("");//전송완료 후 edittext 초기화
                                    //soft키보드 감추는 코드
                                    InputMethodManager immhide = (InputMethodManager) getSystemService(CastDetailActivity.INPUT_METHOD_SERVICE);
                                    immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                    //activity refresh 시켜주기!!
                                    finish();
                                    startActivity(getIntent());
                                } else {
                                    Toast.makeText(NoticeDeatilActivity.this, "댓글 저장에 실패했습니다\n네트워크가 불안정합니다.", Toast.LENGTH_SHORT).show();
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

    }//end of commentBtn() 댓글 전송버튼 누르면 댓글 저장

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

    //ActionBar LeftBtn
    public void backBtn(View v) {
        finish();
    }

}//end of class
