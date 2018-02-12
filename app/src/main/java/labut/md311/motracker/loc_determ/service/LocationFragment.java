package labut.md311.motracker.loc_determ.service;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.lang.ref.WeakReference;

import labut.md311.motracker.view.TrackingActivity;

public class LocationFragment extends Fragment implements ServiceConnection {

    private Application app;
    private LocationService.LocationBinder loc_binder;
    private Intent intent;
    private WeakReference<TrackingActivity> reference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        app = getActivity().getApplication();
        if (intent != null)
            app.getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        try {
            app.getApplicationContext().unbindService(this);
        } catch (IllegalArgumentException e) {

        }
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        loc_binder = (LocationService.LocationBinder) service;
        reference.get().setLoc_binder(loc_binder);
        reference.get().startTracking();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        loc_binder = null;
    }

    public static LocationFragment getInstance(Intent intent, WeakReference<TrackingActivity> reference) {
        LocationFragment locationFragment = new LocationFragment();
        locationFragment.intent = intent;
        locationFragment.reference = reference;
        return locationFragment;
    }

    public LocationService.LocationBinder getLoc_binder() {
        return loc_binder;
    }

    public void unbind() {
        try {
            app.getApplicationContext().unbindService(this);
        } catch (IllegalArgumentException e) {

        }
    }
}
