package com.tarungoyaldev.android.calculator.tools;

import android.view.MotionEvent;
import android.view.View;

/**
 * Handle different swipe events on a view.
 */
public class SwipeDetector implements View.OnTouchListener{

    private float downX, downY, upX, upY;
    private final float minSwipeDistance;
    private final Callback swipeCallback;

    /**
     * Constructer for handling Swipe events.
     * @param minSwipeDistance minimum swipe distance in dp.
     * @param swipeCallback callback to handle swipe events.
     */
    public SwipeDetector(Callback swipeCallback, float minSwipeDistance) {
        this.minSwipeDistance = minSwipeDistance;
        this.swipeCallback = swipeCallback;
    }

    /**
     * Constructer for handling Swipe events.
     * @param swipeCallback callback to handle swipe events.
     */
    public SwipeDetector(Callback swipeCallback) {
        this.minSwipeDistance = 50;
        this.swipeCallback = swipeCallback;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                upX = event.getX();
                upY = event.getY();
                float deltaX = upX - downX;
                float deltaY = upY - downY;
                if (Math.abs(deltaX) >= Math.abs(deltaY) && Math.abs(deltaX) >= minSwipeDistance) {
                    if (deltaX > 0) {
                        swipeCallback.onRightSwipe();
                    } else {
                        swipeCallback.onLeftSwipe();
                    }
                    return true;
                } else if (Math.abs(deltaY) >= minSwipeDistance) {
                    if (deltaY > 0) {
                        swipeCallback.onUpSwipe();
                    } else {
                        swipeCallback.onDownSwipe();
                    }
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    /**
     * Callback class to handle various types of swipes. Override one or more methods to handle
     * swipes.
     */
    public static class Callback {
        public boolean onLeftSwipe() {
            return false;
        }
        public boolean onRightSwipe() {
            return false;
        }
        public boolean onUpSwipe() {
            return false;
        }
        public boolean onDownSwipe() {
            return false;
        }
    }
}
