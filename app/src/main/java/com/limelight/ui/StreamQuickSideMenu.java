package com.limelight.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.limelight.R;

public class StreamQuickSideMenu {
    public interface Listener {
        void onToggleKeyboard();
        void onToggleMouse();
        void onToggleZoomMode();
    }

    private final FrameLayout parent;
    private final Context context;
    private final Listener listener;

    private View rootView;
    private ImageButton handleButton;
    private LinearLayout actionsLayout;
    private ImageButton keyboardButton;
    private ImageButton mouseButton;
    private ImageButton zoomButton;

    private boolean expanded = false;
    private boolean mouseToggleAvailable = false;
    private boolean visible = true;

    public StreamQuickSideMenu(FrameLayout parent, Context context, Listener listener) {
        this.parent = parent;
        this.context = context;
        this.listener = listener;

        LayoutInflater inflater = LayoutInflater.from(context);
        rootView = inflater.inflate(R.layout.stream_quick_side_menu, parent, false);

        handleButton = rootView.findViewById(R.id.quick_side_menu_handle);
        actionsLayout = rootView.findViewById(R.id.quick_side_menu_actions);
        keyboardButton = rootView.findViewById(R.id.quick_side_menu_keyboard);
        mouseButton = rootView.findViewById(R.id.quick_side_menu_mouse);
        zoomButton = rootView.findViewById(R.id.quick_side_menu_zoom);

        int marginEnd = (int) (8 * context.getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.END | Gravity.CENTER_VERTICAL);
        layoutParams.setMarginEnd(marginEnd);
        parent.addView(rootView, layoutParams);

        rootView.setElevation(12f);
        rootView.bringToFront();

        handleButton.setOnClickListener(v -> toggleExpanded());
        keyboardButton.setOnClickListener(v -> listener.onToggleKeyboard());
        mouseButton.setOnClickListener(v -> listener.onToggleMouse());
        zoomButton.setOnClickListener(v -> listener.onToggleZoomMode());

        setExpanded(false);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void toggleExpanded() {
        setExpanded(!expanded);
    }

    public boolean isVisible() {
        return visible;
    }

    public void toggleVisibility() {
        if (visible) {
            hide();
        } else {
            show();
        }
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        actionsLayout.setVisibility(expanded ? View.VISIBLE : View.GONE);
        handleButton.setContentDescription(context.getString(
                expanded ? R.string.quick_side_menu_collapse : R.string.quick_side_menu_expand));
    }

    public void setMouseToggleAvailable(boolean available) {
        mouseToggleAvailable = available;
        mouseButton.setVisibility(available ? View.VISIBLE : View.GONE);
    }

    public void syncToggleState(boolean keyboardVisible, boolean mouseVisible, boolean zoomEnabled) {
        keyboardButton.setSelected(keyboardVisible);
        if (mouseToggleAvailable) {
            mouseButton.setSelected(mouseVisible);
        }
        zoomButton.setSelected(zoomEnabled);
    }

    public void show() {
        if (rootView != null) {
            rootView.setVisibility(View.VISIBLE);
            rootView.bringToFront();
            visible = true;
        }
    }

    public void hide() {
        if (rootView != null) {
            rootView.setVisibility(View.GONE);
            visible = false;
        }
    }

    public void destroy() {
        if (rootView != null) {
            parent.removeView(rootView);
            rootView = null;
        }
    }
}
