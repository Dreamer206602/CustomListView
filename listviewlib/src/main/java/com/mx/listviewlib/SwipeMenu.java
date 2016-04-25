package com.mx.listviewlib;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by boobooL on 2016/4/25 0025
 * Created 邮箱 ：boobooMX@163.com
 */
public class SwipeMenu {
    private Context mContext;
    private List<SwipeMenuItem>mItems;
    private int mViewType;

    public SwipeMenu(Context context) {
        mContext = context;
        mItems=new ArrayList<>();
    }
    public Context getContext(){
        return mContext;
    }
    public void addMenuItem(SwipeMenuItem item){
        mItems.add(item);
    }
    public void removeMenuItem(SwipeMenuItem item){
        mItems.remove(item);
    }

    public List<SwipeMenuItem>getMenuItems(){
        return mItems;
    }
    public int getViewType(){
        return mViewType;
    }
    public void setViewType(int viewType){
        this.mViewType=viewType;
    }
}
