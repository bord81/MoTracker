package labut.md311.motracker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    private static final String NAME = "trck.db";
    private static final int SCHEMA = 1;
    public static final String TBL_TRCK = "trck";
    public static final String TBL_CRD = "coord";
    public static final String ID = "trackid";
    public static final String TRC = "track";
    public static final String DST= "distance";
    public static final String M_SPD = "spdmax";
    public static final String A_SPD = "spdavg";
    public static final String DUR = "duration";
    public static final String LAT = "lat";
    public static final String LON = "lon";

    public DbHelper(Context context) {
        super(context, NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE trck (trackid INTEGER, distance INTEGER, spdmax REAL, spdavg REAL, duration TEXT);");
        db.execSQL("CREATE TABLE coord (track INTEGER, lat INTEGER, lon INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new RuntimeException("Upgrade not supported yet.");
    }
}
