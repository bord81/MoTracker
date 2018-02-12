package labut.md311.motracker.coords;

public class CoordinatesCalc {
    //max distance in meters between two points assuming polling interval of 1 sec
    private final static int MAX_DISTANCE = 111; //111 m/s is approx. 400 km/h
    private final static double EARTH_RADIUS_IN_METERS = 6372797.560856;

    //filter initial coordinates by distance between them (too large means unrealistic speed)
    public static CoordData checkData(double lat1, double lat2, double lon1, double lon2) {
        double dist = getDistance(lat1, lat2, lon1, lon2);
        return new CoordData(dist > MAX_DISTANCE, dist);
    }

    private static double getDistance(double lat1, double lat2, double lon1, double lon2) {
        double dist_lat = Math.toRadians(lat2 - lat1);
        double dist_lon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dist_lat / 2) * Math.sin(dist_lat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dist_lon / 2) * Math.sin(dist_lon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return c * EARTH_RADIUS_IN_METERS;
    }
}
