package kr.co.company.database_test;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class dbHelper extends SQLiteOpenHelper {

    public dbHelper(Context context, String DATABASE_NAME, SQLiteDatabase.CursorFactory factory, int DATABASE_VERSION){
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS date_route_db (" +
                "idx INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT, route TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS alarm_time (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "hour INTEGER," +
                "minute INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS date_route_db");
        onCreate(db);
    }
}
