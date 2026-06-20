package com.example.kursovaya3;

public class Client {
    private int idClient;
    private String fullName;
    private String gender;
    private String phone;

    public Client(int idClient, String fullName, String gender, String phone) {
        this.idClient = idClient;
        this.fullName = fullName;
        this.gender = gender;
        this.phone = phone;
    }

    public int getIdClient() { return idClient; }
    public String getFullName() { return fullName; }
    public String getGender() { return gender; }
    public String getPhone() { return phone; }
    @Override
    public String toString() {
        return this.fullName;
    }
}