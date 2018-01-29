package com.missionofseoul.seoul.adapter;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.missionofseoul.seoul.DetailActivity;
import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.model.SearchMissionBean;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropCircleTransformation;


/**
 * Created by hyunho on 2017-01-19.
 */

public class SearchMissionAdapter extends RecyclerView.Adapter<SearchMissionAdapter.Holder> {

    Context context;

    LayoutInflater inflater;
    ArrayList<SearchMissionBean> missions;
    public RequestManager mGlideRequestManager;

    SearchMissionBean missionBean;

    public SearchMissionAdapter(LayoutInflater inflater, ArrayList<SearchMissionBean> missions, RequestManager mGlideRequestManager) {
        this.inflater = inflater;
        this.missions = missions;
        this.mGlideRequestManager = mGlideRequestManager;
    }

    @Override

    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.missionpage_recycler, parent, false);

        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {

        missionBean = missions.get(position);

        context = holder.itemView.getContext();

        //innerclass의 멤버변수의 객체생성 후 사용

        if (missionBean.getMission_img().contains("gif")) {//main이미지타입이 gif면 시작
            mGlideRequestManager.load(G.domain + missionBean.getMission_img()).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.mission_img);
        } else {
            mGlideRequestManager.load(G.domain + missionBean.getMission_img()).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.mission_img);
        }


        mGlideRequestManager.load(G.domain + missionBean.getBtn_galleryimg1()).bitmapTransform(new CropCircleTransformation(context)).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.btn_img01);
        mGlideRequestManager.load(G.domain + missionBean.getBtn_galleryimg2()).bitmapTransform(new CropCircleTransformation(context)).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.btn_img02);
        mGlideRequestManager.load(G.domain + missionBean.getBtn_galleryimg3()).bitmapTransform(new CropCircleTransformation(context)).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.btn_img03);
        mGlideRequestManager.load(G.domain + missionBean.getBtn_galleryimg4()).bitmapTransform(new CropCircleTransformation(context)).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.btn_img04);
        mGlideRequestManager.load(G.domain + missionBean.getBtn_galleryimg5()).bitmapTransform(new CropCircleTransformation(context)).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.btn_img05);

        holder.mission_subtitle.setText(missionBean.getSub_title());
        holder.mission_maintitle.setText(missionBean.getMain_title());
        holder.mission_leveltext.setText(missionBean.getLevel_text());

        //2016-12-01 수정 lever_star 개수를 비교해서 drawable에서 level_star저장
        int level = Integer.parseInt(missionBean.getLevel_star().trim());
        switch (level) {
            case 1:
                mGlideRequestManager.load("").placeholder(R.drawable.level_star1).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.level_star);
                break;
            case 2:
                mGlideRequestManager.load("").placeholder(R.drawable.level_star2).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.level_star);
                break;
            case 3:
                mGlideRequestManager.load("").placeholder(R.drawable.level_star3).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.level_star);
                break;
            case 4:
                mGlideRequestManager.load("").placeholder(R.drawable.level_star4).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.level_star);
                break;
            case 5:
                mGlideRequestManager.load("").placeholder(R.drawable.level_star5).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.level_star);
                break;

        }//end of switch

        holder.stamp.setVisibility(View.INVISIBLE);

    }//end of onBindViewHolder()

    @Override
    public int getItemCount() {
        return missions.size();
    }

    /****************************************************************************************
     * inner class
     *****************************************************************************************/
    //재활용할 View객체들의 참조값을 저장할 inner클래스
    class Holder extends RecyclerView.ViewHolder {
        ImageView mission_img;

        ImageView stamp;

        ImageView btn_img01;
        ImageView btn_img02;
        ImageView btn_img03;
        ImageView btn_img04;
        ImageView btn_img05;

        TextView mission_subtitle;
        TextView mission_maintitle;
        TextView mission_leveltext;

        ImageView level_star;

        public Holder(View itemView) {
            super(itemView);


            mission_img = (ImageView) itemView.findViewById(R.id.mission_img);
            stamp = (ImageView) itemView.findViewById(R.id.stamp);

            btn_img01 = (ImageView) itemView.findViewById(R.id.btn_img01);
            btn_img02 = (ImageView) itemView.findViewById(R.id.btn_img02);
            btn_img03 = (ImageView) itemView.findViewById(R.id.btn_img03);
            btn_img04 = (ImageView) itemView.findViewById(R.id.btn_img04);
            btn_img05 = (ImageView) itemView.findViewById(R.id.btn_img05);

            mission_subtitle = (TextView) itemView.findViewById(R.id.mission_subtitle);
            mission_maintitle = (TextView) itemView.findViewById(R.id.mission_maintitle);
            mission_leveltext = (TextView) itemView.findViewById(R.id.mission_leveltext);

            level_star = (ImageView) itemView.findViewById(R.id.level_star);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 미션카드뷰를 클릭하면 상세페이지로 이동
                    Intent intent = new Intent(context, DetailActivity.class);
                    int pos = getLayoutPosition();
                    missionBean = missions.get(pos);
                    intent.putExtra("MissionPosition", missionBean.getLayoutPos());
                    context.startActivity(intent);

                }
            });

            btn_img01.setOnClickListener(listener);
            btn_img02.setOnClickListener(listener);
            btn_img03.setOnClickListener(listener);
            btn_img04.setOnClickListener(listener);
            btn_img05.setOnClickListener(listener);

        }//***************************************** end of 생성자 *************************************//

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "사진 업로드는 홈 화면에서만 가능합니다.", Toast.LENGTH_LONG).show();
            }
        };

    }//end of inner class
    /*****************************************************************************************************/


}
