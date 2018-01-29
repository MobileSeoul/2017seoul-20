package com.missionofseoul.seoul.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.missionofseoul.seoul.FindFriendDetailActivity;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.model.FindFriendBean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hyunho on 2017-08-07.
 */

public class FindFriendRecylerAdapter extends RecyclerView.Adapter<FindFriendRecylerAdapter.Holder> {

    List<FindFriendBean> friendBeens;
    LayoutInflater inflater;
    public RequestManager mGlideRequestManager;

    FindFriendBean friendBean;

    public FindFriendRecylerAdapter(List<FindFriendBean> friendBeens, LayoutInflater inflater, RequestManager mGlideRequestManager) {
        this.friendBeens = friendBeens;
        this.inflater = inflater;
        this.mGlideRequestManager = mGlideRequestManager;

    }//end of 생성자()

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.listview_findfriend, parent, false);

        return new Holder(view);
    }//end of onCreateViewHolder()

    @Override
    public void onBindViewHolder(Holder holder, int position) {


        friendBean = friendBeens.get(position);

        holder.continet.setText(friendBean.getContinent());
        holder.nation.setText(friendBean.getNation());
        holder.nickName.setText(friendBean.getNickName());
        holder.title.setText(friendBean.getTitle());
        holder.date.setText(friendBean.getDate());
        holder.views_num.setText(friendBean.getViews() + "");
        holder.comment_num.setText(friendBean.getComment_num() + "");


    }//end of onBindViewHolder();

    @Override
    public int getItemCount() {
        return friendBeens.size();
    }//end of getItemCount()

    public class Holder extends RecyclerView.ViewHolder {

        TextView continet;
        TextView nation;
        TextView nickName;
        TextView title;
        TextView date;
        TextView views_num;
        TextView comment_num;

        public Holder(View itemView) {
            super(itemView);

            continet = (TextView) itemView.findViewById(R.id.continent_text);
            nation = (TextView) itemView.findViewById(R.id.nation_text);
            nickName = (TextView) itemView.findViewById(R.id.nick_text);
            title = (TextView) itemView.findViewById(R.id.title_text);
            date = (TextView) itemView.findViewById(R.id.date_text);
            views_num = (TextView) itemView.findViewById(R.id.views_num);
            comment_num = (TextView) itemView.findViewById(R.id.comment_num);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int position = getLayoutPosition();

                    Intent intent = new Intent(v.getContext(), FindFriendDetailActivity.class);
                    intent.putExtra("friendBeens", (Serializable) friendBeens); //ArrayList<FindFriendBeen>에는 서버에서 가져온 값들이 다 저장되어있다.
                    intent.putExtra("position", position);//리스트뷰 클릭한 위치
                    v.getContext().startActivity(intent);

                }//end of onClick();
            });

        }//end of 생성자()


    }//end of innerClass()


}//end of class
