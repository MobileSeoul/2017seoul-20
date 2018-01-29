package com.missionofseoul.seoul.adapter;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.missionofseoul.seoul.DateParse;
import com.missionofseoul.seoul.DetailActivity;
import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.MainActivity;
import com.missionofseoul.seoul.MissionImageActivity;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.intro.LoginActivity;
import com.missionofseoul.seoul.model.MissionBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by hyunho on 2016-10-25.
 */

public class MissionRecyclerAdapter extends RecyclerView.Adapter<MissionRecyclerAdapter.Holder> {

    Context context;

    int layoutPos;//미션카드 리스트 위치(0부터시작)
    int imgbtnPos; // 미션사진 업로드용 이미지버튼 아이디값
    ImageView imgView;
    LayoutInflater inflater;
    ArrayList<MissionBean> missions;

    MainActivity mainActivity;

    MissionBean mission;

    public RequestManager mGlideRequestManager;

    String[] row;
    double missionLatitude; //미션장소의 위도
    double missionLogitude; //미션장소의 경도

    String imgPath; // MissionImageActivity로 보낼 미션이미지변수

    Intent intent;


    public MissionRecyclerAdapter(LayoutInflater inflater, ArrayList<MissionBean> missions, RequestManager requestManager) {
        this.inflater = inflater;
        this.missions = missions;
        mGlideRequestManager = requestManager;

    }//end of MissionRecyclerAdapter()

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.missionpage_recycler, parent, false);
        mainActivity = (MainActivity) view.getContext();

        Holder holder = new Holder(view);

        return holder;

    }//end of onCreateViewHolder()

    @Override
    public void onBindViewHolder(Holder holder, int position) {

        mission = missions.get(position);

        context = holder.itemView.getContext();


        if (mission.getMission_img().contains("gif")) {
            mGlideRequestManager.load(mission.getMission_img()).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.mission_img);
        } else {
            mGlideRequestManager.load(mission.getMission_img()).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.mission_img);
        }


        mGlideRequestManager.load(mission.getBtn_galleryimg1()).bitmapTransform(new CropCircleTransformation(context)).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.btn_img01);
        mGlideRequestManager.load(mission.getBtn_galleryimg2()).bitmapTransform(new CropCircleTransformation(context)).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.btn_img02);
        mGlideRequestManager.load(mission.getBtn_galleryimg3()).bitmapTransform(new CropCircleTransformation(context)).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.btn_img03);
        mGlideRequestManager.load(mission.getBtn_galleryimg4()).bitmapTransform(new CropCircleTransformation(context)).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.btn_img04);
        mGlideRequestManager.load(mission.getBtn_galleryimg5()).bitmapTransform(new CropCircleTransformation(context)).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.btn_img05);

        holder.mission_subtitle.setText(mission.getSub_title());
        holder.mission_maintitle.setText(mission.getMain_title());
        holder.mission_leveltext.setText(mission.getLevel_text());

        int level = Integer.parseInt(mission.getLevel_star().trim());
        switch (level) {
            case 1:
                mGlideRequestManager.load("").placeholder(R.drawable.level_star1).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.level_star);
                break;
            case 2:
                mGlideRequestManager.load("").placeholder(R.drawable.level_star2).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.level_star);
                break;
            case 3:
                mGlideRequestManager.load("").placeholder(R.drawable.level_star3).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.level_star);
                break;
            case 4:
                mGlideRequestManager.load("").placeholder(R.drawable.level_star4).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.level_star);
                break;
            case 5:
                mGlideRequestManager.load("").placeholder(R.drawable.level_star5).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.level_star);
                break;

        }//end of switch

        if (mission.getIsCompleted() == 5) {
            holder.stamp.setImageResource(mission.getStamp());
            holder.stamp.setVisibility(View.VISIBLE);
        } else {
            holder.stamp.setVisibility(View.INVISIBLE);
        }

        //newicon 이미지 보이게 하기
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String iconDate1 = mission.getNewIcon1();
        String iconDate2 = mission.getNewIcon2();
        String iconDate3 = mission.getNewIcon3();
        String iconDate4 = mission.getNewIcon4();
        String iconDate5 = mission.getNewIcon5();

        // 이미지버튼 1번 newicon
        if (!iconDate1.equals("0")) {
            try {
                Date parseDate = format.parse(iconDate1);
                String msg = DateParse.newIconParse(parseDate);
                int date = Integer.parseInt(msg);
                if (date < 2) { // 이미지 업로드 한지 2틀이 지나지 않았으면 new 아이콘을 보여주기
                    holder.newicon1.setVisibility(View.VISIBLE);
                } else {
                    holder.newicon1.setVisibility(View.INVISIBLE);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
            holder.newicon1.setVisibility(View.INVISIBLE);
        }//이미지 버튼 1번 newicon 끝


        // 이미지버튼 2번 newicon
        if (!iconDate2.equals("0")) { //변경부분
            try {
                Date parseDate = format.parse(iconDate2); //변경부분
                String msg = DateParse.newIconParse(parseDate);
                int date = Integer.parseInt(msg);
                if (date < 2) { // 이미지 업로드 한지 2틀이 지나지 않았으면 new 아이콘을 보여주기
                    holder.newicon2.setVisibility(View.VISIBLE); //변경부분
                } else {
                    holder.newicon2.setVisibility(View.INVISIBLE); //변경부분
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
            holder.newicon2.setVisibility(View.INVISIBLE);//변경부분
        }//이미지 버튼 2번 newicon 끝

        // 이미지버튼 3번 newicon
        if (!iconDate3.equals("0")) { //변경부분
            try {
                Date parseDate = format.parse(iconDate3); //변경부분
                String msg = DateParse.newIconParse(parseDate);
                int date = Integer.parseInt(msg);
                if (date < 2) { // 이미지 업로드 한지 2틀이 지나지 않았으면 new 아이콘을 보여주기
                    holder.newicon3.setVisibility(View.VISIBLE); //변경부분
                } else {
                    holder.newicon3.setVisibility(View.INVISIBLE); //변경부분
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            holder.newicon3.setVisibility(View.INVISIBLE);//변경부분
        }//이미지 버튼 3번 newicon 끝

        // 이미지버튼 4번 newicon
        if (!iconDate4.equals("0")) { //변경부분
            try {
                Date parseDate = format.parse(iconDate4); //변경부분
                String msg = DateParse.newIconParse(parseDate);
                int date = Integer.parseInt(msg);
                if (date < 2) { // 이미지 업로드 한지 2틀이 지나지 않았으면 new 아이콘을 보여주기
                    holder.newicon4.setVisibility(View.VISIBLE); //변경부분
                } else {
                    holder.newicon4.setVisibility(View.INVISIBLE); //변경부분
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
            holder.newicon4.setVisibility(View.INVISIBLE);//변경부분
        }//이미지 버튼 4번 newicon 끝


        // 이미지버튼 5번 newicon
        if (!iconDate5.equals("0")) { //변경부분
            try {
                Date parseDate = format.parse(iconDate5); //변경부분
                String msg = DateParse.newIconParse(parseDate);
                int date = Integer.parseInt(msg);
                if (date < 2) { // 이미지 업로드 한지 2틀이 지나지 않았으면 new 아이콘을 보여주기
                    holder.newicon5.setVisibility(View.VISIBLE); //변경부분
                } else {
                    holder.newicon5.setVisibility(View.INVISIBLE); //변경부분
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            holder.newicon5.setVisibility(View.INVISIBLE);//변경부분
        }//이미지 버튼 5번 newicon 끝


    }//end of onBindViewHolder()

    @Override
    public int getItemCount() {
        return missions.size();
    }//end of getItemCount()

    /****************************************************************************************
     * inner class
     *****************************************************************************************/

    //재활용할 View객체들의 참조값을 저장할 inner클래스
    class Holder extends RecyclerView.ViewHolder {

        ImageView mission_img;

        ImageView stamp;

        ImageView btn_img01;
        ImageView btn_img02;
        ImageView btn_img03;
        ImageView btn_img04;
        ImageView btn_img05;

        TextView mission_subtitle;
        TextView mission_maintitle;
        TextView mission_leveltext;

        ImageView level_star;

        //미션숨기기
        FrameLayout layout_missionhidden;

        //미션 이미지 업로드시 New 이미지 보이기
        ImageView newicon1;
        ImageView newicon2;
        ImageView newicon3;
        ImageView newicon4;
        ImageView newicon5;

        //itemView는 기존 convertView와 용도가 같다.
        public Holder(View itemView) {
            super(itemView);

            //RecyclerView의 CardView를 미션카드를 클릭했을 때 발생되는 리스너
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(v.getContext(), DetailActivity.class);
                    int pos = getLayoutPosition();
                    mission = missions.get(pos);
                    intent.putExtra("MissionPosition", mission.getLayoutPos());
                    mainActivity.startActivity(intent);

                }
            });

            mission_img = (ImageView) itemView.findViewById(R.id.mission_img);
            stamp = (ImageView) itemView.findViewById(R.id.stamp);

            btn_img01 = (ImageView) itemView.findViewById(R.id.btn_img01);
            btn_img02 = (ImageView) itemView.findViewById(R.id.btn_img02);
            btn_img03 = (ImageView) itemView.findViewById(R.id.btn_img03);
            btn_img04 = (ImageView) itemView.findViewById(R.id.btn_img04);
            btn_img05 = (ImageView) itemView.findViewById(R.id.btn_img05);

            btn_img01.setOnClickListener(listener);
            btn_img02.setOnClickListener(listener);
            btn_img03.setOnClickListener(listener);
            btn_img04.setOnClickListener(listener);
            btn_img05.setOnClickListener(listener);

            mission_subtitle = (TextView) itemView.findViewById(R.id.mission_subtitle);
            mission_maintitle = (TextView) itemView.findViewById(R.id.mission_maintitle);
            mission_leveltext = (TextView) itemView.findViewById(R.id.mission_leveltext);

            level_star = (ImageView) itemView.findViewById(R.id.level_star);

            //미션숨기기
            layout_missionhidden = (FrameLayout) itemView.findViewById(R.id.layout_missionhidden);

            layout_missionhidden.setOnClickListener(hiddenListenr);

            //new icon
            newicon1 = (ImageView) itemView.findViewById(R.id.newicon1);
            newicon2 = (ImageView) itemView.findViewById(R.id.newicon2);
            newicon3 = (ImageView) itemView.findViewById(R.id.newicon3);
            newicon4 = (ImageView) itemView.findViewById(R.id.newicon4);
            newicon5 = (ImageView) itemView.findViewById(R.id.newicon5);


        }//*************************** 생성자 끝 ***************************************//

        //여행의 미션 미션이미지버튼을 클릭 했을 때 발동
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imgView = (ImageView) v;

                int pos = getLayoutPosition(); //해당위치레이아웃의
                mission = missions.get(pos); //ArrayList의 값을 가져온다
                layoutPos = mission.getLayoutPos();//MissionBean에 저장된 포지션값을 가져와서 저장
                //미션의 위도 경도 가져오기
                String location = mission.getLocation();
                String[] rows = location.split("#");

                switch (v.getId()) {  //미션사진업로드 이미지버튼 아이디값을 번호값으로 바꿈

                    case R.id.btn_img01:
                        imgbtnPos = 1;

                        row = rows[0].split(",");
                        missionLatitude = new Double(row[0]);//위도
                        missionLogitude = new Double(row[1]);//경도

                        imgPath = mission.getBtn_galleryimg1(); // 미션이미지버튼

                        intent = new Intent(context, MissionImageActivity.class);
                        intent.putExtra("LayoutPos", layoutPos); // 몇번째 미션인지
                        intent.putExtra("ImgbtnPos", imgbtnPos); // 몇번째 미션버튼인지
                        intent.putExtra("Latitude", missionLatitude); //미션위도
                        intent.putExtra("Logitude", missionLogitude);//미션경도
                        intent.putExtra("ImgPath", imgPath); // 전체화면으로 보여줄 미션이미지버튼
                        mainActivity.startActivity(intent);
                        mainActivity.overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);

                        break;
                    case R.id.btn_img02:
                        imgbtnPos = 2;

                        row = rows[1].split(",");
                        missionLatitude = new Double(row[0]);
                        missionLogitude = new Double(row[1]);

                        imgPath = mission.getBtn_galleryimg2(); // 미션이미지버튼

                        intent = new Intent(context, MissionImageActivity.class);
                        intent.putExtra("LayoutPos", layoutPos); // 몇번째 미션인지
                        intent.putExtra("ImgbtnPos", imgbtnPos); // 몇번째 미션버튼인지
                        intent.putExtra("Latitude", missionLatitude); //미션위도
                        intent.putExtra("Logitude", missionLogitude);//미션경도
                        intent.putExtra("ImgPath", imgPath); // 전체화면으로 보여줄 미션이미지버튼
                        mainActivity.startActivity(intent);
                        mainActivity.overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);

                        break;
                    case R.id.btn_img03:
                        imgbtnPos = 3;

                        row = rows[2].split(",");
                        missionLatitude = new Double(row[0]);
                        missionLogitude = new Double(row[1]);

                        imgPath = mission.getBtn_galleryimg3(); // 미션이미지버튼

                        intent = new Intent(context, MissionImageActivity.class);
                        intent.putExtra("LayoutPos", layoutPos); // 몇번째 미션인지
                        intent.putExtra("ImgbtnPos", imgbtnPos); // 몇번째 미션버튼인지
                        intent.putExtra("Latitude", missionLatitude); //미션위도
                        intent.putExtra("Logitude", missionLogitude);//미션경도
                        intent.putExtra("ImgPath", imgPath); // 전체화면으로 보여줄 미션이미지버튼
                        mainActivity.startActivity(intent);
                        mainActivity.overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);

                        break;
                    case R.id.btn_img04:
                        imgbtnPos = 4;

                        row = rows[3].split(",");
                        missionLatitude = new Double(row[0]);
                        missionLogitude = new Double(row[1]);

                        imgPath = mission.getBtn_galleryimg4(); // 미션이미지버튼

                        intent = new Intent(context, MissionImageActivity.class);
                        intent.putExtra("LayoutPos", layoutPos); // 몇번째 미션인지
                        intent.putExtra("ImgbtnPos", imgbtnPos); // 몇번째 미션버튼인지
                        intent.putExtra("Latitude", missionLatitude); //미션위도
                        intent.putExtra("Logitude", missionLogitude);//미션경도
                        intent.putExtra("ImgPath", imgPath); // 전체화면으로 보여줄 미션이미지버튼
                        mainActivity.startActivity(intent);
                        mainActivity.overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);

                        break;
                    case R.id.btn_img05:
                        imgbtnPos = 5;

                        row = rows[4].split(",");
                        missionLatitude = new Double(row[0]);
                        missionLogitude = new Double(row[1]);

                        imgPath = mission.getBtn_galleryimg5(); // 미션이미지버튼

                        intent = new Intent(context, MissionImageActivity.class);
                        intent.putExtra("LayoutPos", layoutPos); // 몇번째 미션인지
                        intent.putExtra("ImgbtnPos", imgbtnPos); // 몇번째 미션버튼인지
                        intent.putExtra("Latitude", missionLatitude); //미션위도
                        intent.putExtra("Logitude", missionLogitude);//미션경도
                        intent.putExtra("ImgPath", imgPath); // 전체화면으로 보여줄 미션이미지버튼
                        mainActivity.startActivity(intent);
                        mainActivity.overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);

                        break;
                }

            }//onClick()

        };//end of OnClickListener

        //미션에 있는 미션숨기기 버튼을 클릭하면 발행 리스너
        View.OnClickListener hiddenListenr = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //다이얼로그 창 띄우기 해당 포지션 번째 값 가져오기
                if (G.isLogin == true) { //로그인된 회원
                    new AlertDialog.Builder(context).setMessage("미션을 숨기시겠습니까?").setPositiveButton("숨기기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //미션 숨기기
                            int pos = getLayoutPosition(); //해당위치레이아웃의
                            mission = missions.get(pos); //ArrayList의 값을 가져온다
                            layoutPos = mission.getLayoutPos();//MissionBean에 저장된 포지션값을 가져와서 저장

                            //
                            G.missionPosList.clear();

                            //기존에 숨긴 미션이 있다면
                            G.HiddenList = G.getStringArrayPref(mainActivity, "Hidden");//

                            if (G.HiddenList.size() != 0) {
                                for (int i = 0; i < G.HiddenList.size(); i++) {
                                    G.missionPosList.add(G.HiddenList.get(i)); //기존에 숨긴 미션을 저장
                                }
                                G.missionPosList.add(layoutPos + "");//현재 클릭한 미션 추가
                                Collections.sort(G.missionPosList); // 오름차순으로 정렬

                                G.setStringArrayPref(mainActivity, "Hidden", G.missionPosList);
                                Toast.makeText(context, "'더보기'에서 숨긴 미션을 취소하실 수 있습니다.", Toast.LENGTH_SHORT).show();
                                mainActivity.finish();
                                mainActivity.startActivity(mainActivity.getIntent());
                                mainActivity.overridePendingTransition(0, 0);//<< activity전환시 깜빡임을 없애 주었다

                            } else { //기존에 미션이 없다면
                                G.missionPosList.add(layoutPos + ""); //현재 클릭한 미션 추가

                                G.setStringArrayPref(mainActivity, "Hidden", G.missionPosList);
                                Toast.makeText(context, "'더보기'에서 숨긴 미션을 취소하실 수 있습니다.", Toast.LENGTH_SHORT).show();
                                mainActivity.finish();
                                mainActivity.startActivity(mainActivity.getIntent());
                                mainActivity.overridePendingTransition(0, 0);//<< activity전환시 깜빡임을 없애 주었다
                            }

                        }//end of onClick()
                    }).setNegativeButton("아니요", null).create().show();

                } else { //비로그인 회원
                    new AlertDialog.Builder(context).setMessage("로그인이 필요합니다. 로그인 하시겠습니까?").setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //로그인 페이지로 이동
                            Intent intent = new Intent(context, LoginActivity.class);
                            context.startActivity(intent);
                            mainActivity.overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
                            //mainActivity.finish();
                        }
                    }).setNegativeButton("다음에", null).create().show();

                }//end of if else

            }//end of onClick
        };//end of hiddenListenr

    }//end of MissionRecyclerAdapter innerclass


}//end of MissionRecyclerAdapter class
