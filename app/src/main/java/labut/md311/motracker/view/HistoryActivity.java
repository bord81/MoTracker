package labut.md311.motracker.view;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import labut.md311.motracker.R;
import labut.md311.motracker.db.DbHelper;
import labut.md311.motracker.presenter.TrackingPresenter;

public class HistoryActivity extends AppCompatActivity implements HistoryFragment.HistActInterface {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_activity);
    }

    @Override
    public void onButtonClicked(long button, long[] date, double[] lat, double[] lon, float m_spd, float a_spd, long dist, String ttime) {
        Intent intent = new Intent(getApplicationContext(), TrackingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(TrackingPresenter.IF_FROM_TRACKING, false);
        bundle.putLong(TrackingPresenter.ID, button);
        bundle.putDoubleArray(TrackingPresenter.LAT_DATA, lat);
        bundle.putDoubleArray(TrackingPresenter.LON_DATA, lon);
        bundle.putLongArray(TrackingPresenter.ID_DATA, date);
        bundle.putFloat(TrackingPresenter.M_SPD, m_spd);
        bundle.putFloat(TrackingPresenter.A_SPD, a_spd);
        bundle.putLong(TrackingPresenter.DST, dist);
        bundle.putString(TrackingPresenter.TOT_TIME, ttime);
        intent.putExtra(TrackingPresenter.COORD_DATA, bundle);
        startActivity(intent);
    }

    @Override
    public void onMenuButtonClicked(int button) {
        switch (button) {
            case HistoryFragment.DELETE_ALL:
                SQLiteDatabase db = new DbHelper(getApplicationContext()).getWritableDatabase();
                db.delete(DbHelper.TBL_TRCK, null, null);
                db.delete(DbHelper.TBL_CRD, null, null);
                break;
            case HistoryFragment.BACK_TO_MAIN:
                finish();
                break;
        }
    }
}
