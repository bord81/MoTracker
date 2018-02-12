package labut.md311.motracker.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import labut.md311.motracker.R;
import labut.md311.motracker.databinding.TrackingActivityBinding;
import labut.md311.motracker.db.DbHelper;
import labut.md311.motracker.loc_determ.service.LocationFragment;
import labut.md311.motracker.loc_determ.service.LocationService;
import labut.md311.motracker.model.ViewBinding;
import labut.md311.motracker.presenter.TrackingPresenter;


public class TrackingActivity extends AppCompatActivity {
    public static final String CONFIRM_TRACKING = "cft";
    public static final String RESULTS = "rst";
    TrackingPresenter presenter;
    TrackingActivityBinding atb;
    ViewBinding vb;
    LocationService.LocationBinder loc_binder;
    DbHelper dbHelper;
    static Handler handler;
    LocationFragment locationFragment;
    WeakReference<TrackingActivity> activityWeakReference = new WeakReference<TrackingActivity>(this);
    private boolean tracking_mode = false;
    private boolean recreate_from_stop = false;
    private Bundle results = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        atb = DataBindingUtil.setContentView(this, R.layout.tracking_activity);
        setViewBinding();
        presenter = new TrackingPresenter(activityWeakReference);
        atb.setPresenter(presenter);
        Bundle bundle = getIntent().getBundleExtra(TrackingPresenter.COORD_DATA);
        if (savedInstanceState != null && savedInstanceState.getBundle(TrackingActivity.RESULTS) != null) {
            recreate_from_stop = true;
            results = savedInstanceState.getBundle(TrackingActivity.RESULTS);
        }
        if (bundle != null) {
            tracking_mode = false;
            presenter.showResults(bundle, false);
        } else if (savedInstanceState == null || savedInstanceState.getBoolean(TrackingActivity.CONFIRM_TRACKING, true)) {
            tracking_mode = true;
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == LocationService.SERV_NOT_AVAIL) {
                        Toast.makeText(activityWeakReference.get().getApplicationContext(), "Location services not available", Toast.LENGTH_LONG).show();
                        activityWeakReference.get().presenter.unsubscribe();
                        activityWeakReference.get().locationFragment.unbind();
                        activityWeakReference.get().finish();
                    }
                }
            };
            dbHelper = new DbHelper(getApplicationContext());
            Messenger messenger = new Messenger(handler);
            Intent intent = new Intent(this, LocationService.class);
            ArrayList<Messenger> messengers = new ArrayList<>();
            messengers.add(messenger);
            intent.putParcelableArrayListExtra("msg", messengers);
            if ((locationFragment = (LocationFragment) getSupportFragmentManager().findFragmentByTag("LF")) == null) {
                locationFragment = LocationFragment.getInstance(intent, activityWeakReference);
                getSupportFragmentManager().beginTransaction().add(locationFragment, "LF").commit();
            }
        }
    }

    public DbHelper getDbHelper() {
        return dbHelper;
    }


    public ViewBinding getVb() {
        return vb;
    }

    public LocationService.LocationBinder getLoc_binder() {
        return loc_binder;
    }

    public void setLoc_binder(LocationService.LocationBinder loc_binder) {
        this.loc_binder = loc_binder;
    }

    private void setViewBinding() {
        vb = new ViewBinding(getLocStr(R.string.distance), getLocStr(R.string.avg_speed),
                getLocStr(R.string.cur_speed), getLocStr(R.string.total_time),
                getLocStr(R.string.finish), getLocStr(R.string.show_map));
        atb.setTracking(vb);
    }

    private String getLocStr(int id) {
        return getResources().getString(id);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (!tracking_mode) {
            outState.putBoolean(TrackingActivity.CONFIRM_TRACKING, false);
            outState.putBundle(TrackingActivity.RESULTS, results);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tracking_mode) {
            setLoc_binder(locationFragment.getLoc_binder());
            if (dbHelper == null)
                dbHelper = new DbHelper(getApplicationContext());
            if (loc_binder != null)
                presenter.startTracking();
        } else if (recreate_from_stop) {
            presenter.showResults(results, true);
        }
    }

    @Override
    protected void onPause() {
        if (tracking_mode) {
            dbHelper.close();
            dbHelper = null;
            presenter.unsubscribe();
        }
        super.onPause();
    }

    public void unbind(Bundle bundle) {
        tracking_mode = false;
        this.results = bundle;
        locationFragment.unbind();
    }

    public void startTracking() {
        presenter.startTracking();
    }
}
