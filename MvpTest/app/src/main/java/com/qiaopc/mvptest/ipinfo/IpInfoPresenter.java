package com.qiaopc.mvptest.ipinfo;

import com.qiaopc.mvptest.LoadTasksCallBack;
import com.qiaopc.mvptest.net.NetTask;

/**
 * Created by qiaopc on 2018/4/8 0008.
 * IpInfoPresenter中含有NetTask和IpInfoContract.View的实例，并且实现了LoadTasksCallBack接口。
 * 在代码注释1处，将自身传入NetTask的execute方法中来获取数据，并回调给IpInfoPresenter，
 * 最后通过addTaskView来和View进行交互，并更改界面。
 * Presenter就是一个中间人的角色，其通过NetTask，也就是Model层来获得和保存数据，然后再通过View更新界面，
 * 这期间通过定义接口使得View和Model没有任何交互。
 */

public class IpInfoPresenter implements IpInfoContract.Presenter, LoadTasksCallBack<IpInfo> {
    private NetTask netTask;
    private IpInfoContract.View addTaskView;
    public IpInfoPresenter(IpInfoContract.View addTaskView, NetTask netTask) {
        this.netTask = netTask;
        this.addTaskView = addTaskView;
    }

    @Override
    public void getIpInfo(String ip) {
        netTask.execute(ip, this); // 1
    }

    @Override
    public void onSuccess(IpInfo ipInfo) {
        if (addTaskView.isActive()) {
            addTaskView.setIpInfo(ipInfo);
        }
    }

    @Override
    public void onStart() {
        if (addTaskView.isActive()) {
            addTaskView.showLoading();
        }
    }

    @Override
    public void onFailed() {
        if (addTaskView.isActive()) {
            addTaskView.showError();
            addTaskView.hideLoading();
        }
    }

    @Override
    public void onFinish() {
        if (addTaskView.isActive()) {
            addTaskView.hideLoading();
        }
    }
}
