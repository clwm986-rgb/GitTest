package com.example.pillmasterapp;

import static com.example.pillmasterapp.login.sId;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class add_pill_user extends AppCompatActivity {

    CheckBox Mon_rb, Sun_rb, Tue_rb, Wed_rb, Thu_rb, Fri_rb, Sat_rb;

    final Context context = this;
    File img_internal_dir;
    String img_file_name;
    String user_id = sId;
    String pill_name;
    static final String TAG = "add_pill_user";
    private Bitmap img;
    private ImageView mImageView;
    final static int PICK_IMAGE = 1;
    final static int CAPTURE_IMAGE = 2;
    private String mCurrentPhotoPath;
    EditText et_nickname;
    TextView pill_name_info;
    static String nickname;

    // 🔹 show_detail에서 static으로 가져오던 값들 직접 정의
    public static String pill;
    public static String pill_comp;
    public static BitmapDrawable pill_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        img_internal_dir = cw.getDir("imgageDir", context.MODE_PRIVATE);

        setContentView(R.layout.add_pill_user);
        pill_name_info = findViewById(R.id.textView6);
        mImageView = findViewById(R.id.imageView);

        Button photoBtn = findViewById(R.id.button);
        photoBtn.setOnClickListener(v -> photoDialogRadio());

        final TimePicker picker = findViewById(R.id.timePicker);
        picker.setIs24HourView(false);
        et_nickname = findViewById(R.id.nick);

        Button saveBtn = findViewById(R.id.button2);
        saveBtn.setOnClickListener(v -> {
            nickname = et_nickname.getText().toString().trim();
            Mon_rb = findViewById(R.id.Button_Mon);
            Sun_rb = findViewById(R.id.Button_Sun);
            Tue_rb = findViewById(R.id.Button_Tue);
            Wed_rb = findViewById(R.id.Button_Wed);
            Thu_rb = findViewById(R.id.Button_Thu);
            Fri_rb = findViewById(R.id.Button_Fri);
            Sat_rb = findViewById(R.id.Button_Sat);

            int hour_24, minute;
            if (Build.VERSION.SDK_INT >= 23) {
                hour_24 = picker.getHour();
                minute = picker.getMinute();
            } else {
                hour_24 = picker.getCurrentHour();
                minute = picker.getCurrentMinute();
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour_24);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);
            }

            diaryNotification(calendar);
        });

        if (pill != null) {
            pill_name = pill;
            pill_name_info.setText(pill);
            mImageView.setImageDrawable(pill_img);
            img = pill_img.getBitmap();
            TextView comp_textview = findViewById(R.id.textView5);
            comp_textview.setText(pill_comp);
        }
    }

    void diaryNotification(Calendar calendar) {
        boolean[] week = {false, Sun_rb.isChecked(), Mon_rb.isChecked(), Tue_rb.isChecked(),
                Wed_rb.isChecked(), Thu_rb.isChecked(), Fri_rb.isChecked(), Sat_rb.isChecked()};

        Intent alarmIntent = new Intent(this, com.example.pillmasterapp.AlarmReceiver.class);
        alarmIntent.putExtra("day_of_week", week);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }

        save();
    }

    private void photoDialogRadio() {
        final CharSequence[] PhotoModels = {"갤러리에서 가져오기", "카메라로 촬영 후 가져오기", "기본사진으로 하기"};
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setTitle("프로필사진 설정");
        alt_bld.setSingleChoiceItems(PhotoModels, -1, (dialog, item) -> {
            if (item == 0) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE);
            } else if (item == 1) {
                takePictureFromCameraIntent();
            } else {
                mImageView.setImageResource(R.drawable.camera2);
                img = null;
            }
        });
        alt_bld.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                InputStream in = getContentResolver().openInputStream(data.getData());
                img = BitmapFactory.decodeStream(in);
                in.close();
                mImageView.setImageBitmap(img);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                File file = new File(mCurrentPhotoPath);
                InputStream in = getContentResolver().openInputStream(Uri.fromFile(file));
                img = BitmapFactory.decodeStream(in);
                mImageView.setImageBitmap(img);
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void takePictureFromCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                return;
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.pillmasterapp.provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void save() {
        nickname = et_nickname.getText().toString().trim();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference()
                .child("pill_images/" + uid + "/" + pill_name + ".jpg");

        if (img != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            img.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            storageRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot ->
                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                Map<String, Object> pillData = new HashMap<>();
                                pillData.put("pill_name", pill_name);
                                pillData.put("nickname", nickname);
                                pillData.put("company", pill_comp);
                                pillData.put("imageUrl", uri.toString());
                                pillData.put("alarmTime", System.currentTimeMillis());

                                db.collection("Users").document(uid)
                                        .collection("Pills").add(pillData)
                                        .addOnSuccessListener(docRef -> {
                                            Toast.makeText(add_pill_user.this, "알약 저장 성공!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), after_login.class);
                                            startActivity(intent);
                                            overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(add_pill_user.this, "Firestore 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(add_pill_user.this, "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // 이미지가 없는 경우 Firestore에 텍스트 데이터만 저장
            Map<String, Object> pillData = new HashMap<>();
            pillData.put("pill_name", pill_name);
            pillData.put("nickname", nickname);
            pillData.put("company", pill_comp);
            pillData.put("imageUrl", ""); // 기본값
            pillData.put("alarmTime", System.currentTimeMillis());

            db.collection("Users").document(uid)
                    .collection("Pills").add(pillData)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(add_pill_user.this, "알약 저장 성공!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), after_login.class);
                        startActivity(intent);
                        overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(add_pill_user.this, "Firestore 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
