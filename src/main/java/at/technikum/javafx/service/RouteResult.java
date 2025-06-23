package at.technikum.javafx.service;

import com.fasterxml.jackson.databind.JsonNode;

public class RouteResult {
    private final double distance;
    private final double duration;
    private final JsonNode geometry;
    private final double[] bbox;

    public RouteResult(double distance, double duration, JsonNode geometry, double[] bbox) {
        this.distance = distance;
        this.duration = duration;
        this.geometry = geometry;
        this.bbox     = bbox;
    }

    public double getDistance()   { return distance; }
    public double getDuration()   { return duration; }
    public JsonNode getGeometry() { return geometry; }
    public double[] getBbox()     { return bbox; }
}