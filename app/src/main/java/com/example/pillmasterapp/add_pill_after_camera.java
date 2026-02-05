package com.example.pillmasterapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class add_pill_after_camera extends AppCompatActivity {

    private ImageView pillImageView;
    private Uri imageUri;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_pill_after_camera);

        pillImageView = findViewById(R.id.imageView);
        db = FirebaseFirestore.getInstance();

        // 전달받은 이미지 URI 표시
        imageUri = getIntent().getParcelableExtra("imageUri");
        if (imageUri != null) {
            pillImageView.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "이미지를 불러오지 못했습니다", Toast.LENGTH_SHORT).show();
        }
    }

    // 뒤로가기 버튼 (XML의 imageButton과 연결)
    public void back_button(View v) {
        finish();
    }

    // 검색하기 버튼 (XML의 button과 연결)
    public void search_button(View v) {
        if (imageUri == null) {
            Toast.makeText(this, "이미지가 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 AI 인식(tflite) 실행 → pillName 추출
        String pillName = runModel(imageUri);

        if (pillName == null || pillName.isEmpty()) {
            Toast.makeText(this, "알약 인식 실패", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Firebase 검색
        db.collection("medicines")
                .whereEqualTo("pill_name", pillName)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        for (QueryDocumentSnapshot doc : query) {
                            // ✅ Firebase에 데이터 있음 → 상세 화면 이동
                            Intent intent = new Intent(getApplicationContext(), show_detail.class);
                            intent.putExtra("pillName", pillName);
                            startActivity(intent);
                            overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);
                            return;
                        }
                    } else {
                        // ❌ Firebase에 없음 → 식약처 API 호출
                        fetchFromMFDS(pillName);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "검색 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // 🔹 AI 모델 실행 메서드 (tflite)
    private String runModel(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            // TODO: tflite 모델 로드 및 실행
            // 예시: Interpreter interpreter = new Interpreter(tfliteModel);
            // float[][] result = new float[1][NUM_CLASSES];
            // interpreter.run(inputTensor, result);
            // pillName = 클래스 인덱스 → 알약 이름 매핑

            return ""; // 결과 pillName 반환
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 🔹 식약처 API 호출 메서드
    private void fetchFromMFDS(String pillName) {
        OkHttpClient client = new OkHttpClient();

        // 식약처 OpenAPI URL (예시, 실제 키와 엔드포인트로 교체 필요)
        String url = "https://apis.data.go.kr/1471000/DrugPrdtPrmsnInfoService/getDrugPrdtPrmsnInfoList"
                + "?serviceKey=YOUR_API_KEY"
                + "&item_name=" + pillName
                + "&type=json";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(add_pill_after_camera.this, "식약처 API 호출 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonData);

                        // TODO: JSON 파싱해서 필요한 정보 추출
                        Map<String, String> pillInfo = new HashMap<>();
                        pillInfo.put("pillName", pillName);
                        pillInfo.put("pillData", jsonObject.toString());

                        // 상세 화면 이동
                        Intent intent = new Intent(getApplicationContext(), show_detail.class);
                        intent.putExtra("pillName", pillName);
                        intent.putExtra("pillInfo", pillInfo.get("pillData"));
                        startActivity(intent);
                        overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
