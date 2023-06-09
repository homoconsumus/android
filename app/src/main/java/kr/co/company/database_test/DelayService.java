package kr.co.company.database_test;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DelayService extends Service {
    private static final String TAG = "DelayService";
    private static final String CHANNEL_ID = "DelayServiceChannel";
    public List<String> route_list = new ArrayList<>();
    private Timer timer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        // 주기적 작업 수행
        performDelayedRequest();

        // 서비스를 포그라운드 서비스로 실행
        createNotificationChannel();
        showNotification("Delay Service", "Service is running");

        // 작업 스케줄링
        scheduleDelayedRequest();

        // START_STICKY를 반환해 서비스를 강제종료후 재시작
        return START_STICKY;
    }

    // 주기적 작업을 수행하는 메소드
    private void performDelayedRequest(){
        // Retrofit으로 서버 정보를 요청하는 코드
        DelayServiceApi api = RetrofitClient.getInstance().getApiService();
        Call<List<String>> call = api.sendRouteList(route_list);

        // 테스트 notification
        showNotification("Test Noti", "Service is running");

        call.enqueue(new Callback<List<String>>() {
            // 통신 성공
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful()) {
                    List<String> responseData = response.body();
                    // 서버 응답을 처리합니다.
                } else {
                    // 서버 요청이 실패한 경우 처리합니다.
                }
            }

            // 통신 실패
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {

            }
        });
    }

    // 작업 스케줄링
    private void scheduleDelayedRequest(){
        // Calendar객체로 현재 시간정보를 가져옴
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.MINUTE, 1);
        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();

        // 10분마다 performDelayedRequest실행
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                performDelayedRequest();
            }
        }, delay, 1 * 60 * 1000);
    }

    // Noti채널 생성
    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "DelayServiceChannel";
            String description = "Channel for Delay Service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Noti 생성
    private Notification createNotification(String title, String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.smallicon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }

    // 알림 표시
    private void showNotification(String title, String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.smallicon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2, builder.build());
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
}
