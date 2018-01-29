package com.missionofseoul.seoul.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.NoticeDeatilActivity;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.model.NoticeBean;

import java.util.ArrayList;

/**
 * Created by hyunho on 2017-02-23.
 */

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.Holder> {

    ArrayList<NoticeBean> noticeBeens;
    LayoutInflater inflater;
    public RequestManager mGlideRequestManager;

    NoticeBean noticeBean;
    Context mContext;


    public EventRecyclerAdapter(ArrayList<NoticeBean> noticeBeens, LayoutInflater inflater, RequestManager mGlideRequestManager) {
        this.noticeBeens = noticeBeens;
        this.inflater = inflater;
        this.mGlideRequestManager = mGlideRequestManager;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.event_recycler, parent, false);
        mContext = view.getContext();

        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        noticeBean = noticeBeens.get(position);
        mGlideRequestManager.load(G.domain + noticeBean.getMainimg()).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.mainimg);//메인 이미지


    }

    @Override
    public int getItemCount() {
        return noticeBeens.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        ImageView mainimg;

        public Holder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getLayoutPosition();

                    Intent intent = new Intent(mContext, NoticeDeatilActivity.class);
                    intent.putExtra("noticeBeens", noticeBeens); //
                    intent.putExtra("position", position);
                    mContext.startActivity(intent);
                }
            });

            mainimg = (ImageView) itemView.findViewById(R.id.mainimg);
        }//end of 생성자


    }//end of innerClass

}//end of EventRecyclerAdapter
