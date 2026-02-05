package com.example.pillmasterapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class after_camera extends AppCompatActivity {

    private ImageView imageView;
    private Uri imageUri; // 카메라/갤러리에서 받은 이미지 URI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_camera);

        imageView = findViewById(R.id.imageView);

        // 카메라/갤러리에서 전달받은 이미지 URI
        imageUri = getIntent().getParcelableExtra("imageUri");
        if (imageUri != null) {
            imageView.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "이미지를 불러오지 못했습니다", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔹 뒤로가기 버튼 (XML의 imageButton과 연결)
    public void back_button(View v) {
        finish();
    }

    // 🔹 검색하기 버튼 (XML의 searchbutton과 연결)
    public void search_button(View v) {
        if (imageUri == null) {
            Toast.makeText(this, "이미지가 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }
        // add_pill_after_camera로 이미지 전달
        Intent intent = new Intent(getApplicationContext(), add_pill_after_camera.class);
        intent.putExtra("imageUri", imageUri);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);
    }
}
