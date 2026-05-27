package com.limelight.binding.input.stream_keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
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
import android.widget.ScrollView;

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

    public interface VisibilityListener {
        void onVisibilityChanged(boolean visible);
    }

    private enum LayoutMode {
        SPLIT_SIDE,
        BOTTOM
    }

    private final FrameLayout parent;
    private final Context context;
    private final StreamKeyboardInput keyboardInput;
    private final GrabInputCallback grabInputCallback;
    private final VisibilityBlocker visibilityBlocker;
    private final VisibilityListener visibilityListener;

    private View rootView;
    private FrameLayout.LayoutParams rootLayoutParams;
    private LayoutMode layoutMode;
    private final Map<Button, StreamKeyboardKeySpec> buttonSpecs = new HashMap<>();
    private final Map<Byte, List<Button>> stickyModifierButtons = new HashMap<>();

    private boolean visible = false;

    @SuppressLint("ClickableViewAccessibility")
    public StreamKeyboardOverlay(FrameLayout parent, StreamKeyboardInput keyboardInput, Context context,
                                   GrabInputCallback grabInputCallback, VisibilityBlocker visibilityBlocker,
                                   VisibilityListener visibilityListener) {
        this.parent = parent;
        this.context = context;
        this.keyboardInput = keyboardInput;
        this.grabInputCallback = grabInputCallback;
        this.visibilityBlocker = visibilityBlocker;
        this.visibilityListener = visibilityListener;

        layoutMode = resolveLayoutMode();
        inflateAndBuild();
    }

    private LayoutMode resolveLayoutMode() {
        Configuration config = context.getResources().getConfiguration();
        return config.screenWidthDp > config.screenHeightDp ? LayoutMode.SPLIT_SIDE : LayoutMode.BOTTOM;
    }

    private void inflateAndBuild() {
        if (rootView != null) {
            parent.removeView(rootView);
        }
        buttonSpecs.clear();
        stickyModifierButtons.clear();

        LayoutInflater inflater = LayoutInflater.from(context);
        if (layoutMode == LayoutMode.SPLIT_SIDE) {
            rootView = inflater.inflate(R.layout.stream_keyboard_split_overlay, parent, false);
            rootLayoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            buildSplitLayout();
        } else {
            rootView = inflater.inflate(R.layout.stream_keyboard_overlay, parent, false);
            rootLayoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);
            buildBottomLayout();
        }

        parent.addView(rootView, rootLayoutParams);

        View closeTarget = rootView.findViewById(R.id.stream_keyboard_root);
        if (closeTarget == null) {
            closeTarget = rootView;
        }
        Button closeButton = closeTarget.findViewById(R.id.stream_keyboard_close);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> hide());
        }

        rootView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void buildSplitLayout() {
        LinearLayout leftRail = rootView.findViewById(R.id.stream_keyboard_left_rail);
        LinearLayout rightRail = rootView.findViewById(R.id.stream_keyboard_right_rail);

        for (List<StreamKeyboardKeySpec> rowSpecs : StreamKeyboardLayoutBuilder.buildLeftRailRows(context)) {
            addRowToRail(leftRail, rowSpecs);
        }
        for (List<StreamKeyboardKeySpec> rowSpecs : StreamKeyboardLayoutBuilder.buildRightRailRows(context)) {
            addRowToRail(rightRail, rowSpecs);
        }

        populateRow(rootView.findViewById(R.id.stream_keyboard_nav_row_up),
                StreamKeyboardLayoutBuilder.buildCenterNavRowUp());
        populateRow(rootView.findViewById(R.id.stream_keyboard_nav_row_arrows),
                StreamKeyboardLayoutBuilder.buildCenterNavRowArrows());

        applyRailWidths();
    }

    private void buildBottomLayout() {
        populateRow(R.id.stream_keyboard_row_fn, StreamKeyboardLayoutBuilder.buildBottomFnRow1(context));
        populateRow(R.id.stream_keyboard_row_fn2, StreamKeyboardLayoutBuilder.buildBottomFnRow2());
        populateRow(R.id.stream_keyboard_row_number, StreamKeyboardLayoutBuilder.buildBottomNumberRow(context));
        populateRow(R.id.stream_keyboard_row_q, StreamKeyboardLayoutBuilder.buildBottomQwertyRow(context));
        populateRow(R.id.stream_keyboard_row_a, StreamKeyboardLayoutBuilder.buildBottomAsdfRow(context));
        populateRow(R.id.stream_keyboard_row_z, StreamKeyboardLayoutBuilder.buildBottomZxcvRow(context));
        populateRow(R.id.stream_keyboard_row_modifiers, StreamKeyboardLayoutBuilder.buildBottomModifierRow(context));
        populateRow(R.id.stream_keyboard_nav_row_up, StreamKeyboardLayoutBuilder.buildBottomNavRowUp());
        populateRow(R.id.stream_keyboard_nav_row_arrows, StreamKeyboardLayoutBuilder.buildBottomNavRowArrows());
    }

    private void applyRailWidths() {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int minWidth = context.getResources().getDimensionPixelSize(R.dimen.stream_keyboard_rail_width_min);
        int maxWidth = context.getResources().getDimensionPixelSize(R.dimen.stream_keyboard_rail_width_max);
        int railWidth = (int) (metrics.widthPixels * 0.28f);
        railWidth = Math.max(minWidth, Math.min(maxWidth, railWidth));

        LinearLayout leftRail = rootView.findViewById(R.id.stream_keyboard_left_rail);
        LinearLayout rightRail = rootView.findViewById(R.id.stream_keyboard_right_rail);

        ViewGroup.LayoutParams leftLp = leftRail.getLayoutParams();
        leftLp.width = railWidth;
        leftRail.setLayoutParams(leftLp);

        int clearance = context.getResources().getDimensionPixelSize(
                R.dimen.stream_quick_side_menu_clearance_end);
        FrameLayout.LayoutParams rightLp = (FrameLayout.LayoutParams) rightRail.getLayoutParams();
        rightLp.width = railWidth;
        rightLp.setMarginEnd(clearance);
        rightRail.setLayoutParams(rightLp);
    }

    private void applyBottomMaxHeight() {
        if (!(rootView instanceof ScrollView)) {
            return;
        }
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int maxHeight = (int) (metrics.heightPixels * 0.32f);
        ViewGroup.LayoutParams lp = rootView.getLayoutParams();
        lp.height = maxHeight;
        rootView.setLayoutParams(lp);
    }

    private void addRowToRail(LinearLayout rail, List<StreamKeyboardKeySpec> specs) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        populateRow(row, specs);
        rail.addView(row);
    }

    private View getContentRoot() {
        View content = rootView.findViewById(R.id.stream_keyboard_root);
        return content != null ? content : rootView;
    }

    private void populateRow(int rowId, List<StreamKeyboardKeySpec> specs) {
        populateRow(getContentRoot().findViewById(rowId), specs);
    }

    private void populateRow(LinearLayout row, List<StreamKeyboardKeySpec> specs) {
        if (row == null) {
            return;
        }
        for (StreamKeyboardKeySpec spec : specs) {
            if (spec.keyCode == KeyEvent.KEYCODE_UNKNOWN) {
                continue;
            }
            Button button = createKeyButton(spec);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, spec.widthWeight);
            int margin = context.getResources().getDimensionPixelSize(R.dimen.stream_key_margin);
            lp.setMargins(margin, margin, margin, margin);
            row.addView(button, lp);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private Button createKeyButton(StreamKeyboardKeySpec spec) {
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

    private boolean handleKeyTouch(Button button, StreamKeyboardKeySpec spec, MotionEvent event) {
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

    private void onKeyDown(StreamKeyboardKeySpec spec) {
        if (spec.stickyModifierMask != 0) {
            return;
        }
        keyboardInput.sendAndroidKey(spec.keyCode, true);
    }

    private void onKeyUp(StreamKeyboardKeySpec spec) {
        if (spec.stickyModifierMask != 0) {
            keyboardInput.toggleModifier(spec.stickyModifierMask, spec.stickyModifierKeyCode);
            updateStickyModifierButtons();
            if (spec.stickyModifierMask == KeyboardPacket.MODIFIER_SHIFT) {
                updateShiftedLabels();
            }
            return;
        }

        keyboardInput.sendAndroidKey(spec.keyCode, false);

        if (spec.keyCode == KeyEvent.KEYCODE_CAPS_LOCK) {
            return;
        }

        if (keyboardInput.isModifierActive(KeyboardPacket.MODIFIER_SHIFT)
                && spec.keyCode != KeyEvent.KEYCODE_SHIFT_LEFT
                && spec.keyCode != KeyEvent.KEYCODE_SHIFT_RIGHT
                && spec.isLetter) {
            keyboardInput.setModifierHeld(
                    KeyboardPacket.MODIFIER_SHIFT,
                    KeyEvent.KEYCODE_SHIFT_LEFT,
                    false);
            updateStickyModifierButtons();
            updateShiftedLabels();
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

    private String getButtonLabel(StreamKeyboardKeySpec spec) {
        if (keyboardInput.isModifierActive(KeyboardPacket.MODIFIER_SHIFT) && spec.shiftedLabel != null) {
            return spec.shiftedLabel;
        }
        return spec.label;
    }

    private void updateShiftedLabels() {
        for (Map.Entry<Button, StreamKeyboardKeySpec> entry : buttonSpecs.entrySet()) {
            if (entry.getValue().shiftedLabel != null) {
                entry.getKey().setText(getButtonLabel(entry.getValue()));
            }
        }
    }

    public void refreshLayoutMode() {
        boolean wasVisible = visible;
        LayoutMode newMode = resolveLayoutMode();
        if (newMode == layoutMode && rootView != null) {
            if (layoutMode == LayoutMode.SPLIT_SIDE) {
                applyRailWidths();
            } else {
                applyBottomMaxHeight();
            }
            return;
        }

        layoutMode = newMode;
        inflateAndBuild();

        if (wasVisible) {
            show();
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

        if (layoutMode == LayoutMode.SPLIT_SIDE) {
            applyRailWidths();
        } else {
            applyBottomMaxHeight();
        }

        rootView.setVisibility(View.VISIBLE);
        visible = true;
        rootView.requestLayout();
        updateStickyModifierButtons();
        updateShiftedLabels();
        notifyVisibilityListener();
    }

    public void hide() {
        if (rootView != null) {
            rootView.setVisibility(View.GONE);
        }
        visible = false;
        notifyVisibilityListener();
    }

    private void notifyVisibilityListener() {
        if (visibilityListener != null) {
            visibilityListener.onVisibilityChanged(visible);
        }
    }

    public void destroy() {
        if (rootView != null) {
            parent.removeView(rootView);
            rootView = null;
        }
    }
}
