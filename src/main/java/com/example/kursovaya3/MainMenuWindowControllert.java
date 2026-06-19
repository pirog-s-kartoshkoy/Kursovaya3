package com.example.kursovaya3;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MainMenuWindowControllert {

    // --- ТАБЛИЦА МАШИН ---
    @FXML private TableView<Car> carsTable;
    @FXML private TableColumn<Car, Integer> idColumn;
    @FXML private TableColumn<Car, String> modelColumn;
    @FXML private TableColumn<Car, String> regNumberColumn;

    // --- ТАБЛИЦА КЛИЕНТОВ ---
    @FXML private TableView<Client> clientsTable;
    @FXML private TableColumn<Client, Integer> clientIdColumn;
    @FXML private TableColumn<Client, String> clientNameColumn;
    @FXML private TableColumn<Client, String> clientPhoneColumn;

    @FXML
    public void initialize() {
        // 1. Инициализация машин
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idCar"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        regNumberColumn.setCellValueFactory(new PropertyValueFactory<>("regNumber"));
        loadCarsFromDatabase();

        // 2. Инициализация клиентов
        clientIdColumn.setCellValueFactory(new PropertyValueFactory<>("idClient"));       // Ищет getIdClient()
        clientNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));     // Ищет getFullName()
        clientPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));       // Ищет getPhone()
        loadClientsFromDatabase();
    }

    private void loadCarsFromDatabase() {
        ObservableList<Car> carList = FXCollections.observableArrayList();
        String url = "jdbc:mysql://localhost:3306/carrent";
        String user = "root";
        String dbPassword = "";

        String query = "SELECT car.id_car, car_model.brand, car.reg_number " +
                "FROM car " +
                "INNER JOIN car_model ON car.id_model = car_model.id_model";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(url, user, dbPassword);
                 PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    int idCar = resultSet.getInt("id_car");
                    String brand = resultSet.getString("brand");
                    String regNumber = resultSet.getString("reg_number");

                    carList.add(new Car(idCar, brand, regNumber));
                }
                carsTable.setItems(carList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadClientsFromDatabase() {
        ObservableList<Client> clientList = FXCollections.observableArrayList();
        String url = "jdbc:mysql://localhost:3306/carrent";
        String user = "root";
        String dbPassword = "";

        String query = "SELECT id_client, CONCAT(last_name, ' ', first_name) AS fio, phone FROM client";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(url, user, dbPassword);
                 PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    int idClient = resultSet.getInt("id_client");
                    String fullName = resultSet.getString("fio"); // Берем склеенное ФИО
                    String phone = resultSet.getString("phone");

                    clientList.add(new Client(idClient, fullName, phone));
                }
                clientsTable.setItems(clientList);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}