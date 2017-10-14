package io.github.utshaw.iut;

/**
 * Created by Utshaw on 10/13/2017.
 */

public class LocationInfo {

    private static double lat = 23.7595711;
        private static double lon = 90.375337;

    public static double getLon() {
        return lon;
    }

    public static void setLon(double lon) {
        LocationInfo.lon = lon;
    }

    public static double getLat() {
        return lat;

    }

    public static void setLat(double lat) {
        LocationInfo.lat = lat;
    }
}
