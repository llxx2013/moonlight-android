package com.limelight.binding.input.virtual_mouse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import com.limelight.binding.input.MouseStickEmulationHelper;
import com.limelight.binding.input.virtual_controller.VirtualControllerConfigurationLoader;
import com.limelight.nvstream.NvConnection;

public class VirtualMoveStick extends View {
    private static final int NORMAL_COLOR = 0x73888888;
    private static final int PRESSED_COLOR = 0xB03080FF;
    private static final int BORDER_COLOR = 0xA0FFFFFF;
    private static final long REPORT_PERIOD_MS = 50;
    private static final long DEADZONE_RELEASE_MS = 150;

    private enum StickState {
        NO_MOVEMENT,
        MOVED_IN_DEAD_ZONE,
        MOVED_ACTIVE
    }

    private final NvConnection conn;
    private final Handler handler;
    private final Paint paint = new Paint();

    private float radiusComplete;
    private float radiusDeadZone;
    private float radiusAnalogStick;

    private double movementRadius;
    private double movementAngle;
    private float positionStickX;
    private float positionStickY;
    private StickState stickState = StickState.NO_MOVEMENT;
    private boolean pressed;
    private long timeLastDown;

    private float stickX;
    private float stickY;

    private final Runnable reportRunnable = new Runnable() {
        @Override
        public void run() {
            if (!pressed) {
                return;
            }
            MouseStickEmulationHelper.sendEmulatedMouseMove(conn, stickX, stickY);
            handler.postDelayed(this, REPORT_PERIOD_MS);
        }
    };

    public VirtualMoveStick(Context context, NvConnection conn) {
        super(context);
        this.conn = conn;
        this.handler = new Handler(Looper.getMainLooper());
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
        radiusDeadZone = radiusComplete * 0.30f;
        radiusAnalogStick = radiusComplete * 0.20f;
        positionStickX = w / 2f;
        positionStickY = h / 2f;
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

        paint.setColor(NORMAL_COLOR);
        canvas.drawCircle(cx, cy, radiusDeadZone, paint);

        paint.setColor(pressed ? PRESSED_COLOR : NORMAL_COLOR);
        canvas.drawCircle(positionStickX, positionStickY, radiusAnalogStick, paint);

        paint.setColor(BORDER_COLOR);
        canvas.drawCircle(cx, cy, radiusComplete, paint);
    }

    private static double getMovementRadius(float x, float y) {
        return Math.sqrt(x * x + y * y);
    }

    private static double getAngle(float wayX, float wayY) {
        if (wayX == 0) {
            return wayY < 0 ? Math.PI : 0;
        }
        if (wayY == 0) {
            if (wayX > 0) {
                return Math.PI * 3 / 2;
            }
            return Math.PI / 2;
        }
        if (wayX > 0) {
            if (wayY < 0) {
                return 3 * Math.PI / 2 + Math.atan((double) (-wayY / wayX));
            }
            return Math.PI + Math.atan((double) (wayX / wayY));
        }
        if (wayY > 0) {
            return Math.PI / 2 + Math.atan((double) (wayY / -wayX));
        }
        return Math.atan((double) (-wayX / -wayY));
    }

    private void updatePosition(long eventTime) {
        float complete = radiusComplete - radiusAnalogStick;
        float correlatedY = (float) (Math.sin(Math.PI / 2 - movementAngle) * movementRadius);
        float correlatedX = (float) (Math.cos(Math.PI / 2 - movementAngle) * movementRadius);

        positionStickX = getWidth() / 2f - correlatedX;
        positionStickY = getHeight() / 2f - correlatedY;

        stickState = (stickState == StickState.MOVED_ACTIVE ||
                eventTime - timeLastDown > DEADZONE_RELEASE_MS ||
                movementRadius > radiusDeadZone) ?
                StickState.MOVED_ACTIVE : StickState.MOVED_IN_DEAD_ZONE;

        if (stickState == StickState.MOVED_ACTIVE) {
            stickX = -correlatedX / complete;
            stickY = correlatedY / complete;
        } else {
            stickX = 0;
            stickY = 0;
        }
    }

    private void startReporting() {
        handler.removeCallbacks(reportRunnable);
        handler.post(reportRunnable);
    }

    private void stopReporting() {
        handler.removeCallbacks(reportRunnable);
        stickX = 0;
        stickY = 0;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float relativeX = -(getWidth() / 2f - event.getX());
        float relativeY = -(getHeight() / 2f - event.getY());
        movementRadius = getMovementRadius(relativeX, relativeY);
        movementAngle = getAngle(relativeX, relativeY);

        if (movementRadius > radiusComplete && !pressed) {
            return false;
        }

        if (movementRadius > (radiusComplete - radiusAnalogStick)) {
            movementRadius = radiusComplete - radiusAnalogStick;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                requestUnbufferedDispatch(event);
                stickState = StickState.MOVED_IN_DEAD_ZONE;
                timeLastDown = event.getEventTime();
                pressed = true;
                startReporting();
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                pressed = false;
                stickState = StickState.NO_MOVEMENT;
                stopReporting();
                positionStickX = getWidth() / 2f;
                positionStickY = getHeight() / 2f;
                break;

            default:
                break;
        }

        if (pressed) {
            updatePosition(event.getEventTime());
        }

        invalidate();
        return true;
    }
}
