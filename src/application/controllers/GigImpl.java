package application.controllers;

public class GigImpl implements Gig {
    private String title;
    private String date;
    private String pay;
    private String description;
    private String requirements;
    private int gigId;
    private int cafeId;

    public GigImpl(String title, String date, String pay) {
        this.title = title;
        this.date = date;
        this.pay = pay;
    }

    public GigImpl(int gigId, String title, String date, String pay, String description, String requirements, int cafeId) {
        this.gigId = gigId;
        this.title = title;
        this.date = date;
        this.pay = pay;
        this.description = description;
        this.requirements = requirements;
        this.cafeId = cafeId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDate() {
        return date;
    }

    @Override
    public String getPay() {
        return pay;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getRequirements() {
        return requirements;
    }

    @Override
    public int getGigId() {
        return gigId;
    }

    @Override
    public int getCafeId() {
        return cafeId;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public void setPay(String pay) {
        this.pay = pay;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    @Override
    public void setGigId(int gigId) {
        this.gigId = gigId;
    }

    @Override
    public void setCafeId(int cafeId) {
        this.cafeId = cafeId;
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s", title, date, pay);
    }
}