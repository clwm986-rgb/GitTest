package com.example.pillmasterapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class name_search_result extends AppCompatActivity {

    private static final String TAG = "name_search_result";

    ListView mlistView;
    ListViewAdapterResult adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.name_search_result);

        adapter = new ListViewAdapterResult();
        mlistView = findViewById(R.id.result_listView);

        // 🔹 name_search.java에서 전달받은 검색어
        String pillName = getIntent().getStringExtra("pillName");

        if (pillName != null) {
            searchFirestore(pillName);   // Firestore 검색
            searchKFDAApi(pillName);     // 식약처 API 검색
        } else {
            Toast.makeText(this, "검색어가 없습니다", Toast.LENGTH_SHORT).show();
        }

        // 🔹 리스트 클릭 시 상세 화면으로 이동
        mlistView.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            String selectedName = adapter.getPillName(position);
            Intent intent = new Intent(getApplicationContext(), show_detail.class);
            intent.putExtra("pillName", selectedName); // ✅ pillId → pillName으로 수정
            startActivity(intent);
        });
    }

    // Firestore에서 검색
    private void searchFirestore(String pillName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("medicines") // ✅ SearchResults → medicines
                .whereEqualTo("pill_name", pillName)
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        String name = doc.getString("pill_name");
                        adapter.addItem(null, name); // 이미지 Glide로 처리 가능
                    }
                    mlistView.setAdapter(adapter);

                    if (query.isEmpty()) {
                        Toast.makeText(this, "Firestore 검색 결과 없음", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Firestore search error", e));
    }

    // 식약처 API 호출
    private void searchKFDAApi(String pillName) {
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

                String name = item.optString("ITEM_NAME", "정보 없음");

                adapter.addItem(null, name);
                mlistView.setAdapter(adapter);
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error", e);
        }
    }
}
