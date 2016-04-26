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

import java.util.Date;

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
        mHeaderTimeView = (TextView) mHeaderView.findViewById(R.id.xlistview_header_time);
        addHeaderView(mHeaderView);


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
                    mTouchView = (SwipeMenuLayout) view;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //手势滑动的事件
                final float deltaY = ev.getRawY() - mLastY;
                float dy = Math.abs(ev.getY() - mDownY);
                float dx = Math.abs(ev.getX() - mDownX);
                mLastY = ev.getRawY();
                //判断左滑菜单是否未激活，或者x轴偏移平方小于y轴偏移平方3倍的时候
                if ((mTouchView == null || !mTouchView.isActive()) && Math.pow(dx, 2) / Math.pow(dy, 2) <= 3) {
                    //判断第一个可见位置并且头布局可见高度大于0时或者y轴偏移量>0
                    if (getFirstVisiblePosition() == 0 && (mHeaderView.getVisibleHeight() > 0) || deltaY > 0) {
                        //重新更新头部高度
                        updateHeaderHeight(deltaY / OFFSET_RADIO);
                        invokeOnScrolling();
                    }
                }
                if (mTouchState == TOUCH_STATE_X) {
                    //如果x轴偏移量则弹出左滑的菜单
                    if (mTouchView != null) {
                        mTouchView.onSwipe(ev);
                    }

                    getSelector().setState(new int[]{0});
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(ev);
                    return true;
                } else if (mTouchState == TOUCH_STATE_NONE) {
                    if (Math.abs(dy) > MAX_Y) {
                        //如果y轴的偏移量>指定y轴的偏移量，设置y轴的偏移状态
                        mTouchState = TOUCH_STATE_Y;
                    } else if (dx > MAX_X) {
                        //如果x轴偏移量>指定x轴偏移量，设置x轴偏移状态，开始弹出左滑的菜单
                        mTouchState = TOUCH_STATE_X;
                        if (mOnSwipeListener != null) {
                            mOnSwipeListener.onSwipeStart(mTouchPosition);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mLastY = -1;//reset
                if (getFirstVisiblePosition() == 0) {
                    //设置下拉刷新状态值，开启下拉刷新状态
                    if (mEnablePullRefresh && mHeaderView.getVisibleHeight() > mHeaderViewHeight) {
                        mPullRefreshing = true;
                        mHeaderView.setState(RefreshListHeader.STATE_REFRESHING);
                        if (mOnRefreshListener != null) {
                            tag = REFRESH;
                            mOnRefreshListener.onRefresh();
                        }
                    }
                    resetHeaderHeight();
                }

                lastTouchY = ev.getRawY();//获取上次y轴偏移量
                if (canLoadMore()) {
                    //判断是否满足上拉
                    loadMore();
                }

                if (mTouchState == TOUCH_STATE_Y) {
                    //如果为x轴偏移状态，开启左滑
                    if (mTouchView != null) {
                        mTouchView.onSwipe(ev);
                        if (!mTouchView.isOpen()) {
                            mTouchPosition = -1;
                            mTouchView = null;
                        }
                    }

                    if (mOnSwipeListener != null) {
                        mOnSwipeListener.onSwipeEnd(mTouchPosition);
                    }
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(ev);
                    return true;
                }
                break;
        }

        return super.onTouchEvent(ev);
    }

    public void smoothOpenMenu(int position) {
        if (position >= getFirstVisiblePosition() && position <= getLastVisiblePosition()) {
            View view = getChildAt(position - getFirstVisiblePosition());
            if (view instanceof SwipeMenuLayout) {
                mTouchPosition = position;
                if (mTouchView != null && mTouchView.isOpen()) {
                    mTouchView.smoothCloseMenu();
                }
                mTouchView = (SwipeMenuLayout) view;
                mTouchView.smoothOpenMenu();
            }

        }
    }

    public void setMenuCreator(SwipeMenuCreator menuCreator) {
        mMenuCreator = menuCreator;
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        mOnMenuItemClickListener = onMenuItemClickListener;
    }

    /**
     * 设置刷新可用
     *
     * @param enable
     */
    private void setPullRefreshEnable(boolean enable) {
        mEnablePullRefresh = enable;
        if (!mEnablePullRefresh) {
            //disable,hide the content
            mHeaderViewContent.setVisibility(INVISIBLE);
        } else {
            mHeaderViewContent.setVisibility(VISIBLE);
        }
    }

    /**
     * stop  refresh, reset header view
     * 停止刷新，重置头部控件
     *
     * @param enable
     */
    private void setPullLoadEnable(boolean enable) {
        mEnablePullLoad = enable;
        if (!mEnablePullLoad) {
            mFootetView.setVisibility(GONE);
            mFootetView.setOnClickListener(null);
        } else {
            mPullLoading = false;
            mFootetView.setVisibility(VISIBLE);
            mFootetView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLoadMore();
                }
            });
        }
    }

    /**
     * stop refresh,reset header view
     */
    private void stopRefresh(){
        if(mPullRefreshing==true){
            mPullRefreshing=false;
            resetHeaderHeight();
        }
    }




    /**
     * stop load more，reset footer view
     * 停止加载更多，重置尾部控件
     */
    private void stopLoadMore() {
        if (mPullLoading == true) {
            mPullLoading = false;
            mFootetView.setVisibility(GONE);
        }
    }


    /**
     * set last refresh time
     *
     * @param time
     */
    private void setRefreshTime(String time) {
        mHeaderTimeView.setText(time);
    }

    private void invokeOnScrolling() {
        if (mScrollListener instanceof OnXScrollListener) {
            OnXScrollListener l = (OnXScrollListener) mScrollListener;
            l.onXScrolling(this);
        }
    }

    /**
     * 更新头部的高度，设置状态值
     *
     * @param delta
     */
    private void updateHeaderHeight(float delta) {
        mHeaderView.setVisibleHeight((int) (delta = mHeaderView.getVisibleHeight()));
        if (mEnablePullRefresh && !mPullLoading) {
            if (mHeaderView.getVisibleHeight() > mHeaderViewHeight) {
                mHeaderView.setState(RefreshListHeader.STATE_READY);
            } else {
                mHeaderView.setState(RefreshListHeader.STATE_NORMAL);
            }
        }

        setSelection(0);// scroll to top each time

    }


    /**
     * 重置头部视图的高度
     */
    private void resetHeaderHeight() {
        int height = mHeaderView.getVisibleHeight();
        if (height == 0)//不可见
            return;

        //如果正在刷新并且头部高度没有完全显示不做操作
        if (mPullRefreshing && height < mHeaderViewHeight) {
            return;
        }
        int finalHeight = 0;//默认
        //如果正在刷新并且滑动高度大于头部高度
        if (mPullRefreshing && height > mHeaderViewHeight) {
            finalHeight = mHeaderViewHeight;
        }
        mScrollBack = SCROLLBACK_HEADER;
        mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);
        //触发 computeScroll()
        invalidate();

    }

    /**
     * 开始上拉
     */
    private void startLoadMore() {
        mPullLoading = true;
        mFootetView.setVisibility(VISIBLE);
        if (mOnRefreshListener != null) {
            tag = LOAD;
            mOnRefreshListener.onLoadMore();
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (mScrollBack == SCROLLBACK_HEADER) {
                mHeaderView.setVisibleHeight(mScroller.getCurrY());
            }
            postInvalidate();
            invokeOnScrolling();
        }
        super.computeScroll();
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        this.mScrollListener=l;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mScrollListener != null) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }

    }

    /**
     * 滑动监听
     *
     * @param view
     * @param firstVisibleItem
     * @param visibleItemCount
     * @param totalItemCount
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        mTotalItemCount = totalItemCount;
        if (mScrollListener != null) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
        if (firstVisibleItem + visibleItemCount >= totalItemCount) {
            isFooterVisible = true;
        } else {
            isFooterVisible = false;
        }

    }
    public void setOnSwipeListener(OnSwipeListener onSwipeListener){
        this.mOnSwipeListener=onSwipeListener;
    }

    public void setOnRefreshListener(OnRefreshListener l) {
        this.mOnRefreshListener = l;
    }

    public interface OnXScrollListener extends OnScrollListener {
        void onXScrolling(View view);
    }


    public interface OnMenuItemClickListener {
        void onMenuItemClick(int position, SwipeMenu menu, int index);
    }

    public interface OnSwipeListener {
        void onSwipeStart(int position);

        void onSwipeEnd(int position);
    }


    /**
     * implements this interface to get refresh/load more event
     */
    public interface OnRefreshListener {
         void onRefresh();

         void onLoadMore();
    }

    /**
     * 上拉加载和下拉刷新请求完毕
     */
    public void complete() {
        stopLoadMore();
        stopRefresh();
        if (REFRESH.equals(tag)) {
            RefreshTime.setRefreshTime(getContext(), new Date());
        }
    }

    /**
     * 设置ListView的模式，上拉和下拉
     *
     * @param mode
     */
    public void setListViewMode(int mode) {
        if (mode == BOTH) {
            setPullRefreshEnable(true);
            setPullLoadEnable(true);
        } else if (mode == FOOTER) {
            setPullLoadEnable(true);
        } else if (mode == HEADER) {
            setPullRefreshEnable(true);
        }
    }

    /**
     * 判断是否可以上拉加载
     *
     * @return
     */
    private boolean canLoadMore() {
        return isBottom() && !mPullLoading && !isPullingUp();
    }

    /**
     * 判断是否到达底部
     *
     * @return
     */
    private boolean isBottom() {
        if (getCount() > 0) {
            if (getLastVisiblePosition() == getAdapter().getCount() - 1
                    && getChildAt(getChildCount() - 1).getBottom() <= getHeight()) {
                 return true;
            }
        }
        return false;
    }


    private boolean isPullingUp(){
        return (firstTouchY-lastTouchY)>=200;
    }

    private void loadMore(){
        if(mOnRefreshListener!=null){
            setLoading(true);
        }
    }

    /**
     * 设置是否上拉
     * @param loading
     */
    private void setLoading(boolean loading){
        if(this==null)return;
        mPullLoading=loading;
        if(loading){
            mFootetView.setVisibility(VISIBLE);

            setSelection(getAdapter().getCount()-1);
            mOnRefreshListener.onLoadMore();
        }else {
            mFootetView.setVisibility(GONE);
            firstTouchY=0;
            lastTouchY=0;
        }
    }


}
