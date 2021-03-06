package com.zzw.guanglan.http.retrofit;

import android.text.TextUtils;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by zzw on 2017/5/30.
 * Version:
 * Des: Retrofit网络引擎
 */

public class RetrofitHttpEngine {

    private static Retrofit mRetrofit;
    private static Map<String, Object> mRetrofitServiceCache = new LinkedHashMap<>();

    private static final int TOME_OUT = 20; //s  秒
    private HttpUrl mBaseUrl;
    private Interceptor[] mInterceptors;
    private GlobeHttpHandler mHandler;


    public RetrofitHttpEngine(Builder builder) {
        this.mBaseUrl = builder.baseUrl;
        this.mHandler = builder.handler;
        this.mInterceptors = builder.interceptors;
        mRetrofit = configureRetrofit();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 得到对应的Service请求类
     *
     * @param service
     * @param <T>
     * @return
     */
    public static <T> T obtainRetrofitService(Class<T> service) {
        if (!mRetrofitServiceCache.containsKey(service.getName()))
            mRetrofitServiceCache.put(service.getName(), mRetrofit.create(service));

        return (T) mRetrofitServiceCache.get(service.getName());
    }

    /**
     * 配置Retrofit
     *
     * @return
     */
    private Retrofit configureRetrofit() {
        Retrofit.Builder builder = new Retrofit.Builder();
        return builder
                .baseUrl(mBaseUrl)//域名
                .client(configureClient())//设置okhttp
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//使用rxjava
                .addConverterFactory(GsonConverterFactory.create())//使用Gson
                .build();
    }

    /**
     * 配置OkHttpClient
     *
     * @return
     */
    private OkHttpClient configureClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(TOME_OUT, TimeUnit.SECONDS)
                .readTimeout(TOME_OUT, TimeUnit.SECONDS);
//                .addNetworkInterceptor(new RequestIntercept(mHandler));

        if (mInterceptors != null && mInterceptors.length > 0) {//如果外部提供了interceptor的数组则遍历添加
            for (Interceptor interceptor : mInterceptors) {
                builder.addInterceptor(interceptor);
            }
        }
        return builder.build();
    }


    public static final class Builder {
        private HttpUrl baseUrl;
        private GlobeHttpHandler handler;
        private Interceptor[] interceptors;

        private Builder() {
        }

        public Builder baseUrl(String baseUrl) {//基础url
            if (TextUtils.isEmpty(baseUrl)) {
                throw new IllegalArgumentException("baseurl can not be empty");
            }
            this.baseUrl = HttpUrl.parse(baseUrl);
            return this;
        }

        public Builder globeHttpHandler(GlobeHttpHandler handler) {//用来处理http响应结果
            this.handler = handler;
            return this;
        }

        public Builder interceptors(Interceptor[] interceptors) {//动态添加任意个interceptor
            this.interceptors = interceptors;
            return this;
        }

        public RetrofitHttpEngine build() {
            if (baseUrl == null) {
                throw new IllegalStateException("baseurl is required");
            }
            return new RetrofitHttpEngine(this);
        }

    }

}
