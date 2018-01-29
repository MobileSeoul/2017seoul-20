package com.missionofseoul.seoul;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.missionofseoul.seoul.intro.LoginActivity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MissionImageActivity extends AppCompatActivity {
    public RequestManager mGlideRequestManager;

    int layoutPos;//미션카드 리스트 위치(0부터시작)
    int imgbtnPos; // 미션사진 업로드용 이미지버튼 아이디값
    double missionLatitude; //미션장소의 위도
    double missionLogitude; //미션장소의 경도
    String imgMission;//미션이미지 버튼 주소

    //내 위치 확인 로캐이션 매니져
    LocationManager locationManager;
    boolean isEntering = false;
    boolean isInternetGPSEnabled = false; //무선인터넷 사용가능
    boolean isGpsEnabled = false; //gps 사용가능

    //
    ImageView missionimg;

    Button takepic_btn, choicepic_btn; // 다이얼로그 버튼 멤버변수
    DialogInterface picDlg;//다이얼로그 종료를 위한 멤버변수

    ProgressDialog dialog;


    Uri imgUri;//사용자가 선택한 이미지의 경로정보를 보유한 Uri객체의 참조변수
    //file입출력을 위해서는 file의 절대경로 필요 file// 스키마는 필요없다.(서버전송)
    String imgPath;//imgUri의 경로정보 문자열
    final int REQ_PICTURE = 10;//갤러리앱에서 결과를 가져오는 intent의 식별할 수있게 도와주는 상수값
    Bitmap bm;

    public static final int REQUEST_CAMERA = 1;
    public static final int MY_PERMISSION_REQUEST_STORAGE = 2; //외부저장장치쓰고/읽기 퍼미션 상수값
    public static final int REQUESET_GALLERY = 3;

    ImgUploadThread imgUploadThread; //이미지 업로드 쓰레드

    String path;// 실제패스 구하기 (파일사이즈 별로 크기 줄이는데 사용)
    byte[] byteArray; //
    final long ONEMB = 1048576; //1MB

    // 갤러리/ 사진첩에서 선택된 이미지 보여주는 레이아웃 변수
    RelativeLayout layout_picshow;
    ImageView selected_img;
    RelativeLayout layout_show_hidden;

    MainActivity mainActivity;

    //미션사진 설명 달기 17/02/25추가
    EditText comment_edittext;
    String mText;

    // 위치인증 17/03/04추가
    TextView locationtext;
    TextView uploadimgtext;
    int check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_image);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//statusbar hidden

        mGlideRequestManager = Glide.with(this);

        mainActivity = (MainActivity) MainActivity.mainActivity;

        Intent intent = getIntent();
        layoutPos = intent.getIntExtra("LayoutPos", 0);
        imgbtnPos = intent.getIntExtra("ImgbtnPos", 0);
        missionLatitude = intent.getDoubleExtra("Latitude", 0);
        missionLogitude = intent.getDoubleExtra("Logitude", 0);
        imgMission = intent.getStringExtra("ImgPath");
        //
        missionimg = (ImageView) findViewById(R.id.missionimg);

        //로케이션 매니져 객체생성
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //선택된 이미지 보여주는 레이아웃 객체생성
        layout_picshow = (RelativeLayout) findViewById(R.id.layout_picshow);
        selected_img = (ImageView) findViewById(R.id.selected_img);
        layout_show_hidden = (RelativeLayout) findViewById(R.id.layout_show_hidden);

        //버튼클릭한 미션이미지 보여주기
        mGlideRequestManager.load(imgMission).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(missionimg);

        //사진을 고를 후 스트린 터치화면 선택 버튼 사라졌다 나오게 하는 코드
        selected_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layout_show_hidden.getVisibility() == View.VISIBLE) {
                    layout_show_hidden.setVisibility(View.INVISIBLE);

                } else if (layout_show_hidden.getVisibility() == View.INVISIBLE) {
                    layout_show_hidden.setVisibility(View.VISIBLE);
                }
            }
        });

        //17/02/25 미션이미지 설명 추가
        comment_edittext = (EditText) findViewById(R.id.comment_edittext);


        //17/03/04 미션위치확인컴펌 추가
        //객체생성
        locationtext = (TextView) findViewById(R.id.locationtext);
        uploadimgtext = (TextView) findViewById(R.id.uploadimgtext);
        //서버에서
        new Thread() {
            @Override
            public void run() {
                String serverUrl = G.domain + "php/mission/loadcheck.php";

                try {
                    URL url = new URL(serverUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);


                    String data = "id=" + G.mem_id + "&layoutPos=" + layoutPos + "&imgbtnPos=" + imgbtnPos;
                    OutputStream os = connection.getOutputStream();
                    os.write(data.getBytes());

                    os.flush();
                    os.close();

                    InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");
                    BufferedReader reader = new BufferedReader(isr);

                    StringBuffer buffer = new StringBuffer();
                    String line = reader.readLine();

                    while (true) {
                        buffer.append(line);
                        line = reader.readLine();
                        if (line == null) break;
                        buffer.append("\n");
                    }

                    String result = buffer.toString().trim();
                    check = Integer.parseInt(result);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Toast.makeText(MissionImageActivity.this, result + "", Toast.LENGTH_SHORT).show();
                            switch (check) {
                                case 0://위치인증 안함사람
                                    break;
                                case 1: // 위치확인 후 이미지 업로드 한사람
                                    locationtext.setVisibility(View.INVISIBLE);
                                    uploadimgtext.setVisibility(View.VISIBLE);

                                    break;
                                case 10: // 이미지는 업로드 안하고 위치확인만 한사람
                                    locationtext.setVisibility(View.INVISIBLE);
                                    uploadimgtext.setVisibility(View.VISIBLE);
                                    break;
                            }
                        }
                    });

                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }//emd of run()
        }.start();


    }//***************************************************************** end of onCreate ****************************************************//

    //위치확인 글씨
    public void isLocation(View v) {
        if (G.isLogin == false) {//비로그인 상태라면
            new AlertDialog.Builder(MissionImageActivity.this).setMessage("로그인이 필요합니다. 로그인 하시겠습니까?").setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //로그인 페이지로 이동
                    Intent intent = new Intent(MissionImageActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로
                }
            }).setNegativeButton("다음에", null).create().show();
        } else {//로그인했다면!!

            isInternetGPSEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);// 시스템 > 설정 > 위치 및 보안 > 무선 네트워크 사용 여부 체크.
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);// 시스템 > 설정 > 위치 및 보안 > GPS 위성 사용 여부 체크.

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //마쉬멜로 버젼 이상
                int permissionCheck = ContextCompat.checkSelfPermission(MissionImageActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                    // 위치권한 없음
                    //위치권한 설정 페이지로 이동
                    new AlertDialog.Builder(MissionImageActivity.this).setMessage("미션을 수행하기 위해서는 '위치'권한 설정이 반드시 필요합니다.").setPositiveButton("권한설정", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //로그인 페이지로 이동
                            //이 앱에 대한 사용자가 권한을 on/off 할 수 있는 system setting으로 이동
                            Uri uri = Uri.fromParts("package", "com.kommaz.mot_main", null);
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
                            startActivity(intent);
                        }
                    }).setNegativeButton("다음에", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MissionImageActivity.this, "'더보기' 화면 사용자 권한 설정에서 변경하실 수 있습니다.", Toast.LENGTH_LONG).show();
                        }
                    }).create().show();
                } else { // 위치권한 있음
                    //Gpa 무선 네트워크 사용 체크
                    if (!isGpsEnabled && !isInternetGPSEnabled) {
                        new AlertDialog.Builder(MissionImageActivity.this).setMessage("미션 위치 확인 시 GPS(위치) 활성화가 필요합니다.").setPositiveButton("활성화", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);//gps(위치) 접근 인텐트
                                startActivity(myIntent);
                            }
                        }).setNegativeButton("다음에", null).create().show();
                    } else {
                        missionLocation(); //미션위치확인
                    }
                }
            } else {//마시멜로 이하 버젼
                if (!isGpsEnabled && !isInternetGPSEnabled) {
                    new AlertDialog.Builder(MissionImageActivity.this).setMessage("미션 위치 확인 시 GPS(위치) 활성화가 필요합니다.").setPositiveButton("활성화", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        }
                    }).setNegativeButton("다음에", null).create().show();
                } else {
                    missionLocation(); //미션위치확인
                }
            }
        }//end of if else


    }//미션위치확인 글씨 끝

    //미션위치확인
    public void missionLocation() {

        //사진을 업로드할때 프로그레스다이알로그 만들기
        dialog = new ProgressDialog(MissionImageActivity.this);
        dialog.setMessage("미션장소 위치 확인 중\n시간이 다소 소요됩니다.\n(*실내에서는 위치 확인이 안될 수 있습니다.)");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        //해당번째 미션 레이아웃 번호와,  미션이미지 버튼 번호를 가지고 위도 경도를 저장한다.
        //미션의 위도 경도와 내 위치를 비교해 반경1km에서 사진을 업로드 시킬 수 있게 한다.
        //퍼미션 체크
        if (ActivityCompat.checkSelfPermission(MissionImageActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MissionImageActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (locationManager.isProviderEnabled("gps")) {
            locationManager.requestLocationUpdates("gps", 1000, 2, locationListener); //10000 >10초마다 2는 2미터마다 해당 값을 갱신한다, ,
        } else {
            locationManager.requestLocationUpdates("network", 1000, 2, locationListener);
        }

    }//end of missionLocation

    //위치정보를 듣는 리스너 객체
    LocationListener locationListener = new LocationListener() {
        //위치가 변경될 때 마다
        @Override
        public void onLocationChanged(Location location) {

            double latitude = location.getLatitude();//위도
            double logitude = location.getLongitude();//경도

            //현재 내위치와(latitude, logitude)와 미션지사이의 실제거리(m)
            float[] result = new float[3];//거리결과를 저장할 float[] 참조변수 선언
            Location.distanceBetween(latitude, logitude, missionLatitude, missionLogitude, result);//두점사이의 거리를 구해주는 메서드
            // 37.738075, 127.033632 의정부 시청 // 37.781710, 127.024666 우리집
            //result[0]에 두 좌표사이의 m거리가 계산되어 저장되어 있음.
            if (result[0] < 500) {//두 좌표의 거리가 500m 이내인가?

                if (isEntering == false) {

                    dialog.dismiss();

                    //미션위치확인 쓰레드 올리기
                    new Thread() {
                        @Override
                        public void run() {
                            String serverUrl = G.domain + "php/mission/islocation.php";

                            try {
                                URL url = new URL(serverUrl);

                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setUseCaches(false);
                                connection.setDoInput(true);
                                connection.setDoOutput(true);


                                String data = "id=" + G.mem_id + "&layoutPos=" + layoutPos + "&imgbtnPos=" + imgbtnPos;
                                OutputStream os = connection.getOutputStream();
                                os.write(data.getBytes());

                                os.flush();
                                os.close();

                                InputStream is = connection.getInputStream();
                                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                                BufferedReader reader = new BufferedReader(isr);

                                StringBuffer buffer = new StringBuffer();
                                String line = reader.readLine();

                                while (true) {
                                    buffer.append(line);
                                    line = reader.readLine();
                                    if (line == null) break;
                                    buffer.append("\n");
                                }

                                final String result = buffer.toString().trim();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (result.equals("success")) {
                                            Toast.makeText(MissionImageActivity.this, "위치 인증 완료\n미션 사진을 변경하실 수 있습니다.", Toast.LENGTH_LONG).show();
                                            finish();
                                            startActivity(getIntent());
                                        } else {
                                            new AlertDialog.Builder(MissionImageActivity.this).setMessage(result).setPositiveButton("ok", null).create().show();
                                        }
                                    }
                                });

                                is.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }//emd of run()
                    }.start();


                    if (ActivityCompat.checkSelfPermission(MissionImageActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MissionImageActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.removeUpdates(locationListener);
                    //isEntering = true;
                } else {
                    locationManager.removeUpdates(locationListener);
                    isEntering = false;
                    dialog.dismiss();
                }
            } else {
                locationManager.removeUpdates(locationListener);
                dialog.dismiss();
                Toast.makeText(MissionImageActivity.this, "미션장소와 일치하지 않습니다.\n미션정보보기에서 미션장소를 확인하세요.", Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }; //end of LocationListener 끝

    //미션사진변경 글씨 누르면 발동
    public void uploadImg(View v) {

        //미션사진선택을 위한 다이얼로그
        AlertDialog.Builder builder = new AlertDialog.Builder(MissionImageActivity.this);
        builder.setTitle("미션사진선택");
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_dialog, null);

        takepic_btn = (Button) layout.findViewById(R.id.takepic_btn); //사진촬영 객체
        choicepic_btn = (Button) layout.findViewById(R.id.choicepic_btn); //사진선택 객체

        builder.setView(layout).create();
        picDlg = builder.show();

        //다이얼로그 사진촬영 버튼 누르면 발동
        takepic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraIntent();
                picDlg.dismiss();//다이얼로그 종료
            }
        });//end of takepic_btn Listener

        //다이얼로그 사진선택 버튼 누르면 발동
        choicepic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picDlg.dismiss();//다이얼로그 종료
                //사진첩은 메인액티비티에서 접근 가능해서 메인에 메서드를 만들고 호출해줌
                choicePic();
            }
        });//end of choicepic_btn

    }//미션사진변경 글씨 누르면 발동 메서드 끝


    //다이얼로그에서 사진선택 누르면 발동
    public void choicePic() {

        //사진갤러리 접근 전에 퍼미션 받기
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to write the permission.
                Toast.makeText(this, "Read/Write external storage", Toast.LENGTH_SHORT).show();
            }

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUESET_GALLERY);

        } else { //항상허용
            //api 버젼에 상관없이 갤러리에 접근하는 방법
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, REQ_PICTURE);
        }

    }//end of choicePic()

    //다이얼로그에서 사진촬영 누르면 발동
    public void cameraIntent() {
        // Check permission for CAMERA
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            // Callback onRequestPermissionsResult interceptado na Activity MainActivity
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MainActivity.REQUEST_CAMERA);
        } else {
            // permission has been granted, continue as usual
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//카메라 앱 실행
            startActivityForResult(intent, REQ_PICTURE); //REQ_CAMERA로 변경해야함!!
        }

    }//end of cameraIntent


    //카메라, 외부저장장치 읽고 쓰기,사용자가 퍼미션을 승인을 안했을 경우. 자동 콜백메서드
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA: //카메라 퍼미션
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//카메라 앱 실행
                    startActivityForResult(intent, REQ_PICTURE); //REQ_PICTURE에서 >> 변경해야함 REQ_CAMERA == '20'은 식별자,

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;

            case REQUESET_GALLERY: //사진선택을 눌렀을때 퍼미션 허용을 눌렀을 경우

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    //api 버젼에 상관없이 갤러리에 접근하는 방법
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    startActivityForResult(intent, REQ_PICTURE);

                } else {

                    Toast.makeText(this, "승인이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }//end of switch
    }//end of onRequestPermissionsResult

    //startActivityForResult()가 실행되고 callback되는 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case REQ_PICTURE:
                if (resultCode == RESULT_OK) {
                    //선택된 이미지의 정보를 가지고 있는 Uri객체 얻어오기/
                    imgUri = data.getData();

                    path = FileUtility.getRealPathFromURI(this, imgUri);//???? FileUtillity는 API번젼에 따라 다른  디바이스의 이미지 경로 가져오는 형식
                    File imgFile = new File(path);

                    if (imgFile.exists()) {
                        long imgFileSize = imgFile.length();

                        if (imgFileSize >= ONEMB) { //이미지 사이즈가 1mb 넘어가면 붙이기

                            try {
                                //해상도를 낮게 해서 oom(out of memory)에러에 대처하기 방법2
                                InputStream is = getContentResolver().openInputStream(imgUri);

                                // 폰의 화면 사이즈를 구한다.
                                Display display = ((WindowManager) this.getSystemService(
                                        Context.WINDOW_SERVICE)).getDefaultDisplay();
                                int displayWidth = display.getWidth();
                                int displayHeight = display.getHeight();

                                BitmapFactory.Options opts = new BitmapFactory.Options();

                                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                                opts.inJustDecodeBounds = true;

                                BitmapFactory.decodeStream(is, null, opts);
                                // 화면 사이즈에 가장 근접하는 이미지의 스케일 팩터를 구한다.
                                // 스케일 팩터는 이미지 손실을 최소화하기 위해 짝수로 한다.
                                float widthScale = opts.outWidth / displayWidth;
                                float heightScale = opts.outHeight / displayHeight;
                                float scale = widthScale > heightScale ? widthScale : heightScale;

                                if (scale >= 8) {
                                    opts.inSampleSize = 8;
                                } else if (scale >= 6) {
                                    opts.inSampleSize = 6;
                                } else if (scale >= 4) {
                                    opts.inSampleSize = 4;
                                } else if (scale >= 2) {
                                    opts.inSampleSize = 2;
                                } else {
                                    opts.inSampleSize = 1;
                                }
                                // Log.i("samplesize", opts.inSampleSize + "");
                                opts.inJustDecodeBounds = false;

                                is = getContentResolver().openInputStream(imgUri);
                                bm = BitmapFactory.decodeStream(is, null, opts);
                                int degree = 0;
                                ExifInterface exif = null;

                                // Toast.makeText(this, "실제패스입니다 : "+path, Toast.LENGTH_SHORT).show();
                                try {
                                    exif = new ExifInterface(path);
                                } catch (IOException e) {
                                    Log.e("TAG", "cannot read exif");
                                    e.printStackTrace();
                                }
                                if (exif != null) {
                                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
                                    if (orientation != -1) {
                                        // We only recognize a subset of orientation tag values.
                                        switch (orientation) {
                                            case ExifInterface.ORIENTATION_ROTATE_90:
                                                degree = 90;
                                                break;
                                            case ExifInterface.ORIENTATION_ROTATE_180:
                                                degree = 180;
                                                break;
                                            case ExifInterface.ORIENTATION_ROTATE_270:
                                                degree = 270;
                                                break;
                                        }//end of switch
                                    }
                                }//end of if
                                if (degree != 0 && bm != null) {
                                    Matrix m = new Matrix();
                                    m.setRotate(degree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
                                    try {
                                        Bitmap bm2 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                                        float ratio = (float) bm2.getHeight() / (float) bm2.getWidth(); //이미지의 높이/넓이 :세로가 가로에 대비한 길이비율
                                        int w = selected_img.getWidth(); //버튼도 가지고 있는 속성
                                        int h = (int) (w * ratio);
                                        // bm = Bitmap.createScaledBitmap(bm2, w, h, true);
                                        bm = Bitmap.createScaledBitmap(bm2, w / 2, h / 2, true);

                                        //생성된 bitmap객체를 byte[]로 만들어서 서버에 바로 전송!!!!!!
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                        byteArray = stream.toByteArray();

                                    } catch (OutOfMemoryError ex) {
                                        // We have no memory to rotate. Return the original bitmap.
                                    }
                                } else {
                                    float ratio = (float) bm.getHeight() / (float) bm.getWidth(); //이미지의 높이/넓이 :세로가 가로에 대비한 길이비율
                                    int w = selected_img.getWidth(); //버튼도 가지고 있는 속성
                                    int h = (int) (w * ratio);
                                    // bm = Bitmap.createScaledBitmap(bm2, w, h, true);
                                    bm = Bitmap.createScaledBitmap(bm, w / 2, h / 2, true);

                                    //생성된 bitmap객체를 byte[]로 만들어서 서버에 바로 전송!!!!!!
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    byteArray = stream.toByteArray();
                                }
                                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//statusbar hidden
                                layout_picshow.setVisibility(View.VISIBLE); //선택이미지 보여지는 뷰
                                selected_img.setImageBitmap(bm); // 선택된 이미지

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //END OF IF ONEMB 1MB보다 크면 이미지 사이즈를 줄임
                        } else {
                            //**********************************************    이미지 사이즈가 1MB보다 작으면 그냥 바로 업로드 ******************************************************//

                            try {
                                byteArray = null;
                                byteArray = org.apache.commons.io.FileUtils.readFileToByteArray(new File(path));

                                //해상도를 낮게 해서 oom(out of memory)에러에 대처하기 방법2
                                InputStream is = getContentResolver().openInputStream(imgUri);

                                // 폰의 화면 사이즈를 구한다.
                                Display display = ((WindowManager) this.getSystemService(
                                        Context.WINDOW_SERVICE)).getDefaultDisplay();
                                int displayWidth = display.getWidth();
                                int displayHeight = display.getHeight();

                                BitmapFactory.Options opts = new BitmapFactory.Options();

                                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                                opts.inJustDecodeBounds = true;

                                BitmapFactory.decodeStream(is, null, opts);
                                // 화면 사이즈에 가장 근접하는 이미지의 스케일 팩터를 구한다.
                                // 스케일 팩터는 이미지 손실을 최소화하기 위해 짝수로 한다.
                                float widthScale = opts.outWidth / displayWidth;
                                float heightScale = opts.outHeight / displayHeight;
                                float scale = widthScale > heightScale ? widthScale : heightScale;

                                if (scale >= 8) {
                                    opts.inSampleSize = 8;
                                } else if (scale >= 6) {
                                    opts.inSampleSize = 6;
                                } else if (scale >= 4) {
                                    opts.inSampleSize = 4;
                                } else if (scale >= 2) {
                                    opts.inSampleSize = 2;
                                } else {
                                    opts.inSampleSize = 1;
                                }
                                // Log.i("samplesize", opts.inSampleSize + "");
                                opts.inJustDecodeBounds = false;

                                is = getContentResolver().openInputStream(imgUri);
                                bm = BitmapFactory.decodeStream(is, null, opts);
                                int degree = 0;
                                ExifInterface exif = null;

                                try {
                                    exif = new ExifInterface(path);
                                } catch (IOException e) {
                                    Log.e("TAG", "cannot read exif");
                                    e.printStackTrace();
                                }
                                if (exif != null) {
                                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
                                    if (orientation != -1) {
                                        // We only recognize a subset of orientation tag values.
                                        switch (orientation) {
                                            case ExifInterface.ORIENTATION_ROTATE_90:
                                                degree = 90;
                                                break;
                                            case ExifInterface.ORIENTATION_ROTATE_180:
                                                degree = 180;
                                                break;
                                            case ExifInterface.ORIENTATION_ROTATE_270:
                                                degree = 270;
                                                break;
                                        }//end of switch
                                    }
                                }//end of if
                                if (degree != 0 && bm != null) {
                                    Matrix m = new Matrix();
                                    m.setRotate(degree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
                                    try {
                                        Bitmap bm2 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                                        float ratio = (float) bm2.getHeight() / (float) bm2.getWidth(); //이미지의 높이/넓이 :세로가 가로에 대비한 길이비율
                                        int w = selected_img.getWidth(); //버튼도 가지고 있는 속성
                                        int h = (int) (w * ratio);
                                        bm = Bitmap.createScaledBitmap(bm2, w / 2, h / 2, true);
                                    } catch (OutOfMemoryError ex) {
                                    }
                                }
                                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//statusbar hidden
                                layout_picshow.setVisibility(View.VISIBLE); //선택이미지 보여지는 뷰
                                selected_img.setImageBitmap(bm); // 선택된 이미지
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }//end of if 이미지 사이즈 확인해서 1mb보다 크면 사이지 줄이고 아니면 바로 업로드

                    } else {//파일이 존재 하지 않을 때
                        Toast.makeText(MissionImageActivity.this, "이미지 선택 에러", Toast.LENGTH_SHORT).show();
                    }

                }
                break;

        }//end of switch

        super.onActivityResult(requestCode, resultCode, data);
    }//end of onActivityResult


    //사진 저장버튼 클릭시
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void uploadPic(View v) {

        //17/02/25 추가 미션사진 설명텍스트 서버에 업로드 시키기
        String text = comment_edittext.getText().toString();
        //오늘 날짜 구하기
        Date currentDate = new Date();
        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat sdfMon = new SimpleDateFormat("MM");
        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        String date = sdfYear.format(currentDate) + "." + sdfMon.format(currentDate) + "." + sdfDay.format(currentDate) + " 미션수행";

        if (text.equals("")) { //미션 설명을 쓰지 않았다면
            //오늘날짜를 구함
            mText = date;
        } else {// 미션 설명 내용이 있다면
            mText = date + "\n\n" + text;
        }

        //사진업로드 전에 API번젼 체크를 한다. 19이상은 퍼미션을 받아야 한다.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {//현재 버전이 api(KitKet)버젼보다 낮은가?
            //FileInputStream객체 생성을 위해 이미지의 절대경로 얻어오기.
            imgPath = imgUri.toString();
            if (imgPath.contains("content://")) {//갤러리앱(or사진앱)으로 선택했을 때
                Cursor cursor = getContentResolver().query(imgUri, null, null, null, null);
                //커서를 처음 위치를 가리키기
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToFirst();//커서의 위치를 처음으로 옮기기
                    imgPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)); //!!!! 타겟폰에서 에러 !!!!
                    //컬럼이름이 '_data'의 데이타를 가져오는 것
                    cursor.close();
                }
            } else if (imgPath.contains("file://")) {
                imgPath = imgUri.getPath();//스키마없이 가져온다
            }

            imgUploadThread = new ImgUploadThread();
            imgUploadThread.start();
            processDialog();

        } else {//api 19이상은

            //FileInputStream객체 생성을 위해 이미지의 절대경로 얻어오기 (!!카메라로 사진 찍었을 때!!)
            imgPath = imgUri.toString();
            // content://media/external/images/media/.... 카메라로 사진을 찍을때 [겔럭시s5 테스트 확인]
            // 안드로이드 버전에 관계없이 경로가 com.android... 형식으로 집히지 않을 수 도 있음. [ 겔럭시S4 테스트 확인 ]
            if (imgPath.contains("external")) {
                Cursor cursor = getContentResolver().query(imgUri, null, null, null, null);
                //커서를 처음 위치를 가리키기
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToFirst();//커서의 위치를 처음으로 옮기기
                    imgPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    //컬럼이름이 '_data'의 데이타를 가져오는 것
                    cursor.close();
                }

            } else { //api19이상에서 //갤러리앱(or사진앱)으로 선택했을 때
                //content:///com.android.providers.media.documents/document/image/
                if (imgPath.contains("content://")) {
                    // Toast.makeText(this, imgPath, Toast.LENGTH_LONG).show();

                    String wholeID = DocumentsContract.getDocumentId(imgUri);
                    String id = wholeID.split(":")[1];
                    String[] column = {MediaStore.Images.Media.DATA};
                    String sel = MediaStore.Images.Media._ID + "=?";
                    Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            column, sel, new String[]{id}, null);
                    int columnIndex = cursor.getColumnIndex(column[0]);
                    if (cursor.moveToFirst()) {
                        imgPath = cursor.getString(columnIndex);
                    }//end of 안쪽 if end
                    cursor.close();
                } else if (imgPath.contains("file://")) {//파일매니져로 선택했을 대(디바이스의 기종에 따라 다름)
                    imgPath = imgUri.getPath();//스키마없이 가져온다
                }
            }//안쪽 if~ else 끝

            imgUploadThread = new ImgUploadThread();
            imgUploadThread.start();
            processDialog();

            // externalPermission();
        }
    }//end of uploadPic()

    //사진업로드 쓰레드
    class ImgUploadThread extends Thread {
        @Override
        public void run() {

            try {
                //이미지 upload할 서버주소
                String serverUri = G.domain + "php/uploadimg.php";
                URL urL = new URL(serverUri);

                HttpURLConnection connection = (HttpURLConnection) urL.openConnection(); //서버에 전송할 수 있게 HttpURL Connection에서 열어준다
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                //파일전송의 헤더영역 속성 설정(한글교안 헤더부분 파란색 부분)
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=*****");//boundary - 데이터 바디의 파일의 구분자역할

                //파일전송의 바디영역에 들어가 data 작성 및 Output
                OutputStream os = connection.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os); //DataOutputStream은 자동으로 utf-8로 전송이 된다. 한글 깨짐 생각안해도 됨.
                dos.writeBytes("\r\n--*****\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"id\"\r\n\r\n" + G.mem_id);
                dos.writeBytes("\r\n--*****\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"layoutPos\"\r\n\r\n" + layoutPos);
                dos.writeBytes("\r\n--*****\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"imgbtnPos\"\r\n\r\n" + imgbtnPos);

                //17/02/25추가 미션설명 텍스트
                dos.writeBytes("\r\n--*****\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"text\"\r\n\r\n" + URLEncoder.encode(mText, "utf-8"));

                dos.writeBytes("\r\n--*****\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"upload\"; filename=\"" + imgPath + "\"\r\n");//upload는 서버에서 받을 키값
                dos.writeBytes("\r\n");

                dos.write(byteArray, 0, byteArray.length); //이미지를 보여줄때 byte[]로 만든 객체를 바로 보내기

                dos.writeBytes("\r\n");
                dos.writeBytes("--*****--\r\n");

                dos.flush();
                //   fis.close();

                //서버로부터 파일업로드가 잘 되었는지 응답받기
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isr);

                StringBuffer sb = new StringBuffer();
                String line = reader.readLine();
                while (line != null) {
                    sb.append(line + "\n");
                    line = reader.readLine();
                }
                final String result = sb.toString().trim();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (result.equals("success")) { //이미지 업로드 성공시 보여주기
                            layout_picshow.setVisibility(View.INVISIBLE);
                            Toast.makeText(MissionImageActivity.this, "사진이 업로드 되었습니다.", Toast.LENGTH_LONG).show();
                            mGlideRequestManager.load(imgPath).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(missionimg);
                            mainActivity.finish();
                        }//end of if

                    }
                });
            } catch (FileNotFoundException e) {
                Toast.makeText(MissionImageActivity.this, "FileInputStream error", Toast.LENGTH_SHORT).show();
            }//end of catch
            catch (MalformedURLException e) {
                Toast.makeText(MissionImageActivity.this, "ServerUrl error", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(MissionImageActivity.this, "HttpURLConnection error", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        }//end of run()

    }//end of ImgUploadThread class

    public void processDialog() {
        //사진을 업로드할때 프로그레스다이알로그 만들기
        dialog = new ProgressDialog(MissionImageActivity.this);
        dialog.setMessage("사진을 업로드 중입니다.");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }//end of processDialog()

    //선택된 이미지 보여주는 화면 갤러리로 접근
    public void leftBtn(View v) {

        //갤러리로 접근
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQ_PICTURE);

    }

    //엑스자 닫기 버튼
    public void clickClose(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
        startActivity(intent);
        finish();
    }
}//end of class
