package com.asiatravel.roundrippleprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by lslMac on 16/9/28.
 */
public class RoundRippleProgressView extends View {
    private int width;
    private int height;

    private int windowWidth;
    private int windowHeight;
    private Paint roundPaint;
    private Paint fontPaint;
    private Paint progressPaint;
    private String centerText = "";
    private int centerTextColor;
    private float centerTextSize;
    private int ballColor;
    private int progressColor;
    private int progress = 50;
    private int currentProgress = 0;
    private int maxProgress = 100;
    private float radius;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Path path = new Path();
    private SingleTapThread singleTapThread;
    private GestureDetector detector;
    private int space=30;
    private int move=0;

    public RoundRippleProgressView(Context context) {
        this(context, null);
    }

    public RoundRippleProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundRippleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        windowWidth = getResources().getDisplayMetrics().widthPixels;
        windowHeight = getResources().getDisplayMetrics().heightPixels;

        getCustomAttribute(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint() {
        roundPaint = new Paint();
        roundPaint.setColor(ballColor);
        roundPaint.setAntiAlias(true);
        fontPaint = new Paint();

        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setColor(progressColor);
        //取两层绘制交集。显示上层 PorterDuff.Mode 颜色渲染的模式 一共有16种枚举值.
        progressPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        fontPaint.setTextSize(centerTextSize);
        fontPaint.setColor(centerTextColor);
        fontPaint.setAntiAlias(true);
        fontPaint.setFakeBoldText(true);

        // 创建画布
        bitmap = Bitmap.createBitmap((int) radius * 2, (int) radius * 2, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
    }

    private void getCustomAttribute(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.customBallView);
        int indexCount = t.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int attr = t.getIndex(i);
            switch (attr) {
                case R.styleable.customBallView_centerText:
                    centerText = t.getString(R.styleable.customBallView_centerText);
                    break;
                case R.styleable.customBallView_centerTextColor:
                    centerTextColor = t.getColor(R.styleable.customBallView_centerTextColor, 0xFFFFFF);
                    break;
                case R.styleable.customBallView_centerTextSize:
                    centerTextSize = t.getDimension(R.styleable.customBallView_centerTextSize, 24f);
                    break;
                case R.styleable.customBallView_ballColor:
                    ballColor = t.getColor(R.styleable.customBallView_ballColor, 0x3A8C6C);
                    break;
                case R.styleable.customBallView_progressColor:
                    progressColor = t.getColor(R.styleable.customBallView_progressColor, 0x00ff00);
                    break;
                case R.styleable.customBallView_ballRadius:
                    radius = t.getDimension(R.styleable.customBallView_ballRadius, 260f);
                    radius = Math.min(Math.min(windowWidth/2,windowHeight/2),radius);
                    break;
            }

        }
        t.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int w;
        int h;
        if (widthMode == MeasureSpec.EXACTLY) {
            w = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            w = (int) Math.min(widthSize, radius * 2);
        } else {

            w = windowWidth;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            h = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            h = (int) Math.min(heightSize, radius * 2);
        } else {
            h = windowHeight;
        }
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getWidth();
        height = getHeight();
        bitmapCanvas.drawCircle(width / 2, height / 2, radius, roundPaint);

        path.reset();
        int count = (int) (radius + 1) * 2 / space;
        float y = (1 - (float) currentProgress / maxProgress) * radius * 2 + height / 2 - radius;
        move+=20;
        if (move>width)
        {
            move=width;
        }
        path.moveTo(-width+y, y);
        float d = (1 - (float) currentProgress / maxProgress) *space;
        for (int i = 0; i < count; i++) {
            path.rQuadTo(space, -d, space * 2, 0);
            path.rQuadTo(space, d, space * 2, 0);
        }
        path.lineTo(width, y);
        path.lineTo(width, height);
        path.lineTo(0, height);
        path.close();
        bitmapCanvas.drawPath(path, progressPaint);
        String text = currentProgress + "%";
        float textWidth = fontPaint.measureText(centerText);
        Paint.FontMetrics fontMetrics = fontPaint.getFontMetrics();
        float x = width / 2 - textWidth / 2;
        float dy = -(fontMetrics.descent + fontMetrics.ascent) / 2;
        float y1 = height / 2 + dy;
        bitmapCanvas.drawText(text, x, y1, fontPaint);
        canvas.drawBitmap(bitmap, 0, 0, null);
        setClickable(true);
        if (detector==null){
            detector = new GestureDetector(new MyGestureDetector());
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return detector.onTouchEvent(event);
                }
            });

        }
    }

    public class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            getHandler().removeCallbacks(singleTapThread);
            singleTapThread=null;
            Snackbar.make(RoundRippleProgressView.this, "暂停进度，是否重置进度？", Snackbar.LENGTH_LONG).setAction("重置", new OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentProgress=0;
                    invalidate();
                }
            }).show();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Snackbar.make(RoundRippleProgressView.this, "单机了", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            startProgressAnimation();
            return super.onSingleTapConfirmed(e);
        }
    }

    private void startProgressAnimation() {
        if (singleTapThread == null) {
            singleTapThread = new SingleTapThread();
            getHandler().postDelayed(singleTapThread, 100);
        }
    }

    private class SingleTapThread implements Runnable {
        @Override
        public void run() {
            if (currentProgress < maxProgress) {
                invalidate();
                getHandler().postDelayed(singleTapThread, 100);
                currentProgress++;
            } else {
                getHandler().removeCallbacks(singleTapThread);
            }
        }
    }
}
