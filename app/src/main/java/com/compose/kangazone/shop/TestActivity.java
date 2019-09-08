package com.compose.kangazone.shop;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.compose.kangazone.shop.utils.BytesUtil;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        TextView tv = findViewById(R.id.tv);
        TextView tv2 = findViewById(R.id.tv2);
        String str = null;
        try {
            str = Arrays.toString(BytesUtil.getBytesFromHexString("b2e2cad4b4f2d3a1"));
            byte[] gbks = "测试打印".getBytes("gbk");
            tv2.setText(Arrays.toString(gbks));
            tv.setText(str);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }

}
