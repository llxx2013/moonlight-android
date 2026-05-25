package com.limelight.binding.input.virtual_mouse;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.limelight.R;
import com.limelight.nvstream.NvConnection;
import com.limelight.nvstream.input.MouseButtonPacket;

import java.util.ArrayList;
import java.util.List;

public class VirtualMouse {
    private static final float OPACITY = 0.45f;
    private static final int MARGIN_DP = 15;
    private static final float STICK_SIZE_RATIO = 0.14f;
    private static final float BUTTON_SIZE_RATIO = 0.06f;
    private static final float ELEMENT_GAP_RATIO = 0.01f;

    private final NvConnection conn;
    private final FrameLayout parent;
    private final Context context;
    private final List<View> elements = new ArrayList<>();
    private boolean visible = true;

    public VirtualMouse(NvConnection conn, FrameLayout parent, Context context) {
        this.conn = conn;
        this.parent = parent;
        this.context = context;
    }

    public void refreshLayout() {
        removeElements();

        DisplayMetrics screen = context.getResources().getDisplayMetrics();
        float density = screen.density;
        int margin = (int) (MARGIN_DP * density);
        int width = screen.widthPixels;
        int height = screen.heightPixels;

        int stickSize = (int) (height * STICK_SIZE_RATIO);
        int buttonSize = (int) (height * BUTTON_SIZE_RATIO);
        int gap = Math.max(margin / 2, (int) (height * ELEMENT_GAP_RATIO));

        int moveStickX = width - margin - stickSize;
        int moveStickY = height - margin - stickSize;

        int scrollStickX = moveStickX - gap - stickSize;
        int scrollStickY = moveStickY;

        int buttonColumnX = scrollStickX - gap - buttonSize;
        int buttonBottomY = moveStickY + stickSize - buttonSize;

        VirtualMoveStick moveStick = new VirtualMoveStick(context, conn);
        addElement(moveStick, moveStickX, moveStickY, stickSize, stickSize);

        VirtualRotaryScrollStick scrollStick = new VirtualRotaryScrollStick(context, conn);
        addElement(scrollStick, scrollStickX, scrollStickY, stickSize, stickSize);

        VirtualMouseButton leftButton = new VirtualMouseButton(
                context, conn, MouseButtonPacket.BUTTON_LEFT, "L");
        addElement(leftButton, buttonColumnX, buttonBottomY, buttonSize, buttonSize);

        VirtualMouseButton rightButton = new VirtualMouseButton(
                context, conn, MouseButtonPacket.BUTTON_RIGHT, "R");
        addElement(rightButton, buttonColumnX, buttonBottomY - gap - buttonSize, buttonSize, buttonSize);

        VirtualMouseButton middleButton = new VirtualMouseButton(
                context, conn, MouseButtonPacket.BUTTON_MIDDLE, "M");
        addElement(middleButton, buttonColumnX, buttonBottomY - (gap + buttonSize) * 2, buttonSize, buttonSize);

        setOpacity(OPACITY);
        applyVisibility();
    }

    private void addElement(View element, int x, int y, int width, int height) {
        elements.add(element);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
        layoutParams.setMargins(x, y, 0, 0);
        parent.addView(element, layoutParams);
    }

    private void setOpacity(float opacity) {
        for (View element : elements) {
            element.setAlpha(opacity);
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        applyVisibility();
    }

    public void toggleVisibility() {
        setVisible(!visible);
        Toast.makeText(context,
                visible ? R.string.toast_virtual_mouse_shown : R.string.toast_virtual_mouse_hidden,
                Toast.LENGTH_SHORT).show();
    }

    public void show() {
        applyVisibility();
    }

    public void hide() {
        for (View element : elements) {
            element.setVisibility(View.INVISIBLE);
        }
    }

    private void applyVisibility() {
        for (View element : elements) {
            element.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void removeElements() {
        for (View element : elements) {
            parent.removeView(element);
        }
        elements.clear();
    }
}
