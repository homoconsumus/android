package kr.co.company.database_test;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DelayRetrofit {

    @GET("delay/get")
    Call<List<Delay>> getData(@Query("number") String number);

}
