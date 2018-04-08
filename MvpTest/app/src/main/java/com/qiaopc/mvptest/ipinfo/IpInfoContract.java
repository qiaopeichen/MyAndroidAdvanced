package com.qiaopc.mvptest.ipinfo;

import com.qiaopc.mvptest.BaseView;

/**
 * Created by qiaopc on 2018/4/8 0008.
 * 定义契约接口IpInfoContract，
 * 契约接口主要用来存放相同业务的Presenter和View的接口，
 * 便于查找和维护。
 */

public interface IpInfoContract {
    interface Presenter {
        void getIpInfo(String ip);
    }
    interface View extends BaseView<Presenter> {
        void setIpInfo(IpInfo ipInfo);
        void showLoading();
        void hideLoading();
        void showError();
        boolean isActive();
    }
}
