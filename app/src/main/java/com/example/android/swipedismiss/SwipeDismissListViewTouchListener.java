/*
 * Copyright 2013 Google Inc. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.example.android.swipedismiss;

import lombok.val;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * A {@link View.OnTouchListener} that makes the list items in a
 * {@link ListView} dismissable. {@link ListView} is given special treatment
 * because by default it handles touches for its list items... i.e. it's in
 * charge of drawing the pressed state (the list selector), handling list item
 * clicks, etc.
 * <p>
 * After creating the listener, the caller should also call
 * {@link ListView#setOnScrollListener(AbsListView.OnScrollListener)}, passing
 * in the scroll listener returned by {@link #makeScrollListener()} . If a
 * scroll listener is already assigned, the caller should still pass scroll
 * changes through to this listener. This will ensure that this
 * {@link SwipeDismissListViewTouchListener} is paused during list view
 * scrolling.
 * </p>
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(listView, new SwipeDismissListViewTouchListener.OnDismissCallback() {
 * 
 *     public void onDismiss(ListView listView, int[] reverseSortedPositions) {
 * 
 *         for (int position : reverseSortedPositions) {
 *             adapter.remove(adapter.getItem(position));
 *         }
 *         adapter.notifyDataSetChanged();
 *     }
 * });
 * listView.setOnTouchListener(touchListener);
 * listView.setOnScrollListener(touchListener.makeScrollListener());
 * </pre>
 * <p>
 * This class Requires API level 12 or later due to use of
 * {@link ViewPropertyAnimator}.
 * </p>
 * <p>
 * For a generalized {@link View.OnTouchListener} that makes any view
 * dismissable, see {@link SwipeDismissTouchListener}.
 * </p>
 * 
 * @see SwipeDismissTouchListener
 */
public class SwipeDismissListViewTouchListener implements View.OnTouchListener {

    // Cached ViewConfiguration and system-wide constant values
    private int mSlop;
    private long mAnimationTime;

    // Fixed properties
    private final ListView mListView;
    private DismissCallbacks mCallbacks;
    private int mViewWidth = 0;

    // Transient properties
    private int mDismissAnimationRefCount = 0;
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private int mSwipingSlop;
    private int mDownPosition;
    private View mDownView;
    private Drawable mDownViewBackground;
    private boolean mPaused;

    /**
     * The callback interface used by {@link SwipeDismissListViewTouchListener}
     * to inform its client about a successful dismissal of one or more list
     * item positions.
     */
    public interface DismissCallbacks {

        /**
         * Called when the user has indicated they she would like to dismiss one
         * or more list item positions.
         * 
         * @param listView
         *            The originating {@link ListView}.
         * @param reverseSortedPositions
         *            An array of positions to dismiss, sorted in descending
         *            order for convenience.
         */
        void onDismiss(ListView listView, int position);
    }

    /**
     * Constructs a new swipe-to-dismiss touch listener for the given list view.
     * 
     * @param listView
     *            The list view whose items should be dismissable.
     * @param callbacks
     *            The callback to trigger when the user has indicated that she
     *            would like to dismiss one or more list items.
     */
    public SwipeDismissListViewTouchListener(ListView listView, DismissCallbacks callbacks) {

        ViewConfiguration vc = ViewConfiguration.get(listView.getContext());

        mSlop = vc.getScaledTouchSlop();
        mAnimationTime = listView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
        mListView = listView;
        mCallbacks = callbacks;

        mListView.setOnScrollListener(makeScrollListener());
    }

    /**
     * Enables or disables (pauses or resumes) watching for swipe-to-dismiss
     * gestures.
     * 
     * @param enabled
     *            Whether or not to watch for gestures.
     */
    public void setEnabled(boolean enabled) {

        mPaused = !enabled;
    }

    /**
     * Returns an {@link AbsListView.OnScrollListener} to be added to the
     * {@link ListView} using
     * {@link ListView#setOnScrollListener(AbsListView.OnScrollListener)}. If a
     * scroll listener is already assigned, the caller should still pass scroll
     * changes through to this listener. This will ensure that this
     * {@link SwipeDismissListViewTouchListener} is paused during list view
     * scrolling.</p>
     * 
     * @see SwipeDismissListViewTouchListener
     */
    public AbsListView.OnScrollListener makeScrollListener() {

        return new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {

                setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }

        };
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN: {
                if (mPaused) {
                    return false;
                }

                mViewWidth = mListView.getWidth();
                mDownView = getListViewRow(event);

                if (mDownView != null) {
                    mDownX = event.getRawX();
                    mDownY = event.getRawY();
                    mDownViewBackground = mDownView.getBackground();
                    mDownPosition = mListView.getPositionForView(mDownView);
                }

                return false;
            }

            case MotionEvent.ACTION_CANCEL: {
                if (mDownView == null) {
                    break;
                }

                if (mSwiping) {
                    mDownView.animate().translationX(0).setDuration(mAnimationTime).setListener(null);
                }
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mDownView == null) {
                    break;
                }

                float deltaX = event.getRawX() - mDownX;
                boolean dismiss = false;
                if (deltaX > mViewWidth / 2 && mSwiping) {
                    dismiss = true;
                }
                if (dismiss && mDownPosition != ListView.INVALID_POSITION) {
                    // dismiss
                    final View downView = mDownView;
                    // mDownView gets null'd before animation ends
                    final int downPosition = mDownPosition;
                    mDismissAnimationRefCount++;
                    mDownView.animate().translationX(mViewWidth).setDuration(mAnimationTime).setListener(new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(Animator animation) {

                            performDismiss(downView, downPosition);
                        }
                    });
                } else {
                    // cancel
                    mDownView.setBackground(mDownViewBackground);
                    mDownView.animate().translationX(0).setDuration(mAnimationTime);
                    mDownX = 0;
                    mDownY = 0;
                    mDownView = null;
                    mDownViewBackground = null;
                    mDownPosition = ListView.INVALID_POSITION;
                    mSwiping = false;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mDownView == null || mPaused) {
                    break;
                }

                float deltaX = event.getRawX() - mDownX;
                float deltaY = event.getRawY() - mDownY;

                if (Math.abs(deltaX) > mSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                    mSwiping = true;
                    mSwipingSlop = (deltaX > 0 ? mSlop : -mSlop);
                    mListView.requestDisallowInterceptTouchEvent(true);

                    // Cancel ListView's touch (un-highlighting the item)
                    MotionEvent cancelEvent = MotionEvent.obtain(event);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mListView.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }

                if ((deltaX - mSwipingSlop) < 0) {
                    return false;
                }

                if (mSwiping) {
                    mDownView.setTranslationX(deltaX - mSwipingSlop);
                    if (mDownView.getTranslationX() > mViewWidth / 2) {
                        mDownView.setBackgroundColor(mListView.getContext().getResources().getColor(android.R.color.holo_red_light));
                    } else if (mDownViewBackground != null) {
                        mDownView.setBackground(mDownViewBackground);
                    }
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private View getListViewRow(MotionEvent event) {

        Rect rect = new Rect();

        int[] listViewCoords = new int[2];
        mListView.getLocationOnScreen(listViewCoords);

        int x = (int) event.getRawX() - listViewCoords[0];
        int y = (int) event.getRawY() - listViewCoords[1];

        int childCount = mListView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mListView.getChildAt(i);
            child.getHitRect(rect);
            if (rect.contains(x, y)) {
                return child;
            }
        }
        return null;
    }

    private void performDismiss(final View dismissView, final int dismissPosition) {

        ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        int originalHeight = dismissView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);

        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {

                if (--mDismissAnimationRefCount == 0) {
                    mCallbacks.onDismiss(mListView, dismissPosition);
                    mDownPosition = ListView.INVALID_POSITION;

                    dismissView.setTranslationX(0);
                    lp.height = originalHeight;
                    dismissView.setLayoutParams(lp);
                    dismissView.setBackground(mDownViewBackground);

                    long time = SystemClock.uptimeMillis();
                    MotionEvent cancelEvent = MotionEvent.obtain(time, time, MotionEvent.ACTION_CANCEL, 0, 0, 0);
                    mListView.dispatchTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                lp.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });

        animator.start();
    }

}
