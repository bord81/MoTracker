package labut.md311.motracker.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import labut.md311.motracker.R;


public class MainMenuFragment extends Fragment {

    public static final int START_TRACKING = 0;
    public static final int SHOW_HISTORY = 1;
    public OnButtonClickListener actCallback;

    public MainMenuFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
        Button button0 = (Button) view.findViewById(R.id.button_start_track);
        Button button1 = (Button) view.findViewById(R.id.button_show_history);
        button0.setOnClickListener(v -> {
            actCallback.onMenuButtonClicked(START_TRACKING);
        });
        button1.setOnClickListener(v -> {
            actCallback.onMenuButtonClicked(SHOW_HISTORY);
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnButtonClickListener) {
            actCallback = (OnButtonClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        actCallback = null;
    }

    public interface OnButtonClickListener {
        void onMenuButtonClicked(int button);
    }
}
