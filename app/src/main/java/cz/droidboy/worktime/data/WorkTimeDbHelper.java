package cz.droidboy.worktime.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cz.droidboy.worktime.data.WorkTimeContract.WorkTimeEntry;

/**
 * @author Jonas Sevcik
 */
public class WorkTimeDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "worktime.db";
    private static final int DATABASE_VERSION = 1;

    public WorkTimeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + WorkTimeEntry.TABLE_NAME + " (" +
                WorkTimeEntry._ID + " INTEGER PRIMARY KEY," +
                WorkTimeEntry.COLUMN_START_DATE_TEXT + " TEXT NOT NULL, " +
                WorkTimeEntry.COLUMN_END_DATE_TEXT + " TEXT," +
                "UNIQUE (" + WorkTimeEntry.COLUMN_START_DATE_TEXT + ", " + WorkTimeEntry.COLUMN_END_DATE_TEXT + ") ON CONFLICT REPLACE" +
                " );";
        db.execSQL(SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + WorkTimeEntry.TABLE_NAME);
        onCreate(db);
    }

}
