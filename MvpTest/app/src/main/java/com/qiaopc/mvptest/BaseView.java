package com.qiaopc.mvptest;

/**
 * Created by qiaopc on 2018/4/8 0008.
 * 给View绑定Presenter。
 */

public interface BaseView<T> {
    void setPresenter(T presenter);
}
