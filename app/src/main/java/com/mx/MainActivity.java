package com.mx;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mx.bean.MsgBean;
import com.mx.listviewlib.RefreshSwipeMenuListView;
import com.zhy.base.adapter.ViewHolder;
import com.zhy.base.adapter.abslistview.CommonAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RefreshSwipeMenuListView.OnRefreshListener {

    private RefreshSwipeMenuListView mListView;
    private List<MsgBean>data;
    private CommonAdapter<MsgBean> mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView= (RefreshSwipeMenuListView) findViewById(R.id.listView);

        data=new ArrayList<>();
        initData();

        mAdapter=new CommonAdapter<MsgBean>(MainActivity.this,R.layout.msg_item,data) {
            @Override
            public void convert(ViewHolder holder, MsgBean msgBean) {
                    holder.setText(R.id.tv_name,msgBean.getName());
                    holder.setText(R.id.tv_content,msgBean.getContent());
                    holder.setText(R.id.tv_time,msgBean.getTime());
            }
        };
        mListView.setAdapter(mAdapter);
        //设置加载的模式
        mListView.setListViewMode(RefreshSwipeMenuListView.BOTH);
        mListView.setOnRefreshListener(this);


    }

    private void initData() {

        for (int i = 0; i <15 ; i++) {
            MsgBean msgBean=new MsgBean();
            msgBean.setName("张某某"+i);
            msgBean.setContent("你好，在么？"+i);
            msgBean.setTime("上午10:30");
            data.add(msgBean);
        }

    }

    @Override
    public void onRefresh() {
        mListView.postDelayed(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i <5 ; i++) {
                    MsgBean msgBean=new MsgBean();
                    msgBean.setName("赛后类"+i);
                    msgBean.setContent("我想你"+i);
                    msgBean.setTime("下午14:00");
                    data.add(msgBean);
                }
                mListView.complete();
                mAdapter.notifyDataSetChanged();

            }
        },2000);
    }

    @Override
    public void onLoadMore() {

        mListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <10 ; i++) {
                    MsgBean msgBean=new MsgBean();
                    msgBean.setName("萌妹子"+i);
                    msgBean.setContent("萌萌哒"+i);
                    msgBean.setTime("晚上19:00");
                    data.add(msgBean);
                }
                mListView.complete();
                mAdapter.notifyDataSetChanged();

            }
        },2000);


    }
}
