package com.missionofseoul.seoul.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.model.FindFriendBean;

import java.util.ArrayList;

/**
 * Created by hyunho on 2016-12-08.
 */

public class FindFriendAdapter extends BaseAdapter {

    ArrayList<FindFriendBean> friendBeens;
    LayoutInflater inflater;
    public RequestManager mGlideRequestManager;

    public FindFriendAdapter(ArrayList<FindFriendBean> friendBeens, LayoutInflater inflater, RequestManager mGlideRequestManager) {
        this.friendBeens = friendBeens;
        this.inflater = inflater;
        this.mGlideRequestManager = mGlideRequestManager;
    }

    @Override
    public int getCount() {
        return friendBeens.size();
    }

    @Override
    public Object getItem(int position) {
        return friendBeens.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_findfriend, parent, false);
        }

        TextView continet = (TextView) convertView.findViewById(R.id.continent_text);
        TextView nation = (TextView) convertView.findViewById(R.id.nation_text);
        TextView nickName = (TextView) convertView.findViewById(R.id.nick_text);
        TextView title = (TextView) convertView.findViewById(R.id.title_text);
        TextView date = (TextView) convertView.findViewById(R.id.date_text);
        TextView views_num = (TextView) convertView.findViewById(R.id.views_num);
        TextView comment_num = (TextView) convertView.findViewById(R.id.comment_num);

        try { //당겨서 새로고침 할때 새롭게 리스트 가져올때 IndexOutOfBoundsException 발생
            FindFriendBean friendBean = friendBeens.get(position);
            continet.setText(friendBean.getContinent());
            nation.setText(friendBean.getNation());
            nickName.setText(friendBean.getNickName());
            title.setText(friendBean.getTitle());
            date.setText(friendBean.getDate());
            views_num.setText(friendBean.getViews() + "");
            comment_num.setText(friendBean.getComment_num() + "");

        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(convertView.getContext(), e + "", Toast.LENGTH_SHORT).show();
        }


        return convertView;
    }
}
