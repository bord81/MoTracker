package labut.md311.motracker.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DbReader {
    public static CoordsFromDb readAll(Context context) {
        List<Object[]> tracks = new ArrayList<>();
        long[] date;
        double[] lat, lon;
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT trackid, distance, spdmax, spdavg, duration FROM trck", null);
        Cursor cursor_crds = db.rawQuery("SELECT track, lat, lon FROM coord", null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            Object[] track = new Object[6];
            Calendar track_date = Calendar.getInstance();
            track[4] = cursor.getLong(0);
            track_date.setTimeInMillis((long) track[4]);
            int month_int = track_date.get(Calendar.MONTH) + 1;
            String month_str = new String();
            if (month_int < 10)
                month_str = "0" + String.valueOf(month_int);
            String track_date_string = track_date.get(Calendar.DAY_OF_MONTH) + "." + month_str + "." + track_date.get(Calendar.YEAR);
            track[0] = track_date_string;
            track[1] = cursor.getFloat(2);
            track[2] = cursor.getLong(1);
            track[3] = cursor.getString(4);
            track[5] = cursor.getFloat(3);
            tracks.add(track);
            cursor.moveToNext();
        }
        date = new long[cursor_crds.getCount()];
        lat = new double[cursor_crds.getCount()];
        lon = new double[cursor_crds.getCount()];
        cursor_crds.moveToFirst();
        for (int i = 0; i < cursor_crds.getCount(); i++) {
            date[i] = cursor_crds.getLong(0);
            lat[i] = cursor_crds.getDouble(1);
            lon[i] = cursor_crds.getDouble(2);
            cursor_crds.moveToNext();
        }
        cursor.close();
        cursor_crds.close();
        return new CoordsFromDb(tracks, date, lat, lon);
    }
}
