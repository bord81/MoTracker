package labut.md311.motracker.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import labut.md311.motracker.R;
import labut.md311.motracker.coords.CoordinatesCalc;
import labut.md311.motracker.presenter.TrackingPresenter;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private long button;
    private long[] date;
    private boolean if_from_tracking;
    private double[] latitude_vals;
    private double[] longitude_vals;
    private boolean map_data_avail = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Bundle bundle = getIntent().getBundleExtra(TrackingPresenter.COORD_DATA);
        if (bundle != null) {
            if_from_tracking = bundle.getBoolean(TrackingPresenter.IF_FROM_TRACKING, false);
            button = bundle.getLong(TrackingPresenter.ID);
            date = bundle.getLongArray(TrackingPresenter.ID_DATA);
            latitude_vals = bundle.getDoubleArray(TrackingPresenter.LAT_DATA);
            longitude_vals = bundle.getDoubleArray(TrackingPresenter.LON_DATA);
            if (latitude_vals != null && longitude_vals != null && (date != null || if_from_tracking))
                map_data_avail = true;
        }
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (map_data_avail) {
            double[] latitude_vals_for_map;
            double[] longitude_vals_for_map;
            int start = -1;
            int map_elems_count = 0;
            googleMap.clear();
            if (!if_from_tracking) {
                for (int i = 0; i < latitude_vals.length; i++) {
                    if (date[i] == button) {
                        if (start < 0) {
                            start = i;
                            map_elems_count++;
                        } else {
                            map_elems_count++;
                        }
                    }
                }
                if (map_elems_count > 0) {
                    latitude_vals_for_map = new double[map_elems_count];
                    longitude_vals_for_map = new double[map_elems_count];
                    for (int i = 0; i < map_elems_count; i++) {
                        latitude_vals_for_map[i] = latitude_vals[i + start];
                        longitude_vals_for_map[i] = longitude_vals[i + start];
                    }
                } else {
                    return;
                }
            } else {
                latitude_vals_for_map = latitude_vals;
                longitude_vals_for_map = longitude_vals;
            }

            //filter for incorrect start values
            boolean found_bad_start = false;
            int start_index = 0;
            for (int i = 0; i < latitude_vals_for_map.length; i++) {
                if (i < latitude_vals_for_map.length - 1) {
                    if (CoordinatesCalc.checkData(latitude_vals_for_map[i], latitude_vals_for_map[i + 1], longitude_vals_for_map[i], longitude_vals_for_map[i + 1]).isInaccr()) {
                        found_bad_start = true;
                        start_index++;
                    } else if (found_bad_start || i > 0) {
                        break;
                    }
                }
            }


            PolylineOptions route = new PolylineOptions();
            for (int i = start_index; i < latitude_vals_for_map.length; i++) {
                if (latitude_vals_for_map[i] > 0.0 && longitude_vals_for_map[i] > 0.0)
                    route.add(new LatLng(latitude_vals_for_map[i], longitude_vals_for_map[i]));
            }
            route.width(8).color(Color.GREEN);
            googleMap.addPolyline(route);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude_vals_for_map[latitude_vals_for_map.length - 1], longitude_vals_for_map[longitude_vals_for_map.length - 1]))      // Sets the center of the map to Mountain View
                    .zoom(17)                   // Sets the zoom
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }
}
