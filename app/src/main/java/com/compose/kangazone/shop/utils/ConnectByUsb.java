package com.compose.kangazone.shop.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Create by ymc on 2019-09-07 10:27.
 */
public class ConnectByUsb {

    private final String ACTION_USB_PERMISSION = "com.template.USB_PERMISSION";//可自定义
    private UsbManager usbManager;
    private UsbEndpoint inputEndPoint, outputEndPoint;
    private UsbDeviceConnection connection;


    //获取连接
    public void getConnect(Context context, int venderId) {
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        UsbDevice targetDevice = null;
        HashMap<String, UsbDevice> deviceMap = usbManager.getDeviceList();
        for (UsbDevice device : deviceMap.values()) {
            if (venderId == device.getVendorId()) {
                targetDevice = device;
                break;
            }
        }
        if (targetDevice.getInterfaceCount() == 0) {
            return;
        }
        int count = targetDevice.getInterfaceCount();
        for (int i = 0; i < count; i++) {
            UsbInterface intf = targetDevice.getInterface(i);
            // 之后我们会根据 intf的 getInterfaceClass 判断是哪种类型的Usb设备，
            // 并且结合 device.getVectorID() 或者厂家ID进行过滤，比如 UsbConstants.USB_CLASS_PRINTER
            if (intf.getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                // 这个device就是你要找的UsbDevice，此时还需要进行权限判断
                if (!usbManager.hasPermission(targetDevice)) { // 没有权限
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
                    IntentFilter filter = new IntentFilter("com.android.example.USB_PERMISSION");
                    context.registerReceiver(mUsbPermissionReceiver, filter);
                    usbManager.requestPermission(targetDevice, permissionIntent);
                    return;
                } else {
                    usbDeviceInit(targetDevice);
                }
            }
        }
    }

    private BroadcastReceiver mUsbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                context.unregisterReceiver(this);//解注册
                synchronized (this) {
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (null != usbDevice) {
                            Log.e("111", usbDevice.getDeviceName() + "已获取USB权限");
                            usbDeviceInit(usbDevice);
                        }
                    } else {
                        //user choose NO for your previously popup window asking for grant perssion for this usb device
                        Log.e("222", String.valueOf("USB权限已被拒绝，Permission denied for device" + usbDevice));
                    }
                }

            }
        }
    };

    private void usbDeviceInit(UsbDevice device) {
        int interfaceCount = device.getInterfaceCount();
        UsbInterface usbInterface = null;
        for (int i = 0; i < interfaceCount; i++) {
            usbInterface = device.getInterface(i);
            if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                break;
            }
        }
        if (usbInterface != null) {
            //获取UsbDeviceConnection
            connection = usbManager.openDevice(device);
            if (connection != null) {
                if (connection.claimInterface(usbInterface, true)) {
                    for (int j = 0; j < usbInterface.getEndpointCount(); j++) {
                        UsbEndpoint endpoint = usbInterface.getEndpoint(j);
                        if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                            if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                                inputEndPoint = endpoint;
                            } else {
                                outputEndPoint = endpoint;
                            }
                        }
                    }
                }
            }
            connection.claimInterface(usbInterface, true);
        }
    }

    public void print(Context context, byte[] bytes) {
        new Thread(() -> {
            connection.bulkTransfer(outputEndPoint, bytes, bytes.length, 5000);
        }).start();

    }
}
