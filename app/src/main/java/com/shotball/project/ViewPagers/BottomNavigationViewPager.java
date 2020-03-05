package com.shotball.project.ViewPagers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

public class BottomNavigationViewPager extends ViewPager {

    private boolean isPagingEnabled;

    public BottomNavigationViewPager(@NonNull Context context) {
        super(context);
        this.isPagingEnabled = false;
    }

    public BottomNavigationViewPager(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        this.isPagingEnabled = false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean executeKeyEvent(KeyEvent event) {
        return isPagingEnabled && super.executeKeyEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean enabled) {
        this.isPagingEnabled = enabled;
    }
}
