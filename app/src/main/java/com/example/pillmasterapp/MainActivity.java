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
import android.view.MenuItem;
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

import org.pytorch.Module;
import org.pytorch.IValue;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int CAMERA_REQUEST = 100;
    private static final int GALLERY_REQUEST = 200;
    static final int REQUEST_TAKE_PHOTO = 1;

    private Module module;   // CRNN 모델 객체

    /* 이미지 socket 통신 부분 */
    private Handler mHandler;
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private String ip = "172.29.58.139"; //PC IPv4 주소
    private int port = 5000; //pill_img_search_server.py
    private String img_path;

    final String TAG = getClass().getSimpleName();
    ImageView imageView;
    ImageButton cameraBtn;
    TextView result_text;
    Button buttonEvent;
    static int check_flag = 1;
    String mCurrentPhotoPath;
    private Bitmap img;
    private Bitmap rotatedBitmap = null;
    ImageView photoImageView;
    private String mImage;

    static String mark = null;
    static String shape = null;
    private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // CRNN 모델 불러오기 (assets 폴더에 crnn_scripted.pt 넣어야 함)
        module = Module.load(assetFilePath(this, "crnn_scripted.pt"));

        // 레이아웃과 변수 연결
        imageView = findViewById(R.id.imageView4);
        cameraBtn = findViewById(R.id.cameraButton);
        cameraBtn.setOnClickListener(this);

        // 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "권한 설정 완료");
            } else {
                Log.d(TAG, "권한 설정 요청");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    // 카메라 촬영 및 갤러리 결과 처리 (하나로 합침)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(bitmap);
                runYolo(bitmap);

                // === CRNN 추론 추가 ===
                Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                        bitmap,
                        new float[]{0.5f}, new float[]{0.5f}
                );
                IValue output = module.forward(IValue.from(inputTensor));
                Tensor outputTensor = output.toTensor();
                float[] scores = outputTensor.getDataAsFloatArray();
                result_text.setText("인식 결과: " + "decodedText");

            } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
                File file = new File(mCurrentPhotoPath);
                InputStream in = getContentResolver().openInputStream(Uri.fromFile(file));
                img = BitmapFactory.decodeStream(in);

                setContentView(R.layout.after_camera);
                buttonEvent = findViewById(R.id.searchbutton);
                buttonEvent.setOnTouchListener((view, motionEvent) -> {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        buttonEvent.setBackgroundColor(Color.parseColor("#3EB6A0"));
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        buttonEvent.setBackgroundColor(Color.parseColor("#7E56C5"));
                        buttonEvent.setText("검색 중");
                    }
                    return false;
                });

                photoImageView = findViewById(R.id.imageView);

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
                        default:
                            rotatedBitmap = img;
                    }

                    photoImageView.setImageBitmap(rotatedBitmap);
                    in.close();

                    runYolo(rotatedBitmap);

                    // === CRNN 추론 추가 ===
                    Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                            rotatedBitmap,
                            new float[]{0.5f}, new float[]{0.5f}
                    );
                    IValue output = module.forward(IValue.from(inputTensor));
                    Tensor outputTensor = output.toTensor();
                    float[] scores = outputTensor.getDataAsFloatArray();
                    result_text.setText("인식 결과: " + "decodedText");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // YOLO 추론 실행 함수
    private void runYolo(Bitmap bitmap) {
        try {
            YoloDetector detector = new YoloDetector(getAssets());
            float[][][][] input = preprocessBitmap(bitmap, 640, 640);
            float[][][] results = detector.runInference(input);

            for (int i = 0; i < results[0].length; i++) {
                float[] prediction = results[0][i];
                float confidence = prediction[4];
                if (confidence > 0.25f) {
                    int classId = getMaxClass(prediction);
                    float x = prediction[0];
                    float y = prediction[1];
                    float w = prediction[2];
                    float h = prediction[3];
                    Log.d(TAG, "탐지됨: class=" + classId + " conf=" + confidence +
                            " bbox=(" + x + "," + y + "," + w + "," + h + ")");
                }
            }
            detector.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private float[][][][] preprocessBitmap(Bitmap bitmap, int inputWidth, int inputHeight) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true);
        float[][][][] input = new float[1][inputHeight][inputWidth][3];

        for (int y = 0; y < inputHeight; y++) {
            for (int x = 0; x < inputWidth; x++) {
                int pixel = resized.getPixel(x, y);
                input[0][y][x][0] = ((pixel >> 16) & 0xFF) / 255.0f;
                input[0][y][x][1] = ((pixel >> 8) & 0xFF) / 255.0f;
                input[0][y][x][2] = (pixel & 0xFF) / 255.0f;
            }
        }
        return input;
    }

    private int getMaxClass(float[] prediction) {
        int maxIndex = 5;
        float maxValue = prediction[5];
        for (int i = 6; i < prediction.length; i++) {
            if (prediction[i] > maxValue) {
                maxValue = prediction[i];
                maxIndex = i;
            }
        }
        return maxIndex - 5;
    }

    public String readUTF8(DataInputStream in) throws IOException {
        int length = in.readInt();
        byte[] encoded = new byte[length];
        in.readFully(encoded, 0, length);
        return new String(encoded, UTF8_CHARSET);
    }

    void connect() {
        mHandler = new Handler();
        Thread checkUpdate = new Thread() {
            public void run() {
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArray);
                byte[] bytes = byteArray.toByteArray();

                try {
                    socket = new Socket(ip, port);
                } catch (IOException e1) {
                    Log.w("서버접속못함", "서버접속못함");
                    e1.printStackTrace();
                }

                try {
                    dos = new DataOutputStream(socket.getOutputStream());
                    dis = new DataInputStream(socket.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.w("버퍼", "버퍼생성 잘못됨");
                }

                try {
                    dos.writeUTF(Integer.toString(bytes.length));
                    dos.flush();

                    dos.write(bytes);
                    dos.flush();

                    img_path = readUTF8(dis);
                    mark = readUTF8(dis);
                    shape = readUTF8(dis);
                    System.out.println(img_path);
                    System.out.println(mark);
                    System.out.println(shape);
                    socket.close();

                } catch (Exception e) {
                    Log.w("error", "error occur");
                }
            }
        };
        checkUpdate.start();
        try {
            checkUpdate.join();
        } catch (InterruptedException e) {
        }
        System.out.println("Thread terminated");
    }

    public void search_button(View v) {
        connect();
        Intent intent = new Intent(getApplicationContext(), com.example.pillmasterapp.search_result.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);
    }

    public void back_button(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_a, R.transition.anim_slide_b);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Toast.makeText(getApplicationContext(), " main 가 눌렸습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cameraButton) {
            dispatchTakePictureIntent();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.pillmasterapp.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
        check_flag++;
    }

    public void camera_click(View v) {
    }

    public void login_click(View v) {
        Intent intent = new Intent(getApplicationContext(), login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);
    }

    public void alarm_click(View v) {
        Intent intent = new Intent(getApplicationContext(), add_pill_user.class);
        startActivity(intent);
    }

    public void signup_click(View v) {
        Intent intent = new Intent(getApplicationContext(), signup.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);
    }

    private static String assetFilePath(android.content.Context context, String assetName) {
        java.io.File file = new java.io.File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }
        try (InputStream is = context.getAssets().open(assetName)) {
            try (FileOutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e("assetFilePath", "Error process asset " + assetName, e);
        }
        return null;
    }
}
