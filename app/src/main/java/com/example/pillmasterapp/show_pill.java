package com.example.pillmasterapp;

import static com.example.pillmasterapp.saved_pill.saved_pill;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class show_pill extends AppCompatActivity {

    private ListView mlistView;
    private ListViewAdapter adapter;
    private TextView my_pill_textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_pill);

        adapter = new ListViewAdapter();
        mlistView = findViewById(R.id.listview1);
        my_pill_textview = findViewById(R.id.my_pill_text);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Users").document(uid).collection("Pills")
                .get()
                .addOnSuccessListener((QuerySnapshot querySnapshot) -> {
                    if (querySnapshot.isEmpty()) {
                        my_pill_textview.setText("복용 중인 알약이 없습니다");
                    }
                    for (DocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("pill_name");
                        String nickname = doc.getString("nickname");
                        String imageUrl = doc.getString("imageUrl");

                        adapter.addItem(name, nickname, imageUrl);
                    }
                    mlistView.setAdapter(adapter);

                    mlistView.setOnItemClickListener((parent, view, position, id) -> {
                        saved_pill = adapter.getPillName(position);
                        saved_pill();
                    });
                })
                .addOnFailureListener(e -> {
                    my_pill_textview.setText("알약 목록 불러오기 실패: " + e.getMessage());
                });
    }

    public void after_back(android.view.View v) {
        Intent intent = new Intent(getApplicationContext(), after_login.class);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_a, R.transition.anim_slide_b);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public void saved_pill() {
        Intent intent = new Intent(getApplicationContext(), saved_pill.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);
    }
}

