package com.missionofseoul.seoul;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.missionofseoul.seoul.adapter.GalleryPagerAdapter;
import com.missionofseoul.seoul.model.MImageBean;

import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {

    ViewPager gallery_viewpager;
    // ArrayList<String> images;
    ArrayList<MImageBean> mImageBeens; //17/02/25추가

    GalleryPagerAdapter galleryPagerAdapter;

    int total;
    TextView img_size, img_pos;

    public RequestManager mGlideRequestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        mGlideRequestManager = Glide.with(this);

        gallery_viewpager = (ViewPager) findViewById(R.id.gallery_viewpager);
        img_size = (TextView) findViewById(R.id.img_size);
        img_pos = (TextView) findViewById(R.id.img_pos);

        Intent intent = getIntent();
        mImageBeens = (ArrayList<MImageBean>) intent.getSerializableExtra("mImageBeens");
        int postion = intent.getIntExtra("position", 0);

        //뷰에 이미지 총 사이즈 찍어줄 코드,
        total = mImageBeens.size();
        img_size.setText(total + "");

        //Toast.makeText(this, images.get(1) + "", Toast.LENGTH_SHORT).show();
        galleryPagerAdapter = new GalleryPagerAdapter(getLayoutInflater(), mImageBeens, mGlideRequestManager);
        gallery_viewpager.setAdapter(galleryPagerAdapter);

        gallery_viewpager.setCurrentItem(postion, false);

        //변경된 포지션값 텍스트에 찍기
        gallery_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                img_pos.setText(position + 1 + "");
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void closeBtn(View v) {
        finish();
    }
}
