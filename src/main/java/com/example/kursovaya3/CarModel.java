package com.example.kursovaya3;

public class CarModel {
    private final int idModel;
    private final String brand;
    private final double pricePerDay;

    public CarModel(int idModel, String brand, double pricePerDay) {
        this.idModel = idModel;
        this.brand = brand;
        this.pricePerDay = pricePerDay;
    }

    public int getIdModel() { return idModel; }
    public String getBrand() { return brand; }
    public double getPricePerDay() { return pricePerDay; }

    @Override
    public String toString() {
        return brand + " (" + pricePerDay + " руб/сут)";
    }
}