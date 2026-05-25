package com.limelight.binding.input.stream_keyboard;

public class StreamKeyboardKeySpec {
    public final String label;
    public final String shiftedLabel;
    public final int keyCode;
    public final float widthWeight;
    public final byte stickyModifierMask;
    public final int stickyModifierKeyCode;
    public final boolean isLetter;

    public StreamKeyboardKeySpec(String label, int keyCode, float widthWeight) {
        this(label, null, keyCode, widthWeight, (byte) 0, 0, false);
    }

    public StreamKeyboardKeySpec(String label, String shiftedLabel, int keyCode, float widthWeight, boolean isLetter) {
        this(label, shiftedLabel, keyCode, widthWeight, (byte) 0, 0, isLetter);
    }

    public StreamKeyboardKeySpec(String label, int keyCode, float widthWeight,
                                 byte stickyModifierMask, int stickyModifierKeyCode) {
        this(label, null, keyCode, widthWeight, stickyModifierMask, stickyModifierKeyCode, false);
    }

    public StreamKeyboardKeySpec(String label, String shiftedLabel, int keyCode, float widthWeight,
                                 byte stickyModifierMask, int stickyModifierKeyCode, boolean isLetter) {
        this.label = label;
        this.shiftedLabel = shiftedLabel;
        this.keyCode = keyCode;
        this.widthWeight = widthWeight;
        this.stickyModifierMask = stickyModifierMask;
        this.stickyModifierKeyCode = stickyModifierKeyCode;
        this.isLetter = isLetter;
    }
}
