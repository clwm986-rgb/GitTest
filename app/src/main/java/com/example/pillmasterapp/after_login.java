package com.example.pillmasterapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class after_login extends AppCompatActivity implements View.OnClickListener {

    /* 이미지 socket 통신 부분 */
    private Handler mHandler;
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private String ip = "203.255.176.79";
    private int port = 8088; //pill_img_search_server.py
    private String img_path;

    final String TAG = getClass().getSimpleName();
    ImageView imageView;
    ImageButton cameraBtn;
    ImageButton cameraBtn2;
    Button buttonEvent;
    final static int TAKE_PICTURE = 1;
    private Bitmap img; ///
    private Bitmap rotatedBitmap = null;
    ImageView photoImageView;

    String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;

    static String after_login_mark = null;
    static String after_login_shape = null;
    private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_login);
        // 🔹 로그인된 이메일 표시
        String email = getIntent().getStringExtra("email");
        if (email != null) { TextView tvEmail = findViewById(R.id.tvEmail);
            tvEmail.setText("로그인된 계정: " + email); }
        // 레이아웃과 변수 연결

        cameraBtn = findViewById(R.id.cameraButton);

        // 카메라 버튼에 리스터 추가
        cameraBtn.setOnClickListener(this);
        //cameraBtn2.setOnClickListener(this);

        // 6.0 마쉬멜로우 이상일 경우에는 권한 체크 후 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "권한 설정 완료");
            } else {
                Log.d(TAG, "권한 설정 요청");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }


        }
    }

    public String readUTF8 (DataInputStream in) throws IOException {
        int length = in.readInt();
        byte[] encoded = new byte[length];
        in.readFully(encoded, 0, length);
        return new String(encoded, UTF8_CHARSET);
    }

    void connect(){
        mHandler = new Handler();

        //Log.w("connect","연결 하는중");
        Thread checkUpdate = new Thread() {
            public void run() {
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArray);
                byte[] bytes = byteArray.toByteArray();

                // 서버 접속
                try {
                    socket = new Socket(ip, port);
                    Log.w("after_login.java 서버 접속됨", "서버 접속됨");
                } catch (IOException e1) {
                    Log.w("서버접속못함", "서버접속못함");
                    e1.printStackTrace();
                }

                Log.w("edit 넘어가야 할 값 : ","안드로이드에서 서버로 연결요청");

                try {
                    Log.w("Image Length:", Integer.toString(bytes.length));
                    dos = new DataOutputStream(socket.getOutputStream());
                    dis = new DataInputStream(socket.getInputStream());

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.w("버퍼", "버퍼생성 잘못됨");
                }
                Log.w("버퍼","버퍼생성 잘됨");

                try{
                    dos.writeUTF(Integer.toString(bytes.length));
                    dos.flush();

                    dos.write(bytes);
                    dos.flush();

                    img_path = readUTF8(dis);
                    Log.w("img_path", img_path);
                    after_login_mark = readUTF8(dis);
                    after_login_shape = readUTF8(dis);
                    socket.close();

                }
                catch (Exception e){
                    Log.w("error", "error occur");
                }
            }
        };
        checkUpdate.start();
        try {
            checkUpdate.join();
        }catch (InterruptedException e){

        }
        System.out.println("Thread terminated");
    }

    public void search_button(View v) {
        connect();
        Intent intent = new Intent(getApplicationContext(), com.example.pillmasterapp.search_result_after_login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);

        Toast.makeText(getApplicationContext()," 검색하기가 눌렸습니다.", Toast.LENGTH_SHORT).show();
    }
    public void back_button(View v) {
        Intent intent = new Intent(getApplicationContext(), after_login.class);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_a, R.transition.anim_slide_b);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Toast.makeText(getApplicationContext()," 뒤로가기가 눌렸습니다.", Toast.LENGTH_SHORT).show();
    }

    // 권한 요청
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cameraButton) {// 카메라 앱을 여는 소스
            dispatchTakePictureIntent();
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    // 카메라로 촬영한 영상을 가져오는 부분
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) { //camera 화면 후
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO: {
                    if (resultCode == RESULT_OK) {
                        File file = new File(mCurrentPhotoPath);
                        InputStream in = getContentResolver().openInputStream(Uri.fromFile(file));
                        img = BitmapFactory.decodeStream(in);
                        setContentView(R.layout.after_camera);////////////
                        buttonEvent = (Button)findViewById(R.id.searchbutton);
                        buttonEvent.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {

                                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                    buttonEvent.setBackgroundColor(Color.parseColor("#3EB6A0"));
                                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                                    buttonEvent.setBackgroundColor(Color.parseColor("#7E56C5"));
                                    buttonEvent.setText("검색 중");
                                }

                                return false;
                            }
                        });

                        photoImageView = findViewById(R.id.imageView); /////

                        if (img != null) {
                            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED);

                            switch (orientation) {

                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotatedBitmap = rotateImage(img, 90);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotatedBitmap = rotateImage(img, 180);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotatedBitmap = rotateImage(img, 270);
                                    break;

                                case ExifInterface.ORIENTATION_NORMAL:
                                default:
                                    rotatedBitmap = img;
                            }

                            photoImageView.setImageBitmap(rotatedBitmap);

                            in.close();
                        }
                    }
                    break;
                }
            }

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    // 카메라로 촬영한 이미지를 파일로 저장해주는 함수
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // 카메라 인텐트를 실행하는 부분
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.myapplication.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public void after_back(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_a, R.transition.anim_slide_b);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    }

    public void name_search_button(View v) {
        Intent intent = new Intent(getApplicationContext(), name_search.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);

        //Toast.makeText(getApplicationContext()," 이름으로 검색 하기가 눌렸습니다.", Toast.LENGTH_SHORT).show();
    }
    public void show_pill_button(View v) {
        Intent intent = new Intent(getApplicationContext(), show_pill.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);

        //Toast.makeText(getApplicationContext()," 이름으로 검색 하기가 눌렸습니다.", Toast.LENGTH_SHORT).show();
    }
    public void add_pill(View v) {
        Intent intent = new Intent(getApplicationContext(), add_pill_user.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);

        //Toast.makeText(getApplicationContext()," 내 알약 등록이 눌렸습니다.", Toast.LENGTH_SHORT).show();
    }
}
