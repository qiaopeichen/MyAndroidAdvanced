package com.qiaopc.mvptest.ipinfo;

/**
 * Created by qiaopc on 2018/4/8 0008.
 * Model实体IpInfo
 */

public class IpInfo {

    private int code;
    private IpData data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public IpData getData() {
        return data;
    }

    public void setData(IpData data) {
        this.data = data;
    }
}
