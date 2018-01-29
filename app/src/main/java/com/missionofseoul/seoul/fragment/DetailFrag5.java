package com.missionofseoul.seoul.fragment;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.missionofseoul.seoul.AnotherUserActivity;
import com.missionofseoul.seoul.DateParse;
import com.missionofseoul.seoul.DetailActivity;
import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.GalleryActivity;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.TotalProfileActivity;
import com.missionofseoul.seoul.TotalReviewActivity;
import com.missionofseoul.seoul.adapter.ReviewListAdapter;
import com.missionofseoul.seoul.intro.LoginActivity;
import com.missionofseoul.seoul.model.MImageBean;
import com.missionofseoul.seoul.model.MissionProfileBean;
import com.missionofseoul.seoul.model.ReviewBean;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by hyunho on 2016-11-13.
 */

public class DetailFrag5 extends Fragment {
    // ArrayList<String> images = new ArrayList<>();
    LinearLayout thumImgContainer;

    ArrayList<MImageBean> mImageBeens = new ArrayList<>();//17/02/25 수정 미션 이미제 텍스트 붙이기

    ThumGalleryThread galleryThread;

    int column = 5;
    int tableNum;

    ProgressDialog dialog;

    //리뷰관련 멤버변수 생성
    public ArrayList<ReviewBean> reviewBeen = new ArrayList<>();
    ListView listView_review;
    ReviewListAdapter listAdapter;

    TextView reviewTotalNum;// 리뷰 총 개수 표시 할 변수
    TextView clicktotal_review; // 리뷰전체보기 클릭

    //리뷰작성버튼
    Button review_btn;

    //리뷰 로드하는 스레드 멤버변수
    LoadReviewThread loadReviewThread;

    //미션수행한 대원들 프로필 이미지 가지고 오는 변수 설정
    LinearLayout loadprofile_area;

    ArrayList<MissionProfileBean> profileBeens = new ArrayList<>();
    MissionProfileBean profileBean;
    ProfileThread profileThread;

    TextView profile_totalnum;

    //xml로 미션 내용을 가져와서 변경 시킬 변수
    ArrayList<String> contents = new ArrayList<>();
    TextView title_text;
    TextView footnote_text;

    //미션정보보기 버튼 변수
    Button missioninfo_btn;

    //review_dialog laouy 변수설정
    ImageView review_profile;
    TextView review_nick;
    Button review_canclebtn;
    Button review_savebtn;
    EditText review_edittext;

    //review 작성버튼 다이얼로그 띄우기
    AlertDialog alertDialog;

    //mssioninfo_dialog
    Button minfo_close_btn;
    TextView minfo_title;
    TextView minfo_contents;

    AlertDialog minfoDialog;

    public RequestManager mGlideRequestManager;

    //프로필 전체보기 클릭하면 2017-07-08 추가
    TextView clicktotal_profile;

    //당겨서 새로고침
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGlideRequestManager = Glide.with(this);

        //DetailActivity 전역변수 접근
        tableNum = ((DetailActivity) getActivity()).tableNum;

        //리스트 불러올 때 프로그레스다이알로그 만들기
        dialog = new ProgressDialog(getContext());
        dialog.setMessage("이미지를 불러오는 중입니다.");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        //review_dialog layout 객체 생성
        LayoutInflater inflater = getActivity().getLayoutInflater();
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.review_dialog, null);

        review_profile = (ImageView) layout.findViewById(R.id.review_profile);
        review_nick = (TextView) layout.findViewById(R.id.review_nick);
        review_edittext = (EditText) layout.findViewById(R.id.review_edittext);
        review_canclebtn = (Button) layout.findViewById(R.id.review_cancelbtn);
        review_savebtn = (Button) layout.findViewById(R.id.review_savebtn);

        review_canclebtn.setOnClickListener(reviewCloseListener);
        review_savebtn.setOnClickListener(reviewSaveListener);

        //AlertDialog 객체생성
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout);
        alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);//키보드 올라오면 스크롤바 주기

        //minfo_dialog layout객체 생성
        RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.missioninfo_dialog, null);
        minfo_close_btn = (Button) relativeLayout.findViewById(R.id.minfo_close_btn);
        minfo_title = (TextView) relativeLayout.findViewById(R.id.minfo_title);
        minfo_contents = (TextView) relativeLayout.findViewById(R.id.minfo_contents);

        minfo_close_btn.setOnClickListener(infoCloseListener);

        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity(), R.style.Theme_TransparentBackground);
        builder1.setView(relativeLayout);
        minfoDialog = builder1.create();


    }//**********************************  end of onCreate() ************************************//

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag1_detail, container, false);
        thumImgContainer = (LinearLayout) view.findViewById(R.id.layout_linear);//썸네일로 보여줄 이미지 레이아웃객체생성

        galleryThread = new ThumGalleryThread();
        galleryThread.start();

        profileThread = new ProfileThread();
        profileThread.start();

        //리스트 뷰 불러오기
        loadReviewThread = new LoadReviewThread();
        loadReviewThread.start();

        //리뷰작성버튼 객체 만들기
        review_btn = (Button) view.findViewById(R.id.review_btn);
        review_btn.setOnClickListener(listener);

        //리뷰토탈넘 실시간으로 가져오기.
        reviewTotalNum = (TextView) view.findViewById(R.id.review_totalnum);
        reviewTotalNum.setText(reviewBeen.size() + "");

        //리뷰전체보기 클릭
        clicktotal_review = (TextView) view.findViewById(R.id.clicktotal_review);
        clicktotal_review.setOnClickListener(listener01);

        listView_review = (ListView) view.findViewById(R.id.listview_review);//리뷰 리스트뷰 객체 생성
        listAdapter = new ReviewListAdapter(reviewBeen, inflater, mGlideRequestManager);
        listView_review.setAdapter(listAdapter);

        //리스트뷰 클릭하면 개인프로필로 이동
        listView_review.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                String id = reviewBeen.get(position).getId();
                int isPrivate = reviewBeen.get(position).getIsPrivate();
                //아이디 값을 가지고 프로필 클릭한 유저의 화면을 이동
                if (G.mem_id.equals(id)) {//내 아이디와 클릭한 아이디가 같은면 토스트
                    Toast.makeText(getActivity(), "본인 프로필은 내 활동에서\n확인하실 수 있습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), AnotherUserActivity.class);
                    intent.putExtra("ID", id);
                    intent.putExtra("IsPrivate", isPrivate);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                    startActivity(intent);
                }
            }
        });

        //리뷰 롱클릭 삭제
        listView_review.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {

                if (G.mem_id.equals(reviewBeen.get(position).getId())) {//아이디가 같으면
                    Toast.makeText(getContext(), "리뷰는 삭제하실 수 없습니다.", Toast.LENGTH_SHORT).show();
                } //end of if(해당 댓글 리스트에 position의 아이디와 내 아이디가 같으면 발동
                return true;
            }
        });

        //미션을 수행한 대원들 프로필 가지고 오는 영역 객체생성
        loadprofile_area = (LinearLayout) view.findViewById(R.id.loadprofile_area);
        profile_totalnum = (TextView) view.findViewById(R.id.profile_totalnum); //프로필 총 개수

        //DetailPage에 보이는 미션지역이름, 밑에 주석 객체생성
        title_text = (TextView) view.findViewById(R.id.title_text);
        footnote_text = (TextView) view.findViewById(R.id.footnote_text);

        missioninfo_btn = (Button) view.findViewById(R.id.missioninfo_btn);//미션정보보기 버튼 객체생성

        /// /2017-01-08 추가 이 미션을 수행한 대원 전체보기
        clicktotal_profile = (TextView) view.findViewById(R.id.clicktotal_profile);
        clicktotal_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TotalProfileActivity.class);
                intent.putExtra("MissionProfileBean", profileBeens);
                startActivity(intent);
            }
        });

        //이 미션을 수행한 대원 프로필 총 개수를 누르면
        profile_totalnum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TotalProfileActivity.class);
                intent.putExtra("MissionProfileBean", profileBeens);
                startActivity(intent);
            }
        });

        //DeatailPage에 들어가는 내용, title,footnote, 미션정보, xml로 가져오기
        new Thread() {
            @Override
            public void run() {
                String address = G.domain + "xml/mission" + tableNum + ".xml"; //미션번째에 맞게 xml 가져오기
                String no = "no" + column;//몇번째 fragment인지 나태는는 값
                String noTag = "";//파싱하면서 태그이름 저장할 변수
                String tmp = ""; //임시저장변수

                //InputStream을 열어주는 객체 생성
                URL url = null;
                try {
                    url = new URL(address);
                    InputStream is = url.openStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");

                    //InputStreamReader가 읽어드리는 byte값을 xml문서로 읽어들이는 객체 생성
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser xpp = factory.newPullParser();
                    xpp.setInput(isr);

                    xpp.next();
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_DOCUMENT:
                                break;
                            case XmlPullParser.START_TAG:
                                noTag = xpp.getName();
                                if (noTag.equals(no)) {
                                    xpp.next();
                                    tmp = xpp.getText();
                                    String[] tmpArr = tmp.split(";");
                                    for (int i = 0; i < tmpArr.length; i++) {
                                        contents.add(tmpArr[i]);
                                    }
                                }
                                break;
                            case XmlPullParser.TEXT:
                                break;
                            case XmlPullParser.END_TAG:
                                break;
                            case XmlPullParser.END_DOCUMENT:
                                break;
                        }//end of switch;
                        eventType = xpp.next();
                    }//end of while

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            title_text.setText(contents.get(0).trim());
                            footnote_text.setText(contents.get(1).trim());
                            missioninfo_btn.setOnClickListener(missioninfoListener);
                        }
                    });

                } catch (Exception e) {

                }
            }


        }.start();


        //당겨서 새로고침
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.layout_swipe_frag);
        mSwipeRefreshLayout.setOnRefreshListener(SwipeListener);

        return view;

    }//************************** end of onCreateView() **********************************//

    //당겨서 새로고침 리스너
    SwipeRefreshLayout.OnRefreshListener SwipeListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            ((DetailActivity) getActivity()).loadFragment();

        }
    };//end of 당겨서 새로고침

    class ThumGalleryThread extends Thread {//서버에서 해당 미션수행한사진 가져오기 //썸네일로 보여주는 이미지 (비회원/회원 동일하게 보인다)
        String serverUrl = G.domain + "php/detailgallery.php";

        @Override
        public void run() {
            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);

                String data = "column=" + "imgbtn" + column + "&tableNum=" + tableNum + "&check=" + "check" + column + "&text=" + "text" + column;
                OutputStream os = connection.getOutputStream();
                os.write(data.getBytes());

                os.flush();
                os.close();

                //서버로부터 오는 echo를 읽어오기
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
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
                String[] rows = buffer.toString().split(";");
                mImageBeens.clear();

                for (String row : rows) {
                    String img = row.split("&")[0];
                    String text = row.split("&")[1];
                    mImageBeens.add(new MImageBean(img, text));
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        inflateThumbnails();
                    }
                });

                is.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            dialog.dismiss();

        }
    } //*************************** end of ThumGalleryThread class ****************************///

    private void inflateThumbnails() {
        for (int i = 0; i < mImageBeens.size(); i++) {
            View imageLayout = getActivity().getLayoutInflater().inflate(R.layout.item_thumbimg, null);
            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.img_thumb);
            imageView.setOnClickListener(onChagePageClickListener(i));

            mGlideRequestManager.load(G.domain + mImageBeens.get(i).getImg()).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);


            thumImgContainer.addView(imageLayout);
        }
    } //end of inflateThumbnails()

    //썸네일 img클릭했을 때 화면 보이기
    View.OnClickListener onChagePageClickListener(final int i) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), GalleryActivity.class);
                intent.putExtra("mImageBeens", mImageBeens); //17/02/25 수정
                intent.putExtra("position", i);
                startActivity(intent);

            }
        };
    }//end of onChagePageClickListener

    //review를 가지고 오는 쓰레드
    class LoadReviewThread extends Thread {
        String serverUrl = G.domain + "php/review/load_review.php";

        @Override
        public void run() {
            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);

                String data = "column=" + "review" + column + "&tableNum=" + tableNum;
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
                reviewBeen.clear();

                for (String row : rows) {
                    String profileImg = row.split("&")[0];
                    String nickName = row.split("&")[1];
                    String textArea = row.split("&")[2];
                    String date = row.split("&")[3];
                    String id = row.split("&")[4];
                    int isPrivate = Integer.parseInt(row.split("&")[5]);

                    //2016-12-04수정 date sns식으로 보이게 하기
                    //sns에서 자주보이는 현재 보여지는 글이 쓰여진 시점부 지금까지의 시간을 표현하는 방법
                    String stirngDate = date;
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date parseDate = format.parse(stirngDate);
                        String mag = DateParse.formatTimeString(parseDate);
                        reviewBeen.add(new ReviewBean(profileImg, nickName, textArea, mag, id, isPrivate));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setListViewHeightBasedOnChildren(listView_review); //리스트뷰 높이 결정
                        listAdapter.notifyDataSetChanged();
                        reviewTotalNum.setText(reviewBeen.size() + "");
                    }
                });
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }//end of run() 리뷰를 가지고 오는 쓰레드
    }//**************************** end of ListReview class ***********************************//

    //리뷰작성하기 버튼을 클릭하면 발생
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (G.isLogin == true) { //로그인된 회원
                //Thread생성 후  아이디 값으로 멤버테이블에서 닉네임가 프로필 사진을 가지고  온다.
                new Thread() {
                    @Override
                    public void run() {
                        String serverUrl = G.domain + "php/review/load_nick_profile.php";

                        try {
                            URL url = new URL(serverUrl);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setDoOutput(true);
                            connection.setDoInput(true);
                            connection.setUseCaches(false);

                            String data = "id=" + G.mem_id;
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
                            final String[] rows = buffer.toString().split("&");

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alertDialog.show();
                                    mGlideRequestManager.load(G.domain + rows[0].trim()).bitmapTransform(new CropCircleTransformation(getActivity()))
                                            .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(review_profile);//프로필사진
                                    review_nick.setText(rows[1].trim() + "님의 리뷰");//닉네임*/
                                }
                            });

                        } catch (Exception e) {

                        }
                    }//end of run()
                }.start();

            } else { //비로그인 회원
                //다이얼로그로 바꾸기!!!!!
                new AlertDialog.Builder(getActivity()).setMessage("로그인이 필요합니다. 로그인 하시겠습니까?").setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //로그인 페이지로 이동
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
                        getActivity().finish();
                    }
                }).setNegativeButton("다음에", null).create().show();
            }
        }
    };//listener

    //AlertDialog 리뷰닫기 버튼
    View.OnClickListener reviewCloseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            review_edittext.setText("");
            alertDialog.dismiss();
        }
    };

    //AlertDialog 리뷰저장 버튼
    View.OnClickListener reviewSaveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            new Thread() {//리뷰저장쓰레드
                @Override
                public void run() {
                    String reviewText = review_edittext.getText().toString().trim();
                    if (reviewText.equals("")) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "리뷰를 입력해 주세요!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else { //review_edittext의 내용을 가지고 와서 서버db에 저장시키기
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //리스트 불러올 때 프로그레스다이알로그 만들기
                                dialog = new ProgressDialog(getContext());
                                dialog.setMessage("리뷰를 저장 중입니다.");
                                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.show();
                            }
                        });

                        String serverUrl = G.domain + "php/review/write_review.php";

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
                            dos.writeBytes("Content-Disposition: form-data; name=\"column\"\r\n\r\n" + "review" + column);

                            dos.writeBytes("\r\n--*****\r\n");
                            dos.writeBytes("Content-Disposition: form-data; name=\"tableNum\"\r\n\r\n" + tableNum);

                            dos.writeBytes("\r\n--*****\r\n");
                            dos.writeBytes("Content-Disposition: form-data; name=\"reviewText\"\r\n\r\n" + URLEncoder.encode(reviewText, "utf-8"));

                            dos.flush();

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
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    review_edittext.setText("");
                                    alertDialog.dismiss();
                                    dialog.dismiss();

                                    ((DetailActivity) getActivity()).loadFragment();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }//end of if ~ esle
                }//end of run()
            }.start();
        }//end of onClick
    };//end of listener

    //리뷰전체보기 클릭하면 발생
    View.OnClickListener listener01 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), TotalReviewActivity.class);
            intent.putExtra("reviewBeen", reviewBeen);
            Bundle bundle = ActivityOptions.makeCustomAnimation(getActivity(), R.anim.anim_slide_in_bottom, R.anim.anim_transparent).toBundle();
            startActivity(intent, bundle);
        }
    };

    //이 미션을 수행한 회원들 영역에 회원들 프로필 이미지를 서버에서 가져올 쓰레드
    class ProfileThread extends Thread {

        @Override
        public void run() {

            String serverUrl = G.domain + "php/loadprofile.php";

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);

                String data = "column=" + column + "&tableNum=" + tableNum; //2017-01-08 수정 컬럼 숫자만 넘김
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
                String[] rows = buffer.toString().split(";");
                profileBeens.clear();

                for (String row : rows) {

                    try {
                        String id = row.split("&")[0];
                        String profile = row.split("&")[1];
                        int isPrivate = Integer.parseInt(row.split("&")[2]);
                        String nick = row.split("&")[3];
                        String imgAddr = row.split("&")[4];

                        profileBeens.add(new MissionProfileBean(id, profile, isPrivate, nick, imgAddr));

                    } catch (IndexOutOfBoundsException e) {

                    }

                }//end of for

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        inflateProfiles();
                    }
                });

                is.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }//end of run

    }// ******************************* end of profileThread **************************************

    private void inflateProfiles() {
        if (profileBeens.size() > 6) { //화면에 총 6개의 프로필만 보여주기
            for (int i = 0; i < 6; i++) {
                View imageLayout = getActivity().getLayoutInflater().inflate(R.layout.item_loadprofileimg, null);
                ImageView imageView = (ImageView) imageLayout.findViewById(R.id.img_loadprofile);

                imageView.setOnClickListener(onProfileClickListener(i));//프로필 이미지 클릭하면 사용자 프로필 화면 보여주기
                profileBean = profileBeens.get(i);
                mGlideRequestManager.load(G.domain + profileBean.getProfile()).bitmapTransform(new CropCircleTransformation(getActivity()))
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);

                loadprofile_area.addView(imageLayout);
            }
            int num = profileBeens.size() - 6;
            profile_totalnum.setText("+" + num);
        } else {

            for (int i = 0; i < profileBeens.size(); i++) {
                View imageLayout = getActivity().getLayoutInflater().inflate(R.layout.item_loadprofileimg, null);
                ImageView imageView = (ImageView) imageLayout.findViewById(R.id.img_loadprofile);

                imageView.setOnClickListener(onProfileClickListener(i)); //프로필 이미지 클릭하면 사용자 프로필 화면 보여주기
                profileBean = profileBeens.get(i);
                mGlideRequestManager.load(G.domain + profileBean.getProfile()).bitmapTransform(new CropCircleTransformation(getActivity()))
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);
                loadprofile_area.addView(imageLayout);
            }
            profile_totalnum.setText("0");
        }//end of if

    }//end of inflateProfiles()

    //프로필 이미지를 클릭하면 사용자 프로필 화면 보여줄
    View.OnClickListener onProfileClickListener(final int positon) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileBean = profileBeens.get(positon);
                String id = profileBean.getId();
                int isprivate = profileBean.getIsPrivate();
                //아이디 값을 가지고 프로필 클릭한 유저의 화면을 이동
                if (G.mem_id.equals(id)) {//내 아이디와 클릭한 아이디가 같은면 내 활동으로 이동
                    Toast.makeText(getActivity(), "본인 프로필은 내 활동에서\n확인하실 수 있습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), AnotherUserActivity.class);
                    intent.putExtra("ID", id);
                    intent.putExtra("IsPrivate", isprivate);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                    startActivity(intent);
                }
            }
        };
    }//미션수행 대원 프로필이미지 클릭시 개인정보화면 이동 끝

    //(missioninfo_btn)미션정보보기 버튼 클릭하면 발생하는 리스너
    View.OnClickListener missioninfoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (G.isLogin == true) { //로그인된 회원
                minfo_title.setText(contents.get(0).trim());
                minfo_contents.setText(contents.get(2).trim());
                minfoDialog.show();

            } else { //비로그인 회원
                //다이얼로그로 바꾸기!!!!!
                new AlertDialog.Builder(getActivity()).setMessage("로그인이 필요합니다. 로그인 하시겠습니까?").setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //로그인 페이지로 이동
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
                        getActivity().finish();
                    }
                }).setNegativeButton("다음에", null).create().show();
                //Toast.makeText(getContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show();

            }

        }
    };//end of listener
    View.OnClickListener infoCloseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            minfoDialog.dismiss();
        }
    };

    //리스트뷰 Height 결정시켜주는 메서드
    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 150; // << 이 값으로 마지막 리스트뷰의 BOTTOM을 띄울수 있다.
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        if (reviewBeen.size() >= 5) { //리뷰가 5개가 넘어가면 리뷰 5개만 보이게 하기

            for (int i = 0; i < 4; i++) {
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                totalHeight += listItem.getMeasuredHeight();
            }

        } else {

            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                totalHeight += listItem.getMeasuredHeight();
            }

        }


        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }//end of setListViewHeightBasedOnChildren // listview Hieght 결정

}
