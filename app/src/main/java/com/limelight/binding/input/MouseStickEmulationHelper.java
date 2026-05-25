package com.limelight.binding.input;

import com.limelight.nvstream.NvConnection;
import com.limelight.utils.Vector2d;

public final class MouseStickEmulationHelper {
    private static final float STICK_AXIS_MAX = 32766.0f;

    private MouseStickEmulationHelper() {}

    public static Vector2d convertRawStickAxisToPixelMovement(short stickX, short stickY) {
        Vector2d vector = new Vector2d();
        vector.initialize(stickX, stickY);
        vector.scalarMultiply(1 / STICK_AXIS_MAX);
        vector.scalarMultiply(4);
        if (vector.getMagnitude() > 0) {
            // Move faster as the stick is pressed further from center
            vector.scalarMultiply(Math.pow(vector.getMagnitude(), 2));
        }
        return vector;
    }

    public static void sendEmulatedMouseMove(NvConnection conn, short stickX, short stickY) {
        Vector2d vector = convertRawStickAxisToPixelMovement(stickX, stickY);
        if (vector.getMagnitude() >= 1) {
            conn.sendMouseMove((short) vector.getX(), (short) -vector.getY());
        }
    }

    public static void sendEmulatedMouseMove(NvConnection conn, float stickX, float stickY) {
        sendEmulatedMouseMove(conn,
                (short) (stickX * STICK_AXIS_MAX),
                (short) (stickY * STICK_AXIS_MAX));
    }

    public static void sendEmulatedMouseScroll(NvConnection conn, short stickX, short stickY) {
        Vector2d vector = convertRawStickAxisToPixelMovement(stickX, stickY);
        if (vector.getMagnitude() >= 1) {
            conn.sendMouseHighResScroll((short) vector.getY());
            conn.sendMouseHighResHScroll((short) vector.getX());
        }
    }
}
