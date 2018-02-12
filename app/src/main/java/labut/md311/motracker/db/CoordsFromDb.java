package labut.md311.motracker.db;

import java.util.List;

public class CoordsFromDb {
    private final List<Object[]> tracks;
    private final long[] date;
    private final double[] lat;
    private final double[] lon;

    public CoordsFromDb(List<Object[]> tracks, long[] date, double[] lat, double[] lon) {
        this.tracks = tracks;
        this.date = date;
        this.lat = lat;
        this.lon = lon;
    }

    public List<Object[]> getTracks() {
        return tracks;
    }

    public long[] getDate() {
        return date;
    }

    public double[] getLat() {
        return lat;
    }

    public double[] getLon() {
        return lon;
    }
}
