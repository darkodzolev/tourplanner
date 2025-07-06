package at.technikum.javafx.service;

public class TourSummary {
    private String tourName;
    private double avgDistance;
    private String avgTime;
    private double avgRating;

    public TourSummary() { }

    public TourSummary(String tourName, double avgDistance, String avgTime, double avgRating) {
        this.tourName = tourName;
        this.avgDistance = avgDistance;
        this.avgTime = avgTime;
        this.avgRating = avgRating;
    }

    public String getTourName() {
        return tourName;
    }

    public double getAvgDistance() {
        return avgDistance;
    }

    public String getAvgTime() {
        return avgTime;
    }

    public double getAvgRating() {
        return avgRating;
    }
}