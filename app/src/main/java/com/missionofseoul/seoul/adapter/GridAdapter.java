package com.missionofseoul.seoul.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.GalleryActivity;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.model.MImageBean;

import java.util.ArrayList;

/**
 * Created by hyunho on 2017-01-07.
 */

public class GridAdapter extends BaseAdapter {

    ArrayList<MImageBean> mImageBeens;//17/02/25수정
    LayoutInflater inflater;
    public RequestManager mGlideRequestManager;

    public GridAdapter(ArrayList<MImageBean> mImageBeens, LayoutInflater inflater, RequestManager mGlideRequestManager) {
        this.mImageBeens = mImageBeens;
        this.inflater = inflater;
        this.mGlideRequestManager = mGlideRequestManager;
    }

    @Override
    public int getCount() {
        return mImageBeens.size();
    }

    @Override
    public Object getItem(int position) {
        return mImageBeens.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.gridview_userimg, parent, false);
        }
        //gridview_uerimg 레이아웃에 있는 객체생성
        ImageView image = (ImageView) convertView.findViewById(R.id.image);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GalleryActivity.class);
                intent.putExtra("mImageBeens", mImageBeens); //17/02/25수정
                intent.putExtra("position", position);
                view.getContext().startActivity(intent);
            }
        });

        String imgAddr = mImageBeens.get(position).getImg();
        mGlideRequestManager.load(G.domain + imgAddr).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(image);

        return convertView;
    }


}//end of class
