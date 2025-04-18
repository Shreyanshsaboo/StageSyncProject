package application.models;

public class BandPerformance extends Performance {
    private int numberOfMembers;
    private String genre;
    private boolean needsSoundSystem;

    public BandPerformance(String venue, String date, int duration, double basePayRate,
                           int numberOfMembers, String genre, boolean needsSoundSystem) {
        super(venue, date, duration, basePayRate);
        this.numberOfMembers = numberOfMembers;
        this.genre = genre;
        this.needsSoundSystem = needsSoundSystem;
    }

    @Override
    public double calculatePay() {
        // Band pay is calculated based on base rate, duration, number of members,
        // and additional cost if sound system is needed
        double pay = basePayRate * duration * numberOfMembers;
        if (needsSoundSystem) {
            pay += 100.0; // Sound system fee
        }
        return pay;
    }

    public int getNumberOfMembers() {
        return numberOfMembers;
    }

    public String getGenre() {
        return genre;
    }

    public boolean getNeedsSoundSystem() {
        return needsSoundSystem;
    }
}