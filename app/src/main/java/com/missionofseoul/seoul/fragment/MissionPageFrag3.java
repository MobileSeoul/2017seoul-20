package com.missionofseoul.seoul.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.github.clans.fab.FloatingActionButton;
import com.missionofseoul.seoul.FindFrinedWriteActivity;
import com.missionofseoul.seoul.FriendSearchActivity;
import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.MainActivity;
import com.missionofseoul.seoul.NoticeDeatilActivity;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.adapter.FindFriendRecylerAdapter;
import com.missionofseoul.seoul.adapter.NoticeAdapter;
import com.missionofseoul.seoul.model.FindFriendBean;
import com.missionofseoul.seoul.model.NoticeBean;
import com.missionofseoul.seoul.service.APIClient;
import com.missionofseoul.seoul.service.APIService;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hyunho on 2016-10-25.
 */
//여행친구찾기 프레그먼트
public class MissionPageFrag3 extends Fragment {
    SwipeRefreshLayout mSwipeRefreshLayout;

    FloatingActionButton fabSearch;
    FloatingActionButton fabWrite;

    Spinner spinner;
    ArrayAdapter adapter;

    TextView boardheader_text;

    String header = "";
    String title = "";

    // ListView listview_board;
    //FindFriendAdapter friendAdapter;

    RelativeLayout show_isnotlogin;


    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;

    //notice 공지사항
    ListView listview_notice;
    ArrayList<NoticeBean> noticeBeens = new ArrayList<>();
    NoticeAdapter noticeAdapter;

    //리사이클러뷰
    RecyclerView recyclerView;
    FindFriendRecylerAdapter friendRecylerAdapter;

    //여행친구찾기 목록 가져올 쓰레드
    FindFrinedThread findFrinedThread;
    List<FindFriendBean> friendBeens = new ArrayList<>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGlideRequestManager = Glide.with(this);
    }//********************************************** end of onCreate() ************************************//

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_missionpage3, container, false);

        //당겨서 새로고침 객체 생성
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.layout_swipe_frag3);
        mSwipeRefreshLayout.setOnRefreshListener(listener);


        //notice 리스트뷰 객체생성
        listview_notice = (ListView) view.findViewById(R.id.listview_notice);
        //공지사항 눌렀을때 상세페이지로 이동
        listview_notice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //notice 디테일 페이지로 이동
                Intent intent = new Intent(getActivity(), NoticeDeatilActivity.class);
                intent.putExtra("noticeBeens", noticeBeens); //ArrayList<FindFriendBeen>에는 서버에서 가져온 값들이 다 저장되어있다.
                intent.putExtra("position", position);//리스트뷰 클릭한 위치
                startActivity(intent);
            }
        });
        //floation액션버튼 객체생성
        fabSearch = (FloatingActionButton) view.findViewById(R.id.fab_search);
        fabWrite = (FloatingActionButton) view.findViewById(R.id.fab_write);
        fabSearch.setOnClickListener(searchListener);
        fabWrite.setOnClickListener(writeListener);

        spinner = (Spinner) view.findViewById(R.id.spinner);
        adapter = ArrayAdapter.createFromResource(getActivity(), R.array.select, R.layout.spinner_select_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(selectedListener);

        boardheader_text = (TextView) view.findViewById(R.id.boardheader_text);

        //여행친구찾기 리스트뷰 객체생성
        // listview_board = (ListView) view.findViewById(R.id.listview_board);

        //여행친구찾기 리스트뷰 누르면  발생
     /*   listview_board.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), FindFriendDetailActivity.class);
                intent.putExtra("friendBeens", friendBeens); //ArrayList<FindFriendBeen>에는 서버에서 가져온 값들이 다 저장되어있다.
                intent.putExtra("position", position);//리스트뷰 클릭한 위치
                startActivity(intent);
            }
        });*/
        //리스뷰와 당겨서 새로고침 스크롤 오류 개선 리스너
     /*   listview_board.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listview_board == null || listview_board.getChildCount() == 0) ? 0 :
                        listview_board.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled((topRowVerticalPosition >= 0));
            }
        });*/

        show_isnotlogin = (RelativeLayout) view.findViewById(R.id.show_isnotlogin);

        //로그인했는지 확인 후 로그을 했으면 사용 가능하게 함.
        if (G.isLogin == true && G.isConfirm == true) {//로그인회원 && 이메일 인증을 받은 회원
            show_isnotlogin.setVisibility(View.GONE);
        }
        //공지사항 가져오는 쓰레드
        new Thread() {
            @Override
            public void run() {

                String serverUrl = G.domain + "php/notice/friend_notice.php";

                try {
                    URL url = new URL(serverUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);

                    String data = "kinds=" + "friendnotice";
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
                    noticeBeens.clear();

                    for (String row : rows) {
                        int notice_no = Integer.parseInt(row.split("&")[0]);
                        String title = row.split("&")[1];
                        String date = row.split("&")[2];
                        int comment_num = Integer.parseInt(row.split("&")[3]);
                        String nUrl = row.split("&")[4];

                        noticeBeens.add(new NoticeBean(notice_no, title, date, comment_num, nUrl));

                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            noticeAdapter = new NoticeAdapter(noticeBeens, inflater, mGlideRequestManager);
                            listview_notice.setAdapter(noticeAdapter);
                            noticeAdapter.notifyDataSetChanged();

                        }
                    });
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }//end of run()
        }.start(); //공지사항 가져오는 쓰레드

        //리스트뷰 마이그레이션 > 리사이클러뷰
        recyclerView = (RecyclerView) view.findViewById(R.id.recycerView);

        return view;
    }//*********************************** end of onCreateView ***********************************************//

    //당겨서 새로고침 리스너
    SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {

            ((MainActivity) getActivity()).loadFragment();
            // 새로고침 완료
            mSwipeRefreshLayout.setRefreshing(false);

        }
    };//end of 당겨서 새로고침

    //FloatingActionButton 검색 버튼 클릭시 발동
    FloatingActionButton.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), FriendSearchActivity.class);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
        }
    };//플로팅액션 검색버튼 끝

    //FloatingActionButton 글쓰기 버튼 클릭시 발동
    FloatingActionButton.OnClickListener writeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), FindFrinedWriteActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로

        }
    };//플로팅액션 글쓰기버튼 끝

    //spinner 선택 리스너
    Spinner.OnItemSelectedListener selectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int positon, long id) {

            switch (positon) {
                case 0: //title 여행지를 선택하세요
                    header = "아시아|유럽|아메리카|오세아니아|아프리카";
                    findFriend(header);
                    //findFrinedThread = new FindFrinedThread();
                    //   findFrinedThread.start();
                    title = "전체보기";
                    break;
                case 1://아시아
                    header = "아시아";
                    findFriend(header);
                    // findFrinedThread = new FindFrinedThread();
                    // findFrinedThread.start();
                    title = "아시아";
                    break;
                case 2://유럽
                    header = "유럽";
                    findFriend(header);
                    // findFrinedThread = new FindFrinedThread();
                    // findFrinedThread.start();
                    title = "유럽";
                    break;
                case 3://아메리카
                    header = "아메리카";
                    findFriend(header);
                    // findFrinedThread = new FindFrinedThread();
                    //  findFrinedThread.start();
                    title = "아메리카";
                    break;
                case 4://오세아니아
                    header = "오세아니아";
                    findFriend(header);
                    // findFrinedThread = new FindFrinedThread();
                    //findFrinedThread.start();
                    title = "오세아니아";
                    break;
                case 5://아프리카
                    header = "아프르카";
                    findFriend(header);
                    // findFrinedThread = new FindFrinedThread();
                    // findFrinedThread.start();
                    title = "아프리카";
                    break;

            }//end of switch
            boardheader_text.setText(title + " 여행친구찾기");

        }//선택되어졌을 때

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };//spinner 선택 리스너

    private void findFriend(String continent) {
        APIService service = APIClient.getClient().create(APIService.class);
        Call<List<FindFriendBean>> call = service.loadFindFriend(continent);
        call.enqueue(new Callback<List<FindFriendBean>>() {
            @Override
            public void onResponse(Call<List<FindFriendBean>> call, Response<List<FindFriendBean>> response) {
                friendBeens = response.body();
                try {
                    if (friendBeens.size() != 0) {
                        friendRecylerAdapter = new FindFriendRecylerAdapter(friendBeens, getActivity().getLayoutInflater(), mGlideRequestManager);
                        /*recyclerView.setHasFixedSize(true);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(linearLayoutManager);*/
                        recyclerView.setAdapter(friendRecylerAdapter);
                        friendRecylerAdapter.notifyDataSetChanged();

                    }//end of if
                } catch (NullPointerException e) {

                }

            }

            @Override
            public void onFailure(Call<List<FindFriendBean>> call, Throwable t) {

            }
        });

    }//ned of findFriend()

    //여행지선택 스피너의 텍스트로 서버로 넘어가서 해당 대륙의 친구찾기 글 목록 가져오기
    class FindFrinedThread extends Thread {

        @Override
        public void run() {
            String serverUrl = G.domain + "php/findfriend/loadFindfriend.php";

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);

                String data = "continent=" + header;
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //리스트뷰에 > 리사이클러뷰로 마이그레이션
                        //   friendRecylerAdapter = new FindFriendRecylerAdapter(friendBeens, mInflater, mGlideRequestManager);
                        // recyclerView.setAdapter(friendRecylerAdapter);
                        // friendRecylerAdapter.notifyDataSetChanged();

                        //  friendAdapter = new FindFriendAdapter(friendBeens, getActivity().getLayoutInflater(), mGlideRequestManager);
                        // listview_board.setAdapter(friendAdapter);
                        // friendAdapter.notifyDataSetChanged();
                    }
                });
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }//end of run
    }//end of inner class Thread 여행지선택 스피너의 텍스트로 서버로 넘어가서 해당 대륙의 친구찾기 글 목록 가져오기

}//end of flagment
