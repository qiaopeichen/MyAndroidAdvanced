package com.qiaopc.mvptest.net;

import com.qiaopc.mvptest.LoadTasksCallBack;

/**
 * Created by qiaopc on 2018/4/8 0008.
 * 获取网络数据的接口类
 */

public interface NetTask<T> {
    void execute(T data, LoadTasksCallBack callBack);
}
