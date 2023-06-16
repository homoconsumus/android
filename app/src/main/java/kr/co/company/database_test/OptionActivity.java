package kr.co.company.database_test;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.http.Tag;

public class OptionActivity extends AppCompatActivity {
    static String dateInfo_text = "";
    ArrayList<String> route_list = new ArrayList<>();
    private CardList card_adapter;
    ListView container;
    Date dateInfo;
    String routeInfo = "";
    Calendar calendar;
    String db_name = "date_route_db";
    SQLiteDatabase db;

    // DB생성 또는 열기
    public void create_db(Context context){
        try {
            db = openOrCreateDatabase(db_name, MainActivity.MODE_PRIVATE, null);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        CalendarView calendarView = (CalendarView) findViewById(R.id.calendar);
        calendar = Calendar.getInstance();
        dateInfo = new Date(calendarView.getDate());
        dateInfo_text = Integer.toString(calendar.get(Calendar.YEAR))+ "-"
                + Integer.toString(calendar.get(Calendar.MONTH)+1) + "-"
                +Integer.toString(calendar.get(Calendar.DATE));
        System.out.println(dateInfo_text);

        create_db(this);

        // Intent에서 card_title 정보 추출
        String cardTitle = getIntent().getStringExtra("card_title");
        Log.d(TAG, "Card Title: " + cardTitle);

        // 데이터베이스에서 해당 날짜에 기록된 이용 호선 정보 가져오기
        // 카드 생성
        container = (ListView) findViewById(R.id.route_container);
        card_adapter = new CardList(OptionActivity.this, route_list);
        container.setAdapter(card_adapter);
        update_card_list();
        if (route_list.size()>0){
            container.setPadding(20,20,20,20);
        }else {
            container.setPadding(0,0,0,0);
        }

        // 캘린더 뷰에서 년도, 달, 날짜 정보 가져오기
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView,
                                            int year, int month, int dayOfMonth) {
                dateInfo_text = year + "-" + (month+1) + "-" + dayOfMonth;
                System.out.println(dateInfo_text);

                // route_container 초기화, card생성
                update_card_list();
                if (route_list.size()>0){
                    container.setPadding(10,10,10,10);
                }else {
                    container.setPadding(0,0,0,0);
                }
            }
        });
    }

    // 데이터베이스에 날짜, 호선 정보 입력
    public void insert_data(View v){
        System.out.println(dateInfo_text);

        EditText route_edit = (EditText) findViewById(R.id.insert_info);
        routeInfo = route_edit.getText().toString();

        db.execSQL("INSERT INTO " + db_name + "(date, route) VALUES ('" +
                dateInfo_text + "', '" + routeInfo + "');");
        update_card_list();
    }

    // 데이터베이스에 등록돼있는 정보 삭제
    private void deleteCard(int position) {
        // 선택한 카드를 데이터베이스에서 삭제
        String selectedRoute = route_list.get(position);
        db.execSQL("DELETE FROM " + db_name + " WHERE date = '" + dateInfo_text + "' AND route = '" + selectedRoute + "';");
        route_list.remove(position);
        card_adapter.notifyDataSetChanged();

        if (route_list.size() > 0) {
            container.setPadding(10, 10, 10, 10);
        } else {
            container.setPadding(0, 0, 0, 0);
        }
    }

    // 데이터베이스에 등록돼있는 정보 가져오기
    public ArrayList<String> get_schedule_info(){
        ArrayList<String> route_list = new ArrayList<String>();
        Cursor cursor = null;
        try{
            cursor = db.rawQuery("SELECT date, route FROM " + db_name + " WHERE date = '"
                    + dateInfo_text + "';", null);
            if (cursor != null && cursor.moveToFirst()){
                do {
                    route_list.add(cursor.getString(1));
                }while (cursor.moveToNext());
            }
        }catch (SQLiteException e){
            e.printStackTrace();
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return route_list;
    }

    private void update_card_list(){
        route_list.clear();
        route_list = get_schedule_info();
        System.out.println(route_list);
        card_adapter.notifyDataSetChanged();

        // route_container의 롱 클릭 리스너 등록
        setCardLongClickListener();
    }

    private void setCardLongClickListener() {
        container.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                // 롱 클릭된 카드 삭제 여부를 묻는 다이얼로그 표시
                AlertDialog.Builder builder = new AlertDialog.Builder(OptionActivity.this);
                builder.setTitle("카드 삭제");
                builder.setMessage("선택한 카드를 삭제하시겠습니까?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 카드 삭제
                        deleteCard(position);
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();
                return true;
            }
        });
    }

    public class CardList extends ArrayAdapter<String> {
        // 데이터베이스에서 해당 날짜에 기록된 이용 호선 정보 가져오기
        private  LayoutInflater inflater;
        Activity context;

        public CardList(Activity context, List<String> dataList){
            super(context, 0, dataList);
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount(){
            if(route_list==null) return 0;
            return route_list.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, ViewGroup parent){
            if(convertView == null){
                convertView = inflater.inflate(R.layout.route_card, parent, false);
            }
            if (route_list.size() > position) {
                String item = route_list.get(position);
                TextView card_text = convertView.findViewById(R.id.card_text);
                card_text.setText(item);
            }
            return  convertView;
        }
    }
}