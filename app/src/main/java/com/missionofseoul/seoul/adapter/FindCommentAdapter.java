package com.missionofseoul.seoul.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.missionofseoul.seoul.DateParse;
import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.model.CastCommentBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by hyunho on 2016-12-10.
 */

public class FindCommentAdapter extends BaseAdapter {

    ArrayList<CastCommentBean> commentBeens;
    LayoutInflater inflater;
    public RequestManager mGlideRequestManager;

    public FindCommentAdapter(ArrayList<CastCommentBean> commentBeens, LayoutInflater inflater, RequestManager mGlideRequestManager) {
        this.commentBeens = commentBeens;
        this.inflater = inflater;
        this.mGlideRequestManager = mGlideRequestManager;
    }

    @Override

    public int getCount() {
        return commentBeens.size();
    }

    @Override
    public Object getItem(int position) {
        return commentBeens.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_castcomment, parent, false);
        }
        //listview_castcomment에 있는 view 객체 생성
        ImageView profile_img = (ImageView) convertView.findViewById(R.id.profile_img);
        TextView nick_text = (TextView) convertView.findViewById(R.id.nick_text);
        TextView comment_text = (TextView) convertView.findViewById(R.id.comment_textarea);
        TextView date = (TextView) convertView.findViewById(R.id.date_text);
        TextView recomment_num = (TextView) convertView.findViewById(R.id.recomment_num_text);

        CastCommentBean commentBean = commentBeens.get(position);

        mGlideRequestManager.load(G.domain + commentBean.getProfileImg()).bitmapTransform(new CropCircleTransformation(convertView.getContext()))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(profile_img);
        nick_text.setText(commentBean.getNickName());
        comment_text.setText(commentBean.getCommentArea());
        recomment_num.setText("댓글 " + commentBean.getRecommentNum() + "개");

        //sns에서 자주보이는 현재 보여지는 글이 쓰여진 시점부 지금까지의 시간을 표현하는 방법
        String stirngDate = commentBean.getDate();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date parseDate = format.parse(stirngDate);
            String mag = DateParse.formatTimeString(parseDate);
            date.setText("" + mag);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return convertView;
    }
}
