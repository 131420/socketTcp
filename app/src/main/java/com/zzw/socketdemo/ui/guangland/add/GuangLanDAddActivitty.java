package com.zzw.socketdemo.ui.guangland.add;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.zzw.socketdemo.R;
import com.zzw.socketdemo.base.BaseActivity;
import com.zzw.socketdemo.bean.AreaBean;
import com.zzw.socketdemo.dialogs.area.AreaDialog;
import com.zzw.socketdemo.dialogs.multilevel.OnConfirmCallback;
import com.zzw.socketdemo.http.Api;
import com.zzw.socketdemo.http.retrofit.RetrofitHttpEngine;
import com.zzw.socketdemo.manager.UserManager;
import com.zzw.socketdemo.rx.ErrorObserver;
import com.zzw.socketdemo.rx.LifeObservableTransformer;
import com.zzw.socketdemo.rx.ResultBooleanFunction;
import com.zzw.socketdemo.utils.RequestBodyUtils;
import com.zzw.socketdemo.utils.ToastUtils;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zzw on 2018/10/4.
 * 描述:
 */
public class GuangLanDAddActivitty extends BaseActivity {
    @BindView(R.id.cabel_op_name)
    EditText cabelOpName;
    @BindView(R.id.cabel_op_code)
    EditText cabelOpCode;
    @BindView(R.id.area_id)
    TextView areaId;
    @BindView(R.id.stat_id)
    TextView statId;
    @BindView(R.id.room_id)
    TextView roomId;
    @BindView(R.id.capaticy)
    EditText capaticy;
    @BindView(R.id.op_long)
    EditText opLong;
    @BindView(R.id.org_id)
    TextView orgId;
    @BindView(R.id.org_user_name)
    EditText orgUserName;
    @BindView(R.id.op_start_time)
    TextView opStartTime;
    @BindView(R.id.last_time)
    TextView lastTime;
    @BindView(R.id.pa_cable_id)
    EditText paCableId;
    @BindView(R.id.remark)
    EditText remark;
    @BindView(R.id.state)
    TextView state;


    public static void open(Context context) {
        context.startActivity(new Intent(context, GuangLanDAddActivitty.class));
    }

    @Override
    protected int initLayoutId() {
        return R.layout.activity_guang_lan_d_add;
    }

    public void submit() {
        final String cabelOpNameS = cabelOpName.getText().toString().trim();
        final String cabelOpCodeS = cabelOpCode.getText().toString().trim();
        final String areaIdS = areaId.getText().toString().trim();
        final String statIdS = statId.getText().toString().trim();
        final String roomIdS = roomId.getText().toString().trim();
        final String capaticyS = capaticy.getText().toString().trim();
        final String opLongS = opLong.getText().toString().trim();
        final String orgIdS = orgId.getText().toString().trim();
        final String orgUserNameS = orgUserName.getText().toString().trim();
        final String opStartTimeS = opStartTime.getText().toString().trim();
        final String lastTimeS = lastTime.getText().toString().trim();
        final String paCableIdS = paCableId.getText().toString().trim();
        final String remarkS = remark.getText().toString().trim();
        final String stateS = state.getText().toString().trim();

        RetrofitHttpEngine.obtainRetrofitService(Api.class)
                .duanAppAdd(RequestBodyUtils.generateRequestBody(new HashMap<String, String>() {
                    {
                        put("userId", UserManager.getInstance().getUserId());
                        put("cabelOpName", cabelOpNameS);
                        put("cabelOpCode", cabelOpCodeS);
                        put("areaId", areaIdS);
                        put("statId", statIdS);
                        put("roomId", roomIdS);
                        put("capaticy", capaticyS);
                        put("oplong", opLongS);
                        put("orgId", orgIdS);
                        put("orgUserName", orgUserNameS);
                        put("opStartTime", opStartTimeS);
                        put("lastTime", lastTimeS);
                        put("paCableId", paCableIdS);
                        put("remark", remarkS);
                        put("state", stateS);
                    }
                }))
                .map(ResultBooleanFunction.create())
                .compose(LifeObservableTransformer.<Boolean>create(this))
                .subscribe(new ErrorObserver<Boolean>(this) {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            ToastUtils.showToast("新增成功");
                            finish();
                        }
                    }
                });
    }


    private void area(){
        AreaDialog.createCityDialog(this, "选择地区", new OnConfirmCallback<AreaBean>() {
            @Override
            public void onConfirm(List<AreaBean> selectedEntities) {
                if(selectedEntities.size()>0){
                    AreaBean bean = selectedEntities.get(selectedEntities.size()-1);
                }
            }
        }).show(getSupportFragmentManager(),"area");
    }

    @OnClick({R.id.add,R.id.area_id, R.id.stat_id, R.id.room_id, R.id.org_id, R.id.op_start_time, R.id.last_time})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.area_id:
                area();
                break;
            case R.id.stat_id:
                break;
            case R.id.room_id:
                break;
            case R.id.org_id:
                break;
            case R.id.op_start_time:
                break;
            case R.id.last_time:
                break;
            case R.id.add:
                 submit();
                break;
        }
    }
}
