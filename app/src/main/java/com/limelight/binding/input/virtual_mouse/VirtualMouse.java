package com.limelight.binding.input.virtual_mouse;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.limelight.R;
import com.limelight.binding.input.virtual_controller.VirtualControllerConfigurationLoader;
import com.limelight.nvstream.NvConnection;
import com.limelight.nvstream.input.MouseButtonPacket;

import java.util.ArrayList;
import java.util.List;

public class VirtualMouse {
    private static final float OPACITY = 0.45f;

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
        int width = screen.widthPixels;
        int height = screen.heightPixels;

        int stickSize = VirtualControllerConfigurationLoader.oscScreenScale(
                VirtualControllerConfigurationLoader.OSC_ANALOG_STICK_UNITS, height);
        int buttonSize = VirtualControllerConfigurationLoader.oscScreenScale(
                VirtualControllerConfigurationLoader.OSC_FACE_BUTTON_UNITS, height);
        int bottomMargin = VirtualControllerConfigurationLoader.oscScreenScale(
                VirtualControllerConfigurationLoader.OSC_BOTTOM_MARGIN_UNITS, height);
        int sideMargin = VirtualControllerConfigurationLoader.oscScreenScale(
                VirtualControllerConfigurationLoader.OSC_SIDE_MARGIN_UNITS, height);
        int gap = VirtualControllerConfigurationLoader.oscScreenScale(
                VirtualControllerConfigurationLoader.OSC_ELEMENT_GAP_UNITS, height);

        int stickY = height - bottomMargin - stickSize;
        int buttonY = height - bottomMargin - buttonSize;

        int scrollStickX = sideMargin;

        int buttonsTotal = 3 * buttonSize + 2 * gap;
        int buttonsLeft = (width - buttonsTotal) / 2;

        VirtualRotaryScrollStick scrollStick = new VirtualRotaryScrollStick(context, conn);
        addElement(scrollStick, scrollStickX, stickY, stickSize, stickSize);

        VirtualMouseButton leftButton = new VirtualMouseButton(
                context, conn, MouseButtonPacket.BUTTON_LEFT, "L");
        addElement(leftButton, buttonsLeft, buttonY, buttonSize, buttonSize);

        VirtualMouseButton rightButton = new VirtualMouseButton(
                context, conn, MouseButtonPacket.BUTTON_RIGHT, "R");
        addElement(rightButton, buttonsLeft + buttonSize + gap, buttonY, buttonSize, buttonSize);

        VirtualMouseButton middleButton = new VirtualMouseButton(
                context, conn, MouseButtonPacket.BUTTON_MIDDLE, "M");
        addElement(middleButton, buttonsLeft + 2 * (buttonSize + gap), buttonY, buttonSize, buttonSize);

        setOpacity(OPACITY);
        applyVisibility();

        for (View element : elements) {
            element.bringToFront();
        }
    }

    private void addElement(View element, int x, int y, int width, int height) {
        elements.add(element);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
        layoutParams.setMargins(x, y, 0, 0);
        parent.addView(element, layoutParams);
        element.bringToFront();
        element.setElevation(8f);
    }

    public boolean containsScreenPoint(float rawX, float rawY) {
        if (!visible) {
            return false;
        }
        for (View element : elements) {
            int[] loc = new int[2];
            element.getLocationOnScreen(loc);
            if (rawX >= loc[0] && rawX < loc[0] + element.getWidth()
                    && rawY >= loc[1] && rawY < loc[1] + element.getHeight()) {
                return true;
            }
        }
        return false;
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
