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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

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

    // 🔹 Firebase 인증 객체 (이름/구조 유지)
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // 🔹 기존 ID 그대로 사용
        et_id = (EditText) findViewById(R.id.ID_text);
        et_pw = (EditText) findViewById(R.id.PW_text);

        // 🔹 Firebase 초기화 (추가)
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

        // 🔹 1) 먼저 Firebase 로그인 시도
        fAuth.signInWithEmailAndPassword(sId, sPw)
                .addOnCompleteListener(login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("login", "Firebase 로그인 성공");
                            // 🔹 2) Firebase 성공 후 서버 로그인 병행
                            loginDB IDB = new loginDB();
                            IDB.execute();
                        } else {
                            Log.e("login", "Firebase 로그인 실패", task.getException());
                            Toast.makeText(getApplicationContext(),
                                    "로그인 실패: " + (task.getException() != null ? task.getException().getMessage() : "알 수 없는 오류"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // 🔹 서버 로그인 AsyncTask (이름/구조 유지)
    public class loginDB extends AsyncTask<Void, Integer, Void> {

        String data = "";

        @Override
        protected Void doInBackground(Void... unused) {

            String param = "u_id=" + sId + "&u_pw=" + sPw + "";
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
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    buff.append(line + "\n");
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

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);

            if (data.equals("1")) {
                // 🔹 Firebase + 서버 로그인 모두 성공
                Intent intent = new Intent(getApplicationContext(), com.example.pillmasterapp.after_login.class);
                startActivity(intent);
                overridePendingTransition(R.transition.anim_slide_in_left, R.transition.anim_slide_out_right);
                finish();

            } else if (data.equals("0") || data.equals("Can not find ID")) {
                // 🔹 서버 실패 → Firebase 로그아웃 (완전 동기화)
                fAuth.signOut();

                if (data.equals("0")) Log.e("RESULT", "비밀번호가 일치하지 않습니다.");
                else Log.e("RESULT", "등록되지 않은 아이디입니다.");

                alertBuilder
                        .setMessage("서버 로그인 실패로 Firebase 세션을 종료했습니다.\n사유: " +
                                (data.equals("0") ? "잘못된 비밀번호" : "가입하지 않은 아이디"))
                        .setCancelable(true)
                        .setPositiveButton("확인", (dialog, which) -> {});
                alertBuilder.create().show();

            } else if (data.equals("NETWORK_ERR")) {
                // 🔹 네트워크 오류 → Firebase 로그아웃(정책에 따라 유지해도 되지만 완전 동기화 요청이므로 로그아웃)
                fAuth.signOut();

                Log.e("RESULT", "네트워크 오류 발생");
                alertBuilder
                        .setMessage("서버 통신 오류로 Firebase 세션을 종료했습니다.\n네트워크 상태를 확인해주세요.")
                        .setCancelable(true)
                        .setPositiveButton("확인", (dialog, which) -> {});
                alertBuilder.create().show();

            } else {
                // 🔹 기타 에러 → Firebase 로그아웃
                fAuth.signOut();

                Log.e("RESULT", "에러 발생! ERRCODE = " + data);
                alertBuilder
                        .setMessage("서버 로그인 중 에러가 발생하여 Firebase 세션을 종료했습니다.\nerrcode: " + data)
                        .setCancelable(true)
                        .setPositiveButton("확인", (dialog, which) -> {});
                alertBuilder.create().show();
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

