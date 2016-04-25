package com.mx.listviewlib;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by boobooL on 2016/4/25 0025
 * Created 邮箱 ：boobooMX@163.com
 */
public class SwipeMenuView extends LinearLayout implements View.OnClickListener {
    private RefreshSwipeMenuListView mListView;
    private SwipeMenuLayout mLayout;
    private SwipeMenu mMenu;
    private OnSwipeItemClickListener mItemClickListener;
    private int position;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public SwipeMenuView(SwipeMenu menu,RefreshSwipeMenuListView listView){
        super(menu.getContext());
        mListView=listView;
        mMenu=menu;
        List<SwipeMenuItem>items=menu.getMenuItems();
        int id=0;
        for(SwipeMenuItem item:items){
            addItem(item,id++);
        }
    }

    private void addItem(SwipeMenuItem item, int id) {
        LayoutParams params=new LayoutParams(item.getWidth(),LayoutParams.MATCH_PARENT);
        LinearLayout parent=new LinearLayout(getContext());
        parent.setId(id);
        parent.setGravity(Gravity.CENTER);
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setLayoutParams(params);
        parent.setBackgroundDrawable(item.getBackground());
        parent.setOnClickListener(this);
        addView(parent);

        if(item.getIcon()!=null){
            parent.addView(createIcon(item));
        }
        if(!TextUtils.isEmpty(item.getTitle())){
            parent.addView(createTitle(item));
        }


    }

    private View createTitle(SwipeMenuItem item) {
        TextView tv=new TextView(getContext());
        tv.setText(item.getTitle());
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(item.getTitleSize());
        tv.setTextColor(item.getTitleColor());
        return tv;
    }

    private View createIcon(SwipeMenuItem item) {
        ImageView iv=new ImageView(getContext());
        iv.setImageDrawable(item.getIcon());
        return iv;
    }


    @Override
    public void onClick(View v) {
        if(mItemClickListener!=null&&mLayout.isOpen()){
            mItemClickListener.onItemClick(this,mMenu,v.getId());
        }
    }

    public OnSwipeItemClickListener getItemClickListener() {
        return mItemClickListener;
    }

    public void setLayout(SwipeMenuLayout mLayout){
        this.mLayout=mLayout;
    }


    public static interface OnSwipeItemClickListener{
        void onItemClick(SwipeMenuView view,SwipeMenu menu,int index);
    }
}
