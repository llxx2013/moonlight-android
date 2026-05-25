package com.limelight.binding.input.stream_keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.limelight.R;
import com.limelight.nvstream.input.KeyboardPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreamKeyboardOverlay {
    public interface GrabInputCallback {
        void ensureInputGrabbed();
    }

    public interface VisibilityBlocker {
        boolean shouldBlockToggle();
    }

    private static final class KeySpec {
        final String label;
        final String shiftedLabel;
        final int keyCode;
        final float widthWeight;
        final byte stickyModifierMask;
        final int stickyModifierKeyCode;
        final boolean isLetter;

        KeySpec(String label, int keyCode, float widthWeight) {
            this(label, null, keyCode, widthWeight, (byte) 0, 0, false);
        }

        KeySpec(String label, String shiftedLabel, int keyCode, float widthWeight, boolean isLetter) {
            this(label, shiftedLabel, keyCode, widthWeight, (byte) 0, 0, isLetter);
        }

        KeySpec(String label, int keyCode, float widthWeight, byte stickyModifierMask, int stickyModifierKeyCode) {
            this(label, null, keyCode, widthWeight, stickyModifierMask, stickyModifierKeyCode, false);
        }

        KeySpec(String label, String shiftedLabel, int keyCode, float widthWeight,
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

    private final Context context;
    private final StreamKeyboardInput keyboardInput;
    private final GrabInputCallback grabInputCallback;
    private final VisibilityBlocker visibilityBlocker;

    private final View rootView;
    private final Map<Button, KeySpec> buttonSpecs = new HashMap<>();
    private final Map<Byte, List<Button>> stickyModifierButtons = new HashMap<>();

    private boolean visible = false;

    @SuppressLint("ClickableViewAccessibility")
    public StreamKeyboardOverlay(FrameLayout parent, StreamKeyboardInput keyboardInput, Context context,
                                   GrabInputCallback grabInputCallback, VisibilityBlocker visibilityBlocker) {
        this.context = context;
        this.keyboardInput = keyboardInput;
        this.grabInputCallback = grabInputCallback;
        this.visibilityBlocker = visibilityBlocker;

        LayoutInflater inflater = LayoutInflater.from(context);
        rootView = inflater.inflate(R.layout.stream_keyboard_overlay, parent, false);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
        parent.addView(rootView, lp);

        Button closeButton = rootView.findViewById(R.id.stream_keyboard_close);
        closeButton.setOnClickListener(v -> hide());

        populateRow(R.id.stream_keyboard_row_fn, buildFunctionRow());
        populateRow(R.id.stream_keyboard_row_number, buildNumberRow());
        populateRow(R.id.stream_keyboard_row_q, buildQwertyRow());
        populateRow(R.id.stream_keyboard_row_a, buildAsdfRow());
        populateRow(R.id.stream_keyboard_row_z, buildZxcvRow());
        populateRow(R.id.stream_keyboard_row_modifiers, buildModifierRow());
        populateRow(R.id.stream_keyboard_row_nav, buildNavigationRow());

        rootView.setVisibility(View.GONE);
    }

    private List<KeySpec> buildFunctionRow() {
        List<KeySpec> keys = new ArrayList<>();
        keys.add(new KeySpec(context.getString(R.string.stream_key_esc), KeyEvent.KEYCODE_ESCAPE, 1.2f));
        for (int i = 1; i <= 12; i++) {
            keys.add(new KeySpec("F" + i, KeyEvent.KEYCODE_F1 + (i - 1), 1.0f));
        }
        return keys;
    }

    private List<KeySpec> buildNumberRow() {
        List<KeySpec> keys = new ArrayList<>();
        keys.add(new KeySpec("`", "~", KeyEvent.KEYCODE_GRAVE, 1.0f, false));
        for (int i = 0; i <= 9; i++) {
            keys.add(new KeySpec(String.valueOf(i), KeyEvent.KEYCODE_0 + i, 1.0f));
        }
        keys.add(new KeySpec("-", "_", KeyEvent.KEYCODE_MINUS, 1.0f, false));
        keys.add(new KeySpec("=", "+", KeyEvent.KEYCODE_EQUALS, 1.0f, false));
        keys.add(new KeySpec(context.getString(R.string.stream_key_backspace), KeyEvent.KEYCODE_DEL, 1.8f));
        return keys;
    }

    private List<KeySpec> buildQwertyRow() {
        List<KeySpec> keys = new ArrayList<>();
        keys.add(new KeySpec(context.getString(R.string.stream_key_tab), KeyEvent.KEYCODE_TAB, 1.4f));
        addLetterKeys(keys, "qwertyuiop");
        keys.add(new KeySpec("[", "{", KeyEvent.KEYCODE_LEFT_BRACKET, 1.0f, false));
        keys.add(new KeySpec("]", "}", KeyEvent.KEYCODE_RIGHT_BRACKET, 1.0f, false));
        return keys;
    }

    private List<KeySpec> buildAsdfRow() {
        List<KeySpec> keys = new ArrayList<>();
        keys.add(new KeySpec(context.getString(R.string.stream_key_caps), KeyEvent.KEYCODE_CAPS_LOCK, 1.6f));
        addLetterKeys(keys, "asdfghjkl");
        keys.add(new KeySpec(";", ":", KeyEvent.KEYCODE_SEMICOLON, 1.0f, false));
        keys.add(new KeySpec("'", "\"", KeyEvent.KEYCODE_APOSTROPHE, 1.0f, false));
        keys.add(new KeySpec(context.getString(R.string.stream_key_enter), KeyEvent.KEYCODE_ENTER, 1.8f));
        return keys;
    }

    private List<KeySpec> buildZxcvRow() {
        List<KeySpec> keys = new ArrayList<>();
        keys.add(new KeySpec(
                context.getString(R.string.stream_key_shift),
                KeyEvent.KEYCODE_SHIFT_LEFT,
                1.8f,
                KeyboardPacket.MODIFIER_SHIFT,
                KeyEvent.KEYCODE_SHIFT_LEFT));
        addLetterKeys(keys, "zxcvbnm");
        keys.add(new KeySpec(",", "<", KeyEvent.KEYCODE_COMMA, 1.0f, false));
        keys.add(new KeySpec(".", ">", KeyEvent.KEYCODE_PERIOD, 1.0f, false));
        keys.add(new KeySpec("/", "?", KeyEvent.KEYCODE_SLASH, 1.0f, false));
        keys.add(new KeySpec(
                context.getString(R.string.stream_key_shift),
                KeyEvent.KEYCODE_SHIFT_RIGHT,
                1.8f,
                KeyboardPacket.MODIFIER_SHIFT,
                KeyEvent.KEYCODE_SHIFT_RIGHT));
        return keys;
    }

    private List<KeySpec> buildModifierRow() {
        List<KeySpec> keys = new ArrayList<>();
        keys.add(new KeySpec(
                context.getString(R.string.stream_key_ctrl),
                KeyEvent.KEYCODE_CTRL_LEFT,
                1.2f,
                KeyboardPacket.MODIFIER_CTRL,
                KeyEvent.KEYCODE_CTRL_LEFT));
        keys.add(new KeySpec(
                context.getString(R.string.stream_key_win),
                KeyEvent.KEYCODE_META_LEFT,
                1.2f,
                KeyboardPacket.MODIFIER_META,
                KeyEvent.KEYCODE_META_LEFT));
        keys.add(new KeySpec(
                context.getString(R.string.stream_key_alt),
                KeyEvent.KEYCODE_ALT_LEFT,
                1.2f,
                KeyboardPacket.MODIFIER_ALT,
                KeyEvent.KEYCODE_ALT_LEFT));
        keys.add(new KeySpec(context.getString(R.string.stream_key_space), KeyEvent.KEYCODE_SPACE, 5.0f));
        keys.add(new KeySpec(
                context.getString(R.string.stream_key_alt),
                KeyEvent.KEYCODE_ALT_RIGHT,
                1.2f,
                KeyboardPacket.MODIFIER_ALT,
                KeyEvent.KEYCODE_ALT_RIGHT));
        keys.add(new KeySpec(context.getString(R.string.stream_key_menu), KeyEvent.KEYCODE_MENU, 1.2f));
        keys.add(new KeySpec(
                context.getString(R.string.stream_key_ctrl),
                KeyEvent.KEYCODE_CTRL_RIGHT,
                1.2f,
                KeyboardPacket.MODIFIER_CTRL,
                KeyEvent.KEYCODE_CTRL_RIGHT));
        return keys;
    }

    private List<KeySpec> buildNavigationRow() {
        List<KeySpec> keys = new ArrayList<>();
        keys.add(new KeySpec(context.getString(R.string.stream_key_ins), KeyEvent.KEYCODE_INSERT, 1.2f));
        keys.add(new KeySpec(context.getString(R.string.stream_key_home), KeyEvent.KEYCODE_MOVE_HOME, 1.2f));
        keys.add(new KeySpec(context.getString(R.string.stream_key_pgup), KeyEvent.KEYCODE_PAGE_UP, 1.2f));
        keys.add(new KeySpec(context.getString(R.string.stream_key_up), KeyEvent.KEYCODE_DPAD_UP, 1.2f));
        keys.add(new KeySpec(context.getString(R.string.stream_key_left), KeyEvent.KEYCODE_DPAD_LEFT, 1.2f));
        keys.add(new KeySpec(context.getString(R.string.stream_key_down), KeyEvent.KEYCODE_DPAD_DOWN, 1.2f));
        keys.add(new KeySpec(context.getString(R.string.stream_key_right), KeyEvent.KEYCODE_DPAD_RIGHT, 1.2f));
        keys.add(new KeySpec(context.getString(R.string.stream_key_del), KeyEvent.KEYCODE_FORWARD_DEL, 1.2f));
        keys.add(new KeySpec(context.getString(R.string.stream_key_end), KeyEvent.KEYCODE_MOVE_END, 1.2f));
        keys.add(new KeySpec(context.getString(R.string.stream_key_pgdn), KeyEvent.KEYCODE_PAGE_DOWN, 1.2f));
        return keys;
    }

    private void addLetterKeys(List<KeySpec> keys, String chars) {
        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            if (c == ' ') {
                continue;
            }
            String lower = String.valueOf(c);
            String upper = lower.toUpperCase();
            int keyCode = KeyEvent.KEYCODE_A + (Character.toLowerCase(c) - 'a');
            keys.add(new KeySpec(lower, upper, keyCode, 1.0f, true));
        }
    }

    private void populateRow(int rowId, List<KeySpec> specs) {
        LinearLayout row = rootView.findViewById(rowId);
        for (KeySpec spec : specs) {
            Button button = createKeyButton(spec);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, spec.widthWeight);
            int margin = context.getResources().getDimensionPixelSize(R.dimen.stream_key_margin);
            lp.setMargins(margin, margin, margin, margin);
            row.addView(button, lp);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private Button createKeyButton(KeySpec spec) {
        Button button = new Button(context);
        button.setAllCaps(false);
        button.setText(getButtonLabel(spec));
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context.getResources().getDimension(R.dimen.stream_key_text_size));
        button.setMinHeight(context.getResources().getDimensionPixelSize(R.dimen.stream_key_height));
        button.setMinWidth(0);
        button.setPadding(4, 4, 4, 4);
        button.setFocusable(false);
        button.setBackgroundResource(R.drawable.stream_key_background);

        buttonSpecs.put(button, spec);
        if (spec.stickyModifierMask != 0) {
            List<Button> stickyButtons = stickyModifierButtons.get(spec.stickyModifierMask);
            if (stickyButtons == null) {
                stickyButtons = new ArrayList<>();
                stickyModifierButtons.put(spec.stickyModifierMask, stickyButtons);
            }
            stickyButtons.add(button);
            updateStickyButtonState(button, spec.stickyModifierMask);
        }

        button.setOnTouchListener((view, event) -> handleKeyTouch(button, spec, event));
        return button;
    }

    private boolean handleKeyTouch(Button button, KeySpec spec, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                button.setPressed(true);
                onKeyDown(spec);
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                button.setPressed(false);
                onKeyUp(spec);
                return true;

            default:
                return false;
        }
    }

    private void onKeyDown(KeySpec spec) {
        if (spec.stickyModifierMask != 0) {
            return;
        }
        keyboardInput.sendAndroidKey(spec.keyCode, true);
    }

    private void onKeyUp(KeySpec spec) {
        if (spec.stickyModifierMask != 0) {
            keyboardInput.toggleModifier(spec.stickyModifierMask, spec.stickyModifierKeyCode);
            updateStickyModifierButtons();
            if (spec.stickyModifierMask == KeyboardPacket.MODIFIER_SHIFT) {
                updateLetterLabels();
            }
            return;
        }

        keyboardInput.sendAndroidKey(spec.keyCode, false);

        if (spec.keyCode == KeyEvent.KEYCODE_CAPS_LOCK) {
            return;
        }

        if (keyboardInput.isModifierActive(KeyboardPacket.MODIFIER_SHIFT)
                && spec.keyCode != KeyEvent.KEYCODE_SHIFT_LEFT
                && spec.keyCode != KeyEvent.KEYCODE_SHIFT_RIGHT) {
            // One-shot shift behavior for character keys typed with shift held via sticky key
            if (spec.isLetter) {
                keyboardInput.setModifierHeld(
                        KeyboardPacket.MODIFIER_SHIFT,
                        KeyEvent.KEYCODE_SHIFT_LEFT,
                        false);
                updateStickyModifierButtons();
                updateLetterLabels();
            }
        }
    }

    private void updateStickyButtonState(Button button, byte modifierMask) {
        button.setActivated(keyboardInput.isModifierActive(modifierMask));
    }

    private void updateStickyModifierButtons() {
        for (Map.Entry<Byte, List<Button>> entry : stickyModifierButtons.entrySet()) {
            for (Button button : entry.getValue()) {
                updateStickyButtonState(button, entry.getKey());
            }
        }
    }

    private String getButtonLabel(KeySpec spec) {
        if (keyboardInput.isModifierActive(KeyboardPacket.MODIFIER_SHIFT) && spec.shiftedLabel != null) {
            return spec.shiftedLabel;
        }
        return spec.label;
    }

    private void updateLetterLabels() {
        for (Map.Entry<Button, KeySpec> entry : buttonSpecs.entrySet()) {
            if (entry.getValue().shiftedLabel != null) {
                entry.getKey().setText(getButtonLabel(entry.getValue()));
            }
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void toggleVisibility() {
        if (visibilityBlocker != null && visibilityBlocker.shouldBlockToggle()) {
            return;
        }

        if (visible) {
            hide();
        } else {
            show();
        }
    }

    public void show() {
        if (visibilityBlocker != null && visibilityBlocker.shouldBlockToggle()) {
            return;
        }

        grabInputCallback.ensureInputGrabbed();
        rootView.setVisibility(View.VISIBLE);
        visible = true;
        rootView.requestLayout();
        updateStickyModifierButtons();
        updateLetterLabels();
    }

    public void hide() {
        rootView.setVisibility(View.GONE);
        visible = false;
    }

    public void destroy() {
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
    }
}
