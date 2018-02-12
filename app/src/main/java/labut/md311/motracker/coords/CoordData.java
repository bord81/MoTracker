package labut.md311.motracker.coords;

public class CoordData {
    private final boolean isInaccr;
    private final double dist;

    public CoordData(boolean isInaccr, double dist) {
        this.isInaccr = isInaccr;
        this.dist = dist;
    }

    public boolean isInaccr() {
        return isInaccr;
    }

    public double getDist() {
        return dist;
    }
}
