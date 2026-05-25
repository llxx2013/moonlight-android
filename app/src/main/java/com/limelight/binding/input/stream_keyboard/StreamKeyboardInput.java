package com.limelight.binding.input.stream_keyboard;

import android.view.KeyEvent;

import com.limelight.binding.input.KeyboardTranslator;
import com.limelight.nvstream.NvConnection;
import com.limelight.nvstream.input.KeyboardPacket;
import com.limelight.nvstream.jni.MoonBridge;

public class StreamKeyboardInput {
    private final NvConnection conn;
    private final KeyboardTranslator keyboardTranslator;

    private int modifierFlags = 0;

    public StreamKeyboardInput(NvConnection conn, KeyboardTranslator keyboardTranslator) {
        this.conn = conn;
        this.keyboardTranslator = keyboardTranslator;
    }

    public int getModifierFlags() {
        return modifierFlags;
    }

    public void clearModifierFlags() {
        modifierFlags = 0;
    }

    public void applyModifierMask(byte modifierMask, boolean down) {
        if (down) {
            modifierFlags |= modifierMask;
        } else {
            modifierFlags &= ~modifierMask;
        }
    }

    public byte getModifierState() {
        return (byte) modifierFlags;
    }

    public boolean isModifierActive(byte mask) {
        return (modifierFlags & mask) != 0;
    }

    public boolean sendAndroidKey(int keyCode, boolean down) {
        short translated = keyboardTranslator.translate(keyCode, -1);
        if (translated == 0) {
            return false;
        }

        byte flags = keyboardTranslator.hasNormalizedMapping(keyCode, -1)
                ? 0 : MoonBridge.SS_KBE_FLAG_NON_NORMALIZED;
        conn.sendKeyboardInput(
                translated,
                down ? KeyboardPacket.KEY_DOWN : KeyboardPacket.KEY_UP,
                getModifierState(),
                flags);
        return true;
    }

    public void setModifierHeld(byte mask, int modifierKeyCode, boolean held) {
        boolean wasActive = isModifierActive(mask);
        if (held == wasActive) {
            return;
        }

        applyModifierMask(mask, held);
        sendAndroidKey(modifierKeyCode, held);
    }

    public void toggleModifier(byte mask, int modifierKeyCode) {
        setModifierHeld(mask, modifierKeyCode, !isModifierActive(mask));
    }

    public static byte modifierMaskForKeyCode(int androidKeyCode) {
        if (androidKeyCode == KeyEvent.KEYCODE_CTRL_LEFT
                || androidKeyCode == KeyEvent.KEYCODE_CTRL_RIGHT) {
            return KeyboardPacket.MODIFIER_CTRL;
        }
        if (androidKeyCode == KeyEvent.KEYCODE_SHIFT_LEFT
                || androidKeyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            return KeyboardPacket.MODIFIER_SHIFT;
        }
        if (androidKeyCode == KeyEvent.KEYCODE_ALT_LEFT
                || androidKeyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
            return KeyboardPacket.MODIFIER_ALT;
        }
        if (androidKeyCode == KeyEvent.KEYCODE_META_LEFT
                || androidKeyCode == KeyEvent.KEYCODE_META_RIGHT) {
            return KeyboardPacket.MODIFIER_META;
        }
        return 0;
    }
}
