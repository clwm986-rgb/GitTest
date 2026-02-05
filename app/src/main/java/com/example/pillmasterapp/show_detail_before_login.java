package com.example.pillmasterapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

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

public class show_detail_before_login extends AppCompatActivity {

    private static final String TAG = "show_detail_before_login";

    ListView mlistView;
    ListViewAdapterDetail adapter;
    TextView tab2, tab3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_detail_before_login);

        TabHost tabHost1 = findViewById(R.id.tabHost1);
        tabHost1.setup();

        // 첫 번째 Tab
        TabHost.TabSpec ts1 = tabHost1.newTabSpec("Tab Spec 1");
        ts1.setContent(R.id.content1);
        ts1.setIndicator("기본 정보");
        tabHost1.addTab(ts1);

        // 두 번째 Tab
        TabHost.TabSpec ts2 = tabHost1.newTabSpec("Tab Spec 2");
        ts2.setContent(R.id.content2);
        ts2.setIndicator("효능 효과");
        tabHost1.addTab(ts2);
        tab2 = findViewById(R.id.tab2);

        // 세 번째 Tab
        TabHost.TabSpec ts3 = tabHost1.newTabSpec("Tab Spec 3");
        ts3.setContent(R.id.content3);
        ts3.setIndicator("주의 사항");
        tabHost1.addTab(ts3);
        tab3 = findViewById(R.id.tab3);

        adapter = new ListViewAdapterDetail();
        mlistView = findViewById(R.id.tab1_listView_before);

        // 🔥 Intent로 전달받은 알약 이름
        String pillName = getIntent().getStringExtra("pillName");

        if (pillName != null) {
            loadPillData(pillName);      // Firestore 데이터
            loadKFDAApiData(pillName);   // 식약처 API 데이터
        } else {
            Log.w(TAG, "pillName is null - no pill selected");
        }
    }

    // Firestore에서 데이터 가져오기
    private void loadPillData(String pillName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("medicines")
                .whereEqualTo("pill_name", pillName)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        for (DocumentSnapshot doc : query) {
                            showFirestoreResult(doc);
                        }
                    } else {
                        Log.d(TAG, "No Firestore document found for pill_name: " + pillName);
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error getting document", e));
    }

    private void showFirestoreResult(DocumentSnapshot document) {
        String name = document.getString("pill_name");
        String company = document.getString("company");
        String ingredient = document.getString("ingredient");
        String capacity = document.getString("capacity");
        String imageUrl = document.getString("imageUrl");

        // 기본 정보 탭 리스트뷰에 추가
        adapter.addItem(name, company, ingredient, capacity);
        mlistView.setAdapter(adapter);

        // 이미지 표시
        ImageView pillImage = findViewById(R.id.imageView_before);
        Glide.with(this)
                .load(imageUrl)
                .into(pillImage);
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
                Log.d(TAG, "KFDA API response: " + response);

                runOnUiThread(() -> showApiResult(response));

            } catch (Exception e) {
                Log.e(TAG, "Error calling KFDA API", e);
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
            Log.e(TAG, "JSON parsing error", e);
        }
    }

    public void show_detail_before_login_back(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_a, R.transition.anim_slide_b);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}

