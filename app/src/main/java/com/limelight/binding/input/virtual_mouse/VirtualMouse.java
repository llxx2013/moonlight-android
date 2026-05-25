package com.limelight.binding.input.virtual_mouse;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;

import com.limelight.nvstream.NvConnection;
import com.limelight.nvstream.input.MouseButtonPacket;

import java.util.ArrayList;
import java.util.List;

public class VirtualMouse {
    private static final float OPACITY = 0.45f;
    private static final int MARGIN_DP = 15;
    private static final float TRACKPAD_WIDTH_RATIO = 0.38f;
    private static final float TRACKPAD_HEIGHT_RATIO = 0.28f;
    private static final float BUTTON_SIZE_RATIO = 0.06f;
    private static final float ELEMENT_GAP_RATIO = 0.01f;

    private final NvConnection conn;
    private final FrameLayout parent;
    private final View streamView;
    private final Context context;
    private final List<View> elements = new ArrayList<>();

    public VirtualMouse(NvConnection conn, FrameLayout parent, View streamView, Context context) {
        this.conn = conn;
        this.parent = parent;
        this.streamView = streamView;
        this.context = context;
    }

    public void refreshLayout() {
        removeElements();

        DisplayMetrics screen = context.getResources().getDisplayMetrics();
        float density = screen.density;
        int margin = (int) (MARGIN_DP * density);
        int width = screen.widthPixels;
        int height = screen.heightPixels;

        int buttonSize = (int) (height * BUTTON_SIZE_RATIO);
        int gap = Math.max(margin / 2, (int) (height * ELEMENT_GAP_RATIO));
        int trackpadWidth = (int) (width * TRACKPAD_WIDTH_RATIO);
        int trackpadHeight = (int) (height * TRACKPAD_HEIGHT_RATIO);

        int trackpadX = width - margin - trackpadWidth;
        int trackpadY = height - margin - trackpadHeight;

        int buttonColumnX = trackpadX - gap - buttonSize;
        int buttonBottomY = trackpadY + trackpadHeight - buttonSize;

        VirtualTrackpad trackpad = new VirtualTrackpad(context, conn, streamView);
        addElement(trackpad, trackpadX, trackpadY, trackpadWidth, trackpadHeight);

        VirtualMouseButton leftButton = new VirtualMouseButton(
                context, conn, MouseButtonPacket.BUTTON_LEFT, "L");
        addElement(leftButton, buttonColumnX, buttonBottomY, buttonSize, buttonSize);

        VirtualMouseButton rightButton = new VirtualMouseButton(
                context, conn, MouseButtonPacket.BUTTON_RIGHT, "R");
        addElement(rightButton, buttonColumnX, buttonBottomY - gap - buttonSize, buttonSize, buttonSize);

        VirtualMouseButton middleButton = new VirtualMouseButton(
                context, conn, MouseButtonPacket.BUTTON_MIDDLE, "M");
        addElement(middleButton, buttonColumnX, buttonBottomY - (gap + buttonSize) * 2, buttonSize, buttonSize);

        VirtualMouseScrollButton scrollDown = new VirtualMouseScrollButton(context, conn, false);
        addElement(scrollDown, buttonColumnX, buttonBottomY - (gap + buttonSize) * 3, buttonSize, buttonSize);

        VirtualMouseScrollButton scrollUp = new VirtualMouseScrollButton(context, conn, true);
        addElement(scrollUp, buttonColumnX, buttonBottomY - (gap + buttonSize) * 4, buttonSize, buttonSize);

        setOpacity(OPACITY);
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

    public void show() {
        for (View element : elements) {
            element.setVisibility(View.VISIBLE);
        }
    }

    public void hide() {
        for (View element : elements) {
            element.setVisibility(View.INVISIBLE);
        }
    }

    public void removeElements() {
        for (View element : elements) {
            parent.removeView(element);
        }
        elements.clear();
    }
}
