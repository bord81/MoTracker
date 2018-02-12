package labut.md311.motracker.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import labut.md311.motracker.R;
import labut.md311.motracker.db.CoordsFromDb;
import labut.md311.motracker.db.DbReader;

public class HistoryFragment extends Fragment {

    WeakReference<HistActInterface> actCallback;
    List<Object[]> tracks;
    long[] date;
    double[] lat;
    double[] lon;
    ListView lv;
    ListAdapter listAdapter;
    Button del_button;
    Button main_m_button;
    public static final int DELETE_ALL = 0;
    public static final int BACK_TO_MAIN = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        lv = (ListView) view.findViewById(R.id.history_items_list_view);
        lv.addHeaderView(getLayoutInflater().inflate(R.layout.header, null));
        listAdapter = new ListAdapter();
        lv.setAdapter(listAdapter);
        del_button = (Button) view.findViewById(R.id.history_delete_button);
        main_m_button = (Button) view.findViewById(R.id.history_back_to_main);
        del_button.setOnClickListener(v -> {
            actCallback.get().onMenuButtonClicked(DELETE_ALL);
            tracks = new ArrayList<>();
            listAdapter.notifyDataSetChanged();
            del_button.setVisibility(View.GONE);
        });
        main_m_button.setOnClickListener(v -> {
            actCallback.get().onMenuButtonClicked(BACK_TO_MAIN);
        });
        if (tracks.size() > 0) {
            del_button.setVisibility(View.VISIBLE);
        } else {
            del_button.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HistActInterface) {
            actCallback = new WeakReference<HistActInterface>((HistActInterface) context);
            CoordsFromDb coordsFromDb = DbReader.readAll(getActivity().getApplicationContext());
            tracks = coordsFromDb.getTracks();
            date = coordsFromDb.getDate();
            lat = coordsFromDb.getLat();
            lon = coordsFromDb.getLon();
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

    private class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return tracks.size();
        }

        @Override
        public Object getItem(int position) {
            return tracks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.history_item, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.history_item_date)).setText((String) tracks.get(tracks.size() - position - 1)[0]);
            float dist_f = (float) Math.round(((float) ((long) tracks.get(tracks.size() - position - 1)[2]) / 1000) * 10) / 10;
            ((TextView) convertView.findViewById(R.id.history_item_dst)).setText(String.valueOf(dist_f));
            ((TextView) convertView.findViewById(R.id.history_item_total_time)).setText((String) tracks.get(tracks.size() - position - 1)[3]);
            Button button = (Button) convertView.findViewById(R.id.history_item_map);
            button.setText(String.valueOf(tracks.size() - position));
            button.setTag(tracks.get(tracks.size() - position - 1)[4]);
            button.setOnClickListener(v -> {
                float max_speed = 0f;
                float avg_speed = 0f;
                long dist = 0L;
                String ttime = new String();
                for (int i = 0; i < tracks.size(); i++) {
                    if ((long) tracks.get(i)[4] == (long) v.getTag()) {
                        max_speed = (float) tracks.get(i)[1];
                        avg_speed = (float) tracks.get(i)[5];
                        dist = (long) tracks.get(i)[2];
                        ttime = (String) tracks.get(i)[3];
                        break;
                    }
                }
                actCallback.get().onButtonClicked((long) v.getTag(), date, lat, lon, max_speed, avg_speed, dist, ttime);
            });
            return convertView;
        }
    }

    public interface HistActInterface {
        void onButtonClicked(long button, long date[], double[] lat, double[] lon, float m_spd, float a_spd, long dist, String ttime);

        void onMenuButtonClicked(int button);
    }
}
