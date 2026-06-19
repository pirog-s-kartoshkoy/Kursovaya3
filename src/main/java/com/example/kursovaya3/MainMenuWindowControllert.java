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

    @FXML
    private TableView<Car> carsTable;

    @FXML
    private TableColumn<Car, Integer> idColumn;

    @FXML
    private TableColumn<Car, String> modelColumn; // Эта колонка теперь будет выводить бренд

    @FXML
    private TableColumn<Car, String> regNumberColumn;

    @FXML
    public void initialize() {
        // Связываем колонки таблицы с геттерами обновленного класса Car
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idCar"));       // Ищет getIdCar()
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));     // Ищет getBrand() вместо getIdModel()
        regNumberColumn.setCellValueFactory(new PropertyValueFactory<>("regNumber")); // Ищет getRegNumber()

        loadCarsFromDatabase();
    }

    private void loadCarsFromDatabase() {
        ObservableList<Car> carList = FXCollections.observableArrayList();

        String url = "jdbc:mysql://localhost:3306/carrent";
        String user = "root";
        String dbPassword = "";

        // СВЯЗЫВАЕМ ТАБЛИЦЫ ЧЕРЕЗ INNER JOIN
        String query = "SELECT car.id_car, car_model.brand, car.reg_number " +
                "FROM car " +
                "INNER JOIN car_model ON car.id_model = car_model.id_model";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection connection = DriverManager.getConnection(url, user, dbPassword);
                 PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    // Достаем данные из результирующей выборки SQL
                    int idCar = resultSet.getInt("id_car");
                    String brand = resultSet.getString("brand"); // Из таблицы car_model
                    String regNumber = resultSet.getString("reg_number");

                    carList.add(new Car(idCar, brand, regNumber));
                }

                carsTable.setItems(carList);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}