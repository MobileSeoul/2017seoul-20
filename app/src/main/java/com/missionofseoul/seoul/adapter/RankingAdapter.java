package com.missionofseoul.seoul.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.model.RankingBean;

import java.text.DecimalFormat;
import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by hyunho on 2017-02-11.
 */

public class RankingAdapter extends BaseAdapter {

    ArrayList<RankingBean> rankingBeens;
    LayoutInflater inflater;
    public RequestManager mGlideRequestManager;


    public RankingAdapter(ArrayList<RankingBean> rankingBeens, LayoutInflater inflater, RequestManager mGlideRequestManager) {
        this.rankingBeens = rankingBeens;
        this.inflater = inflater;
        this.mGlideRequestManager = mGlideRequestManager;
    }

    @Override
    public int getCount() {
        return rankingBeens.size();
    }

    @Override
    public Object getItem(int position) {
        return rankingBeens.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_ranking, parent, false);
        }

        //레이아웃 객체생성
        TextView rankNum = (TextView) convertView.findViewById(R.id.rank_num); // 랭킹 순위 1부터 10까지
        ImageView profile = (ImageView) convertView.findViewById(R.id.profile); //프로필 이미지
        TextView nick = (TextView) convertView.findViewById(R.id.nick); //닉네임
        TextView rate = (TextView) convertView.findViewById(R.id.rate); //미션 수행 비율
        ImageView medal = (ImageView) convertView.findViewById(R.id.medal);//메달 1~3위까지만 붙이가 라른건 빈칸

        ImageView crown = (ImageView) convertView.findViewById(R.id.crwon);//1등한테만 붙여줄것
        ImageView winner = (ImageView) convertView.findViewById(R.id.winner);

        RankingBean rankingBean = rankingBeens.get(position);

        rankNum.setText(position + 1 + ""); //순위
        //프로필
        mGlideRequestManager.load(G.domain + rankingBean.getProfile())
                .bitmapTransform(new CropCircleTransformation(convertView.getContext())).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(profile);
        nick.setText(rankingBean.getNickName());//닉네임

        //미션수행률 계산
        float mRate = ((float) rankingBean.getMissionComplete() / G.totalMission) * 100;
        //DecimalFormat는 소수점 자리 결정 '#'은 '0'을 표시하지 않는다.
        DecimalFormat df = new DecimalFormat("#.#");
        String result = df.format(mRate);
        rate.setText(result + "%");

        //1~3등까지 메달
        if (position == 0) {
            medal.setImageResource(R.drawable.medal1);
        } else if (position == 1) {
            medal.setImageResource(R.drawable.medal2);
        } else if (position == 2) {
            medal.setImageResource(R.drawable.medal3);
        } else {
            medal.setImageResource(R.drawable.blank);
        }

        //1~3등만 왕관
        if (position == 0) {
            crown.setImageResource(R.drawable.crown_1);
        } else if (position == 1) {
            crown.setImageResource(R.drawable.crown_2);
        } else if (position == 2) {
            crown.setImageResource(R.drawable.crown_3);
        } else {
            crown.setImageResource(R.drawable.blank);
        }

        //1등만 훈장
        if (position == 0) {
            winner.setImageResource(R.drawable.winner);
        } else {
            winner.setImageResource(R.drawable.blank);
        }

        return convertView;
    }//end of getView
}
