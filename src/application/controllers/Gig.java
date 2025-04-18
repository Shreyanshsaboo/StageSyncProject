package application.controllers;

public interface Gig {
    // Getters
    String getTitle();
    String getDate();
    String getPay();
    String getDescription();
    String getRequirements();
    int getGigId();
    int getCafeId();

    // Setters
    void setTitle(String title);
    void setDate(String date);
    void setPay(String pay);
    void setDescription(String description);
    void setRequirements(String requirements);
    void setGigId(int gigId);
    void setCafeId(int cafeId);
}