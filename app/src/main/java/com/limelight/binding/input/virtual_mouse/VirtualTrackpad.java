package com.limelight.binding.input.virtual_mouse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import com.limelight.nvstream.NvConnection;

public class VirtualTrackpad extends View {
    private static final int NORMAL_COLOR = 0x73888888;
    private static final int BORDER_COLOR = 0xA0FFFFFF;

    private final NvConnection conn;
    private final View streamView;
    private final Paint fillPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final RectF bounds = new RectF();

    public VirtualTrackpad(Context context, NvConnection conn, View streamView) {
        super(context);
        this.conn = conn;
        this.streamView = streamView;
        setFocusable(false);

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int strokeWidth = Math.max(2, getHeight() / 80);
        borderPaint.setStrokeWidth(strokeWidth);

        bounds.set(strokeWidth, strokeWidth, getWidth() - strokeWidth, getHeight() - strokeWidth);
        fillPaint.setColor(NORMAL_COLOR);
        canvas.drawRoundRect(bounds, strokeWidth * 2, strokeWidth * 2, fillPaint);
        borderPaint.setColor(BORDER_COLOR);
        canvas.drawRoundRect(bounds, strokeWidth * 2, strokeWidth * 2, borderPaint);
    }

    private void sendPositionForTouch(float localX, float localY) {
        int streamWidth = streamView.getWidth();
        int streamHeight = streamView.getHeight();
        if (streamWidth <= 0 || streamHeight <= 0 || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        float normalizedX = localX / getWidth();
        float normalizedY = localY / getHeight();
        normalizedX = Math.min(Math.max(normalizedX, 0f), 1f);
        normalizedY = Math.min(Math.max(normalizedY, 0f), 1f);

        short x = (short) (normalizedX * streamWidth);
        short y = (short) (normalizedY * streamHeight);
        conn.sendMousePosition(x, y, (short) streamWidth, (short) streamHeight);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                requestUnbufferedDispatch(event);
                sendPositionForTouch(event.getX(), event.getY());
                return true;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getHistorySize(); i++) {
                    sendPositionForTouch(
                            event.getHistoricalX(i),
                            event.getHistoricalY(i));
                }
                sendPositionForTouch(event.getX(), event.getY());
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return true;

            default:
                return super.onTouchEvent(event);
        }
    }
}
