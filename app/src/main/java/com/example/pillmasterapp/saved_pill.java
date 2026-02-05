package com.example.pillmasterapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.bumptech.glide.Glide;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class saved_pill extends AppCompatActivity {

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

        // 🔥 Intent로 전달받은 알약 이름
        String pillName = getIntent().getStringExtra("pillName");

        if (pillName != null) {
            loadPillDetail(pillName);
            loadKFDAApiData(pillName);
        } else {
            tab2.setText("알약 이름 없음");
            tab3.setText("알약 이름 없음");
        }
    }

    private void loadPillDetail(String pillName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Users").document(uid).collection("Pills")
                .whereEqualTo("pill_name", pillName)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        for (DocumentSnapshot doc : query) {
                            String name = doc.getString("pill_name");
                            String company = doc.getString("company");
                            String ingredient = doc.getString("ingredient");
                            String capacity = doc.getString("capacity");
                            String imageUrl = doc.getString("imageUrl");

                            // 기본 정보 탭 리스트뷰에 추가
                            adapter.addItem(name, company, ingredient, capacity);
                            mlistView.setAdapter(adapter);

                            // 이미지 표시
                            ImageView pillImage = findViewById(R.id.saved_imageView);
                            Glide.with(this)
                                    .load(imageUrl)
                                    .into(pillImage);

                            // Firestore에 효능/주의사항이 있으면 표시
                            String efficiency = doc.getString("efficiency");
                            String warning = doc.getString("warning");
                            tab2.setText(efficiency != null ? efficiency : "정보 없음");
                            tab3.setText(warning != null ? warning : "정보 없음");
                        }
                    } else {
                        tab2.setText("저장된 알약 없음");
                        tab3.setText("저장된 알약 없음");
                    }
                })
                .addOnFailureListener(e -> {
                    tab2.setText("데이터 불러오기 실패: " + e.getMessage());
                    tab3.setText("데이터 불러오기 실패");
                });
    }

    // 식약처 API 호출
    private void loadKFDAApiData(String pillName) {
        new Thread(() -> {
            try {
                String apiUrl = "https://api.foodsafetykorea.go.kr/pillInfo?name=" + pillName;

                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                String response = sb.toString();

                runOnUiThread(() -> showApiResult(response));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    tab2.setText("API 호출 실패");
                    tab3.setText("API 호출 실패");
                });
            }
        }).start();
    }

    private void showApiResult(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray items = jsonObject.getJSONArray("items");

            if (items.length() > 0) {
                JSONObject item = items.getJSONObject(0);

                String efficiency = item.optString("EFFICIENCY", "정보 없음");
                String warning = item.optString("WARNING", "정보 없음");

                tab2.setText(efficiency);
                tab3.setText(warning);
            }

        } catch (JSONException e) {
            tab2.setText("API 데이터 파싱 오류");
            tab3.setText("API 데이터 파싱 오류");
        }
    }

    public void back_button(View v) {
        Intent intent = new Intent(getApplicationContext(), show_pill.class);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_a, R.transition.anim_slide_b);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}


