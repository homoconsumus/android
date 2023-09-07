package kr.co.company.database_test;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlarmReceiver extends BroadcastReceiver {

    // DB 접속
    String db_name = "date_route_db";
    SQLiteDatabase db;

    @Override
    public void onReceive(Context context, Intent intent) {

        // 오늘의 노선 정보 받아오기
        ArrayList<String> route_list = getData(context);
        RetrofitClient retrofitClient = new RetrofitClient();

        for (String number : route_list) {
            retrofitClient.run(number, context);
        }


//        for (Delay delay : responseData) {
//            showNoti(context, delay.getNumber(), delay.getTitle(), delay.getLink(), delay.getId());
//        }

//        showNoti(context, "5호선", "타이틀", "url이지롱", 1);
    }
//    public void requestToApi(Context context) {
//       RetrofitClient retrofitClient = new RetrofitClient();
//       List<Delay> responseData = retrofitClient.run("4호선");
//    }

    // DB 정보 받아오기
    public ArrayList<String> getData(Context context) {
        ArrayList<String> route_list = new ArrayList<String>();
        try {
            dbHelper dbHelper = new dbHelper(context, db_name, null, 1);
            db = dbHelper.getReadableDatabase();
            String today = getToday();

            String query = "SELECT route FROM "+db_name+" WHERE date = '"+today+"'";
            Cursor cursor = db.rawQuery(query, null);

            while(cursor.moveToNext()){
                String route = cursor.getString(0);
                Log.d("number", route);
                route_list.add(route);
            }
            cursor.close();
            db.close();

        }catch(Exception e){
            e.printStackTrace();
        }
        finally {
            return route_list;
        }
    }

    // 오늘날짜 문자열로 반환
    public String getToday(){
        String today = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime now = LocalDateTime.now();
            int year = now.getYear();
            int month = now.getMonthValue();
            int day = now.getDayOfMonth();

            today += (year + "-" + month + "-" + day);
        }
        Log.d("Today", today);
        return today;
    }
}
