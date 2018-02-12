package labut.md311.motracker.presenter;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import labut.md311.motracker.db.DbHelper;
import labut.md311.motracker.loc_determ.service.LocationService;
import labut.md311.motracker.model.ViewBinding;
import labut.md311.motracker.view.MapActivity;
import labut.md311.motracker.view.TrackingActivity;

public class TrackingPresenter {
    public static final String IF_FROM_TRACKING = "if_from";
    public static final String ID = "id";
    public static final String ID_DATA = "date";
    public static final String LAT_DATA = "lat";
    public static final String LON_DATA = "lon";
    public static final String M_SPD = "mspd";
    public static final String A_SPD = "aspd";
    public static final String DST = "dst";
    public static final String TOT_TIME = "time";
    public static final String COORD_DATA = "map";
    private WeakReference<TrackingActivity> reference;
    private WeakReference<ViewBinding> vb;
    private WeakReference<LocationService.LocationBinder> loc_binder;
    private Disposable loc_sub;
    private ScheduledExecutorService time_exec;
    private boolean started = false;
    private long start_time;
    private double[] latitude_vals;
    private double[] longitude_vals;
    private long[] date;
    private long cur_id = 0L;
    private boolean if_was_tracking = false;
    private String tot_time = new String();

    public TrackingPresenter(WeakReference<TrackingActivity> reference) {
        this.reference = reference;
        this.vb = new WeakReference<ViewBinding>(this.reference.get().getVb());
    }

    public void startTracking() {
        if (started) {
            startTimerAndLocUpdate();
            return;
        }
        started = true;
        this.loc_binder = new WeakReference<LocationService.LocationBinder>(this.reference.get().getLoc_binder());
        startTimerAndLocUpdate();
        this.loc_binder.get().first_loc_upd();
        vb.get().setIn_progress(true);
    }

    public void showResults(Bundle bundle, boolean from_tracking) {
        if_was_tracking = from_tracking;
        cur_id = bundle.getLong(TrackingPresenter.ID);
        if (!from_tracking)
            date = bundle.getLongArray(TrackingPresenter.ID_DATA);
        latitude_vals = bundle.getDoubleArray(TrackingPresenter.LAT_DATA);
        longitude_vals = bundle.getDoubleArray(TrackingPresenter.LON_DATA);
        tot_time = bundle.getString(TrackingPresenter.TOT_TIME);
        displayFinalValues(bundle.getFloat(TrackingPresenter.M_SPD), bundle.getFloat(TrackingPresenter.A_SPD), bundle.getLong(TrackingPresenter.DST));
    }

    private void startTimerAndLocUpdate() {
        loc_sub = loc_binder.get().getLastLocation().observeOn(Schedulers.io()).subscribeOn(AndroidSchedulers.mainThread()).subscribe(numbers -> {
            vb.get().setSpeed_var(String.valueOf(numbers[0]));
            vb.get().setDist_var(String.valueOf(numbers[1]));
        });
        this.start_time = loc_binder.get().getStartTime();
        TimerTask time = new TimerTask() {
            long start_time = loc_binder.get().getStartTime();
            StringBuilder cur_time = new StringBuilder();

            @Override
            public void run() {
                long tot_secs = (System.currentTimeMillis() - start_time) / 1000;
                long hours = tot_secs / 3600;
                long mins = (tot_secs - hours * 3600) / 60;
                long secs = tot_secs - hours * 3600 - mins * 60;
                append(hours, false);
                append(mins, true);
                append(secs, true);
                vb.get().setTime_var(cur_time.toString());
                cur_time.delete(0, cur_time.length());
            }

            private void append(long value, boolean mins_secs) {
                if (value < 10 && mins_secs) {
                    cur_time.append(":0");
                    cur_time.append(value);
                } else if (value < 60 && mins_secs) {
                    cur_time.append(":");
                    cur_time.append(value);
                } else {
                    cur_time.append(value);
                }
            }
        };
        time_exec = Executors.newSingleThreadScheduledExecutor();
        time_exec.scheduleAtFixedRate(time, 0L, 1000L, TimeUnit.MILLISECONDS);
    }

    private void finish() {
        unsubscribe();
        List<Number[]> tracking_data = loc_binder.get().getTrackingData();
        loc_binder.get().stopService();

        float max_speed = -Float.MAX_VALUE;
        float avg_speed = 0f;
        long tr_dst = 0L;
        float tr_speed;
        double tr_lat;
        double tr_lon;
        SQLiteDatabase db = reference.get().getDbHelper().getWritableDatabase();
        ContentValues cv = new ContentValues();
        try {
            db.beginTransaction();
            Cursor cursor = db.rawQuery("SELECT trackid FROM trck", null);
            if (cursor.getCount() > 20) {
                //remove oldest entry
                cursor.moveToFirst();
                long to_delete = Long.MAX_VALUE;
                for (int i = 0; i < cursor.getCount(); i++) {
                    long current_id = cursor.getLong(0);
                    if (current_id < to_delete) {
                        to_delete = current_id;
                    }
                    cursor.moveToNext();
                }
                String[] to_del = new String[1];
                to_del[0] = String.valueOf(to_delete);
                String del_tr = DbHelper.ID + " = ?";
                String del_cr = DbHelper.TRC + " = ?";
                db.delete(DbHelper.TBL_TRCK, del_tr, to_del);
                db.delete(DbHelper.TBL_CRD, del_cr, to_del);
            }
            cursor.close();

            long current_id = start_time;
            latitude_vals = new double[tracking_data.size()];
            longitude_vals = new double[tracking_data.size()];
            for (int i = 0; i < tracking_data.size(); i++) {
                tr_speed = tracking_data.get(i)[0].floatValue();
                tr_lat = tracking_data.get(i)[2].doubleValue();
                tr_lon = tracking_data.get(i)[3].doubleValue();
                latitude_vals[i] = tr_lat;
                longitude_vals[i] = tr_lon;
                cv.put(DbHelper.TRC, current_id);
                cv.put(DbHelper.LAT, tr_lat);
                cv.put(DbHelper.LON, tr_lon);
                db.insert(DbHelper.TBL_CRD, DbHelper.LAT, cv);
                cv.clear();
                if (tr_speed > max_speed) {
                    max_speed = tr_speed;
                }
                avg_speed += tr_speed;
            }
            tr_dst = tracking_data.get(tracking_data.size() - 1)[1].longValue();
            avg_speed = avg_speed / tracking_data.size();
            cv.clear();
            cv.put(DbHelper.ID, current_id);
            cv.put(DbHelper.DST, tr_dst);
            cv.put(DbHelper.A_SPD, avg_speed);
            cv.put(DbHelper.M_SPD, max_speed);
            cv.put(DbHelper.DUR, vb.get().getTime_var());
            db.insert(DbHelper.TBL_TRCK, DbHelper.DST, cv);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        Bundle bundle = new Bundle();
        bundle.putLong(TrackingPresenter.ID, start_time);
        bundle.putString(TrackingPresenter.TOT_TIME, vb.get().getTime_var());
        bundle.putBoolean(IF_FROM_TRACKING, true);
        bundle.putDoubleArray(LAT_DATA, latitude_vals);
        bundle.putDoubleArray(LON_DATA, longitude_vals);
        bundle.putFloat(TrackingPresenter.M_SPD, max_speed);
        bundle.putFloat(TrackingPresenter.A_SPD, avg_speed);
        bundle.putFloat(TrackingPresenter.DST, tr_dst);
        reference.get().unbind(bundle);
        tracking_data = null;
        if_was_tracking = true;
        cur_id = -1L;
        displayFinalValues(max_speed, avg_speed, tr_dst);
    }

    private void displayFinalValues(float max_speed, float avg_speed, long dist) {
        vb.get().setIn_progress(false);
        String avg_max_speed = String.valueOf(Math.round(avg_speed)) + " / " + String.valueOf(Math.round(max_speed));
        vb.get().setSpeed_var(avg_max_speed);
        vb.get().setDist_var(String.valueOf(dist));
        if (tot_time != null && tot_time.length() > 1)
            vb.get().setTime_var(tot_time);
    }

    public void clickFinishOrMap(View view) {
        if (vb.get().isIn_progress()) {
            finish();
        } else {
            showMap(if_was_tracking, cur_id);
        }
    }

    private void showMap(boolean from_tracking, long id) {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(reference.get().getApplicationContext()) == ConnectionResult.SUCCESS) {
            Intent intent = new Intent(reference.get().getApplicationContext(), MapActivity.class);
            Bundle bundle = new Bundle();
            bundle.putBoolean(IF_FROM_TRACKING, from_tracking);
            bundle.putDoubleArray(LAT_DATA, latitude_vals);
            bundle.putDoubleArray(LON_DATA, longitude_vals);
            intent.putExtra(COORD_DATA, bundle);
            if (!from_tracking) {
                bundle.putLongArray(TrackingPresenter.ID_DATA, date);
                bundle.putLong(TrackingPresenter.ID, id);
            }
            reference.get().startActivity(intent);
            reference.get().finish();
        }
    }

    public void unsubscribe() {
        if (!loc_sub.isDisposed())
            loc_sub.dispose();
        if (time_exec != null)
            time_exec.shutdown();
    }
}