package com.compose.kangazone.shop.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;

import com.compose.kangazone.shop.R;
import com.qmuiteam.qmui.widget.QMUILoadingView;

public class WaitingDialog extends Dialog {
    private QMUILoadingView qmuiLoadingView;

    public WaitingDialog(@NonNull Context context) {
        super(context, R.style.defaultDialogStyle);
        initDialog();
    }

    private void initDialog() {
        setContentView(R.layout.wait_response_dialog);
        // 居中
        qmuiLoadingView = findViewById(R.id.empty_view_loading);
        getWindow().getAttributes().gravity = Gravity.CENTER;
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        qmuiLoadingView.setVisibility(View.VISIBLE);
        qmuiLoadingView.setColor(R.color.qmui_config_color_black);
        qmuiLoadingView.setActivated(true);
        qmuiLoadingView.setSize(100);
        qmuiLoadingView.start();
    }
}