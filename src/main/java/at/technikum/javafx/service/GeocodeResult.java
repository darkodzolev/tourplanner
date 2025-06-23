package at.technikum.javafx.service;

public class GeocodeResult {
    private final double latitude;
    private final double longitude;
    private final double[] bbox;

    public GeocodeResult(double latitude, double longitude, double[] bbox) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.bbox = bbox;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double[] getBbox() { return bbox; }
}