package kr.co.company.database_test;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://192.168.24.171:8080/";

    private static RetrofitClient instance;
    private DelayServiceApi delayServiceApi;

    private RetrofitClient(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        delayServiceApi = retrofit.create(DelayServiceApi.class);
    }

    public static synchronized RetrofitClient getInstance(){
        if(instance == null){
            instance = new RetrofitClient();
        }
        return instance;
    }

    public DelayServiceApi getApiService(){
        return delayServiceApi;
    }
}
