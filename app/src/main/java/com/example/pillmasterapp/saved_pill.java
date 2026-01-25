package com.example.pillmasterapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class saved_pill extends AppCompatActivity {

    public static String saved_pill = null; // show_pill에서 선택된 알약 이름

    private ListView mlistView;
    private ListViewAdapterDetail adapter;
    private TextView tab2;
    private TextView tab3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_pill);

        TabHost tabHost1 = findViewById(R.id.saved_tabHost1);
        tabHost1.setup();

        // 첫 번째 Tab
        TabHost.TabSpec ts1 = tabHost1.newTabSpec("Tab Spec 1");
        ts1.setContent(R.id.saved_content1);
        ts1.setIndicator("기본 정보");
        tabHost1.addTab(ts1);

        // 두 번째 Tab
        TabHost.TabSpec ts2 = tabHost1.newTabSpec("Tab Spec 2");
        ts2.setContent(R.id.saved_content2);
        ts2.setIndicator("효능 효과");
        tabHost1.addTab(ts2);
        tab2 = findViewById(R.id.saved_tab2);

        // 세 번째 Tab
        TabHost.TabSpec ts3 = tabHost1.newTabSpec("Tab Spec 3");
        ts3.setContent(R.id.saved_content3);
        ts3.setIndicator("주의 사항");
        tabHost1.addTab(ts3);
        tab3 = findViewById(R.id.saved_tab3);

        adapter = new ListViewAdapterDetail();
        mlistView = findViewById(R.id.saved_tab1_listView);

        loadPillDetail();
    }

    private void loadPillDetail() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Users").document(uid).collection("Pills")
                .document(saved_pill) // 선택된 알약 이름으로 문서 가져오기
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (doc.exists()) {
                        String name = doc.getString("pill_name");
                        String company = doc.getString("company");
                        String ingredient = doc.getString("ingredient");
                        String capacity = doc.getString("capacity");
                        String efficiency = doc.getString("efficiency");
                        String warning = doc.getString("warning");

                        // 기본 정보 탭 리스트뷰에 추가
                        adapter.addItem(name, company, ingredient, capacity);
                        mlistView.setAdapter(adapter);

                        // 효능 효과 탭
                        tab2.setText(efficiency != null ? efficiency : "정보 없음");

                        // 주의사항 탭
                        tab3.setText(warning != null ? warning : "정보 없음");
                    }
                })
                .addOnFailureListener(e -> {
                    tab2.setText("데이터 불러오기 실패: " + e.getMessage());
                    tab3.setText("데이터 불러오기 실패");
                });
    }

    public void back_button(View v) {
        Intent intent = new Intent(getApplicationContext(), show_pill.class);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_a, R.transition.anim_slide_b);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}

