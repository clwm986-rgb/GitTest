package com.example.pillmasterapp;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class YoloDetector {
    private Interpreter interpreter;

    // 모델 로드
    public YoloDetector(AssetManager assetManager) throws IOException {
        MappedByteBuffer modelBuffer = loadModelFile(assetManager, "best-fp16.tflite");
        interpreter = new Interpreter(modelBuffer);
    }

    // assets에서 모델 파일 읽기
    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // 추론 실행
    public float[][][] runInference(float[][][][] input) {
        // YOLOv5s 출력 구조: [1][25200][85]
        float[][][] output = new float[1][25200][85];
        interpreter.run(input, output);
        return output;
    }

    // 리소스 해제
    public void close() {
        if (interpreter != null) {
            interpreter.close();
        }
    }
}
