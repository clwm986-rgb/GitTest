package com.example.pillmasterapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class signup extends AppCompatActivity {

    final Context context = this;

    EditText et_id, et_pw, et_pw_chk;
    String sId, sPw, sPw_chk;
    private FirebaseAuth fAuth;
    private DatabaseReference dRef; // 실시간 데이터베이스
    private FirebaseUser currentUser; // 현재 Firebase 유저

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        fAuth = FirebaseAuth.getInstance(); // FirebaseAuth 초기화
        dRef = FirebaseDatabase.getInstance().getReference();

        et_id = findViewById(R.id.signup_ID_text);
        et_pw = findViewById(R.id.signup_PW_text);
        et_pw_chk = findViewById(R.id.signup_chkPW_text);
    }

    public void signup_button(View v) {
        sId = et_id.getText().toString().trim();
        sPw = et_pw.getText().toString().trim();
        sPw_chk = et_pw_chk.getText().toString().trim();

        if (sId.equals("") || sPw.equals("") || sPw_chk.equals("")) {
            Toast.makeText(getApplicationContext(), "정보를 모두 기입해주세요.", Toast.LENGTH_SHORT).show();
        } else {
            if (sPw.equals(sPw_chk)) {
                // 🔹 Firebase 회원가입 처리
                fAuth.createUserWithEmailAndPassword(sId, sPw)
                        .addOnCompleteListener(signup.this, task -> {
                            if (task.isSuccessful()) {
                                currentUser = fAuth.getCurrentUser();
                                if (currentUser == null) return;

                                String uid = currentUser.getUid();
                                dRef.child("Users").child(uid).child("email").setValue(sId);

                                Toast.makeText(getApplicationContext(), "Firebase 회원가입 성공!", Toast.LENGTH_SHORT).show();

                                // 🔹 서버에도 동시에 회원가입 요청
                                registDB rdb = new registDB();
                                rdb.execute();

                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Firebase 회원가입 실패: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(getApplicationContext(), "비밀번호가 불일치합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 🔹 서버 DB 등록용 AsyncTask
    public class registDB extends AsyncTask<Void, Integer, Void> {
        String data = "";

        @Override
        protected Void doInBackground(Void... unused) {
            String param = "u_id=" + sId + "&u_pw=" + sPw;
            try {
                URL url = new URL("http://203.255.176.79:8000/snclib_join.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                InputStream is = conn.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line;
                StringBuffer buff = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    buff.append(line).append("\n");
                }
                data = buff.toString().trim();
                Log.e("RECV DATA", data);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e("RECV DATA", data);

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
            if (data.equals("0")) {
                alertBuilder.setMessage("서버 회원가입 완료")
                        .setCancelable(true)
                        .setPositiveButton("확인", (dialog, which) -> {
                            // 서버까지 성공했을 때만 로그인 화면 이동
                            Intent intent = new Intent(getApplicationContext(), login.class);
                            startActivity(intent);
                            finish();
                        });
            } else {
                alertBuilder.setMessage("서버 회원가입 실패 → Firebase 계정 롤백")
                        .setCancelable(true)
                        .setPositiveButton("확인", (dialog, which) -> {
                            // 서버 실패 시 Firebase 계정 삭제
                            if (currentUser != null) {
                                currentUser.delete().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(),
                                                "Firebase 계정 삭제 완료", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
            }
            alertBuilder.create().show();
        }
    }

    // 🔹 뒤로가기 버튼 처리
    public void after_back(View v) {
        Intent intent = new Intent(getApplicationContext(), login.class);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_a, R.transition.anim_slide_b);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Toast.makeText(getApplicationContext(), "뒤로가기가 눌렸습니다.", Toast.LENGTH_SHORT).show();
    }
}
