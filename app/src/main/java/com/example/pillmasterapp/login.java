package com.example.pillmasterapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class login extends AppCompatActivity {

    final Context context = this;

    EditText et_id, et_pw;
    public static String sId;
    static String sPw;

    // 🔹 Firebase 인증 객체
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        et_id = findViewById(R.id.ID_text);
        et_pw = findViewById(R.id.PW_text);

        fAuth = FirebaseAuth.getInstance();
    }

    public void login_button(View v) {
        try {
            sId = et_id.getText().toString();
            sPw = et_pw.getText().toString();
        } catch (NullPointerException e) {
            Log.e("err", e.getMessage());
        }

        if (sId == null || sId.isEmpty() || sPw == null || sPw.isEmpty()) {
            Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Firebase 로그인 시도
        fAuth.signInWithEmailAndPassword(sId, sPw)
                .addOnCompleteListener(login.this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("login", "Firebase 로그인 성공");
                        // 🔹 Firebase 성공 후 서버 로그인 병행
                        loginDB IDB = new loginDB();
                        IDB.execute();
                    } else {
                        Log.e("login", "Firebase 로그인 실패", task.getException());
                        Toast.makeText(getApplicationContext(),
                                "로그인 실패: " + (task.getException() != null ? task.getException().getMessage() : "알 수 없는 오류"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 🔹 서버 로그인 AsyncTask
    public class loginDB extends AsyncTask<Void, Integer, Void> {

        String data = "";

        @Override
        protected Void doInBackground(Void... unused) {
            String param = "email=" + sId + "&password=" + sPw;
            Log.e("POST", param);
            try {
                URL url = new URL("http://203.255.176.79:8000/login.php");
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
                StringBuilder buff = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    buff.append(line).append("\n");
                }
                data = buff.toString().trim();

                Log.e("RECV DATA", data);

            } catch (MalformedURLException e) {
                e.printStackTrace();
                data = "NETWORK_ERR";
            } catch (IOException e) {
                e.printStackTrace();
                data = "NETWORK_ERR";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                JSONObject jsonObject = new JSONObject(data);
                boolean success = jsonObject.getBoolean("success");

                if (success) {
                    String email = jsonObject.optString("email");
                    Toast.makeText(login.this, "로그인 성공: " + email, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), com.example.pillmasterapp.after_login.class);
                    startActivity(intent);
                    overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);
                    finish();
                } else {
                    fAuth.signOut();
                    Toast.makeText(login.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                fAuth.signOut();
                Toast.makeText(login.this, "JSON 파싱 오류", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void signup_button(View v) {
        Intent intent = new Intent(getApplicationContext(), com.example.pillmasterapp.signup.class);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public void login_back(View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.transition.anim_slide_a, R.transition.anim_slide_b);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}


