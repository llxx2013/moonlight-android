package com.limelight.binding.input.stream_keyboard;

import android.content.Context;
import android.view.KeyEvent;

import com.limelight.R;
import com.limelight.nvstream.input.KeyboardPacket;

import java.util.ArrayList;
import java.util.List;

public final class StreamKeyboardLayoutBuilder {

    private StreamKeyboardLayoutBuilder() {}

    public static List<List<StreamKeyboardKeySpec>> buildLeftRailRows(Context context) {
        List<List<StreamKeyboardKeySpec>> rows = new ArrayList<>();

        List<StreamKeyboardKeySpec> fnRow = new ArrayList<>();
        fnRow.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_esc), KeyEvent.KEYCODE_ESCAPE, 1.2f));
        for (int i = 1; i <= 6; i++) {
            fnRow.add(new StreamKeyboardKeySpec("F" + i, KeyEvent.KEYCODE_F1 + (i - 1), 1.0f));
        }
        rows.add(fnRow);

        List<StreamKeyboardKeySpec> numberRow = new ArrayList<>();
        numberRow.add(new StreamKeyboardKeySpec("`", "~", KeyEvent.KEYCODE_GRAVE, 1.0f, false));
        for (int i = 0; i <= 5; i++) {
            numberRow.add(new StreamKeyboardKeySpec(String.valueOf(i), KeyEvent.KEYCODE_0 + i, 1.0f));
        }
        rows.add(numberRow);

        List<StreamKeyboardKeySpec> qwertyRow = new ArrayList<>();
        qwertyRow.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_tab), KeyEvent.KEYCODE_TAB, 1.4f));
        addLetterKeys(qwertyRow, "qwert");
        qwertyRow.add(new StreamKeyboardKeySpec("[", "{", KeyEvent.KEYCODE_LEFT_BRACKET, 1.0f, false));
        rows.add(qwertyRow);

        List<StreamKeyboardKeySpec> asdfRow = new ArrayList<>();
        asdfRow.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_caps), KeyEvent.KEYCODE_CAPS_LOCK, 1.6f));
        addLetterKeys(asdfRow, "asdfg");
        asdfRow.add(new StreamKeyboardKeySpec(";", ":", KeyEvent.KEYCODE_SEMICOLON, 1.0f, false));
        rows.add(asdfRow);

        List<StreamKeyboardKeySpec> zxcvRow = new ArrayList<>();
        zxcvRow.add(stickyShift(context, KeyEvent.KEYCODE_SHIFT_LEFT, 1.6f));
        addLetterKeys(zxcvRow, "zxcv");
        rows.add(zxcvRow);

        List<StreamKeyboardKeySpec> modifierRow = new ArrayList<>();
        modifierRow.add(stickyCtrl(context, KeyEvent.KEYCODE_CTRL_LEFT, 1.2f));
        modifierRow.add(stickyWin(context, KeyEvent.KEYCODE_META_LEFT, 1.2f));
        modifierRow.add(stickyAlt(context, KeyEvent.KEYCODE_ALT_LEFT, 1.2f));
        rows.add(modifierRow);

        List<StreamKeyboardKeySpec> navRow = new ArrayList<>();
        navRow.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_ins), KeyEvent.KEYCODE_INSERT, 1.0f));
        navRow.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_home), KeyEvent.KEYCODE_MOVE_HOME, 1.0f));
        navRow.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_pgup), KeyEvent.KEYCODE_PAGE_UP, 1.0f));
        rows.add(navRow);

        return rows;
    }

    public static List<List<StreamKeyboardKeySpec>> buildRightRailRows(Context context) {
        List<List<StreamKeyboardKeySpec>> rows = new ArrayList<>();

        List<StreamKeyboardKeySpec> fnRow = new ArrayList<>();
        for (int i = 7; i <= 12; i++) {
            fnRow.add(new StreamKeyboardKeySpec("F" + i, KeyEvent.KEYCODE_F1 + (i - 1), 1.0f));
        }
        rows.add(fnRow);

        List<StreamKeyboardKeySpec> numberRow = new ArrayList<>();
        for (int i = 6; i <= 9; i++) {
            numberRow.add(new StreamKeyboardKeySpec(String.valueOf(i), KeyEvent.KEYCODE_0 + i, 1.0f));
        }
        numberRow.add(new StreamKeyboardKeySpec("-", "_", KeyEvent.KEYCODE_MINUS, 1.0f, false));
        numberRow.add(new StreamKeyboardKeySpec("=", "+", KeyEvent.KEYCODE_EQUALS, 1.0f, false));
        numberRow.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_backspace), KeyEvent.KEYCODE_DEL, 1.6f));
        rows.add(numberRow);

        List<StreamKeyboardKeySpec> qwertyRow = new ArrayList<>();
        addLetterKeys(qwertyRow, "yuiop");
        qwertyRow.add(new StreamKeyboardKeySpec("]", "}", KeyEvent.KEYCODE_RIGHT_BRACKET, 1.0f, false));
        qwertyRow.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_enter), KeyEvent.KEYCODE_ENTER, 1.6f));
        rows.add(qwertyRow);

        List<StreamKeyboardKeySpec> asdfRow = new ArrayList<>();
        addLetterKeys(asdfRow, "hjkl");
        asdfRow.add(new StreamKeyboardKeySpec("'", "\"", KeyEvent.KEYCODE_APOSTROPHE, 1.0f, false));
        rows.add(asdfRow);

        List<StreamKeyboardKeySpec> zxcvRow = new ArrayList<>();
        addLetterKeys(zxcvRow, "bnm");
        zxcvRow.add(new StreamKeyboardKeySpec(",", "<", KeyEvent.KEYCODE_COMMA, 1.0f, false));
        zxcvRow.add(new StreamKeyboardKeySpec(".", ">", KeyEvent.KEYCODE_PERIOD, 1.0f, false));
        zxcvRow.add(new StreamKeyboardKeySpec("/", "?", KeyEvent.KEYCODE_SLASH, 1.0f, false));
        zxcvRow.add(stickyShift(context, KeyEvent.KEYCODE_SHIFT_RIGHT, 1.6f));
        rows.add(zxcvRow);

        List<StreamKeyboardKeySpec> modifierRow = new ArrayList<>();
        modifierRow.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_space), KeyEvent.KEYCODE_SPACE, 4.0f));
        modifierRow.add(stickyAlt(context, KeyEvent.KEYCODE_ALT_RIGHT, 1.2f));
        modifierRow.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_menu), KeyEvent.KEYCODE_MENU, 1.2f));
        modifierRow.add(stickyCtrl(context, KeyEvent.KEYCODE_CTRL_RIGHT, 1.2f));
        rows.add(modifierRow);

        List<StreamKeyboardKeySpec> navRow = new ArrayList<>();
        navRow.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_del), KeyEvent.KEYCODE_FORWARD_DEL, 1.0f));
        navRow.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_end), KeyEvent.KEYCODE_MOVE_END, 1.0f));
        navRow.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_pgdn), KeyEvent.KEYCODE_PAGE_DOWN, 1.0f));
        rows.add(navRow);

        return rows;
    }

    public static List<StreamKeyboardKeySpec> buildCenterNavRowUp() {
        List<StreamKeyboardKeySpec> keys = new ArrayList<>();
        keys.add(new StreamKeyboardKeySpec("\u2191", KeyEvent.KEYCODE_DPAD_UP, 1.0f));
        return keys;
    }

    public static List<StreamKeyboardKeySpec> buildCenterNavRowArrows() {
        List<StreamKeyboardKeySpec> keys = new ArrayList<>();
        keys.add(new StreamKeyboardKeySpec("\u2190", KeyEvent.KEYCODE_DPAD_LEFT, 1.0f));
        keys.add(new StreamKeyboardKeySpec("\u2193", KeyEvent.KEYCODE_DPAD_DOWN, 1.0f));
        keys.add(new StreamKeyboardKeySpec("\u2192", KeyEvent.KEYCODE_DPAD_RIGHT, 1.0f));
        return keys;
    }

    public static List<StreamKeyboardKeySpec> buildBottomNavRowUp() {
        return buildCenterNavRowUp();
    }

    public static List<StreamKeyboardKeySpec> buildBottomNavRowArrows() {
        return buildCenterNavRowArrows();
    }

    public static List<StreamKeyboardKeySpec> buildBottomFnRow1(Context context) {
        List<StreamKeyboardKeySpec> keys = new ArrayList<>();
        keys.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_esc), KeyEvent.KEYCODE_ESCAPE, 1.2f));
        for (int i = 1; i <= 6; i++) {
            keys.add(new StreamKeyboardKeySpec("F" + i, KeyEvent.KEYCODE_F1 + (i - 1), 1.0f));
        }
        return keys;
    }

    public static List<StreamKeyboardKeySpec> buildBottomFnRow2() {
        List<StreamKeyboardKeySpec> keys = new ArrayList<>();
        for (int i = 7; i <= 12; i++) {
            keys.add(new StreamKeyboardKeySpec("F" + i, KeyEvent.KEYCODE_F1 + (i - 1), 1.0f));
        }
        return keys;
    }

    public static List<StreamKeyboardKeySpec> buildBottomNumberRow(Context context) {
        List<StreamKeyboardKeySpec> keys = new ArrayList<>();
        keys.add(new StreamKeyboardKeySpec("`", "~", KeyEvent.KEYCODE_GRAVE, 1.0f, false));
        for (int i = 0; i <= 9; i++) {
            keys.add(new StreamKeyboardKeySpec(String.valueOf(i), KeyEvent.KEYCODE_0 + i, 1.0f));
        }
        keys.add(new StreamKeyboardKeySpec("-", "_", KeyEvent.KEYCODE_MINUS, 1.0f, false));
        keys.add(new StreamKeyboardKeySpec("=", "+", KeyEvent.KEYCODE_EQUALS, 1.0f, false));
        keys.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_backspace), KeyEvent.KEYCODE_DEL, 1.6f));
        return keys;
    }

    public static List<StreamKeyboardKeySpec> buildBottomQwertyRow(Context context) {
        List<StreamKeyboardKeySpec> keys = new ArrayList<>();
        keys.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_tab), KeyEvent.KEYCODE_TAB, 1.4f));
        addLetterKeys(keys, "qwertyuiop");
        keys.add(new StreamKeyboardKeySpec("[", "{", KeyEvent.KEYCODE_LEFT_BRACKET, 1.0f, false));
        keys.add(new StreamKeyboardKeySpec("]", "}", KeyEvent.KEYCODE_RIGHT_BRACKET, 1.0f, false));
        return keys;
    }

    public static List<StreamKeyboardKeySpec> buildBottomAsdfRow(Context context) {
        List<StreamKeyboardKeySpec> keys = new ArrayList<>();
        keys.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_caps), KeyEvent.KEYCODE_CAPS_LOCK, 1.6f));
        addLetterKeys(keys, "asdfghjkl");
        keys.add(new StreamKeyboardKeySpec(";", ":", KeyEvent.KEYCODE_SEMICOLON, 1.0f, false));
        keys.add(new StreamKeyboardKeySpec("'", "\"", KeyEvent.KEYCODE_APOSTROPHE, 1.0f, false));
        keys.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_enter), KeyEvent.KEYCODE_ENTER, 1.8f));
        return keys;
    }

    public static List<StreamKeyboardKeySpec> buildBottomZxcvRow(Context context) {
        List<StreamKeyboardKeySpec> keys = new ArrayList<>();
        keys.add(stickyShift(context, KeyEvent.KEYCODE_SHIFT_LEFT, 1.8f));
        addLetterKeys(keys, "zxcvbnm");
        keys.add(new StreamKeyboardKeySpec(",", "<", KeyEvent.KEYCODE_COMMA, 1.0f, false));
        keys.add(new StreamKeyboardKeySpec(".", ">", KeyEvent.KEYCODE_PERIOD, 1.0f, false));
        keys.add(new StreamKeyboardKeySpec("/", "?", KeyEvent.KEYCODE_SLASH, 1.0f, false));
        keys.add(stickyShift(context, KeyEvent.KEYCODE_SHIFT_RIGHT, 1.8f));
        return keys;
    }

    public static List<StreamKeyboardKeySpec> buildBottomModifierRow(Context context) {
        List<StreamKeyboardKeySpec> keys = new ArrayList<>();
        keys.add(stickyCtrl(context, KeyEvent.KEYCODE_CTRL_LEFT, 1.2f));
        keys.add(stickyWin(context, KeyEvent.KEYCODE_META_LEFT, 1.2f));
        keys.add(stickyAlt(context, KeyEvent.KEYCODE_ALT_LEFT, 1.2f));
        keys.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_space), KeyEvent.KEYCODE_SPACE, 5.0f));
        keys.add(stickyAlt(context, KeyEvent.KEYCODE_ALT_RIGHT, 1.2f));
        keys.add(new StreamKeyboardKeySpec(context.getString(R.string.stream_key_menu), KeyEvent.KEYCODE_MENU, 1.2f));
        keys.add(stickyCtrl(context, KeyEvent.KEYCODE_CTRL_RIGHT, 1.2f));
        return keys;
    }

    private static void addLetterKeys(List<StreamKeyboardKeySpec> keys, String chars) {
        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            String lower = String.valueOf(c);
            String upper = lower.toUpperCase();
            int keyCode = KeyEvent.KEYCODE_A + (Character.toLowerCase(c) - 'a');
            keys.add(new StreamKeyboardKeySpec(lower, upper, keyCode, 1.0f, true));
        }
    }

    private static StreamKeyboardKeySpec stickyShift(Context context, int keyCode, float weight) {
        return new StreamKeyboardKeySpec(
                context.getString(R.string.stream_key_shift),
                keyCode,
                weight,
                KeyboardPacket.MODIFIER_SHIFT,
                keyCode);
    }

    private static StreamKeyboardKeySpec stickyCtrl(Context context, int keyCode, float weight) {
        return new StreamKeyboardKeySpec(
                context.getString(R.string.stream_key_ctrl),
                keyCode,
                weight,
                KeyboardPacket.MODIFIER_CTRL,
                keyCode);
    }

    private static StreamKeyboardKeySpec stickyAlt(Context context, int keyCode, float weight) {
        return new StreamKeyboardKeySpec(
                context.getString(R.string.stream_key_alt),
                keyCode,
                weight,
                KeyboardPacket.MODIFIER_ALT,
                keyCode);
    }

    private static StreamKeyboardKeySpec stickyWin(Context context, int keyCode, float weight) {
        return new StreamKeyboardKeySpec(
                context.getString(R.string.stream_key_win),
                keyCode,
                weight,
                KeyboardPacket.MODIFIER_META,
                keyCode);
    }
}
