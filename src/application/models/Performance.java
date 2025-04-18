package application.models;

public abstract class Performance {
    protected String venue;
    protected String date;
    protected int duration; // in minutes
    protected double basePayRate;

    public Performance(String venue, String date, int duration, double basePayRate) {
        this.venue = venue;
        this.date = date;
        this.duration = duration;
        this.basePayRate = basePayRate;
    }

    // Abstract method that derived classes must implement
    public abstract double calculatePay();

    // Common methods that all performances share
    public String getVenue() {
        return venue;
    }

    public String getDate() {
        return date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    // Template method pattern
    public final String getPerformanceDetails() {
        return String.format("Venue: %s, Date: %s, Duration: %d minutes, Pay: $%.2f",
                venue, date, duration, calculatePay());
    }
}