package com.example.Fatia.Prime;

public final class Haversine {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    private Haversine() {
    }

    public static double distanciaKm(double latitudeA, double longitudeA, double latitudeB, double longitudeB) {
        double latitudeDelta = Math.toRadians(latitudeB - latitudeA);
        double longitudeDelta = Math.toRadians(longitudeB - longitudeA);
        double haversine = Math.pow(Math.sin(latitudeDelta / 2), 2)
            + Math.cos(Math.toRadians(latitudeA))
            * Math.cos(Math.toRadians(latitudeB))
            * Math.pow(Math.sin(longitudeDelta / 2), 2);
        double angle = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
        return EARTH_RADIUS_KM * angle;
    }
}