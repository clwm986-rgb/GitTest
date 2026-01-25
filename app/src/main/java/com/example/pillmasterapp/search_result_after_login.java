package com.example.pillmasterapp;

import static com.example.pillmasterapp.show_detail.pill;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class search_result_after_login extends AppCompatActivity {

    private ListView mlistView;
    private ListViewAdapterResult adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result);

        adapter = new ListViewAdapterResult();
        mlistView = findViewById(R.id.result_listView);
        mlistView.setAdapter(adapter);

        loadSearchResults();
    }

    /**
     * Firestore에서 로그인 후 검색 결과 불러오기
     */
    private void loadSearchResults() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Users").document(uid).collection("SearchResults")
                .get()
                .addOnSuccessListener((QuerySnapshot querySnapshot) -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(getApplicationContext(),
                                "검색 결과가 없습니다", Toast.LENGTH_SHORT).show();
                    }
                    for (DocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("pill_name");
                        String imageUrl = doc.getString("imageUrl");
                        adapter.addItem(imageUrl, name);
                    }
                    mlistView.setAdapter(adapter);

                    // 아이템 클릭 → 상세 화면 이동
                    mlistView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                        pill = adapter.getPillName(position);
                        show_detail();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(),
                            "검색 결과 불러오기 실패: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * 뒤로가기 버튼
     */
    public void after_back(View v) {
        Intent intent = new Intent(getApplicationContext(), after_login.class);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_a, R.transition.anim_slide_b);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    /**
     * 로그인 후 상세보기 화면 이동
     */
    public void show_detail() {
        Intent intent = new Intent(getApplicationContext(), show_detail.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);
    }
}

