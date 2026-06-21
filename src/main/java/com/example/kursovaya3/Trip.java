package com.example.kursovaya3;

public class Trip {
    private int idTrip;
    private String clientName;
    private String carBrand;
    private int durationDays;
    private String tripDate;
    private double totalPrice;

    public Trip(int idTrip, String clientName, String carBrand, int durationDays, String tripDate, double totalPrice) {
        this.idTrip = idTrip;
        this.clientName = clientName;
        this.carBrand = carBrand;
        this.durationDays = durationDays;
        this.tripDate = tripDate;
        this.totalPrice = totalPrice;
    }

    public int getIdTrip() { return idTrip; }
    public String getClientName() { return clientName; }
    public String getCarBrand() { return carBrand; }
    public int getDurationDays() { return durationDays; }
    public String getTripDate() { return tripDate; }
    public double getTotalPrice() { return totalPrice; }
}