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
    }

    private static final String SQL_CREATE_ENTRIES_DAY =
            "CREATE TABLE day (" +
                    "timestamp TEXT PRIMARY KEY, " +
                    "hr INTEGER" +
                    "max INTEGER" +
                    "min INTEGER" +
                    "sd INTEGER" +
                    " )";

    private static final String SQL_CREATE_ENTRIES_WEEK =
            "CREATE TABLE week (" +
                    "timestamp TEXT PRIMARY KEY, " +
                    "hr INTEGER" +
                    "max INTEGER" +
                    "min INTEGER" +
                    "sd INTEGER" +
                    " )";

    private static final String SQL_CREATE_ENTRIES_MONTH =
            "CREATE TABLE month (" +
                    "timestamp TEXT PRIMARY KEY, " +
                    "hr INTEGER" +
                    "max INTEGER" +
                    "min INTEGER" +
                    "sd INTEGER" +
                    " )";

    private static final String SQL_DELETE_ENTRIES_DAY =
            "DROP TABLE IF EXISTS day";
    private static final String SQL_DELETE_ENTRIES_WEEK =
            "DROP TABLE IF EXISTS week";
    private static final String SQL_DELETE_ENTRIES_MONTH =
            "DROP TABLE IF EXISTS month";

    public class HRReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 2;
        public static final String DATABASE_NAME = "HRReader.db";

        public HRReaderDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES_DAY);
            db.execSQL(SQL_CREATE_ENTRIES_WEEK);
            db.execSQL(SQL_CREATE_ENTRIES_MONTH);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES_DAY);
            db.execSQL(SQL_DELETE_ENTRIES_WEEK);
            db.execSQL(SQL_DELETE_ENTRIES_MONTH);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private HRReaderDbHelper mDbHelper;

    public void insertHR(String table, long unixTime, int hr, int max, int min, int sd) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put("timestamp", unixTime);
        values.put("hr", hr);
        values.put("max", max);
        values.put("min", min);
        values.put("sd", sd);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                table,
                null,
                values);
    }

    public List<Long[]> getHRs() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        long unixTime = System.currentTimeMillis();
        Cursor cursor =
                db.query("day", // a. table
                        new String[]{"timestamp", "hr"}, // b. column names
                        " " + "timestamp" + " > ?", // c. selections
                        new String[]{String.valueOf(unixTime - 1000000)}, // d. selections args
                        null, // e. group by
                        null, // f. having
                        "timestamp" + " DESC", // g. order by
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
