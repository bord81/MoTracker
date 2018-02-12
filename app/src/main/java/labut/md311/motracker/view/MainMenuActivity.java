package labut.md311.motracker.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import labut.md311.motracker.R;

public class MainMenuActivity extends AppCompatActivity implements MainMenuFragment.OnButtonClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    @Override
    public void onMenuButtonClicked(int button) {
        switch (button) {
            case MainMenuFragment.START_TRACKING:
                Intent intent_tracking = new Intent(getApplicationContext(), TrackingActivity.class);
                intent_tracking.putExtra(TrackingActivity.CONFIRM_TRACKING, true);
                startActivity(intent_tracking);
                break;
            case MainMenuFragment.SHOW_HISTORY:
                Intent intent_history = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(intent_history);
                break;
            default:
                break;
        }
    }
}
