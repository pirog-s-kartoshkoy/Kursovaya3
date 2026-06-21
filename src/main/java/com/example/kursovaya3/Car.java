package com.example.kursovaya3;

public class Car {
    private int idCar;
    private String brand;
    private String regNumber;

    public Car(int idCar, String brand, String regNumber) {
        this.idCar = idCar;
        this.brand = brand;
        this.regNumber = regNumber;
    }

    public int getIdCar() { return idCar; }
    public String getBrand() { return brand; }
    public String getRegNumber() { return regNumber; }
}