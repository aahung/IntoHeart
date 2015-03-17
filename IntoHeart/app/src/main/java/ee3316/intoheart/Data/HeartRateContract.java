package ee3316.intoheart.Data;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by aahung on 3/9/15.
 */
public class HeartRateContract {
    private Activity activity;

    public HeartRateContract(Activity activity) {
        this.activity = activity;
        mDbHelper = new HRReaderDbHelper(activity);
    }

    public static abstract class HREntry implements BaseColumns {
        public static final String TABLE_NAME = "hr";
        public static final String COLUMN_NAME_ENTRY_ID = "timestamp";
        public static final String COLUMN_NAME_VALUE = "heart_rate";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + HREntry.TABLE_NAME + " (" +
                    HREntry.COLUMN_NAME_ENTRY_ID + INT_TYPE + " PRIMARY KEY" + COMMA_SEP +
                    HREntry.COLUMN_NAME_VALUE + INT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + HREntry.TABLE_NAME;

    public class HRReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "HRReader.db";

        public HRReaderDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private HRReaderDbHelper mDbHelper;

    public void insertHR(String hrStr) {
        int hr = Integer.valueOf(hrStr);
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        long unixTime = System.currentTimeMillis();
        values.put(HREntry.COLUMN_NAME_ENTRY_ID, unixTime);
        values.put(HREntry.COLUMN_NAME_VALUE, hr);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                HREntry.TABLE_NAME,
                null,
                values);
    }

    public List<Long[]> getHRs() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        long unixTime = System.currentTimeMillis();
        Cursor cursor =
                db.query(HREntry.TABLE_NAME, // a. table
                        new String[]{HREntry.COLUMN_NAME_ENTRY_ID, HREntry.COLUMN_NAME_VALUE}, // b. column names
                        " " + HREntry.COLUMN_NAME_ENTRY_ID + " > ?", // c. selections
                        new String[]{String.valueOf(unixTime - 1000000)}, // d. selections args
                        null, // e. group by
                        null, // f. having
                        HREntry.COLUMN_NAME_ENTRY_ID + " DESC", // g. order by
                        null); // h. limit
        List<Long[]> data = new ArrayList<>();
        while (cursor.moveToNext()) {
            data.add(new Long[]{cursor.getLong(0), cursor.getLong(1)});
        }
        return data;
    }

    public AnalysisResult getAnalysisResult() {
        List<Long[]> hrs = getHRs();
        long sum = 0;
        long sum2 = 0;
        int min = 1000;
        int max = -1;
        for (Long[] hr : hrs) {
            sum += hr[1];
            sum2 += hr[1] * hr[1];
            if (min > hr[1]) min = hr[1].intValue();
            if (max < hr[1]) max = hr[1].intValue();
        }
        double ave = (sum * 1.0 / hrs.size());
        double ave2 = (sum2 * 1.0 / hrs.size());
        AnalysisResult analysisResult = new AnalysisResult();
        analysisResult.average = ave;
        analysisResult.std_dev = Math.sqrt(ave2 - ave * ave);
        analysisResult.max = max;
        analysisResult.min = min;
        return analysisResult;
    }

    public class AnalysisResult {
        public double average;
        public double std_dev;
        public int max;
        public int min;
    }
}
