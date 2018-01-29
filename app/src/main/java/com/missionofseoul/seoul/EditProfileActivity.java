package com.missionofseoul.seoul;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

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
import java.util.Calendar;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class EditProfileActivity extends AppCompatActivity {

    //액션바 멤버변수
    TextView abs_title;
    TextView save_text;

    ImageView profile_img;   //프로필 이미지
    EditText edit_nick;//닉네임
    TextView nick_check; //닉네임 중복확인

    //생년월일
    TextView edit_birth;//생년월일 선택
    Date currentDate;
    private int iYear, iMonth, iDay;

    Spinner spinner;// 성별선택 스핀너
    ArrayAdapter adapter;

    //자기소개
    EditText edit_introduce;

    //인테트에서 가져온 값 받을 변수
    String profile;
    String nick;
    String gender;
    String birth;
    String introduce;

    public RequestManager mGlideRequestManager;

    //프로필변경 다이얼 로그 버튼
    Button editProfile, delProfile;
    DialogInterface picDlg;//다이얼로그 종료를 위한 멤버변수

    public static final int REQUESET_GALLERY = 1;
    final int REQ_PICTURE = 10;//갤러리앱에서 결과를 가져오는 intent의 식별할 수있게 도와주는 상수값

    Uri imgUri;//사용자가 선택한 이미지의 경로정보를 보유한 Uri객체의 참조변수
    //file입출력을 위해서는 file의 절대경로 필요 file// 스키마는 필요없다.(서버전송)
    String imgPath;//imgUri의 경로정보 문자열

    Bitmap bm;

    String path;// 실제패스 구하기 (파일사이즈 별로 크기 줄이는데 사용)
    byte[] byteArray; //
    final long ONEMB = 1048576; //1MB

    // 갤러리/ 사진첩에서 선택된 이미지 보여주는 레이아웃 변수
    RelativeLayout layout_picshow;
    ImageView selected_img;
    RelativeLayout layout_show_hidden;

    //닉네임 중복체크 확인 변수
    boolean isNickCheck = false;
    //이미지 변경체크 확인 변수
    int isProfileChange = 0; //'0'은 변경안됨 , '1'은 이미지 변경 '2'는 프로필 삭제(디폴트 값을 넣어줘야 함)

    ProgressDialog dialog;

    //쓰레드 변수
    ProfileGallThread profileGallThread;//프로필 이미지 사진에서 선택
    DefaultProfileThread defaultProfileThread;// 프로필을 삭제 후 기본 프로필 이미지 주소값 전송
    NickThread nickThread; // 닉네임 업로드 쓰레드
    BirthThread birthThread; // 생년월일 업로드 쓰레드
    GenderThread genderThread; //성별 업로드 쓰레드
    IntroduceThread introduceThread; //자기소개 업로드 쓰레드

    int mGender; //서버에 저장할 변수

    boolean b1 = true, b2 = true, b3 = true, b4 = true, b5 = true, b6 = true;

    // 모든 것이 변경되지 않고 저장버튼을 눌렀을 때 다이얼로그 끌 boolean 변수
    boolean c1, c2, c3, c4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_edit_profile);

        mGlideRequestManager = Glide.with(this);

        //ActionBar 커스텀하기      //emulator api 16에서 custom error
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        //액션바 타이틀
        abs_title = (TextView) findViewById(R.id.abs_title);
        abs_title.setText("프로필 수정");
        save_text = (TextView) findViewById(R.id.save_text);
        save_text.setVisibility(View.VISIBLE);
        save_text.setText("저장");

        // userActivity에서 가져온 값들
        Intent intent = getIntent();
        profile = intent.getStringExtra("Profile");
        nick = intent.getStringExtra("Nick");
        gender = intent.getStringExtra("Gender");
        birth = intent.getStringExtra("Birth");
        introduce = intent.getStringExtra("Introduce");

        //프로필 이미지
        profile_img = (ImageView) findViewById(R.id.profile);
        mGlideRequestManager.load(G.domain + profile).bitmapTransform(new CropCircleTransformation(this))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(profile_img);//프로필 이미지

        //닉네임 가져온 값으로 설정
        edit_nick = (EditText) findViewById(R.id.edit_nick);
        edit_nick.setText(nick);//닉네임 설정

        nick_check = (TextView) findViewById(R.id.nick_check);


        //닉네임이 변경되면 중복확인 글씨가 나온다
        edit_nick.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //닉네임변경이되면 기존 닉네임과 바뀐 닉네임이 다르면, 중복확인 체크
                if (!nick.equals(edit_nick.getText().toString().trim())) {
                    nick_check.setVisibility(View.VISIBLE);
                } else {
                    nick_check.setVisibility(View.INVISIBLE);
                }
            }
        });//닉네임 edit 변경 감지 리스너 닉네임이 변경되면 중복확인을 누르게 한다.

        //생년월일
        edit_birth = (TextView) findViewById(R.id.edit_birth);
        if (birth.equals("0")) {//서버에서 가져온값이 0이면 사용자가 설정 안함 오늘 날짜로 세팅
            currentDate = new Date();
            SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
            SimpleDateFormat sdfMon = new SimpleDateFormat("MM");
            SimpleDateFormat sdfDay = new SimpleDateFormat("dd");

            edit_birth.setText(sdfYear.format(currentDate) + "년" + sdfMon.format(currentDate) + "월" + sdfDay.format(currentDate) + "일");
        } else {
            edit_birth.setText(birth);
        }

        //성별
        spinner = (Spinner) findViewById(R.id.spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.gender, R.layout.spinner_gender_item);
        spinner.setAdapter(adapter);

        if (gender.equals("0")) {
            spinner.setSelection(0);
        } else if (gender.equals("1")) {
            spinner.setSelection(1);
        } else {
            spinner.setSelection(2);
        }

        //성별 선택 리스너
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        mGender = 0;
                        break;
                    case 1:
                        mGender = 1;
                        break;
                    case 2:
                        mGender = 2;
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //자기소개
        edit_introduce = (EditText) findViewById(R.id.edit_introduce);
        if (!introduce.equals("")) {
            edit_introduce.setText(introduce);
        }

        //선택된 이미지 보여주는 레이아웃 객체생성
        layout_picshow = (RelativeLayout) findViewById(R.id.layout_picshow);
        selected_img = (ImageView) findViewById(R.id.selected_img);
        layout_show_hidden = (RelativeLayout) findViewById(R.id.layout_show_hidden);

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


    }//********************************************** end of onCreate() **********************************************************

    //프로필 이미지 편집 누르면
    public void clickProfile(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("프로필사진 변경하기");
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_dialog, null);

        editProfile = (Button) layout.findViewById(R.id.takepic_btn); //사진선택 객체
        editProfile.setText("사진선택");
        delProfile = (Button) layout.findViewById(R.id.choicepic_btn); //사진삭제 객체
        delProfile.setText("사진삭제");
        builder.setView(layout).create();
        picDlg = builder.show();

        //사진삭제
        delProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (profile.contains("profile_defaultimg.png")) { // 프로필을 변경하지 않은 또는 이미삭제를 한 사람
                    isProfileChange = 0; //사진변경 없음
                } else {
                    isProfileChange = 2; //사진변경 변수 2대입 프로필 이미지
                    //사진이미지를 profile default값으로 변경
                    mGlideRequestManager.load("").bitmapTransform(new CropCircleTransformation(EditProfileActivity.this)).placeholder(R.drawable.profile)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(profile_img);
                }
                picDlg.dismiss();
                //서버에 보낼 때 프로필 기본 이미지 값 주소를 업데이트 보낸다.
            }
        });//사진삭제 끝

        //사진선택
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picDlg.dismiss();//다이얼로그 끄기
                //갤러리 접근
                //사진갤러리 접근 전에 퍼미션 받기
                if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(EditProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        // Explain to the user why we need to write the permission.
                        Toast.makeText(EditProfileActivity.this, "Read/Write external storage", Toast.LENGTH_SHORT).show();
                    }

                    ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUESET_GALLERY);

                } else { //항상허용

                    //api 버젼에 상관없이 갤러리에 접근하는 방법
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    startActivityForResult(intent, REQ_PICTURE);
                }
            }
        });


    }//end of 프로필 이미지 수정


    //startActivityForResult()가 실행되고 callback되는 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case REQ_PICTURE:
                if (resultCode == RESULT_OK) {
                    //선택된 이미지의 정보를 가지고 있는 Uri객체 얻어오기/
                    imgUri = data.getData();

                    path = FileUtility.getRealPathFromURI(this, imgUri);
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
                                ActionBar actionBar = getSupportActionBar();
                                actionBar.hide();
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
                                        // bm = Bitmap.createScaledBitmap(bm2, w, h, true);
                                        bm = Bitmap.createScaledBitmap(bm2, w / 2, h / 2, true);

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
                                ActionBar actionBar = getSupportActionBar();
                                actionBar.hide();
                                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//statusbar hidden
                                layout_picshow.setVisibility(View.VISIBLE); //선택이미지 보여지는 뷰
                                selected_img.setImageBitmap(bm); // 선택된 이미지

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }//end of if 이미지 사이즈 확인해서 1mb보다 크면 사이지 줄이고 아니면 바로 업로드

                    } else {//파일이 존재 하지 않을 때
                        Toast.makeText(EditProfileActivity.this, "이미지 선택 에러", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

        }//end of switch

        super.onActivityResult(requestCode, resultCode, data);
    }//end of onActivityResult

    //사진선택버튼 클릭시
    public void uploadPic(View v) {
        //사진첩에서 사진 선택
        isProfileChange = 1;

        layout_picshow.setVisibility(View.INVISIBLE);
        ActionBar actionBar = getSupportActionBar();
        actionBar.show();//액션바 다시 보이게
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//statusbar show

        imgPath = imgUri.toString();
        mGlideRequestManager.load(imgPath).bitmapTransform(new CropCircleTransformation(EditProfileActivity.this))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(profile_img);//프로필에 이미지 보이게 하기


    }//end of uploadPic

    //이미지 선택하고 left버튼
    public void leftBtn(View v) {
        //갤러리로 접근
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQ_PICTURE);

    }//end of leftBtn()

    /**************************************************************************************************************************/
    //생년월일 생성 DatePickerDialg 사용

    /**************************************************************************************************************************/
    public void clickEditBirth(View v) {

        String strDate = edit_birth.getText().toString();
        strDate = strDate.replace("년", "/").replace("월", "/").replace("일", "/");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
        try {
            Date pickDate = new Date(strDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(pickDate);
            Dialog dia = null;
            //strDate값을 기본값으로 날짜 선택 다이얼로그 생성
            dia = new DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dia.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//end of clickEditBirth

    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            iYear = year;
            iMonth = monthOfYear;
            iDay = dayOfMonth;
            updateEditText();
        }
    };//end of dateSetListener

    protected void updateEditText() {
        StringBuffer sb = new StringBuffer();
        edit_birth.setText(sb.append(iYear + "년").append((iMonth + 1) + "월").append(iDay + "일"));
    }//end of updateEditText

    /**************************************************************************************************************************/

    //액션바 저장 버튼를 누르면 서버에 member테이블 업데이트 시킴
    public void clickSave(View v) {
        //서버에 업데이트
        //1.사진이 변경되었는지 2. 닉네임이 변경되었는지 3.생년월일이 변경되었는지 4.성별이 변경되었는지 5.자기소개가 변경되었는지
        //사진업로드 따로 사진을 삭제 했을 때 기본프로필 이미지로 업로드 / 이미지를 변경했을 때 변경된 이미지를 업로드

        //닉네임이 변경되었으면 중복확인이 되었는지 확인
        if (!nick.equals(edit_nick.getText().toString())) {//닉네임이 변경
            //중복확인이 되었는지 물어어야 한다.
            if ((nick_check.getVisibility() == View.VISIBLE)) {//중복확인을 안누른 상태
                Toast.makeText(this, "닉네임 중복 확인을 하셔야 합니다.", Toast.LENGTH_SHORT).show();
            } else { //중복확인을 눌러 닉네임 인증에 성공시
                //변동사항을 다 각자 검사후 개별적인 쓰레드로 돌린다.

                dialog = new ProgressDialog(EditProfileActivity.this);
                dialog.setMessage("변경사항을 저장 중입니다.");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                //닉네임을 서버에 전송
                nickThread = new NickThread();
                nickThread.start();

                //이미가 변경 되었으면 이미지 전송
                switch (isProfileChange) {
                    case 0: //프로필 변경 없을 때
                        break;
                    case 1: //프로필 이미지 갤러리에서 선택
                        profileGallThread = new ProfileGallThread();
                        profileGallThread.start();
                        break;
                    case 2://프로필 삭제시 기본 이미 주소값 업로드
                        defaultProfileThread = new DefaultProfileThread();
                        defaultProfileThread.start();
                        break;
                } //이미가 변경 되었으면 이미지 전송

                //생년월이 변경 되었으면 생년월일 전송
                currentDate = new Date();
                SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
                SimpleDateFormat sdfMon = new SimpleDateFormat("MM");
                SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
                String result = sdfYear.format(currentDate) + "년" + sdfMon.format(currentDate) + "월" + sdfDay.format(currentDate) + "일";

                if (result.equals(edit_birth.getText().toString())) {//한번도 변경 안된 것
                } else if (birth.equals(edit_birth.getText().toString())) { //이전에 생년월일을 수정을 했지만 현재는 수정을 안한 상태
                } else {
                    //생년월일이 수정된 경우 업데이트 하면 됨.
                    birthThread = new BirthThread();
                    birthThread.start();
                }//생년월이 변경 되었으면 생년월일 전송 끝

                //성별이 변경되었으면 성별을 전송
                switch (mGender) {
                    case 0:
                        break;
                    case 1://남자가 선택 되었을 때
                        if (Integer.parseInt(gender) == 1) {//기존 값이랑 같으면 업로드 안함
                            break;
                        } else {
                            genderThread = new GenderThread();
                            genderThread.start();
                        }
                        break;
                    case 2:
                        if (Integer.parseInt(gender) == 2) {//기존 값이랑 같으면 업로드 안함
                            break;
                        } else {
                            genderThread = new GenderThread();
                            genderThread.start();
                        }
                        break;
                }

                //자기소개가 변경되었으면 자기소개 전송
                if (edit_introduce.getText().toString().trim().equals("") || introduce.equals(edit_introduce.getText().toString())) { //변경된게 없음
                } else {
                    //자기소개 서버에 전송
                    introduceThread = new IntroduceThread();
                    introduceThread.start();
                }

            }//end of if else //중복확인을 눌러 닉네임 인증에 성공시
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        } else {//닉네임이 변경이 안되었을 때

            dialog = new ProgressDialog(EditProfileActivity.this);
            dialog.setMessage("변경사항을 저장 중입니다.");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            //이미가 변경 되었으면 이미지 전송
            switch (isProfileChange) {
                case 0: //프로필 변경 없을 때
                    c1 = true;
                    break;
                case 1: //프로필 이미지 갤러리에서 선택
                    profileGallThread = new ProfileGallThread();
                    profileGallThread.start();
                    break;
                case 2://프로필 삭제시
                    defaultProfileThread = new DefaultProfileThread();
                    defaultProfileThread.start();
                    break;
            } //이미가 변경 되었으면 이미지 전송

            //생년월이 변경 되었으면 생년월일 전송
            currentDate = new Date();
            SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
            SimpleDateFormat sdfMon = new SimpleDateFormat("MM");
            SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
            String result = sdfYear.format(currentDate) + "년" + sdfMon.format(currentDate) + "월" + sdfDay.format(currentDate) + "일";

            if (result.equals(edit_birth.getText().toString())) {//한번도 변경 안된 것
                c2 = true;
            } else if (birth.equals(edit_birth.getText().toString())) { //이전에 생년월일을 수정을 했지만 현재는 수정을 안한 상태
                c2 = true;
            } else {
                //생년월일이 수정된 경우 업데이트 하면 됨.
                birthThread = new BirthThread();
                birthThread.start();
            }//생년월이 변경 되었으면 생년월일 전송 끝

            //성별이 변경되었으면 성별을 전송
            switch (mGender) {
                case 0:
                    c3 = true;
                    break;
                case 1://남자가 선택 되었을 때
                    if (Integer.parseInt(gender) == 1) {//기존 값이랑 같으면 업로드 안함
                        c3 = true;
                        break;
                    } else {
                        genderThread = new GenderThread();
                        genderThread.start();
                    }
                    break;
                case 2:
                    if (Integer.parseInt(gender) == 2) {//기존 값이랑 같으면 업로드 안함
                        c3 = true;
                        break;
                    } else {
                        genderThread = new GenderThread();
                        genderThread.start();
                    }
                    break;
            }

            //자기소개가 변경되었으면 자기소개 전송
            if (edit_introduce.getText().toString().trim().equals("") || introduce.equals(edit_introduce.getText().toString())) { //변경된게 없음
                c4 = true;
            } else {
                //자기소개 서버에 전송
                introduceThread = new IntroduceThread();
                introduceThread.start();
            }

            //아무것도 변경이 되지 않았느데 저장버튼을 누르면 다이얼로그 끄기
            if (c1 == true && c2 == true && c3 == true && c4 == true) {
                Toast.makeText(this, "변경된 내용이 없습니다.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

        }//닉네임이 변경 안되었을 때 다른 사항 체크

    }//end of click Save //액션바 저장 버튼를 누르면 서버에 member테이블 업데이트 시킴

    //닉네임 중복체크
    public void checkNick(View v) {

        if (edit_nick.getText().toString().trim().equals("")) {
            Toast.makeText(this, "닉네임을 입력해 주세요!", Toast.LENGTH_SHORT).show();
        } else if (edit_nick.getText().toString().trim().length() < 2) {
            Toast.makeText(this, "닉네임은 2자 이상 되어야합니다.", Toast.LENGTH_SHORT).show();
        } else {

            dialog = new ProgressDialog(EditProfileActivity.this);
            dialog.setMessage("닉네임 중복 확인 중입니다.");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            //닉네임 중복체크
            new Thread() {
                @Override
                public void run() {
                    String serverUrl = G.domain + "php/validate_nick.php";
                    String nick = edit_nick.getText().toString().trim();

                    try {
                        URL url = new URL(serverUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.setUseCaches(false);

                        String data = "nick=" + nick;

                        OutputStream os = connection.getOutputStream();
                        os.write(data.getBytes());

                        os.flush();
                        os.close();

                        //서버로부터 오는 echo를 읽어오기
                        InputStream is = connection.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader reader = new BufferedReader(isr);

                        final StringBuffer buffer = new StringBuffer();

                        String line = reader.readLine();

                        while (line != null) {
                            buffer.append(line + "\n");
                            line = reader.readLine();

                        }
                        //서버php에서 결과값으로 넘어온 것을 파싱해 블리언 값 주기
                        String result = buffer.toString().trim();

                        if (result.equals("success")) {
                            isNickCheck = true;
                        } else {
                            isNickCheck = false;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isNickCheck) { //중복체크 성공
                                    Toast.makeText(EditProfileActivity.this, "사용가능한 닉네임입니다.", Toast.LENGTH_SHORT).show();
                                    nick_check.setVisibility(View.INVISIBLE); //다시 중복확인 감추기
                                } else { //이미 사용중인 닉네임
                                    Toast.makeText(EditProfileActivity.this, "이미 사용중인 닉네임입니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            }.start();

        }//end of if else 닉네임 유효성 체크


    }//닉네임 중복체크 끝

    /***************************************************************************************************************************************/
    //쓰레드 클래스 영역

    /****************************************************************************************************************************************/

    //1. 사진첩에서 사진을 선택시에 사진을 서버에 전송 후 member 값 업데이트
    class ProfileGallThread extends Thread {
        @Override
        public void run() {
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
            } //사진업로드 전에 API번젼 체크를 한다.19이상은 퍼미션을 받아야 한다. 끝

            b1 = false; //자동 종료를 위해 쓰레드를 실행하면 false로

            try {
                //이미지 upload할 서버주소
                String serverUri = null;

                if (profile.contains("profile_defaultimg.png")) { //프로필 변경전이거나,이미 프로필을 삭제했을 경우
                    serverUri = G.domain + "php/editprofile/profileimg.php";
                } else {
                    serverUri = G.domain + "php/editprofile/del_profileimg.php";
                }

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
                dos.writeBytes("Content-Disposition: form-data; name=\"path\"\r\n\r\n" + profile);
                dos.writeBytes("\r\n--*****\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"upload\"; filename=\"" + imgPath + "\"\r\n");//upload는 서버에서 받을 키값
                dos.writeBytes("\r\n");

                dos.write(byteArray, 0, byteArray.length); //이미지를 보여줄때 byte[]로 만든 객체를 바로 보내기

                dos.writeBytes("\r\n");
                dos.writeBytes("--*****--\r\n");

                dos.flush();

                //서버로부터 파일업로드가 잘 되었는지 응답받기
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                final BufferedReader reader = new BufferedReader(isr);

                StringBuffer sb = new StringBuffer();
                String line = reader.readLine();
                while (line != null) {
                    sb.append(line + "\n");
                    line = reader.readLine();
                }

                String result = sb.toString().trim();
                if (result.equals("success")) {
                    b1 = true;
                    isDone();

                }
            } catch (FileNotFoundException e) {
                Toast.makeText(EditProfileActivity.this, "FileInputStream error", Toast.LENGTH_SHORT).show();
            }//end of catch
            catch (MalformedURLException e) {
                Toast.makeText(EditProfileActivity.this, "ServerUrl error", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(EditProfileActivity.this, "HttpURLConnection error", Toast.LENGTH_SHORT).show();
            }

        }
    }//1. 사진첩에서 사진을 선택시에 사진을 서버에 전송 후 member 값 업데이트

    //2. 사진을 삭제 기본 프로필 이미지 주소값 member 값 업데이트
    class DefaultProfileThread extends Thread {
        @Override
        public void run() {
            b2 = false; //자동 종료를 위해 쓰레드를 실행하면 false로

            String serverUrl = G.domain + "php/editprofile/defalutprofile.php";

            try {
                URL url = new URL(serverUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                String data = "id=" + G.mem_id + "&path=" + profile;
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
                if (buffer.toString().equals("success")) {
                    b2 = true;
                    isDone();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditProfileActivity.this, "네트워크가 불안정합니.\n사진삭제 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }//end of run
    } //2. 사진을 삭제 기본 프로필 이미지 주소값 member 값 업데이 끝

    //3. 닉네임 업로드 쓰레드
    class NickThread extends Thread {
        @Override
        public void run() {
            b3 = false;
            String serverUrl = G.domain + "php/editprofile/updatenick.php";

            try {
                URL url = new URL(serverUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                String data = "id=" + G.mem_id + "&nick=" + edit_nick.getText().toString();
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

                if (buffer.toString().equals("success")) {
                    b3 = true;
                    isDone();
                }

                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }//end of run
    }//3. 닉네임 업로드 쓰레드 끝

    //4.생년월일 업로드 쓰레드
    class BirthThread extends Thread {
        @Override
        public void run() {
            b4 = false;
            String serverUrl = G.domain + "php/editprofile/updatebirth.php";

            try {
                URL url = new URL(serverUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                String data = "id=" + G.mem_id + "&birth=" + edit_birth.getText().toString();
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

                if (buffer.toString().trim().equals("success")) {
                    b4 = true;
                    isDone();

                }
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }//end of run
    }//4.생년월일 업로드 쓰레드  끝

    //5.성별 선택하면 업로드 쓰레드
    class GenderThread extends Thread {
        @Override
        public void run() {
            b5 = false;
            String serverUrl = G.domain + "php/editprofile/updategender.php";
            try {
                URL url = new URL(serverUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                String data = "id=" + G.mem_id + "&gender=" + mGender;
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
                if (buffer.toString().equals("success")) {
                    b5 = true;
                    isDone();
                }

                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }// 성별 선택하면 업로드 쓰레드 끝

    //6.자기소개를 입력하면 업로드 쓰레드
    class IntroduceThread extends Thread {
        @Override
        public void run() {
            b6 = false;
            String serverUrl = G.domain + "php/editprofile/updateintroduce.php";

            try {
                URL url = new URL(serverUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //파일전송의 헤더영역 속성 설정(한글교안 헤더부분 파란색 부분)
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=*****");//boundary - 데이터 바디의 파일의 구분자역할

                //파일전송의 바디영역에 들어가 data 작성 및 Output
                OutputStream os = connection.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os); //DataOutputStream은 자동으로 utf-8로 전송이 된다. 한글 깨짐 생각안해도 됨.
                dos.writeBytes("\r\n--*****\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"id\"\r\n\r\n" + G.mem_id);
                dos.writeBytes("\r\n--*****\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"introduce\"\r\n\r\n"
                        + URLEncoder.encode(edit_introduce.getText().toString(), "utf-8"));

                dos.flush();

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

                if (buffer.toString().equals("success")) {
                    b6 = true;
                    isDone();
                }

                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }//자기소개 업로드 쓰레드  끝

    /************************************************************************************************************************************************/

    //액티비티 자종료를 위한 메서드
    public void isDone() {

        if (b1 == true && b2 == true && b3 == true && b4 == true && b5 == true && b6 == true) {
            dialog.dismiss();
            finish();
        }

    }


    //액션바의 뒤로가기 버튼
    public void backBtn(View v) {
        finish();
    }

    //디바이스 백버튼
    @Override
    public void onBackPressed() {
        //
        if (layout_picshow.getVisibility() == View.VISIBLE) { //이미지 업로드시 갤러시 선택 화면이면 뒤로가기 버튼 클릭시 화면 안보이게하기
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//statusbar show
            ActionBar actionBar = getSupportActionBar();
            actionBar.show();
            layout_picshow.setVisibility(View.INVISIBLE);
        } else {
            finish();
        }
    }//end of onBackPressed()

}//end of class
