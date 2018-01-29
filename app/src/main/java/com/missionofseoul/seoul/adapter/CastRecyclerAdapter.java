package com.missionofseoul.seoul.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.missionofseoul.seoul.CastDetailActivity;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.model.CastBean;

import java.util.ArrayList;

/**
 * Created by hyunho on 2016-11-24.
 */

public class CastRecyclerAdapter extends RecyclerView.Adapter<CastRecyclerAdapter.Holder> {

    LayoutInflater inflater;
    ArrayList<CastBean> castBeens;
    Context mContext;

    public RequestManager mGlideRequestManager;

    public CastRecyclerAdapter(LayoutInflater inflater, ArrayList<CastBean> castBeens, RequestManager requestManager) {
        this.inflater = inflater;
        this.castBeens = castBeens;
        mGlideRequestManager = requestManager;

    }//end of 생성자

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.travelcast_recycler, parent, false);

        Holder holder = new Holder(view);
        return holder;
    }

    //만들어진 뷰에 값을 설정(bind View)
    @Override
    public void onBindViewHolder(Holder holder, int position) {
        CastBean castBean = castBeens.get(position);
        mContext = holder.itemView.getContext();

        if (castBean.getCast_mainimg().contains("gif")) {
            mGlideRequestManager.load(castBean.getCast_mainimg()).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.titleImg);
        } else {

            mGlideRequestManager.load(castBean.getCast_mainimg()).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.titleImg);
        }
        holder.tilteText.setText(castBean.getCast_title());

    }


    @Override
    public int getItemCount() {
        return castBeens.size();
    }


    class Holder extends RecyclerView.ViewHolder {
        ImageView titleImg;
        TextView tilteText;

        public Holder(View itemView) {
            super(itemView);


            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    int pos = getLayoutPosition();
                    Intent intent = new Intent(v.getContext(), CastDetailActivity.class);
                    intent.putExtra("castBeens", castBeens);
                    intent.putExtra("pos", pos);
                    v.getContext().startActivity(intent);

                }
            });

            titleImg = (ImageView) itemView.findViewById(R.id.casttitle_img);
            tilteText = (TextView) itemView.findViewById(R.id.casttitle_text);

        }

    }


}