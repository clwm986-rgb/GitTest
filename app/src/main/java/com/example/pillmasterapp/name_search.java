package com.example.pillmasterapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;

public class name_search extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.name_search);

        SearchView sv = findViewById(R.id.search_bar);
        sv.setSubmitButtonEnabled(true);

        // 🔹 SearchView의 검색 이벤트
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            // 검색버튼을 눌렀을 경우
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 🔹 검색어를 Intent Extra로 전달
                Intent intent = new Intent(getApplicationContext(), name_search_result.class);
                intent.putExtra("pillName", query); // 검색어 전달
                startActivity(intent);
                overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);
                return true;
            }

            // 텍스트가 바뀔 때마다 호출
            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    // 뒤로가기 버튼 클릭 시
    public void after_back(View v) {
        Intent intent = new Intent(getApplicationContext(), after_login.class);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_a, R.transition.anim_slide_b);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}

