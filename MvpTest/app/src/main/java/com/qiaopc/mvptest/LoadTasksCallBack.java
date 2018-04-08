package com.qiaopc.mvptest;

/**
 * Created by qiaopc on 2018/4/8 0008.
 * 回调监听接口LoadTasksCallBack
 */

public interface LoadTasksCallBack<T> {
    void onSuccess(T t);
    void onStart();
    void onFailed();
    void onFinish();
}
