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
import com.missionofseoul.seoul.model.CastRecommBeen;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by hyunho on 2016-12-04.
 */

public class CastRecommAdapter extends BaseAdapter {

    ArrayList<CastRecommBeen> recommBeens;
    LayoutInflater inflater;

    public RequestManager mGlideRequestManager;


    public CastRecommAdapter(ArrayList<CastRecommBeen> recommBeens, LayoutInflater inflater, RequestManager requestManager) {
        this.recommBeens = recommBeens;
        this.inflater = inflater;
        mGlideRequestManager = requestManager;
    }

    @Override

    public int getCount() {
        return recommBeens.size();
    }

    @Override
    public Object getItem(int position) {
        return recommBeens.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_castrecomm, parent, false);

        }
        ImageView profile_img = (ImageView) convertView.findViewById(R.id.profile_img);
        TextView nick_text = (TextView) convertView.findViewById(R.id.nick_text);
        TextView recomment = (TextView) convertView.findViewById(R.id.recomment_textarea);
        TextView date_text = (TextView) convertView.findViewById(R.id.date_text);

        CastRecommBeen recommBeen = recommBeens.get(position);

        mGlideRequestManager.load(G.domain + recommBeen.getProfileImg()).bitmapTransform(new CropCircleTransformation(convertView.getContext()))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(profile_img);

        nick_text.setText(recommBeen.getNickName());
        recomment.setText(recommBeen.getRecommArea());

        String stirngDate = recommBeen.getDate();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date parseDate = format.parse(stirngDate);
            String mag = DateParse.formatTimeString(parseDate);
            date_text.setText("" + mag);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return convertView;
    }

}
