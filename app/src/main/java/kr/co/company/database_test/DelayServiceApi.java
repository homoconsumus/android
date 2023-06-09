package kr.co.company.database_test;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DelayServiceApi {
    @GET("delay/get")
    Call<List<String>> getData(@Query("number") String number);

    @POST("delay/get")
    Call<List<String>> sendRouteList(@Body List<String> routeList);
}
