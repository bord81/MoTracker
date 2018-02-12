package labut.md311.motracker.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import labut.md311.motracker.BR;

public class ViewBinding extends BaseObservable {
    private final String cur_dst_h;
    private final String avg_spd_h;
    private final String cur_spd_h;
    private final String tot_time_h;
    private final String finish;
    private final String show_map;
    private boolean in_progress;
    private String dist_var = new String();
    private String speed_var = new String();
    private String time_var = new String();

    public ViewBinding(String cur_dst_h, String avg_spd_h, String cur_spd_h, String tot_time_h, String finish, String show_map) {
        this.cur_dst_h = cur_dst_h;
        this.avg_spd_h = avg_spd_h;
        this.cur_spd_h = cur_spd_h;
        this.tot_time_h = tot_time_h;
        this.finish = finish;
        this.show_map = show_map;
    }

    public String getCur_dst_h() {
        return cur_dst_h;
    }

    public String getAvg_spd_h() {
        return avg_spd_h;
    }

    public String getCur_spd_h() {
        return cur_spd_h;
    }

    public String getTot_time_h() {
        return tot_time_h;
    }

    public String getFinish() {
        return finish;
    }

    public String getShow_map() {
        return show_map;
    }

    @Bindable
    public boolean isIn_progress() {
        return in_progress;
    }

    public void setIn_progress(boolean in_progress) {
        this.in_progress = in_progress;
        notifyPropertyChanged(BR.in_progress);
    }

    @Bindable
    public String getDist_var() {
        return dist_var;
    }

    public void setDist_var(String dist_var) {
        this.dist_var = dist_var;
        notifyPropertyChanged(BR.dist_var);
    }

    @Bindable
    public String getSpeed_var() {
        return speed_var;
    }

    public void setSpeed_var(String speed_var) {
        this.speed_var = speed_var;
        notifyPropertyChanged(BR.speed_var);
    }

    @Bindable
    public String getTime_var() {
        return time_var;
    }

    public void setTime_var(String time_var) {
        this.time_var = time_var;
        notifyPropertyChanged(BR.time_var);
    }
}
