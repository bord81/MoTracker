package labut.md311.motracker.loc_determ.service;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import labut.md311.motracker.coords.CoordData;
import labut.md311.motracker.coords.CoordinatesCalc;

class LocListener implements LocationListener {
    private final Subject<Number[]> loc_subject;
    private final List<Number[]> tracking_data = new ArrayList<>();
    private Location first_loc;
    private final long start_time = System.currentTimeMillis();
    private long total_distance = 0L;

    LocListener(Location location) {
        this.loc_subject = BehaviorSubject.create();
        this.first_loc = location;
    }

    Subject<Number[]> getSpeed_subject() {
        return loc_subject;
    }

    @Override
    public void onLocationChanged(Location location) {
        loc_update(location);
    }

    private void loc_update(Location location) {
        Number[] loc_data = new Number[4];
        if (location.hasSpeed()) {
            loc_data[0] = location.getSpeed() * 3600 / 1000;
        } else {
            loc_data[0] = 0;
        }
        loc_data[2] = location.getLatitude();
        loc_data[3] = location.getLongitude();
        if (tracking_data.size() > 0) {
            CoordData coordData = CoordinatesCalc.checkData((double) tracking_data.get(tracking_data.size() - 1)[2], (double) loc_data[2], (double) tracking_data.get(tracking_data.size() - 1)[3], (double) loc_data[3]);
            if (!coordData.isInaccr()) {
                total_distance += coordData.getDist();
                loc_data[1] = total_distance;
            }
        } else {
            loc_data[1] = total_distance;
        }
        tracking_data.add(loc_data);
        loc_subject.onNext(loc_data);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    void first_loc_upd() {
        loc_update(first_loc);
    }

    List<Number[]> getTracking_data() {
        return tracking_data;
    }

    long getStart_time() {
        return start_time;
    }
}
