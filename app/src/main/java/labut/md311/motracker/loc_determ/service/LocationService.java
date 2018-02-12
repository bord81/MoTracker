package labut.md311.motracker.loc_determ.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.subjects.Subject;
import labut.md311.motracker.R;

public class LocationService extends Service {
    public static int SERV_NOT_AVAIL = 999;
    LocListener locListener;
    private LocationManager locationManager;
    private int NOT_ID = (int) (Math.random() * 1_000_000);
    private boolean location_avail = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (!location_avail) {
            ArrayList<Messenger> messengers = intent.getParcelableArrayListExtra("msg");
            Messenger messenger = messengers.get(0);
            Message message = new Message();
            message.what = SERV_NOT_AVAIL;
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            stopForeground(true);
            stopSelf();
        }
        return new LocationBinder(locListener);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location last_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (last_loc != null) {
            startForeground(NOT_ID, buildForegroundNotification());
            locListener = new LocListener(last_loc);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locListener);
        } else {
            location_avail = false;
        }
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(locListener);
        super.onDestroy();
    }

    private Notification buildForegroundNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setOngoing(true);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.notification));
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        return builder.build();
    }

    public class LocationBinder extends Binder {

        private final LocListener locListener;

        LocationBinder(LocListener locListener) {
            this.locListener = locListener;
        }

        public Subject<Number[]> getLastLocation() {
            return locListener.getSpeed_subject();
        }

        public List<Number[]> getTrackingData() {
            return locListener.getTracking_data();
        }

        public long getStartTime() {
            return locListener.getStart_time();
        }

        public void first_loc_upd() {
            locListener.first_loc_upd();
        }

        public void stopService() {
            stopForeground(true);
        }
    }
}
