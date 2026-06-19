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
    @FXML private TableColumn<Client, String> clientGenderColumn;

    // --- ТАБЛИЦА ЗАКАЗОВ ---
    @FXML private TableView<Trip> tripsTable;
    @FXML private TableColumn<Trip, Integer> tripIdColumn;
    @FXML private TableColumn<Trip, String> tripClientColumn;
    @FXML private TableColumn<Trip, String> tripCarColumn;
    @FXML private TableColumn<Trip, Integer> tripDurationColumn;
    @FXML private TableColumn<Trip, String> tripDateColumn;
    @FXML private TableColumn<Trip, Double> tripPriceColumn;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idCar"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        regNumberColumn.setCellValueFactory(new PropertyValueFactory<>("regNumber"));
        loadCarsFromDatabase();

        clientIdColumn.setCellValueFactory(new PropertyValueFactory<>("idClient"));
        clientNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        clientPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        clientGenderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        loadClientsFromDatabase();

        tripIdColumn.setCellValueFactory(new PropertyValueFactory<>("idTrip"));
        tripClientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        tripCarColumn.setCellValueFactory(new PropertyValueFactory<>("carBrand"));
        tripDurationColumn.setCellValueFactory(new PropertyValueFactory<>("durationDays"));
        tripDateColumn.setCellValueFactory(new PropertyValueFactory<>("tripDate"));
        tripPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        loadTripsFromDatabase();
    }

    private void loadCarsFromDatabase() {
        ObservableList<Car> carList = FXCollections.observableArrayList();
        String url = "jdbc:mysql://localhost:3306/carrent";
        String user = "root";
        String dbPassword = "";
        String query = "SELECT car.id_car, car_model.brand, car.reg_number FROM car INNER JOIN car_model ON car.id_model = car_model.id_model";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(url, user, dbPassword);
                 PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    carList.add(new Car(resultSet.getInt("id_car"), resultSet.getString("brand"), resultSet.getString("reg_number")));
                }
                carsTable.setItems(carList);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadClientsFromDatabase() {
        ObservableList<Client> clientList = FXCollections.observableArrayList();
        String url = "jdbc:mysql://localhost:3306/carrent";
        String user = "root";
        String dbPassword = "";
        String query = "SELECT id_client, CONCAT(last_name, ' ', first_name) AS fio, gender, phone FROM client";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(url, user, dbPassword);
                 PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    clientList.add(new Client(resultSet.getInt("id_client"), resultSet.getString("fio"), resultSet.getString("gender"), resultSet.getString("phone")));
                }
                clientsTable.setItems(clientList);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadTripsFromDatabase() {
        ObservableList<Trip> tripList = FXCollections.observableArrayList();
        String url = "jdbc:mysql://localhost:3306/carrent";
        String user = "root";
        String dbPassword = "";
        String query = "SELECT t.id_trip, CONCAT(cl.last_name, ' ', cl.first_name) AS client_fio, cm.brand, t.duration_days, t.trip_date, (cm.price_per_day * t.duration_days) AS total_price FROM trip t INNER JOIN client cl ON t.id_client = cl.id_client INNER JOIN car c ON t.id_car = c.id_car INNER JOIN car_model cm ON c.id_model = cm.id_model";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(url, user, dbPassword);
                 PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    tripList.add(new Trip(resultSet.getInt("id_trip"), resultSet.getString("client_fio"), resultSet.getString("brand"), resultSet.getInt("duration_days"), resultSet.getString("trip_date"), resultSet.getDouble("total_price")));
                }
                tripsTable.setItems(tripList);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}