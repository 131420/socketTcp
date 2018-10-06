package com.zzw.socketdemo.ui.qianxin;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zzw.socketdemo.R;
import com.zzw.socketdemo.base.BaseActivity;
import com.zzw.socketdemo.bean.ListDataBean;
import com.zzw.socketdemo.bean.QianXinItemBean;
import com.zzw.socketdemo.http.Api;
import com.zzw.socketdemo.http.retrofit.RetrofitHttpEngine;
import com.zzw.socketdemo.rx.ErrorObserver;
import com.zzw.socketdemo.rx.LifeObservableTransformer;
import com.zzw.socketdemo.rx.ResultRevFunction;
import com.zzw.socketdemo.utils.RequestBodyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;

public class QianXinListActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener {
    @BindView(R.id.recy)
    RecyclerView recy;

    private QianXinListAdapter adapter;

    public static void open(Context context) {
        context.startActivity(new Intent(context, QianXinListActivity.class));
    }

    @Override
    protected int initLayoutId() {
        return R.layout.activity_qian_xin_list;
    }

    @Override
    protected void initData() {
        super.initData();
        recy.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QianXinListAdapter(new ArrayList<QianXinItemBean>());
        adapter.setOnItemClickListener(this);
        recy.setAdapter(adapter);

        getData();
    }

    void getData() {
        RetrofitHttpEngine.obtainRetrofitService(Api.class)
                .getAppListByPage(RequestBodyUtils.generateRequestBody(new HashMap<String, String>() {
                    {
                        put("model.cblOpName", "1");
                        put("model.cblOpCode", "2");
                    }
                }))
                .compose(LifeObservableTransformer.<ListDataBean<QianXinItemBean>>create(this))
                .subscribe(new ErrorObserver<ListDataBean<QianXinItemBean>>(this) {
                    @Override
                    public void onNext(ListDataBean<QianXinItemBean> qianXinItemBeans) {
                        if (qianXinItemBeans.getList() != null && qianXinItemBeans.getList().size() > 0)
                            setData(qianXinItemBeans.getList());
                    }
                });
    }

    void setData(List<QianXinItemBean> datas) {
        adapter.replaceData(datas);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

    }
}
