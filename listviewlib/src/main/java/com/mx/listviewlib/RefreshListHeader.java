package com.mx.listviewlib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by boobooL on 2016/4/25 0025
 * Created 邮箱 ：boobooMX@163.com
 */

/**
 * 自定义头布局
 */
public class RefreshListHeader extends LinearLayout {
    //根布局
    private LinearLayout mContainer;
    //下拉箭头图片
    private ImageView mArrowImageView;
    //下拉进度条
    private ProgressBar mProgressBar;
    //下拉的文本，显示时间
    private TextView mHintTextView;
    //状态值 0-正常 1-准备刷新 2-正在刷新
    private int mState=STATE_NORMAL;


    //抬起动画
    private Animation mRotateUpAnim;
    //下拉动画
    private Animation mRotateDownAnim;

    //下拉动画的时间
    public static final int  ROTATE_ANIM_DURATION=180;

    public static final int STATE_NORMAL=0;
    public static final int STATE_READY=1;
    public static final int STATE_REFRESHING=2;


    public RefreshListHeader(Context context) {
        super(context);
        initView(context);
    }

    public RefreshListHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RefreshListHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /**
     * 初始化组件
     * @param context
     */
    private void initView(Context context) {
        LayoutParams lp=new LayoutParams(LayoutParams.MATCH_PARENT,0);
        //mContainer= LayoutInflater.from(context).inflate()
    }


}
