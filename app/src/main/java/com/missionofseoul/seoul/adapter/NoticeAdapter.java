package com.missionofseoul.seoul.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.model.NoticeBean;

import java.util.ArrayList;

/**
 * Created by hyunho on 2016-12-11.
 */

public class NoticeAdapter extends BaseAdapter {

    ArrayList<NoticeBean> noticeBeens;
    LayoutInflater inflater;
    public RequestManager mGlideRequestManager;

    public NoticeAdapter(ArrayList<NoticeBean> noticeBeens, LayoutInflater inflater, RequestManager mGlideRequestManager) {
        this.noticeBeens = noticeBeens;
        this.inflater = inflater;
        this.mGlideRequestManager = mGlideRequestManager;
    }

    @Override
    public int getCount() {
        return noticeBeens.size();
    }

    @Override
    public Object getItem(int position) {
        return noticeBeens.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_notice, parent, false);
        }

        TextView notice_title = (TextView) convertView.findViewById(R.id.notice_title);
        NoticeBean noticeBean = noticeBeens.get(position);
        notice_title.setText(noticeBean.getTitle());

        ImageView notice_icon = (ImageView) convertView.findViewById(R.id.notice_icon);
        int label = noticeBean.getLabel();

        if (label == 2) {
            notice_icon.setImageResource(R.drawable.event_icon);
        } else {
            notice_icon.setImageResource(R.drawable.notice_icon);
        }

        return convertView;
    }


}
