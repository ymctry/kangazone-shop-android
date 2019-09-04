package com.compose.kangazone.shop;

import android.app.Application;

import com.compose.kangazone.shop.utils.AidlUtil;
import com.sunmi.payment.PaymentService;


public class MyApplication extends Application {
    private boolean isAidl;

    public boolean isAidl() {
        return isAidl;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // 服务初始化
        PaymentService.getInstance().init(this);
        isAidl = true;
        AidlUtil.getInstance().connectPrinterService(this);
    }
}
