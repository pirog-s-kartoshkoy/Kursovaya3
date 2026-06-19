package com.example.kursovaya3;

public class Client {
    private int idClient;
    private String fullName;
    private String phone;

    public Client(int idClient, String fullName, String phone) {
        this.idClient = idClient;
        this.fullName = fullName;
        this.phone = phone;
    }

    public int getIdClient() { return idClient; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
}