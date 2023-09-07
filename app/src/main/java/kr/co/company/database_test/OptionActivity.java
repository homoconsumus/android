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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    TextView route_text;
    String routeInfo = "";
    Calendar calendar;
    String db_name = "date_route_db";
    SQLiteDatabase db;
    private String[] subwayList = {"1호선","2호선","3호선","4호선","5호선","6호선","7호선","8호선","9호선","분당선","경의중앙선","경춘선","신분당선"};
    private TextView subwayListTextView;
    private AlertDialog subwaytSelectDialog;

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

        // 텍스트뷰로 지하철 호선을 클릭하면 입력되게 함
        subwayListTextView = (TextView) findViewById(R.id.insert_info);
        subwayListTextView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                subwaytSelectDialog.show();
            }
        });

        subwaytSelectDialog = new AlertDialog.Builder(OptionActivity.this)
                .setItems(subwayList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        subwayListTextView.setText(subwayList[i]);
                    }
                })
                .setTitle("호선을 선택하세요")
//                .setPositiveButton("확인", null)
                .setNegativeButton("취소", null)
                .create();


        // Intent에서 card_title 정보 추출
        String cardTitle = getIntent().getStringExtra("card_title");
        Log.d(TAG, "Card Title: " + cardTitle);

        // Intent에서 가저온 card_title에 맞춰 날짜 선택
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try{
            if(cardTitle != null){
                dateInfo_text = cardTitle;
                Date date = dateFormat.parse(cardTitle);
                calendar.setTime(date);
                calendarView.setDate(calendar.getTimeInMillis()); // 날짜 설정
            }
        }catch (ParseException e){

        }

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

        // route_text 생성
        route_text = findViewById(R.id.insert_info);
    }

    // 데이터베이스에 날짜, 호선 정보 입력
    public void insert_data(View v){
        System.out.println(dateInfo_text);

        routeInfo = route_text.getText().toString();

        // 중복 여부를 확인할 쿼리
        Cursor cursor = db.rawQuery("SELECT * FROM " + db_name + " WHERE date = '" +
                dateInfo_text + "' AND route = '" + routeInfo + "';", null);

        // 중복 여부 확인
        if (routeInfo.equals("이곳을 클릭하세요")) { // 아무것도 입력되지 않았을 때
            showNothingDataDialog();
        } else if (cursor.getCount() > 0) { // 중복된 데이터가 있는 경우 다이얼로그 창을 띄움
            showDuplicateDataDialog();
        } else {
            // 중복된 데이터가 없는 경우 데이터베이스에 등록
            db.execSQL("INSERT INTO " + db_name + "(date, route) VALUES ('" +
                    dateInfo_text + "', '" + routeInfo + "');");
            update_card_list();
        }

        cursor.close();
    }

    // 중복된 데이터가 있을 때 띄울 다이얼로그 창
    private void showDuplicateDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("중복된 데이터");
        builder.setMessage("이미 같은 날짜에 같은 호선 정보가 등록되어 있습니다.");
        builder.setPositiveButton("확인", null);
        builder.show();
    }
    // 아무것도 선택하지 않았을 땐 입력이 되지 않게 하는 창
    private void showNothingDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("아무것도 선택되지 않음");
        builder.setMessage("아무것도 선택하지 않으셨습니다.");
        builder.setPositiveButton("확인", null);
        builder.show();
    }


    // 기간별로 데이터를 등록
// 기간별로 데이터를 등록하기 위한 다이얼로그 창
    public void duration_option(View view) {
        routeInfo = route_text.getText().toString();

        // 기간 선택을 위한 다이얼로그 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("기간 선택");
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_duration, null);
        // 호선 정보 가져오기
        TextView dialog_route = dialogView.findViewById(R.id.dialog_route);
        dialog_route.setText(routeInfo);
        // 생성
        builder.setView(dialogView);

        // 체크박스들 가져오기
        CheckBox checkBoxEveryday = dialogView.findViewById(R.id.checkbox_everyday);
        CheckBox checkBoxMonday = dialogView.findViewById(R.id.checkbox_monday);
        CheckBox checkBoxTuesday = dialogView.findViewById(R.id.checkbox_tuesday);
        CheckBox checkBoxWednesday = dialogView.findViewById(R.id.checkbox_wednesday);
        CheckBox checkBoxThursday = dialogView.findViewById(R.id.checkbox_thursday);
        CheckBox checkBoxFriday = dialogView.findViewById(R.id.checkbox_friday);
        CheckBox checkBoxSaturday = dialogView.findViewById(R.id.checkbox_saturday);
        CheckBox checkBoxSunday = dialogView.findViewById(R.id.checkbox_sunday);

        // 다이얼로그에서 확인 버튼을 누를 경우
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 여기에서 체크된 체크박스들의 정보를 이용하여 데이터베이스에 등록하는 로직을 추가
                // 이후에 데이터베이스에 등록된 내용을 리스트에 업데이트하고 화면에 보여주는 코드를 작성
                update_card_list();
            }
        });

        // 다이얼로그에서 취소 버튼을 누를 경우
        builder.setNegativeButton("취소", null);

        // 다이얼로그 표시
        builder.show();
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