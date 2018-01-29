package com.missionofseoul.seoul;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.missionofseoul.seoul.adapter.TotalReviewAdapter;
import com.missionofseoul.seoul.model.ReviewBean;

import java.util.ArrayList;

public class TotalReviewActivity extends AppCompatActivity {

    ListView total_listview; //리뷰 전체보기 리스트뷰
    TotalReviewAdapter totalReviewAdapter;
    ArrayList<ReviewBean> reviewBeens;

    public RequestManager mGlideRequestManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_total_review);

        mGlideRequestManager = Glide.with(this);


        //actionBar custom하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);


        reviewBeens = (ArrayList<ReviewBean>) getIntent().getSerializableExtra("reviewBeen");

        total_listview = (ListView) findViewById(R.id.total_listview);
        totalReviewAdapter = new TotalReviewAdapter(reviewBeens, getLayoutInflater(), mGlideRequestManager);
        total_listview.setAdapter(totalReviewAdapter);

        total_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                String id = reviewBeens.get(position).getId();
                int isPrivate = reviewBeens.get(position).getIsPrivate();
                //아이디 값을 가지고 프로필 클릭한 유저의 화면을 이동
                if (G.mem_id.equals(id)) {//내 아이디와 클릭한 아이디가 같은면 토스트
                    Toast.makeText(TotalReviewActivity.this, "본인 프로필은 내 활동에서\n확인하실 수 있습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(TotalReviewActivity.this, AnotherUserActivity.class);
                    intent.putExtra("ID", id);
                    intent.putExtra("IsPrivate", isPrivate);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                    startActivity(intent);
                }

            }
        });

        total_listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (G.mem_id.equals(reviewBeens.get(position).getId())) {//아이디가 같으면
                    Toast.makeText(TotalReviewActivity.this, "리뷰는 삭제하실 수 없습니다.", Toast.LENGTH_SHORT).show();
                } //end of if(해당 댓글 리스트에 position의 아이디와 내 아이디가 같으면 발동
                return true;
            }
        });

        totalReviewAdapter.notifyDataSetChanged();

    }

    //actionbar 뒤로가기 버튼 클릭시
    public void backBtn(View v) {
        finish();
    }
}
