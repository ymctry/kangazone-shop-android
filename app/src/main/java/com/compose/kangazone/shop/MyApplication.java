package com.compose.kangazone.shop;

import android.app.Application;

import com.sunmi.payment.PaymentService;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 服务初始化
        PaymentService.getInstance().init(this);
    }
}
