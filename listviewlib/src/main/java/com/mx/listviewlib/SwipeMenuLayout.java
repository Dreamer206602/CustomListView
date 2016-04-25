package com.mx.listviewlib;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Created by boobooL on 2016/4/25 0025
 * Created 邮箱 ：boobooMX@163.com
 */
public class SwipeMenuLayout extends FrameLayout {
    public static final int CONTENT_VIEW_ID = 1;
    public static final int MENU_VIEW_ID = 2;
    public static final int STATE_CLOSE = 0;
    public static final int STATE_OPEN = 1;

    private View mContentView;
    private SwipeMenuView mMenuView;
    private int mDownX;
    private int state = STATE_CLOSE;
    private GestureDetectorCompat mGestureDetectorCompat;
    private GestureDetector.OnGestureListener mGestureListener;
    private boolean isFling;
    private int MIN_FLING = dp2px(15);
    private int MAX_VELOCITYX = -dp2px(500);
    private ScrollerCompat mOpenScroller;
    private ScrollerCompat mCloseScroller;

    private int mBaseX;
    private int position;
    private Interpolator mCloseInterpolator;
    private Interpolator mOpenInterpolator;

    public SwipeMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SwipeMenuLayout(View contentView, SwipeMenuView menuView) {
        this(contentView, menuView, null, null);
    }

    public SwipeMenuLayout(View contentView, SwipeMenuView menuView, Interpolator closeInterpolator, Interpolator openInterpolator) {
        super(contentView.getContext());
        mCloseInterpolator = closeInterpolator;
        mOpenInterpolator = openInterpolator;
        mContentView = contentView;
        mMenuView.setLayout(this);
        init();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
        mMenuView.setPosition(position);
    }

    private void init() {
        setLayoutParams(new AbsListView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        mGestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                isFling = false;
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 != null && e2 != null) {
                    if (e1.getX() - e2.getX() > MIN_FLING && velocityX < MAX_VELOCITYX) {
                        isFling = true;
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        };

        mGestureDetectorCompat = new GestureDetectorCompat(getContext(), mGestureListener);
        if (mCloseInterpolator != null) {
            mCloseScroller = ScrollerCompat.create(getContext(), mCloseInterpolator);
        } else {
            mCloseScroller = ScrollerCompat.create(getContext());
        }
        if (mOpenInterpolator != null) {
            mOpenScroller = ScrollerCompat.create(getContext(), mCloseInterpolator);
        } else {
            mOpenScroller = ScrollerCompat.create(getContext());
        }
        LayoutParams contentParams=new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mContentView.setLayoutParams(contentParams);
        if(mContentView.getId()<1){
            mContentView.setId(CONTENT_VIEW_ID);
        }


    }


    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources()
                .getDisplayMetrics());
    }
}
