package com.missionofseoul.seoul;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelector;


public class TalkWriteActivity extends AppCompatActivity {

    //액션바 멤버변수
    TextView abs_title;
    TextView save_text;

    EditText talk_edit;
    String talkText;

    //멀티 갤러리
    private static final int REQUEST_IMAGE = 2;
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;

    private ArrayList<String> mSelectPath;
    public RequestManager mGlideRequestManager;

    ArrayList<String> uris = new ArrayList<>();

    Iterator<String> mSelectPathIter;
    Iterator<String> urisIter;

    LinearLayout thumImgContainer;


    Uri[] imgUri;//사용자가 선택한 이미지의 경로정보를 보유한 Uri객체의 참조변수
    String[] imgPath;

    Bitmap bm;
    List<byte[]> byteArrays;

    byte[] bytes;

    MultiImgUploadThread imgUploadThread;

    final long ONEMB = 1048576; //1MB

    ProgressDialog dialog;

    //
    MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_talk_write);

        mGlideRequestManager = Glide.with(this);

        //17/03/17 추가
        mainActivity = MainActivity.mainActivity;

        //ActionBar 커스텀하기      //emulator api 16에서 custom error
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        //액션바 타이틀
        abs_title = (TextView) findViewById(R.id.abs_title);
        abs_title.setText("글쓰기");
        save_text = (TextView) findViewById(R.id.save_text);
        save_text.setVisibility(View.VISIBLE);


        talk_edit = (EditText) findViewById(R.id.talk_edit);

        thumImgContainer = (LinearLayout) findViewById(R.id.layout_linear);

    }//*************************************************** end of onCreate() ******************************************/

    //액션바의 저장버튼 클릭
    public void clickSave(View v) {

        talkText = talk_edit.getText().toString().trim();
        save_text.setClickable(false);
        try {
            if (talkText.equals("")) {
                Toast.makeText(this, "여행톡의 내용과 한 장 이상의 사진을 선택하셔야 합니다,", Toast.LENGTH_SHORT).show();
                save_text.setClickable(true);
            } else {

                //배열값 크기 결정
                imgUri = new Uri[mSelectPath.size()];//imgUrl은 file:///시작하는 전체경로
                imgPath = new String[mSelectPath.size()];
                byteArrays = new ArrayList<>();

                for (int i = 0; i < mSelectPath.size(); i++) {

                    File imgFile = new File(mSelectPath.get(i));
                    String strFileSize;

                    imgPath[i] = mSelectPath.get(i);
                    imgUri[i] = Uri.parse(uris.get(i));

                    if (imgFile.exists()) {
                        long imgFileSize = imgFile.length();

                        if (imgFileSize >= ONEMB) { //사진이 1MB보다 크다면 이미지 사이즈를 줄여라
                            /*************************************************/
                            try {
                                InputStream is = getContentResolver().openInputStream(imgUri[i]);

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

                                is = getContentResolver().openInputStream(imgUri[i]);
                                bm = BitmapFactory.decodeStream(is, null, opts);

                                int degree = 0;
                                ExifInterface exif = null;
                                try {
                                    exif = new ExifInterface(mSelectPath.get(i));
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
                                        int w = bm2.getWidth(); //버튼도 가지고 있는 속성
                                        int h = (int) (w * ratio);
                                        bm = Bitmap.createScaledBitmap(bm2, w / 2, h / 2, true);

                                        //생성된 bitmap객체를 byte[]로 만들어서 서버에 바로 전송!!!!!!
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                        bytes = new byte[stream.toByteArray().length];
                                        bytes = stream.toByteArray();
                                        byteArrays.add(bytes);
                                    } catch (OutOfMemoryError ex) {
                                        // We have no memory to rotate. Return the original bitmap.
                                    }
                                } else {
                                    float ratio = (float) bm.getHeight() / (float) bm.getWidth(); //이미지의 높이/넓이 :세로가 가로에 대비한 길이비율
                                    int w = bm.getWidth(); //버튼도 가지고 있는 속성
                                    int h = (int) (w * ratio);
                                    bm = Bitmap.createScaledBitmap(bm, w / 2, h / 2, true);

                                    //생성된 bitmap객체를 byte[]로 만들어서 서버에 바로 전송!!!!!!
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    bytes = new byte[stream.toByteArray().length];
                                    bytes = stream.toByteArray();
                                    byteArrays.add(bytes);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            //END OF IF 이미지 사이즈 확인 후 1MB보다 크면 이미지 사이즈 줄이기 끝
                            /********************************************************************************************/

                        } else { // 이미지 사이즈가 1MB보다 작으면 사이즈 줄이지 말기

                            bytes = new byte[(int) imgFile.length()];

                            try {
                                bytes = org.apache.commons.io.FileUtils.readFileToByteArray(new File(mSelectPath.get(i)));
                                byteArrays.add(bytes);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                    } else { //선택한 이미지 주소가 없을 때
                        Toast.makeText(this, "사진선택 에러", Toast.LENGTH_SHORT).show();
                    }

                }//end of for

                //사진을 업로드할때 프로그레스다이알로그 만들기
                dialog = new ProgressDialog(TalkWriteActivity.this);
                dialog.setMessage("사진을 업로드 중입니다.");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                //서버전송
                imgUploadThread = new MultiImgUploadThread();
                imgUploadThread.start();


            }//end of if
        } catch (Exception e) {
            Toast.makeText(this, "한 장 이상의 사진을 선택하셔야 합니다.", Toast.LENGTH_SHORT).show();
            save_text.setClickable(true);
        }


    }//액션바의 저장버튼 클릭 서버 전송 끝


    //갤러리 버튼 누르면 발동
    public void clickGallery(View v) {
        pickImage();
    }

    private void pickImage() { //갤러리 이동
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.mis_permission_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            boolean showCamera = true;
            int maxNum = 5;

            MultiImageSelector selector = MultiImageSelector.create(TalkWriteActivity.this);
            selector.showCamera(showCamera);
            selector.count(maxNum);
            selector.multi();
            selector.origin(mSelectPath);
            selector.start(TalkWriteActivity.this, REQUEST_IMAGE);
        }
    }//end of pickImage()

    private void requestPermission(final String permission, String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.mis_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.mis_permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(TalkWriteActivity.this, new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }//end of requestPermission()

    //카메라, 외부저장장치 읽고 쓰기,사용자가 퍼미션을 승인을 안했을 경우. 자동 콜백메서드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_READ_ACCESS_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }//end of onRequsetPermissionsResult


    //startActivityForResult()가 실행되고 callback되는 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {

                mSelectPath = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);

                uris.clear();
                thumImgContainer.removeAllViewsInLayout();
                for (int i = 0; i < mSelectPath.size(); i++) {
                    //    Toast.makeText(this, mSelectPath.get(i) + "", Toast.LENGTH_SHORT).show();
                    //  uris.add(Uri.parse("file://" + mSelectPath.get(i)));
                    uris.add("file://" + mSelectPath.get(i));

                }//end of for

                inflateThumbnails();

            }//end of if
        }
    }//end of onActivityResult


    private void inflateThumbnails() {
        for (int i = 0; i < uris.size(); i++) {
            View imageLayout = getLayoutInflater().inflate(R.layout.item_multi_selectimg, null);
            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.img_thumb01);
            ImageView closeView = (ImageView) imageLayout.findViewById(R.id.icon_close);
            imageView.setOnClickListener(onDeleteThumbnails(closeView));

            mGlideRequestManager.load(uris.get(i)).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView);
            thumImgContainer.addView(imageLayout);
            imageView.setTag(uris.get(i));//tag로  이미지주소값을 넘

        }

    } //end of inflateThumbnails()


    //썸네일 img클릭했을때 삭제
    View.OnClickListener onDeleteThumbnails(final View closeView) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tag = (String) v.getTag();//고유한 이미지 주소값으로 해당번째 이미지 삭제 이렇게해야 indexofboundsexception이 나지 않음
                String result = tag.substring(7);// file:// << 짜름

                mSelectPathIter = mSelectPath.iterator(); //반복자를 만듬
                urisIter = uris.iterator();

                while (mSelectPathIter.hasNext()) {
                    String tmp = mSelectPathIter.next();

                    if (tmp.equals(result)) {
                        mSelectPathIter.remove();
                    }
                }

                while (urisIter.hasNext()) {
                    String tmp01 = urisIter.next();
                    if (tmp01.equals(tag)) {
                        urisIter.remove();

                    }
                }
                if (v != null && closeView != null) {
                    ViewGroup parent = (ViewGroup) v.getParent();
                    ViewGroup close = (ViewGroup) closeView.getParent();

                    if (parent != null && close != null) {
                        parent.removeView(v);
                        close.removeView(closeView);
                    }
                }
            }//end of onClick
        };
    }//end of onChagePageClickListener


    //leftbtn 누르면 발동
    public void backBtn(View v) {
        finish();
    }

    class MultiImgUploadThread extends Thread {

        @Override
        public void run() {

            try {
                //이미지 upload할 서버주소
                String serverUri = G.domain + "php/traveltalk/multiuploadimg.php";
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
                dos.writeBytes("Content-Disposition: form-data; name=\"talkText\"\r\n\r\n" + URLEncoder.encode(talkText, "utf-8"));

                for (int i = 0; i < mSelectPath.size(); i++) {
                    dos.writeBytes("\r\n--*****\r\n");
                    // dos.writeBytes("Content-Disposition: form-data; name=upload_files[]; filename=" + mSelectPath.get(i) + "\r\n");//upload는 서버에서 받을 키값
                    dos.writeBytes("Content-Disposition: form-data; name=\"upload[]\"; filename=\"" + imgPath[i] + "\"\r\n");//upload는 서버에서 받을 키값
                    dos.writeBytes("\r\n");

                    try {
                        dos.write(byteArrays.get(i), 0, byteArrays.get(i).length); //이미지를 보여줄때 byte[]로 만든 객체를 바로 보내기
                    } catch (IndexOutOfBoundsException e) {

                    }
                    dos.writeBytes("\r\n");
                    dos.writeBytes("--*****--\r\n");
                }

                dos.flush();
                //   fis.close();

                //서버로부터 파일업로드가 잘 되었는지 응답받기
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader reader = new BufferedReader(isr);

                StringBuffer sb = new StringBuffer();
                String line = reader.readLine();

                while (line != null) {
                    sb.append(line + "\n");
                    line = reader.readLine();
                }
                final String result = sb.toString();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(TalkWriteActivity.this, result, Toast.LENGTH_LONG).show();
                        //Activity 다시 시작하기
                        save_text.setClickable(true);
                        finish();
                        startActivity(getIntent());

                        //
                        mainActivity.loadFragment();
                    }
                });
            } catch (FileNotFoundException e) {
                Toast.makeText(TalkWriteActivity.this, "FileInputStream error", Toast.LENGTH_SHORT).show();
            }//end of catch
            catch (MalformedURLException e) {
                Toast.makeText(TalkWriteActivity.this, "ServerUrl error", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(TalkWriteActivity.this, "HttpURLConnection error", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        }//end of run()


    }//end of thread class


}//end of TalkWriteActivityclass


