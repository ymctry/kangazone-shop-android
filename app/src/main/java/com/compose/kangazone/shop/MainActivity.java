package com.compose.kangazone.shop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.compose.kangazone.shop.bean.Config;
import com.compose.kangazone.shop.bean.Request;
import com.compose.kangazone.shop.databinding.ActivityMainBinding;
import com.compose.kangazone.shop.dialog.WaitingDialog;
import com.compose.kangazone.shop.receiver.ResultReceiver;
import com.compose.kangazone.shop.utils.AidlUtil;
import com.compose.kangazone.shop.utils.BluetoothUtil;
import com.compose.kangazone.shop.utils.BytesUtil;
import com.compose.kangazone.shop.utils.ConnectByUsb;
import com.compose.kangazone.shop.utils.ESCUtil;
import com.google.gson.Gson;
import com.sunmi.payment.PaymentService;

import java.io.IOException;

@SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private ResultReceiver resultReceiver;
    private ConnectByUsb connectByUsb;

    private String isBack = "0"; // 0能返回，1不能返回

    //private String url = "http://kangazone-shop.herokuapp.com/";
    private String url = "https://shop.kangazone.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initView();
        registerResultReceiver();

    }

    private void initView() {

        WebSettings settings = binding.wvShop.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setDomStorageEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        //防止弹出系统浏览器提示
        settings.setSupportMultipleWindows(true);

        settings.setSupportZoom(true);
        binding.wvShop.loadUrl(url);

        binding.wvShop.addJavascriptInterface(this, "$App");

        binding.wvShop.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //super.onReceivedSslError(view, handler, error);
                handler.proceed();
            }

            @JavascriptInterface
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                binding.acivBg.setVisibility(View.GONE);
            }
        });
        binding.wvShop.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                //super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                }
            }

        });

    }

    @JavascriptInterface
    public String jsCallAndroid(String msg) {
        //Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        consumption(msg);
        return "android返回值";
    }

    @JavascriptInterface
    public void javaCallJS(String result) {
        binding.wvShop.post(() -> binding.wvShop.evaluateJavascript("javascript:sendJSMessage('" + result + "')", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //此处为 js 返回的结果
            }
        }));
    }

    @JavascriptInterface
    public void jsOpenDrawer() {
        AidlUtil.getInstance().openDrawer();
    }

    // 调用消费接口，交易类型00表示消费
    private void consumption(String message) {
        PaymentService.getInstance().callPayment(message, new PaymentService.PaymentCallback() {
            @Override
            public void callFail() {
                PaymentService.getInstance().init(getApplication());
            }

            @Override
            public void callSuccess() {
            }
        });
    }


    // 获取打印机驱动
    @JavascriptInterface
    public void jsGetDevice() {

    }

    // 打印
    @JavascriptInterface
    public void jsPrint(String venderId, String text) {
        connectByUsb = new ConnectByUsb();
        connectByUsb.getConnect(this, Integer.parseInt(venderId));
        //String str = BytesUtil.stringToHexString("测试打印abcd123");
        connectByUsb.print(this, BytesUtil.getBytesFromHexString(text));
    }

    // 获取所有usb设备
    @JavascriptInterface
    public String jsGetAllUSBDevices() {
        UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        return usbManager.getDeviceList().toString();
    }

    private void registerResultReceiver() {
        resultReceiver = new ResultReceiver(this::javaCallJS);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ResultReceiver.RESPONSE_ACTION);
        registerReceiver(resultReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resultReceiver != null) {
            unregisterReceiver(resultReceiver);
        }
    }

    @JavascriptInterface
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String str = "{\"resultCode\":\"-1\",\"resultMsg\":\"isBack\"}";
        binding.wvShop.post(() -> binding.wvShop.evaluateJavascript("javascript:sendJSMessage('" + str + "')", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //此处为 js 返回的结果
                isBack = value;
            }
        }));
        if (isBack.equals("0")) {
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
        /*if (binding.wvShop.getUrl().equals(url)) {
            return super.onKeyDown(keyCode, event);
        } else {
            return false;

        }*/

    }
}
