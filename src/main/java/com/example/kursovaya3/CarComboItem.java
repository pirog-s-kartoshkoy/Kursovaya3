package com.example.kursovaya3;

public class CarComboItem {
    private final int idCar;
    private final String brand;
    private final String regNumber;

    public CarComboItem(int idCar, String brand, String regNumber) {
        this.idCar = idCar;
        this.brand = brand;
        this.regNumber = regNumber;
    }

    public int getIdCar() { return idCar; }

    @Override
    public String toString() {
        return brand + " [" + regNumber + "]";
    }
}