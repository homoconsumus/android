package kr.co.company.database_test;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    dbHelper helper;
    SQLiteDatabase db;
    String db_name = "date_route_db";
    ListView container;
    private ScheduleList schedule_adapter;
    List<List<String>> route_list = new ArrayList<>();

    // 추가된 멤버변수
    // 알림 시간 표시
    TextView alarmTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = new dbHelper(this, db_name, null, 1);
        try{
            db = helper.getWritableDatabase();
        }catch (SQLiteException ex){
            db = helper.getReadableDatabase();
        }

        Button addBtn = findViewById(R.id.move_to_option);
        // 추가 코드
        /// 텍스트뷰 연결
        alarmTime = (TextView) findViewById(R.id.alarm_time);

        // OptionActivity로 이동하는 버튼 리스너
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), OptionActivity.class);
                startActivity(intent);
            }
        });

        // 데이터베이스에서 해당 날짜에 기록된 이용 호선 정보 가져오기
        List<List<String>> route_list = new ArrayList<>();
        route_list = get_schedule_info();
        System.out.println("db에 저장된 정보들 : " + route_list);

        // route_container에 카드 생성
        container = (ListView) findViewById(R.id.schedule_container);
        schedule_adapter = new ScheduleList(route_list);
        container.setAdapter(schedule_adapter);
        update_card_list();

        // 추가 코드
        // DB로 부터 알람 시간을 받아와서 화면에 표시
        setAlarmTime();
    }

    // main페이지로 다시 돌아왔을 때 scheduleList를 update
    @Override
    protected void onResume(){
        super.onResume();
        update_card_list();

        // 추가
        // 시간 지난 일정 삭제 메소드 호출
        deleteOldSchedule();
    }

    public List<List<String>> get_schedule_info() {
        Cursor cursor = db.rawQuery("SELECT date, route FROM " + db_name + " ORDER BY date;", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String previousDate = "";
                List<String> currentList = null;

                do {
                    String currentDate = cursor.getString(0);
                    String currentRoute = cursor.getString(1);

                    if (!currentDate.equals(previousDate)) {
                        // 중복되지 않은 경우에는 새로운 ArrayList 생성
                        currentList = new ArrayList<>();
                        currentList.add(currentDate);
                        route_list.add(currentList);
                    }

                    // ArrayList에 route 추가
                    currentList.add(currentRoute);

                    previousDate = currentDate;
                } while (cursor.moveToNext());
            }
        }
        return route_list;
    }

    // ListView의 항목을 갱신
    private void update_card_list(){
        route_list.clear();
        route_list = get_schedule_info();
        System.out.println("현재 route_list: " + route_list);
        schedule_adapter.notifyDataSetChanged();

        // route_container의 롱 클릭 리스너 등록
        setCardClickListener();
    }

    // schedule_card를 클릭 시 카드의 날짜 정보를 OptionActivity에게 전달하는 메서드
    private void setCardClickListener() {
        container.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (route_list.size() > position) {
                    List<String> item = route_list.get(position);
                    String cardTitle = item.get(0);

                    Intent intent = new Intent(MainActivity.this, OptionActivity.class);
                    intent.putExtra("card_title", cardTitle);
                    startActivity(intent);
                }
            }
        });
    }

    // Listview Adapter 클래스
    public class ScheduleList extends BaseAdapter {
        // 데이터베이스에서 해당 날짜에 기록된 이용 호선 정보 가져오기
        private List<List<String>> dataList;
        private  LayoutInflater inflater;

        public ScheduleList(List<List<String>> dataList){
            this.dataList = dataList;
            inflater = LayoutInflater.from(MainActivity.this);
        }

        @Override
        public int getCount(){
            if(route_list==null) return 0;
            return route_list.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, ViewGroup parent){
            if(convertView == null){
                convertView = inflater.inflate(R.layout.schedule_card, parent, false);
            }
            if (route_list.size() > position) {
                List<String> item = route_list.get(position);
                TextView card_title = convertView.findViewById(R.id.card_title);
                TextView card_text = convertView.findViewById(R.id.card_text);
                card_title.setText(item.get(0));
                StringBuilder cardTextBuilder = new StringBuilder();
                for (int i = 1; i < item.size(); i++) {
                    cardTextBuilder.append(item.get(i)).append("\n");
                }
                card_text.setText(cardTextBuilder.toString().trim());
            }
            return  convertView;
        }
    }


    // 추가된 메소드
    // 알림 시간 설정 페이지로 이동
    public void setTime(View view) {
        Intent intent = new Intent(this, TimeSetting.class);
        startActivityForResult(intent, 1);
    }

    // 추가된 메소드
    // 알림 시간 설정 페이지로 부터 설정된 시간을 받아서 화면에 표시
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // 요청 코드가 일치하고 결과 코드가 성공이면 전달받은 시간을 화면에 표시
                alarmTime.setText("알림 시간\t\t\t" + data.getStringExtra("hour") + ":" + data.getStringExtra("min"));
            }
        }
    }
    // 추가된 메소드
    // DB에 저장된 알람시간 받아오기
    public int[] getAlarmTime() {
        int[] alarmTimeDB = {0, 0};
        try {

            String query = "SELECT hour, minute FROM alarm_time";
            Cursor cursor = db.rawQuery(query, null);
            while(cursor.moveToNext()){
                alarmTimeDB[0] = cursor.getInt(0);
                alarmTimeDB[1] = cursor.getInt(1);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        finally {
            return alarmTimeDB;
        }
    }
    // 받아온 알림 시간 표시하기
    public void setAlarmTime() {
        int[] alarmTimeDB = getAlarmTime();

        String hourStr = (alarmTimeDB[0]>=10) ? "" : "0";
        String minStr = (alarmTimeDB[1]>=10) ? "" : "0";
        hourStr += (alarmTimeDB[0] + "");
        minStr += (alarmTimeDB[1] + "");
        alarmTime.setText("알림 시간\t\t\t" +hourStr + ":" + minStr);
    }

    // 날짜 지난 일정 지우는 메소드
    private void deleteOldSchedule() {

        Cursor cursor = db.rawQuery("SELECT idx, date FROM " + db_name + ";", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {

                LocalDate today = LocalDate.now(); // 오늘 날짜
                List<Integer> deletedDateList = new ArrayList<>();; // 삭제할 날짜 리스트

                do {
                    // 조회된 데이터 확인
                    String date = cursor.getString(1);
                    int idx = cursor.getInt(0);
                    // 문자열을 Datetime으로 변환
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
                    LocalDate dateTime = LocalDate.parse(date, formatter);

                    // 오늘보다 지났으면
                    if (today.isAfter(dateTime)) {
                        // 리스트에 추가
                        deletedDateList.add(idx);
                    }

                } while (cursor.moveToNext());

                // 리스트에 있는 데이터들 DB에서 지우기
                for (int idx : deletedDateList) {
                    String query = "DELETE FROM " + db_name + " WHERE idx = " + idx;
                    db.execSQL(query);
                }
            }
        }
    }
}