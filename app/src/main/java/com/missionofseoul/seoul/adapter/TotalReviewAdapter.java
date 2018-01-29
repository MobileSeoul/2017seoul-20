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
import com.missionofseoul.seoul.model.ReviewBean;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by hyunho on 2016-11-18.
 */

public class TotalReviewAdapter extends BaseAdapter {
    ArrayList<ReviewBean> reviewBeen;
    LayoutInflater inflater;

    public RequestManager mGlideRequestManager;

    public TotalReviewAdapter(ArrayList<ReviewBean> reviewBeen, LayoutInflater inflater, RequestManager requestManager) {
        this.reviewBeen = reviewBeen;
        this.inflater = inflater;
        mGlideRequestManager = requestManager;
    }

    @Override

    public int getCount() {
        return reviewBeen.size(); //화면상에 뿌려줄 개수
    }

    @Override
    public Object getItem(int position) {
        return reviewBeen.get(position); //항목(리스트) 하나 하나 get(int)값으로
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_review, parent, false);
        }
        //listview_review에 있는 view들 객체 생성
        ImageView profie_img = (ImageView) convertView.findViewById(R.id.profile_img);
        TextView nick_text = (TextView) convertView.findViewById(R.id.nick_text);
        TextView date_text = (TextView) convertView.findViewById(R.id.date_text);
        TextView reviewArea_text = (TextView) convertView.findViewById(R.id.review_textarea);


        ReviewBean review = reviewBeen.get(position);

        mGlideRequestManager.load(G.domain + review.getProfileImg()).bitmapTransform(new CropCircleTransformation(convertView.getContext())).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(profie_img);
        nick_text.setText(review.getNickName());
        date_text.setText(review.getDate());
        reviewArea_text.setText(review.getTextArea());


        return convertView;
    }
}
