package com.william.androidsdk.baseui.listener;

import android.support.v7.widget.RecyclerView;


/**
 *  RecyclerView滚动监听类
 */
public interface OnScrollingListener {

    /**
     * 列表滚动到最顶部
     */
    void onScrollTop(RecyclerView recyclerView);

    /**
     * 列表滚动到最底部
     */
    void onScrollDown(RecyclerView recyclerView);

    /**
     * 列表滚动中
     */
    void onScrolling(RecyclerView recyclerView);
}

