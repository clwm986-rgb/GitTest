package com.example.pillmasterapp;

import static com.example.pillmasterapp.login.sId;
import static com.example.pillmasterapp.show_detail.pill;
import static com.example.pillmasterapp.show_detail.pill_comp;
import static com.example.pillmasterapp.show_detail.pill_img;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class add_pill_user extends AppCompatActivity {

    CheckBox Mon_rb, Sun_rb, Tue_rb, Wed_rb, Thu_rb, Fri_rb, Sat_rb;

    final Context context = this;
    com.example.pillmasterapp.search_result sr = new com.example.pillmasterapp.search_result(); //company이름을 받아오기 위한
    login login_ = new login();  // user id를 받아오기 위한
    File img_internal_dir;
    String img_file_name;
    String company;
    String user_id = sId;
    String pill_name;
    private Button button;
    static final String TAG = "ProfileActivityTAG";
    //RequestCode
    private String mTmpDownloadImageUri;
    private Bitmap img;
    private ImageView mImageView;
    final static int PICK_IMAGE = 1; //갤러리에서 사진선택
    final static int CAPTURE_IMAGE = 2;  //카메라로찍은 사진선택
    private String mCurrentPhotoPath;
    EditText et_nickname;
    TextView pill_name_info;
    static String nickname;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mImageView = findViewById(R.id.imageView);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        img_internal_dir = cw.getDir("imgageDir", context.MODE_PRIVATE);

        setContentView(R.layout.add_pill_user);
        pill_name_info = findViewById(R.id.textView6);
        button = findViewById(R.id.button); ////
        mImageView = findViewById(R.id.imageView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override //이미지 불러오기기
            public void onClick(View v) {
                photoDialogRadio(); //갤러리에서 불러오기 or 사진찍어서 불러오기
            }
        });


        final TimePicker picker = (TimePicker) findViewById(R.id.timePicker);
        picker.setIs24HourView(false);
        et_nickname = (EditText) findViewById(R.id.nick);
        // 앞서 설정한 값으로 보여주기 , 없으면 디폴트 값은 현재시간
        SharedPreferences sharedPreferences = getSharedPreferences("daily alarm", MODE_PRIVATE);
        long millis = sharedPreferences.getLong("nextNotifyTime", Calendar.getInstance().getTimeInMillis());

        Calendar nextNotifyTime = new GregorianCalendar();
        nextNotifyTime.setTimeInMillis(millis);
        Date nextDate = nextNotifyTime.getTime();
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(nextDate);

        // 이전 설정값으로 TimePicker 초기화
        Date currentTime = nextNotifyTime.getTime();
        SimpleDateFormat HourFormat = new SimpleDateFormat("kk", Locale.getDefault());
        SimpleDateFormat MinuteFormat = new SimpleDateFormat("mm", Locale.getDefault());

        int pre_hour = Integer.parseInt(HourFormat.format(currentTime));
        int pre_minute = Integer.parseInt(MinuteFormat.format(currentTime));


        //System.out.println("SDK_INT!!: "+ Build.VERSION.SDK_INT);

        if (Build.VERSION.SDK_INT >= 23) {
            picker.setHour(pre_hour);
            picker.setMinute(pre_minute);
        } else {
            picker.setCurrentHour(pre_hour);
            picker.setCurrentMinute(pre_minute);
        }


        Button button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                nickname = et_nickname.getText().toString().trim();
                System.out.println
                        ("========================================================");
                System.out.println(nickname);

                Mon_rb = (CheckBox) findViewById(R.id.Button_Mon);
                Sun_rb = (CheckBox) findViewById(R.id.Button_Sun);
                Tue_rb = (CheckBox) findViewById(R.id.Button_Tue);
                Wed_rb = (CheckBox) findViewById(R.id.Button_Wed);
                Thu_rb = (CheckBox) findViewById(R.id.Button_Thu);
                Fri_rb = (CheckBox) findViewById(R.id.Button_Fri);
                Sat_rb = (CheckBox) findViewById(R.id.Button_Sat);


                int hour, hour_24, minute;
                String am_pm;
                if (Build.VERSION.SDK_INT >= 23) {
                    hour_24 = picker.getHour();
                    minute = picker.getMinute();
                } else {
                    hour_24 = picker.getCurrentHour();
                    minute = picker.getCurrentMinute();
                }
                if (hour_24 > 12) {
                    am_pm = "PM";
                    hour = hour_24 - 12;
                } else {
                    hour = hour_24;
                    am_pm = "AM";
                }

                // 현재 지정된 시간으로 알람 시간 설정
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour_24);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                // 이미 지난 시간을 지정했다면 다음날 같은 시간으로 설정
                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DATE, 1);
                }

                Date currentDateTime = calendar.getTime();
                String date_text = new SimpleDateFormat
                        ("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);

                //  Preference에 설정한 값 저장
                SharedPreferences.Editor editor = getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
                editor.putLong("nextNotifyTime", (long) calendar.getTimeInMillis());
                editor.apply();

                diaryNotification(calendar);
            }

        });
        if (pill != null) {
            pill_name = pill;
            pill_name_info.setText(pill);
            mImageView.setImageDrawable(pill_img);
            img = ((BitmapDrawable) pill_img).getBitmap();
            TextView comp_textview = (TextView) findViewById(R.id.textView5);
            comp_textview.setText(pill_comp);
        }


    }

    void diaryNotification(Calendar calendar) {
        Boolean dailyNotify = true; // 무조건 알람을 사용

        boolean[] week
                = {false, Sun_rb.isChecked(), Mon_rb.isChecked(), Tue_rb.isChecked(), Wed_rb.isChecked(), Thu_rb.isChecked(), Fri_rb.isChecked(), Sat_rb.isChecked()};

        PendingIntent pendingIntent;
        AlarmManager alarmManager;
        PackageManager pm = this.getPackageManager();
        ComponentName receiver = new ComponentName(this, com.example.pillmasterapp.DeviceBootReceiver.class);
        Intent alarmIntent = new Intent(this, com.example.pillmasterapp.AlarmReceiver.class);

        alarmIntent.putExtra("one_time", false);
        alarmIntent.putExtra("day_of_week", week);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // 사용자가 매일 알람을 허용했다면
        if (dailyNotify) {

            if (alarmManager != null) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pendingIntent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }

            // 부팅 후 실행되는 리시버 사용가능하게 설정
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

        }
        save();
    }

    private void photoDialogRadio() {
        final CharSequence[] PhotoModels = {"갤러리에서 가져오기", "카메라로 촬영 후 가져오기", "기본사진으로 하기"};
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        //alt_bld.setIcon(R.drawable.icon);
        alt_bld.setTitle("프로필사진 설정");
        alt_bld.setSingleChoiceItems(PhotoModels, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Toast.makeText(add_pill_user.this, PhotoModels[item] + "가 선택되었습니다.", Toast.LENGTH_SHORT).show();
                if (item == 0) { //갤러리
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, PICK_IMAGE);
                } else if (item == 1) { //카메라찍은 사진가져오기
                    takePictureFromCameraIntent();
                } else { //기본화면으로하기
                    mImageView.setImageResource(R.drawable.camera2);
                    img = null;
                    mTmpDownloadImageUri = null;
                }
            }
        });
        AlertDialog alert = alt_bld.create();
        alert.show();
    }

    //갤러리에서 이미지 불러온 후 행동
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                InputStream in = getContentResolver().openInputStream(data.getData());
                img = BitmapFactory.decodeStream(in);
                in.close();
                // 이미지 표시
                mImageView.setImageBitmap(img);
                Log.d(TAG, "갤러리 inputStream: " + data.getData());
                Log.d(TAG, "갤러리 사진decodeStream: " + img);

                mTmpDownloadImageUri = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (resultCode == RESULT_OK) {
                try {
                    File file = new File(mCurrentPhotoPath);
                    InputStream in = getContentResolver().openInputStream(Uri.fromFile(file));
                    img = BitmapFactory.decodeStream(in);
                    mImageView.setImageBitmap(img);
                    in.close();

                    mTmpDownloadImageUri = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void takePictureFromCameraIntent() {
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
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE);
            }
        }
    }

    //카메라로 촬영한 이미지를파일로 저장해주는 함수
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

    public void after_back(View v) {
        Intent intent = new Intent(getApplicationContext(), com.example.pillmasterapp.show_detail.class);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_a, R.transition.anim_slide_b);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    private void saveToInternalStorage() {
        System.out.println("saveToInternalStorage>>");
        img_file_name = user_id.concat("_").concat(nickname).concat(".jpg");
        File img_file_path = new File(img_internal_dir, img_file_name);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(img_file_path);
            img.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* DB에 저장 */
    private void save() {
        nickname = et_nickname.getText().toString().trim();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("pill_images/" + uid + "/" + pill_name + ".jpg");

        if (img != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            img.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            storageRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> {
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
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(add_pill_user.this, "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
