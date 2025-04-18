package application.models;

public class SoloPerformance extends Performance {
    private String instrument;
    private boolean bringsOwnEquipment;

    public SoloPerformance(String venue, String date, int duration, double basePayRate,
                           String instrument, boolean bringsOwnEquipment) {
        super(venue, date, duration, basePayRate);
        this.instrument = instrument;
        this.bringsOwnEquipment = bringsOwnEquipment;
    }

    @Override
    public double calculatePay() {
        // Solo performers get base pay rate plus equipment bonus if they bring their own
        double pay = basePayRate * duration;
        if (bringsOwnEquipment) {
            pay += 50.0; // Equipment bonus
        }
        return pay;
    }

    public String getInstrument() {
        return instrument;
    }

    public boolean getBringsOwnEquipment() {
        return bringsOwnEquipment;
    }
}