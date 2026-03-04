package com.example.pillmasterapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class OverlayView extends View {

    private List<Box> boxes;
    private Paint paint;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
    }

    public void setBoxes(List<Box> boxes){
        this.boxes = boxes;
        invalidate(); // 화면 다시 그림
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(boxes == null) return;

        for(Box b : boxes){
            float left = b.x - b.w/2;
            float top = b.y - b.h/2;
            float right = b.x + b.w/2;
            float bottom = b.y + b.h/2;

            canvas.drawRect(left, top, right, bottom, paint);
        }
    }
}