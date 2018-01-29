package com.missionofseoul.seoul;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.missionofseoul.seoul.adapter.TotalProfileAdapter;
import com.missionofseoul.seoul.model.MissionProfileBean;

import java.util.ArrayList;
import java.util.Collections;

public class TotalProfileActivity extends AppCompatActivity {
    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;

    ArrayList<MissionProfileBean> profileBeens;
    MissionProfileBean profileBean;

    ListView profile_listview;
    TotalProfileAdapter profileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);

        }
        setContentView(R.layout.activity_total_profile);

        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        mGlideRequestManager = Glide.with(this);

        profileBeens = (ArrayList<MissionProfileBean>) getIntent().getSerializableExtra("MissionProfileBean");
        Collections.reverse(profileBeens); // 미션수행대원 전체보기 역순으로 정렬 17-02-12 추가

        profile_listview = (ListView) findViewById(R.id.profile_listview);
        profileAdapter = new TotalProfileAdapter(profileBeens, getLayoutInflater(), mGlideRequestManager);
        profile_listview.setAdapter(profileAdapter);


        //프로필 항목 클릭하면 개인 프로필 화면으로 이동
        profile_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                profileBean = profileBeens.get(position);
                String userId = profileBean.getId();
                int isPrivate = profileBean.getIsPrivate();
                //아이디 값을 가지고 프로필 클릭한 유저의 화면을 이동
                if (G.mem_id.equals(id)) {//내 아이디와 클릭한 아이디가 같은면 내 활동으로 이동
                    Toast.makeText(TotalProfileActivity.this, "본인 프로필은 내 활동에서\n확인하실 수 있습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(TotalProfileActivity.this, AnotherUserActivity.class);
                    intent.putExtra("ID", userId);
                    intent.putExtra("IsPrivate", isPrivate);
                    startActivity(intent);
                }
            }
        });//end of 클릭리스너

    }//end of onCreate();

    //액션바 뒤로가기버튼 클릭시
    public void backBtn(View v) {
        finish();
    }
}
