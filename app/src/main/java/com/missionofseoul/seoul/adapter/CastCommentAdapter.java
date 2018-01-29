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
 * Created by hyunho on 2016-12-03.
 */

public class CastCommentAdapter extends BaseAdapter {

    ArrayList<CastCommentBean> commentBeen;
    LayoutInflater inflater;

    public RequestManager mGlideRequestManager;

    public CastCommentAdapter(ArrayList<CastCommentBean> commentBeen, LayoutInflater inflater, RequestManager requestManager) {
        this.commentBeen = commentBeen;
        this.inflater = inflater;
        mGlideRequestManager = requestManager;
    }

    @Override

    public int getCount() {
        return commentBeen.size();
    }

    @Override
    public Object getItem(int position) {
        return commentBeen.get(position);
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

        ImageView profile_img = (ImageView) convertView.findViewById(R.id.profile_img);
        TextView nick_text = (TextView) convertView.findViewById(R.id.nick_text);
        TextView comment_text = (TextView) convertView.findViewById(R.id.comment_textarea);
        TextView date = (TextView) convertView.findViewById(R.id.date_text);
        TextView recomment_num = (TextView) convertView.findViewById(R.id.recomment_num_text);


        CastCommentBean castCommentBean = commentBeen.get(position);

        mGlideRequestManager.load(G.domain + castCommentBean.getProfileImg()).bitmapTransform(new CropCircleTransformation(convertView.getContext()))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(profile_img);
        nick_text.setText(castCommentBean.getNickName());
        comment_text.setText(castCommentBean.getCommentArea());
        recomment_num.setText("댓글 " + castCommentBean.getRecommentNum() + "개");

        String stirngDate = castCommentBean.getDate();
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


}//end of class
