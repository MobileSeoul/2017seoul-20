package com.missionofseoul.seoul;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.missionofseoul.seoul.adapter.FindSearchAdapter;
import com.missionofseoul.seoul.model.FindFriendBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FriendSearchActivity extends AppCompatActivity {
    //액션바 멤버변수
    EditText search_edit;
    TextView search_text;

    LinearLayout layout_linear; //인기검색어 영역

    ListView listView_search;
    FindSearchAdapter searchAdapter;

    ArrayList<FindFriendBean> friendBeens = new ArrayList<>();

    SearchThread searchThread;

    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //마시멜로 버젼 이상부터 statusbar색상 변경코드
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else { //API23이하에서는 colorPrimaryDark의 색상을 변경 스타일 적용
            //-setTheme()메소드를 사용하는데, 이것은 컨텍스트 내에서 뷰를 인스턴스화 하기전에 테마 설정이 되야 한다는 조건이 있음.
            // 즉, setContentView() 나 inflate()를 호출하기 전에 설정되야 한다는 뜻.
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_friend_search);

        mGlideRequestManager = Glide.with(this);

        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.search_abs_layout);

        search_edit = (EditText) findViewById(R.id.search_edit);
        search_text = (TextView) findViewById(R.id.search_text);

        layout_linear = (LinearLayout) findViewById(R.id.layout_linear);

        //리스트뷰 객체 색성
        listView_search = (ListView) findViewById(R.id.listview_search);
        //리스트뷰를 누르면 상세 페이지로 이동
        listView_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(FriendSearchActivity.this, FindFriendDetailActivity.class);
                intent.putExtra("friendBeens", friendBeens); //ArrayList<FindFriendBeen>에는 서버에서 가져온 값들이 다 저장되어있다.
                intent.putExtra("position", position);//리스트뷰 클릭한 위치
                startActivity(intent);
            }
        });

        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //검색어가 없으면 다시 레이아웃을 보여주기
                if (search_edit.length() == 0) {
                    listView_search.setVisibility(View.INVISIBLE);
                    layout_linear.setVisibility(View.VISIBLE);

                    //리스트뷰 감추는 코드 추가
                }
            }
        });

    }//************************************ end of onCreate() *************************************//

    //검색을 누르면 발동
    public void clickSearch(View v) {

        String nation = search_edit.getText().toString().trim();

        if (nation.equals("")) {
            Toast.makeText(this, "검색어를 입력해 주세요", Toast.LENGTH_SHORT).show();
        } else {//검색어가 있다면
            //인기검색어 레이아웃 감추기
            layout_linear.setVisibility(View.GONE);

            //서버에서 nation으로 검색 후 리스트뷰로 화면에 뿌려준다
            searchThread = new SearchThread();
            searchThread.start();
        }

    }//end pf clickSearch 검색글씨 누르면 서버에 접속 해당 글씨로 검색

    //인기검색어 클릭하면 edittext에 넣기
    public void clickText(View v) {
        TextView textView = (TextView) v;
        String text = textView.getText().toString();
        search_edit.setText(text);

    }//end of clickText 인기검색어 텍스트 누르면 발동

    //leftBtn
    public void backBtn(View v) {
        finish();
    }

    class SearchThread extends Thread {
        @Override
        public void run() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    dialog = new ProgressDialog(FriendSearchActivity.this);
                    dialog.setMessage("리스트를 불러오고 있습니다.");
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
            });

            String edittext = search_edit.getText().toString().trim();
            String serverUrl = G.domain + "php/findfriend/searchfriend.php";

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);

                String data = "nation=" + edittext;
                OutputStream os = connection.getOutputStream();
                os.write(data.getBytes());

                os.flush();
                os.close();

                //서버로부터 오는 echo를 읽어오기
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isr);

                StringBuffer buffer = new StringBuffer();

                String line = reader.readLine();

                while (true) {
                    buffer.append(line);
                    line = reader.readLine();
                    if (line == null) break;
                    buffer.append("\n");
                }

                //읽어온 데이터 문자열에서 db의 row별로 배열로 구분해서 저장
                String[] rows = buffer.toString().split(";");

                //기존의 데이터랑 겹칠 수 있으므로 모든 데이터를 삭제시킨다.
                friendBeens.clear();

                for (String row : rows) {
                    String profile = row.split("&")[0];
                    String nickName = row.split("&")[1];
                    int isPrivate = Integer.parseInt(row.split("&")[2]);
                    int find_no = Integer.parseInt(row.split("&")[3]);
                    String id = row.split("&")[4];
                    String continent = row.split("&")[5];
                    String nation = row.split("&")[6];
                    String title = row.split("&")[7];
                    String contents = row.split("&")[8];
                    String date = row.split("&")[9];
                    int views = Integer.parseInt(row.split("&")[10]);
                    int comment_num = Integer.parseInt(row.split("&")[11]);

                    //최신순으로 붙이기
                    friendBeens.add(new FindFriendBean(profile, nickName, isPrivate, find_no, id, continent, nation, title, contents, date, views, comment_num));

                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchAdapter = new FindSearchAdapter(friendBeens, getLayoutInflater(), mGlideRequestManager);
                        listView_search.setAdapter(searchAdapter);
                        listView_search.setVisibility(View.VISIBLE);
                        setListViewHeightBasedOnChildren(listView_search);
                        searchAdapter.notifyDataSetChanged();

                        //soft키보드 감추는 코드
                        InputMethodManager immhide = (InputMethodManager) getSystemService(CastDetailActivity.INPUT_METHOD_SERVICE);
                        immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    }
                });

                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            dialog.dismiss();

        }//end of run()
    }//end inner Thread class 검색 쓰레드

    // ScrollView에 가려진 리스트뷰 Height 결정시켜주는 메서드
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 150; // << 이 값으로 마지막 리스트뷰의 BOTTOM을 띄울수 있다.
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();

    }//end of setListViewHeightBasedOnChildren // listview Hieght 결정


}//end of class
