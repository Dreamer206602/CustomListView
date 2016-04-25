package com.mx.listviewlib;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * Created by boobooL on 2016/4/25 0025
 * Created 邮箱 ：boobooMX@163.com
 */
public class RefreshSwipeMenuListView extends ListView implements AbsListView.OnScrollListener {
    public static final int TOUCH_STATE_NONE = 0;
    public static final int TOUCH_STATE_X = 1;//x轴触摸状态值
    public static final int TOUCH_STATE_Y = 2;//y轴触摸状态值
    public static final int BOTH = 2;//上拉和下拉
    public static final int HEADER = 0;//下拉
    public static final int FOOTER = 1;//上拉
    public static String tag;//ListView的动作
    public static final String REFRESH = "refresh";
    public static final String LOAD = "load";
    private int MAX_Y = 5;//Y轴最大的偏移量
    private int MAX_X = 3;//X轴最大偏移量
    private float mDownX;//触摸x
    private float mDownY;//触摸y
    private int mTouchState;//触摸的状态
    private int mTouchPosition;//触摸的位置
    private SwipeMenuLayout mTouchView;//滑动弹出布局
    private OnSwipeListener mOnSwipeListener;//弹出的监听器

    private float firstTouchY;//第一次触摸y坐标
    private float lastTouchY;//最后一次触摸y坐标

    //创建左滑菜单接口
    private SwipeMenuCreator mMenuCreator;

    //菜单点击事件
    private OnMenuItemClickListener mOnMenuItemClickListener;
    //关闭菜单动画修饰符In
    private Interpolator mCloseInterpolator;
    //开启菜单动画修饰Interpolator
    private Interpolator mOpenInterpolator;

    private float mLastY = -1;
    private Scroller mScroller;
    private OnScrollListener mScrollListener;//滑动监听

    //下拉上拉监听器
    private OnRefreshListener mOnRefreshListener;

    //下拉头
    private RefreshListHeader mHeaderView;


    //头部视图内容，用来计算头部高度，不下拉时隐藏
    private RelativeLayout mHeaderViewContent;

    //下拉时间文本控件
    private TextView mHeaderTimeView;
    private int mHeaderViewHeight;//头部高度
    private boolean mEnablePullRefresh = true;//能否下拉刷新
    private boolean mPullRefreshing = false;//是否正在刷新

    //上拉尾部视图
    private LinearLayout mFootetView;
    private boolean mEnablePullLoad;//是否可以上拉加载
    private boolean mPullLoading;//是否正在上拉
    private boolean mIsFooterReady = false;
    private int mTotalItemCount;
    private int mScrollBack;
    public static final int SCROLLBACK_HEADER = 0;
    public static final int SCROLLBACK_FOOTETR = 1;

    public static final int SCROLL_DURATION = 400;
    public static final int PULL_LOAD_MORE_DELTA = 50;
    public static final float OFFSET_RADIO = 1.8f;
    private boolean isFooterVisible = false;


    public RefreshSwipeMenuListView(Context context) {
        super(context);
        init(context);
    }


    public RefreshSwipeMenuListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RefreshSwipeMenuListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化控件
     *
     * @param context
     */
    private void init(Context context) {
        mScroller = new Scroller(context, new DecelerateInterpolator());
        super.setOnScrollListener(this);
        //初始化头部视图
        mHeaderView = new RefreshListHeader(context);
        mHeaderViewContent = (RelativeLayout) mHeaderView.findViewById(R.id.xlistview_header_content);
        mHeaderTimeView = (TextView) mHeaderTimeView.findViewById(R.id.xlistview_header_time);
        addHeaderView(mHeaderTimeView);


        //初始化尾部视图
        mFootetView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.xlistview_footer, null, false);

        //初始化头部高度
        mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mHeaderViewHeight = mHeaderViewContent.getHeight();
                //向ViewTreeObserver 注册方法 ，以获取控件的尺寸
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        MAX_X = dp2px(MAX_X);
        MAX_Y = dp2px(MAX_Y);
        mTouchState = TOUCH_STATE_NONE;

    }

    /**
     * 添加适配器
     *
     * @param adapter
     */
    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mIsFooterReady == false) {
            //添加尾部隐藏
            mIsFooterReady = true;
            addFooterView(mFootetView);
            mFootetView.setVisibility(GONE);
        }
        super.setAdapter(new SwipeMenuAdapter(getContext(), adapter) {


        });

    }

    public Interpolator getOpenInterpolator() {
        return mOpenInterpolator;
    }

    public void setOpenInterpolator(Interpolator openInterpolator) {
        mOpenInterpolator = openInterpolator;
    }

    public Interpolator getCloseInterpolator() {
        return mCloseInterpolator;
    }

    public void setCloseInterpolator(Interpolator closeInterpolator) {
        mCloseInterpolator = closeInterpolator;
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getContext().getResources().getDisplayMetrics());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            //获取上次y轴坐标
            mLastY = ev.getRawY();
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                firstTouchY = ev.getRawY();
                mLastY = ev.getRawY();
                setRefreshTime(RefreshTime.getRefreshTime(getContext()));
                int oldPos = mTouchPosition;
                mDownX = ev.getX();
                mDownY = ev.getY();
                mTouchState = TOUCH_STATE_NONE;

                mTouchPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
                //弹出左滑菜单
                if (mTouchPosition == oldPos && mTouchView != null && mTouchView.isOpen()) {
                    mTouchState = TOUCH_STATE_X;
                    mTouchView.onSwipe(ev);//左滑菜单手势监听事件，根据滑动距离弹出菜单
                    return true;
                }
                //获取item view，此方法是因为getChildAt()传入index值导致ListView不可见的item会报空指针
                //防止ListView不可见的item获取到的为空，使用下面的方法
                View view = getChildAt(mTouchPosition - getFirstVisiblePosition());
                if (mTouchView != null && mTouchView.isOpen()) {
                    //如果画的item不为空并且已经开启，则关闭菜单
                    mTouchView.smoothCloseMenu();
                    mTouchView = null;
                    return super.onTouchEvent(ev);
                }
                if (mTouchView != null) {
                    //否则打开左侧滑菜单
                    mTouchView.onSwipe(ev);

                }
                if (view instanceof SwipeMenuLayout) {
                        mTouchView= (SwipeMenuLayout) view;
                }
                break;


        }

        return super.onTouchEvent(ev);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
