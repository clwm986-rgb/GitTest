package com.example.pillmasterapp;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;

// 🔥 Storage & Auth import
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.auth.FirebaseAuth;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Firebase 초기화
        FirebaseApp.initializeApp(this);

        // App Check 디버그 모드 설치
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
        );

        Log.d("MyApplication", "🔥 MyApplication onCreate 실행됨!");

        // 🔥 Storage 테스트 호출
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child("test.txt");
        ref.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Log.d("StorageTest", "🔥 Storage 호출 성공: " + uri.toString());
                })
                .addOnFailureListener(e -> {
                    Log.e("StorageTest", "🔥 Storage 호출 실패", e);
                });

        // 🔥 Auth 테스트 호출 (익명 로그인)
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("AuthTest", "🔥 익명 로그인 성공");
                    } else {
                        Log.e("AuthTest", "🔥 익명 로그인 실패", task.getException());
                    }
                });
    }
}




