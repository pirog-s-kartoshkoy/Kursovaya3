package com.example.kursovaya3;

public class FineType {
    private int idFineType;
    private String name;
    private double cost;

    public FineType(int idFineType, String name, double cost) {
        this.idFineType = idFineType;
        this.name = name;
        this.cost = cost;
    }

    public int getIdFineType() { return idFineType; }
    public String getName() { return name; }
    public double getCost() { return cost; }

    @Override
    public String toString() {
        return name + " (" + cost + " руб.)";
    }
}