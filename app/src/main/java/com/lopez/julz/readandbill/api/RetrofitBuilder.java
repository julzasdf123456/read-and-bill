package com.lopez.julz.readandbill.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitBuilder {
    public Retrofit retrofit;

    public RetrofitBuilder(String ip) {
        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL.baseUrl(ip))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public RetrofitBuilder() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL.baseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public void setRetrofit(Retrofit retrofit) {
        this.retrofit = retrofit;
    }
}
