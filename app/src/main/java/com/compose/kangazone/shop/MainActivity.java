package com.compose.kangazone.shop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
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
import com.google.gson.Gson;
import com.sunmi.payment.PaymentService;

@SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private ResultReceiver resultReceiver;
    private WaitingDialog waitingDialog;

    private String isBack; // 0能返回，1不能返回

    private String url = "http://172.16.0.250:8080/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initView();
        registerResultReceiver();

        binding.acetGetUrl.setText(url);
    }

    private void initView() {
        WebSettings settings = binding.wvShop.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setDomStorageEnabled(true);
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

        binding.acbSetUrl.setOnClickListener(view -> {
            url = binding.acetGetUrl.getText().toString();
            binding.wvShop.loadUrl(url);
        });
    }

    @JavascriptInterface
    public String jsCallAndroid(String msg) {
        //Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        consumption();
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

    // 调用消费接口，交易类型00表示消费
    private void consumption() {
        waitingDialog = new WaitingDialog(this);
        waitingDialog.show();
        Request request = new Request();
        // 应用类型
        request.appType = "test";
        // 应用包名
        request.appId = getPackageName();
        // 交易类型
        request.transType = "00";
        // 交易金额
        Long amount = 0L;
        try {
            amount = Long.parseLong("1000");
        } catch (Exception e) {
        }
        request.amount = amount;
        // Saas软件订单号
        request.orderId = "1234";
        // 商品信息
        request.orderInfo = "1244";
        // 支付码
        request.payCode = "124";


        Config config = new Config();
        // 交易过程中是否显示UI界面
        config.processDisplay = false;
        // 是否展示交易结果页
        config.resultDisplay = false;
        // 是否打印小票
        config.printTicket = false;
        // 指定签购单上的退款订单号类型
        config.printIdType = "";
        // 备注
        config.remarks = "";
        request.config = config;

        Gson gson = new Gson();
        String jsonStr = gson.toJson(request);
        PaymentService.getInstance().callPayment(jsonStr, new PaymentService.PaymentCallback() {
            @Override
            public void callFail() {
                PaymentService.getInstance().init(getApplication());

                Toast.makeText(getApplicationContext(), "交易失败，请重试", Toast.LENGTH_LONG).show();
            }

            @Override
            public void callSuccess() {

                Toast.makeText(getApplicationContext(), "交易成功", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void registerResultReceiver() {
        resultReceiver = new ResultReceiver(result -> {
            if (waitingDialog != null && waitingDialog.isShowing()) {
                waitingDialog.dismiss();
            }
            javaCallJS(result);
        });
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String str = "{'resultCode':'-1','resultMsg':'isBack'}";
        binding.wvShop.post(() -> binding.wvShop.evaluateJavascript("javascript:sendJSMessage('" + str + "')", value -> {
            //此处为 js 返回的结果
            isBack = value;
        }));
        if (isBack.equals("1")) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
