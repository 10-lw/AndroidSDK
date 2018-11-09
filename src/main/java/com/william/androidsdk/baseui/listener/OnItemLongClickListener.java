package com.william.androidsdk.baseui.listener;

import android.view.View;

/**
 *=RecyclerView条目长按监听类
 */
public interface OnItemLongClickListener {

    /**
     * 当RecyclerView某个条目被长按时回调
     *
     * @param itemView      被点击的条目对象
     * @param position      被点击的条目位置
     * @return              是否拦截事件
     */
    boolean onItemLongClick(View itemView, int position);
}
