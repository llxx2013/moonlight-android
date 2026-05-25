package com.limelight.binding.input.virtual_mouse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import com.limelight.nvstream.NvConnection;

public class VirtualMouseScrollButton extends View {
    private static final short WHEEL_DELTA = 120;

    private static final int NORMAL_COLOR = 0x73888888;
    private static final int PRESSED_COLOR = 0xB03080FF;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private final NvConnection conn;
    private final short scrollAmount;
    private final String label;
    private final Paint fillPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final RectF bounds = new RectF();
    private boolean pressed;

    public VirtualMouseScrollButton(Context context, NvConnection conn, boolean scrollUp) {
        super(context);
        this.conn = conn;
        this.scrollAmount = scrollUp ? WHEEL_DELTA : (short) -WHEEL_DELTA;
        this.label = scrollUp ? "\u25B2" : "\u25BC";
        setFocusable(false);

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(TEXT_COLOR);

        textPaint.setAntiAlias(true);
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int strokeWidth = Math.max(2, getHeight() / 40);
        borderPaint.setStrokeWidth(strokeWidth);
        textPaint.setTextSize(getHeight() * 0.35f);

        bounds.set(strokeWidth, strokeWidth, getWidth() - strokeWidth, getHeight() - strokeWidth);
        fillPaint.setColor(pressed ? PRESSED_COLOR : NORMAL_COLOR);
        canvas.drawRoundRect(bounds, strokeWidth * 2, strokeWidth * 2, fillPaint);
        canvas.drawRoundRect(bounds, strokeWidth * 2, strokeWidth * 2, borderPaint);

        float textY = getHeight() / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(label, getWidth() / 2f, textY, textPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                pressed = true;
                conn.sendMouseHighResScroll(scrollAmount);
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                pressed = false;
                invalidate();
                return true;

            default:
                return super.onTouchEvent(event);
        }
    }
}
