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
import com.missionofseoul.seoul.model.MissionProfileBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by hyunho on 2017-01-08.
 */

public class TotalProfileAdapter extends BaseAdapter {

    ArrayList<MissionProfileBean> profileBeens;
    LayoutInflater inflater;

    public RequestManager mGlideRequestManager;

    public TotalProfileAdapter(ArrayList<MissionProfileBean> profileBeens, LayoutInflater inflater, RequestManager mGlideRequestManager) {
        this.profileBeens = profileBeens;
        this.inflater = inflater;
        this.mGlideRequestManager = mGlideRequestManager;
    }

    @Override
    public int getCount() {
        return profileBeens.size();
    }

    @Override
    public Object getItem(int position) {
        return profileBeens.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_profile, parent, false);
        }

        ImageView profile = (ImageView) convertView.findViewById(R.id.profile);
        TextView nick = (TextView) convertView.findViewById(R.id.nick);
        TextView missionDate = (TextView) convertView.findViewById(R.id.missiondate);

        MissionProfileBean profileBean = profileBeens.get(position);

        mGlideRequestManager.load(G.domain + profileBean.getProfile()).bitmapTransform(new CropCircleTransformation(convertView.getContext()))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(profile);
        nick.setText(profileBean.getNick());

        String date = profileBean.getImgAddr();
        date = date.substring(18, 26); //  >>  ../images/userimg/20170107192449Screenshot_2017-01-03-04-35-16.png 날짜부분만 가져와 사용

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy년 MM월 dd일");

        // missionDate.setText(date);
        try {

            Date parseDate = sdf1.parse(date);
            date = sdf2.format(parseDate);
            missionDate.setText(date + "에 미션을 수행하셨습니다.");
        } catch (Exception e) {
            e.printStackTrace();
        }


        return convertView;
    }


}
