package com.limelight.binding.input.virtual_mouse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import com.limelight.binding.input.virtual_controller.VirtualControllerConfigurationLoader;
import com.limelight.nvstream.NvConnection;

public class VirtualRotaryScrollStick extends View {
    private static final int NORMAL_COLOR = 0x73888888;
    private static final int PRESSED_COLOR = 0xB03080FF;
    private static final int BORDER_COLOR = 0xA0FFFFFF;
    private static final float SCROLL_RADIANS_TO_WHEEL = 120f / ((float) Math.PI / 4f);
    private static final float MIN_DELTA_ANGLE = 0.02f;

    private final NvConnection conn;
    private final Paint paint = new Paint();

    private float radiusComplete;
    private boolean pressed;
    private float lastAngle;
    private boolean hasLastAngle;

    public VirtualRotaryScrollStick(Context context, NvConnection conn) {
        super(context);
        this.conn = conn;
        setFocusable(false);

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
    }

    private int getStrokeWidth() {
        return Math.max(2, VirtualControllerConfigurationLoader.getOscDefaultStrokeWidth(getContext()));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int size = Math.min(w, h);
        int strokeWidth = getStrokeWidth();
        radiusComplete = size / 2f - 2 * strokeWidth;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);

        int strokeWidth = getStrokeWidth();
        paint.setStrokeWidth(strokeWidth);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        paint.setColor(pressed ? PRESSED_COLOR : NORMAL_COLOR);
        canvas.drawCircle(cx, cy, radiusComplete, paint);

        paint.setColor(BORDER_COLOR);
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            float inner = radiusComplete * 0.75f;
            float outer = radiusComplete * 0.92f;
            canvas.drawLine(
                    cx + (float) (Math.cos(angle) * inner),
                    cy + (float) (Math.sin(angle) * inner),
                    cx + (float) (Math.cos(angle) * outer),
                    cy + (float) (Math.sin(angle) * outer),
                    paint);
        }
    }

    private float getAngleFromCenter(float x, float y) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        return (float) Math.atan2(y - cy, x - cx);
    }

    private static float normalizeAngleDelta(float delta) {
        while (delta > Math.PI) {
            delta -= (float) (2 * Math.PI);
        }
        while (delta < -Math.PI) {
            delta += (float) (2 * Math.PI);
        }
        return delta;
    }

    private void handleAngleDelta(float deltaAngle) {
        if (Math.abs(deltaAngle) < MIN_DELTA_ANGLE) {
            return;
        }

        // Counter-clockwise (decreasing angle) scrolls up; clockwise scrolls down.
        short scrollAmount = (short) (-deltaAngle * SCROLL_RADIANS_TO_WHEEL);
        if (scrollAmount != 0) {
            conn.sendMouseHighResScroll(scrollAmount);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float dx = event.getX() - cx;
        float dy = event.getY() - cy;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > radiusComplete && !pressed) {
            return false;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                requestUnbufferedDispatch(event);
                pressed = true;
                lastAngle = getAngleFromCenter(event.getX(), event.getY());
                hasLastAngle = true;
                break;

            case MotionEvent.ACTION_MOVE:
                if (!pressed) {
                    break;
                }
                float angle = getAngleFromCenter(event.getX(), event.getY());
                if (hasLastAngle) {
                    handleAngleDelta(normalizeAngleDelta(angle - lastAngle));
                }
                lastAngle = angle;
                hasLastAngle = true;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                pressed = false;
                hasLastAngle = false;
                break;

            default:
                break;
        }

        invalidate();
        return true;
    }
}
