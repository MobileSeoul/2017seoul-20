package com.missionofseoul.seoul;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.missionofseoul.seoul.intro.LoginActivity;
import com.missionofseoul.seoul.model.CloseMissionBean;
import com.missionofseoul.seoul.model.MarkerBean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;

    //Actionbartext
    TextView titletext;

    //내 위치와 근접한 미션 구하는 버튼
    Button location_btn;

    //내 위치 확인 로캐이션 매니져
    LocationManager locationManager;

    boolean isInternetGPSEnabled = false; //무선인터넷 사용가능
    boolean isGpsEnabled = false; //gps 사용가능

    //BottomNavigation icon 변수
    ImageView home_icon;
    ImageView search_icon;
    ImageView user_icon;
    ImageView thebogi_icon;


    //
    GoogleApiClient mGoogleApiClient;
    //boolean mLocationPermissionGrante = false;
    LocationRequest mLocationRequset;

    Location mCurrentLocation;
    CameraPosition mCameraPosition;

    private static final String CAMERA_POSITION = "camera_position";
    private static final String LOCATION = "location";

    //
    ArrayList<MarkerBean> markerBeens = new ArrayList<>();
    //
    MarkerThread markerThread;

    //
    ArrayList<CloseMissionBean> closeMissionBeens = new ArrayList<>();
    DistanceSort distanceSort = new DistanceSort();


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

        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(CAMERA_POSITION);
        }

        setContentView(R.layout.activity_maps);

        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.bottom_abs_layout);

        titletext = (TextView) findViewById(R.id.actionbar_text);
        titletext.setText("내 주변"); //Actionbar title를 더보기로

        //BottomNavigation icon 객체생성
        home_icon = (ImageView) findViewById(R.id.home_icon);
        search_icon = (ImageView) findViewById(R.id.search_icon);
        user_icon = (ImageView) findViewById(R.id.user_icon);
        thebogi_icon = (ImageView) findViewById(R.id.thebogi_icon);

        search_icon.setImageResource(R.drawable.search_icon_over);



        location_btn = (Button) findViewById(R.id.location_btn);

        //로케이션 매니져 객체생성
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        isInternetGPSEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);// 시스템 > 설정 > 위치 및 보안 > 무선 네트워크 사용 여부 체크.
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);// 시스템 > 설정 > 위치 및 보안 > GPS 위성 사용 여부 체크.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //마쉬멜로 버젼 이상
            int permissionCheck = ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                // 위치권한 없음
                //위치권한 설정 페이지로 이동
                new AlertDialog.Builder(MapsActivity.this).setMessage("내 위치 확인을 위해서는\n'위치'권한 설정이 반드시 필요합니다.").setPositiveButton("권한설정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //로그인 페이지로 이동
                        //이 앱에 대한 사용자가 권한을 on/off 할 수 있는 system setting으로 이동
                        Uri uri = Uri.fromParts("package", "com.missionofseoul.seoul", null); //변경부분 !!
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
                        startActivity(intent);
                    }
                }).setNegativeButton("다음에", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MapsActivity.this.finish();
                    }
                }).create().show();
            } else { // 위치권한 있음
                //Gps 무선 네트워크 사용 체크
                if (!isGpsEnabled && !isInternetGPSEnabled) {
                    new AlertDialog.Builder(MapsActivity.this).setMessage("내 위치 확인 시 GPS(위치) 활성화가 필요합니다.").setPositiveButton("활성화", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);//gps(위치) 접근 인텐트
                            startActivity(myIntent);
                        }
                    }).setNegativeButton("다음에", null).create().show();
                } else {

                }
            }//end of else
        } else {//마시멜로 이하 버젼
            if (!isGpsEnabled && !isInternetGPSEnabled) {
                new AlertDialog.Builder(MapsActivity.this).setMessage("내 위치 확인 시 GPS(위치) 활성화가 필요합니다.").setPositiveButton("활성화", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                }).setNegativeButton("다음에", null).create().show();
            } else {

            }
        }//end of if else 위치 권한설정 끝


        //
        buildGoogleApiClient();
        mGoogleApiClient.connect();


        //
        closeMissionBeens.clear();



    }//************************************** end of onCreate() ***************************

    //마커를 생성할 메서드
    protected void makeMarker(String mainTitle, String location, String search, int num) {

        String[] arrTmp = new String[2];
        arrTmp = location.split(",");
        double latitude = Double.parseDouble(arrTmp[0]);
        double logitude = Double.parseDouble(arrTmp[1]);


        //특정좌표 지정
        LatLng missionMarker = new LatLng(latitude, logitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(missionMarker);
        markerOptions.title(search);
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.snippet(mainTitle);

        //미션추가 할 때마다 색상을 추가!!!!
        switch (num) {
            case 1:
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(0));
                break;
            case 2:
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(30));
                break;
            case 3:
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(60));
                break;
            case 4:
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(90));
                break;
            case 5:
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(120));
                break;
            case 6:
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(150));
                break;
            case 7:
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(180));
                break;
            case 8:
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(210));
                break;
            case 9:
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(240));
                break;
            default:
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(250));
                break;
        }

        mMap.addMarker(markerOptions);//마커는 갯수제한이 없다.

        //내 위치와 가장 가까운 미션 구하기
        double myLatitude = mCurrentLocation.getLatitude();
        double myLongitude = mCurrentLocation.getLongitude();

        float[] result = new float[3];
        Location.distanceBetween(myLatitude, myLongitude, latitude, logitude, result); //거리측정

        closeMissionBeens.add(new CloseMissionBean(result[0], latitude, logitude, search));

        Collections.sort(closeMissionBeens, distanceSort);

        //내 위치와 가장 가까운 미션 구하기 끝


        //마커설명 클릭 리스너 마커 설명을 클릭하면 상세페이지로 이동한다
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                int tmp = Integer.parseInt(marker.getId().substring(1));
                int num = (tmp / 5) + 1;

                //Toast.makeText(MapsActivity.this, tmp / 5 + "", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MapsActivity.this, DetailActivity.class);
                intent.putExtra("MissionPosition", num);
                startActivity(intent);
            }
        });

    }//end of makeMarker

    //나와 가까운 미션구하는 버튼
    public void closeMission(View v) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(closeMissionBeens.get(0).getLatitude(),
                closeMissionBeens.get(0).getLogitude()), 16));

        float distance = closeMissionBeens.get(0).getDistansBetween() / 1000;
        DecimalFormat df = new DecimalFormat("0.###");
        String result = df.format(distance);
        Toast.makeText(this, result + "km 떨어진 위치에 \n'" + closeMissionBeens.get(0).getLocationName() + "' 이(가) 있습니다.", Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onResume() {
        if (mGoogleApiClient.isConnected()) {
            getDeviceLocation();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationListener);
        }
    }//end of onPause()

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setRotateGesturesEnabled(false); //지도 회전못하게 막기
        mMap.getUiSettings().setTiltGesturesEnabled(false); //지도 각도 없애기

        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            //서버에서 마커 가져오기 실행
            markerThread = new MarkerThread();
            markerThread.start();

        } else if (mCurrentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude()), 12));
            //서버에서 마커 가져오기 실행
            markerThread = new MarkerThread();
            markerThread.start();

        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25.3, 34.3), 16));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            Toast.makeText(this, "GPS가 원활하지 않습니다.홈 화면으로 이동 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
        }

        //내 위치 검색
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);


    }//******************************** end of onMapReady() *********************************

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }//end of if
        createLocationRequest();

    }//end of buildGoogleApiClient()

    ///
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getDeviceLocation();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //// LocationListener 메소드
    com.google.android.gms.location.LocationListener locationListener = new com.google.android.gms.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mCurrentLocation = location;

        }
    };


    //
    private void createLocationRequest() {
        mLocationRequset = new LocationRequest();
        mLocationRequset.setInterval(10000);
        mLocationRequset.setFastestInterval(5000);
        mLocationRequset.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequset, locationListener);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(LOCATION, mCurrentLocation);
            super.onSaveInstanceState(outState);
        }
    }

//

    @Override
    protected void onDestroy() {
        mMap.clear();//out of memory를 없애기
        super.onDestroy();
    }


    //BottomNavigationBar
    public void bottomBtn(View v) {
        switch (v.getId()) {
            case R.id.layout_home: //홈버튼
                //Main화면으로 이동
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                break;
            case R.id.layout_search: //미션검색
                //자기 자신 activity는 아무것도 안함
                break;
            case R.id.layout_user://내 활동 버튼
                //Activity이동 전 로그인 했는지 물어보기
                if (G.isLogin == true) {//로그인했다면
                    //user화면으로 이동
                    Intent intent1 = new Intent(this, UserActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                    startActivity(intent1);
                    overridePendingTransition(0, 0); // << activity전환시 깜빡임을 없애 주었다.

                    finish();
                } else { //비로그인 상태라면,
                    new AlertDialog.Builder(this).setMessage("로그인이 필요합니다. 로그인 하시겠습니까?").setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //로그인 페이지로 이동
                            Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                            startActivity(intent);
                            overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
                            finish();
                        }
                    }).setNegativeButton("다음에", null).create().show();

                }//end if ~ else //로그인여부 묻고 userActivity로 이동

                break;
            case R.id.layou_thebogi: //더 보기(설정)버튼
                //thebogi화면으로 이동
                Intent intent2 = new Intent(this, ThebogiActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                startActivity(intent2);
                overridePendingTransition(0, 0);

                finish();
                break;
        }
    }//end of bttomBtn

    //서버에서 마커 가져오고 뿌려주기
    class MarkerThread extends Thread {
        @Override
        public void run() {

            String serverUrl = G.domain + "php/location/load_marker.php";

            try {
                URL url = new URL(serverUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                String data = "dbnum=" + G.missionDBNum; //G.missionDBNum은 미션 테이블개수
                OutputStream os = connection.getOutputStream();
                os.write(data.getBytes());

                os.flush();
                os.close();

                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader reader = new BufferedReader(isr);

                final StringBuffer buffer = new StringBuffer();

                String line = reader.readLine();

                while (true) {
                    buffer.append(line);
                    line = reader.readLine();
                    if (line == null) break;
                    buffer.append("\n");
                }

                String[] rows = buffer.toString().split(";");
                markerBeens.clear();

                for (String row : rows) {
                    String mainTitle = row.split("&")[0];
                    String location = row.split("&")[1];
                    String search = row.split("&")[2];
                    int num = Integer.parseInt(row.split("&")[3]);
                    markerBeens.add(new MarkerBean(mainTitle, location, search, num));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String[] arrLocation = new String[5];
                        String[] arrSearch = new String[5];
                        String maintitle;
                        int num;

                        for (int i = 0; i < markerBeens.size(); i++) {
                            maintitle = markerBeens.get(i).getMainTitle(); //공통적으로 5번 보내질 것
                            num = markerBeens.get(i).getNum(); //공통적으로 5번 보내질 것

                            for (int j = 0; j < arrLocation.length; j++) {
                                arrLocation = markerBeens.get(i).getLocation().split("#");
                                arrSearch = markerBeens.get(i).getSearch().split("#");
                                //메서드 호출
                                makeMarker(maintitle, arrLocation[j], arrSearch[j], num);
                            }

                        }//end of out for
                        //가까운 미션 버튼 보이게 하기
                        location_btn.setVisibility(View.VISIBLE);
                    }//end of run
                });
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
                //  Log.d("에러!!!", e + "");
            }

        }
    }//end of markerthread

    // 내 위치와 가장 가까운 미션 거리값 오름차순 정렬하기
    public class DistanceSort implements Comparator<CloseMissionBean> {

        @Override
        public int compare(CloseMissionBean c1, CloseMissionBean c2) {
            float tmp1 = c1.getDistansBetween();
            float tmp2 = c2.getDistansBetween();
            if (tmp1 > tmp2) {
                return 1;
            } else if (tmp1 < tmp2) {
                return -1;
            } else {
                return 0;
            }
        }
    }//end of inner class


}//end of class
