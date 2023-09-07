package kr.co.company.database_test;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://192.168.24.171:8000/";
//    private static final String BASE_URL = "http://10.0.2.2:8000/";

    private DelayRetrofit delayRetrofit;

    // 채널 아이디
    String CHANNEL_ID = "my_channel_id";

    // 생성자
    public RetrofitClient(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        delayRetrofit = retrofit.create(DelayRetrofit.class);
    }

    public void run(String number, Context context){

        // API에 데이터 요청
        Call<List<Delay>> call = delayRetrofit.getData(number);

        call.enqueue(new Callback<List<Delay>>() {
            // 통신 성공
            @Override
            public void onResponse(Call<List<Delay>> call, Response<List<Delay>> response) {
                if (response.isSuccessful()) {
                    List<Delay> responseData = response.body();
                    // 서버 응답을 처리

                    for (Delay delay : responseData) {
                        showNoti(context, delay.getNumber(), delay.getTitle(), delay.getLink(), delay.getId());
                    }

                } else {
                    // 서버 요청이 실패한 경우 처리합니다.
                    Log.d("api", "실패");
                }
            }
            // 통신 실패
            @Override
            public void onFailure(Call<List<Delay>> call, Throwable t) {
                Log.d("api", "시스템 원인으로 실패\n" +  t.getMessage());
            }
        });
    }

    // 알림 보내는 메소드
    public void showNoti(Context context, String number, String title, String url, int id){
        // 버전이 8.0 이상인지 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 채널 객체 생성
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID, "My Notification", NotificationManager.IMPORTANCE_DEFAULT);

            // 채널 설명 설정
            notificationChannel.setDescription("Channel description");

            // 알림 매니저 객체 생성
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // 채널 생성
            notificationManager.createNotificationChannel(notificationChannel);

            // 빌더 객체 생성
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

            // 알림 설정
            builder.setSmallIcon(R.drawable.smallicon);
            builder.setContentTitle(number + "에서 지연이 발생했습니다!");
            builder.setContentText(title);
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

            // 액션 등록
            Intent intent2 = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    intent2, PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(pendingIntent);


            // 시스템에 알림 전달
            notificationManager.notify(id, builder.build());
        }
    }
}
