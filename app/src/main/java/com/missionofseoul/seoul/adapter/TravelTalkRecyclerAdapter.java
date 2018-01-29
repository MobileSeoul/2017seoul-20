package com.missionofseoul.seoul.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.missionofseoul.seoul.DateParse;
import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.TalkDetailActivity;
import com.missionofseoul.seoul.model.TravelTalkBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;


/**
 * Created by hyunho on 2016-12-13.
 */

public class TravelTalkRecyclerAdapter extends RecyclerView.Adapter<TravelTalkRecyclerAdapter.Holder> {

    ArrayList<TravelTalkBean> travelTalkBeens;
    LayoutInflater inflater;
    public RequestManager mGlideRequestManager;

    TravelTalkBean talkBean;
    Context mContext;

    public TravelTalkRecyclerAdapter(ArrayList<TravelTalkBean> travelTalkBeens, LayoutInflater inflater, RequestManager mGlideRequestManager) {
        this.travelTalkBeens = travelTalkBeens;
        this.inflater = inflater;
        this.mGlideRequestManager = mGlideRequestManager;
    }

    //재활용할 뷰가 없어, 새로운 뷰를 만들어야 할 때 자동 실행. new View
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.traveltalk_recycler, parent, false);
        mContext = view.getContext();
        Holder holder = new Holder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {

        talkBean = travelTalkBeens.get(position);

        mGlideRequestManager.load(G.domain + talkBean.getProfile()).bitmapTransform(new CropCircleTransformation(mContext)).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.profile);//프로필 이미지
        holder.nick.setText(talkBean.getNickName());

        //SNS식 날짜변형
        String date = talkBean.getDate();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date parseDate = format.parse(date);
            String mag = DateParse.formatTimeString(parseDate);
            holder.date.setText(mag);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.contents_text.setText(talkBean.getTalk_text()); // 컨텐츠 텍스트


        ////컨텐츠 이미지
        mGlideRequestManager.load(G.domain + talkBean.getImg0()).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.contents_img);

        holder.likes_num.setText(talkBean.getLikes_num() + "");//좋아요 개수
        holder.comment_num.setText(talkBean.getComment_num() + ""); // 댓글 개수


    }

    @Override
    public int getItemCount() {
        return travelTalkBeens.size();
    }

    /****************************************************************************************
     * inner class
     *****************************************************************************************/

    //재활용할 View객체들의 참조값을 저장할 inner클래스

    class Holder extends RecyclerView.ViewHolder {

        //traveltalk_recycler.xml 변수설정

        ImageView profile;
        TextView nick;
        TextView date;
        TextView contents_text;
        ImageView contents_img;
        TextView likes_num;
        TextView comment_num;

        //


        public Holder(View itemView) {
            super(itemView);
            //RecyclerView의 TravelTalk 컨텐츠를 클릭했을 때 발생되는 리스너 상세페이지로 이동
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //상세페이지로 이동
                    int pos = getLayoutPosition();
                    Intent intent = new Intent(mContext, TalkDetailActivity.class);
                    intent.putExtra("travelTalkBeens", travelTalkBeens);
                    intent.putExtra("pos", pos);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                    mContext.startActivity(intent);
                }
            });

            //layout들 객체생성
            profile = (ImageView) itemView.findViewById(R.id.profile);
            nick = (TextView) itemView.findViewById(R.id.nick);
            date = (TextView) itemView.findViewById(R.id.date);
            contents_text = (TextView) itemView.findViewById(R.id.contents_text);
            contents_img = (ImageView) itemView.findViewById(R.id.contents_img);
            likes_num = (TextView) itemView.findViewById(R.id.likes_num);
            comment_num = (TextView) itemView.findViewById(R.id.comment_num);
            //


        }//end of 생성자

    }//end of innerclass


}
