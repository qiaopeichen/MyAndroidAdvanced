package com.qiaopc.mvptest.ipinfo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qiaopc.mvptest.R;

/**
 * 定义IpInfoFragment实现IpInfoContract.View
 * 注释1处通过实现setPresenter方法来注入InfoPresenter。
 * 注释2处则调用InfoPresenter的getIpInfo方法来获取IP地址的信息。
 * 另外，IpInfoFragment实现了View接口，用来接收IpInfoPresenter的回调并更新界面。
 *
 * IpInfoFragment在IpInfoActivity中调用setPresenter来注入IpInfoPresenter。
 */
public class IpInfoFragment extends Fragment implements IpInfoContract.View{
    private TextView tv_country;
    private TextView tv_area;
    private TextView tv_city;
    private Button bt_ipinfo;
    private Dialog mDialog;
    private IpInfoContract.Presenter mPresenter;

    public static IpInfoFragment newInstance() {
        return new IpInfoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ip_info, container, false);
        tv_country = root.findViewById(R.id.tv_area);
        tv_area= root.findViewById(R.id.tv_area);
        tv_city= root.findViewById(R.id.tv_city);
        bt_ipinfo= root.findViewById(R.id.bt_ipinfo);
        return root;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDialog = new ProgressDialog(getActivity());
        mDialog.setTitle("获取数据中");
        bt_ipinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.getIpInfo("39.155.184.147"); // 2
            }
        });
    }

    @Override
    public void setPresenter(IpInfoContract.Presenter presenter) { // 1
        mPresenter = presenter;
    }

    @Override
    public void setIpInfo(IpInfo ipInfo) {
        if (ipInfo != null && ipInfo.getData() != null) {
            IpData ipData = ipInfo.getData();
            tv_country.setText(ipData.getCountry());
            tv_area.setText(ipData.getCity());
        }
    }

    @Override
    public void showLoading() {
        mDialog.show();
    }

    @Override
    public void hideLoading() {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void showError() {
        Toast.makeText(getActivity().getApplicationContext(), "网络出错", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }
}
