package com.zzw.guanglan.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zzw.guanglan.Contacts;
import com.zzw.guanglan.R;
import com.zzw.guanglan.base.BaseActivity;
import com.zzw.guanglan.service.SocketService;
import com.zzw.guanglan.socket.event.ConnBean;
import com.zzw.guanglan.socket.listener.STATUS;
import com.zzw.guanglan.socket.utils.MyLog;
import com.zzw.guanglan.utils.WifiAPManager;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import butterknife.BindView;

/**
 * Created by zzw on 2018/10/14.
 * 描述:
 */
public class HotConnActivity extends BaseActivity {
    @BindView(R.id.tv_hint)
    TextView tvHint;
    @BindView(R.id.tv_hint2)
    TextView tvHint2;
    @BindView(R.id.start)
    Button start;

    private WifiAPManager wifiAPManager;
    private HotBroadcastReceiver receiver;
    private final String hotName = "光缆共享wifi";
    private boolean isOpen = false;
    private boolean serviceOpen = false;

    public static void open(Context context) {
        context.startActivity(new Intent(context, HotConnActivity.class));
    }

    @Override
    protected int initLayoutId() {
        return R.layout.activity_hot_conn;
    }

    @Override
    protected void initView() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.WRITE_SETTINGS,}, 5);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // 判断是否有WRITE_SETTINGS权限if(!Settings.System.canWrite(this))
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1);
            }
        }

        super.initView();
        wifiAPManager = new WifiAPManager(this);
        receiver = new HotBroadcastReceiver();
        IntentFilter mIntentFilter = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED");
        registerReceiver(receiver, mIntentFilter);
    }


    @Override
    protected void initData() {
        super.initData();
        if (SocketService.isConn()) {
            hintS = "当前链接:" + Contacts.connKey;
            hint();
            start.setText("断开链接");
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopSocketServer();
                    initData();
                }
            });
        } else {
            start.setText("开启共享建立链接");
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isOpen) {
                        startWifiHot();
                    } else {
                        getIp();
                    }
                }
            });
        }
    }


    @Subscriber
    public void conn(ConnBean connBean) {
        if (connBean.status == STATUS.RUNNING) {
            hintS = "与" + connBean.key + "建立连接";
            initData();
        } else if (connBean.status == STATUS.END) {
            hintS = "与" + connBean.key + "断开连接";
            initData();
        } else if (connBean.status == STATUS.INIT) {
            hintS = "与" + connBean.key + "初始化接收线程";
        }
        hint();
    }


    String hintS = "";

    private class HotBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                //state状态为：10---正在关闭；11---已关闭；12---正在开启；13---已开启
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                MyLog.e("state =" + state);
                isOpen = false;
                switch (state) {
                    case 10:
                        hintS = "热点正在关闭";
                        MyLog.e("热点正在关闭");
                        break;
                    case 11:
                        hintS = "热点已关闭";
                        MyLog.e("热点已关闭");
                        break;

                    case 12:
                        hintS = "热点正在开启";
                        MyLog.e("热点正在开启");
                        break;
                    case 13:
                        //开启成功
                        hintS = "热点正在开启";
                        MyLog.e("热点正在开启");
                        getIp();
                        break;
                }
                if (!SocketService.isConn()) {
                    hint();
                }
            }
        }
    }

    private void getIp() {
        //设置个延迟 不然会拿不到
        tvHint.postDelayed(new Runnable() {
            @Override
            public void run() {
                isOpen = true;
                String serverIp = wifiAPManager.getLocalIpAddress();
                Contacts.loaclIp = serverIp;
                hintS = "共享开启成功，请先连接热点，然后socket连接。ip:" + serverIp + "端口:" + 8825;
                MyLog.e("热点已开启 ip=" + serverIp);
                if (!SocketService.isConn()) {
                    startSocketServer();
                    hint();
                }
            }
        }, 2000);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
//        wifiAPManager.closeWifiAp();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    private void startSocketServer() {
        Intent intent = new Intent(this, SocketService.class);
        startService(intent);
    }

    private void stopSocketServer() {
        Intent intent = new Intent(this, SocketService.class);
        stopService(intent);
    }

    private void startWifiHot() {
        //7.0之前
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            wifiAPManager.startWifiAp1(hotName, "1234567890", true);
            //8.0以后
        }
//        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            //7.0
//        }
        else {
            showRequestApDialogOnN_MR1();
        }
    }

    private void showRequestApDialogOnN_MR1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("android7.1系统以上不支持自动开启热点,需要手动开启热点");
        builder.setPositiveButton("去开启", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                openAP();
            }
        });
        builder.create().show();
    }

    //打开系统的便携式热点界面
    private void openAP() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        ComponentName com = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
        intent.setComponent(com);
        startActivityForResult(intent, 1000);
    }

    //判断用户是否开启热点 getWiFiAPConfig(); 这个方法去获取本机的wifi热点的信息就不贴了
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (!wifiAPManager.isWifiApEnabled()) {
                showRequestApDialogOnN_MR1();
            } else {
                hintS = "共享开启成功，正在获取ip...";
                hint();
                getIp();
            }
        }
    }


    private void hint() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvHint.setText(hintS);
            }
        });
    }
}
