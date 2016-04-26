package com.mx.listviewlib;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

/**
 * Created by boobooL on 2016/4/26 0026
 * Created 邮箱 ：boobooMX@163.com
 */
public class SwipeMenuAdapter implements WrapperListAdapter, SwipeMenuView.OnSwipeItemClickListener {
    private ListAdapter mAdapter;
    private Context mContext;
    private RefreshSwipeMenuListView.OnMenuItemClickListener mOnMenuItemClickListener;


    public SwipeMenuAdapter(Context context,ListAdapter adapter) {
        mAdapter = adapter;
        mContext = context;
    }


    @Override
    public ListAdapter getWrappedAdapter() {
        return mAdapter;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        return mAdapter.isEnabled(position);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mAdapter.registerDataSetObserver(observer);

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mAdapter.unregisterDataSetObserver(observer);

    }

    @Override
    public int getCount() {
        return mAdapter.getCount();
    }

    @Override
    public Object getItem(int position) {
        return mAdapter.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return mAdapter.getItemId(position);
    }

    @Override
    public boolean hasStableIds() {
        return mAdapter.hasStableIds();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SwipeMenuLayout layout=null;
        if(convertView==null){
            View contentView=mAdapter.getView(position,convertView,parent);
            SwipeMenu menu=new SwipeMenu(mContext);
            menu.setViewType(mAdapter.getItemViewType(position));
            createMenu(menu);
            SwipeMenuView menuView=new SwipeMenuView(menu, (RefreshSwipeMenuListView) parent);
            menuView.setOnSwipeItemClickListener(this);
            RefreshSwipeMenuListView listView= (RefreshSwipeMenuListView) parent;
            layout=new SwipeMenuLayout(contentView,menuView,listView.getCloseInterpolator(),listView.getOpenInterpolator());
            layout.setPosition(position);
        }else{
            layout= (SwipeMenuLayout) convertView;
            layout.closeMenu();
            layout.setPosition(position);
            mAdapter.getView(position,layout.getContentView(),parent);
        }

        return layout;
    }

    private void createMenu(SwipeMenu menu) {

        SwipeMenuItem item=new SwipeMenuItem(mContext);
        item.setTitle("Item 1");
        item.setBackground(new ColorDrawable(Color.GRAY));
        item.setWidth(300);
        menu.addMenuItem(item);


        item=new SwipeMenuItem(mContext);
        item.setTitle("Item 2");
        item.setBackground(new ColorDrawable(Color.RED));
        item.setWidth(300);
        menu.addMenuItem(item);


    }

    @Override
    public int getItemViewType(int position) {
        return mAdapter.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return mAdapter.getViewTypeCount();
    }

    @Override
    public boolean isEmpty() {
        return mAdapter.isEmpty();
    }

    @Override
    public void onItemClick(SwipeMenuView view, SwipeMenu menu, int index) {
        if (mOnMenuItemClickListener != null) {
            mOnMenuItemClickListener.onMenuItemClick(view.getPosition(),menu,index);
        }
    }

    public void setOnMenuItemClickListener(RefreshSwipeMenuListView.OnMenuItemClickListener onMenuItemClickListener) {
        mOnMenuItemClickListener = onMenuItemClickListener;
    }
}
