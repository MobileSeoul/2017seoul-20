package com.missionofseoul.seoul.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.model.MImageBean;

import java.util.ArrayList;


/**
 * Created by hyunho on 2016-11-13.
 */

public class GalleryPagerAdapter extends PagerAdapter {

    LayoutInflater inflater;
    ArrayList<MImageBean> mImageBeens;//17/02/25 추가
    public RequestManager mGlideRequestManager;

    public GalleryPagerAdapter(LayoutInflater inflater, ArrayList<MImageBean> mImageBeens, RequestManager mGlideRequestManager) {
        this.inflater = inflater;
        this.mImageBeens = mImageBeens;
        this.mGlideRequestManager = mGlideRequestManager;
    }

    @Override
    public int getCount() {
        return mImageBeens.size();
    }//end of getCount();

    @Override
    public boolean isViewFromObject(View view, Object object) {

        return view == object;
    }//end of isViewFromObject


    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        //position번째에 해당하는 View객체를 생성하여 ViewPager(container)에 추가시키는 작업
        View gallery_page = inflater.inflate(R.layout.viewpager_gallery, container, false);
        ImageView img = (ImageView) gallery_page.findViewById(R.id.pager_img);

        TextView text = (TextView) gallery_page.findViewById(R.id.text);//17/02/25 추가
        final ScrollView scrollView = (ScrollView) gallery_page.findViewById(R.id.scrollView);//17/02/25 추가

        container.addView(gallery_page);

        mGlideRequestManager.load(G.domain + mImageBeens.get(position).getImg()).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(img);
        text.setText(mImageBeens.get(position).getText());

        //미션설명 이미지 터치시 보여주고/감추기 //17/02/25 추가
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (scrollView.getVisibility() == View.VISIBLE) {
                    scrollView.setVisibility(View.INVISIBLE);
                } else if (scrollView.getVisibility() == View.INVISIBLE) {
                    scrollView.setVisibility(View.VISIBLE);
                }
            }
        });

        return gallery_page;
    }//end of instantiatleItem()

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }//end of destroyItem()
}
