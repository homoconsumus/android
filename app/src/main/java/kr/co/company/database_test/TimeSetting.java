package kr.co.company.database_test;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class TimeSetting extends AppCompatActivity {

    private TimePicker timePicker;
    private AlarmManager alarmManager;
    private int hour, minute;
    private int REQUEST_CODE = 0;

    private String db_name = "date_route_db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_setting);

        timePicker=findViewById(R.id.time);

    }

    public void set(View view){

        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pIntent = PendingIntent
                .getBroadcast(this, REQUEST_CODE,intent,PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        hour = timePicker.getHour();
        minute = timePicker.getMinute();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 이미 지난 시간인 경우 24시간 후로 알람이 울리도록 설정
        Calendar now = Calendar.getInstance();
        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 알람 설정
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        Toast.makeText(getApplicationContext(), "알람이 설정되었습니다.",
                Toast.LENGTH_SHORT).show();

        // 결과를 DB에 저장하기
        savaTimeAtDB(hour, minute);

        // 메인 액티비티로 결과 보내기
        Intent returnData = new Intent(this, MainActivity.class);
        String hourZero = (hour>=10) ? "" : "0";
        String minZero = (minute>=10) ? "" : "0";
        returnData.putExtra("hour", hourZero+hour);
        returnData.putExtra("min", minZero+minute);
        setResult(RESULT_OK, returnData);
        finish(); // 메인 페이지로 이동
    }

    public void cancle(View view) {

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(this,
                REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        // 빌더 객체 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 빌더의 메소드로 대화상자 설정
        builder.setMessage("취소하시면 알림이 가지 않습니다.\n정말로 취소하시겠습니까?");

        // 취소 버튼 생성
        builder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i){
                    }
                });

        // 확인 버튼 생성
        builder.setPositiveButton("Yes",
                // 무명 객체로 이벤트 리스너 인터페이스 객체 생성
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (alarmManager == null) {
                            alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                        }
                        alarmManager.cancel(pIntent);
                        Toast.makeText(getApplicationContext(), "알람이 취소되었습니다.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        // AlertDialog 객체 생성
        AlertDialog alertDialog = builder.create();
        // Alertdialog 객체 출력
        alertDialog.show();
    }

    // 결과를 DB에 저장하는 메소드
    public void savaTimeAtDB(int hour, int min) {
        try{
            dbHelper dbHelper = new dbHelper(this, db_name, null, 1);
            SQLiteDatabase db;
            db = dbHelper.getWritableDatabase();

            // 기존 데이터 삭제
            String query = "DELETE FROM alarm_time;";
            db.execSQL(query);

            // 새로운 데이터 추가
            query = "INSERT INTO alarm_time(hour, minute) VALUES(" + hour + ", " + min + ");";

            db.execSQL(query);
            dbHelper.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
