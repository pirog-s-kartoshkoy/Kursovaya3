package com.example.kursovaya3;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AddCarController {

    @FXML
    private ComboBox<CarModel> modelComboBox; // Наш раскрывающийся список

    @FXML
    private TextField numberField;

    // Метод initialize вызывается автоматически при загрузке FXML
    @FXML
    public void initialize() {
        loadCarModels();
    }

    // Загружаем модели из БД в ComboBox
    private void loadCarModels() {
        String url = "jdbc:mysql://localhost:3306/carrent";
        String user = "root";
        String dbPassword = "";
        String query = "SELECT id_model, brand, price_per_day FROM car_model";

        ObservableList<CarModel> models = FXCollections.observableArrayList();

        try (Connection connection = DriverManager.getConnection(url, user, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                models.add(new CarModel(
                        resultSet.getInt("id_model"),
                        resultSet.getString("brand"),
                        resultSet.getDouble("price_per_day")
                ));
            }
            modelComboBox.setItems(models);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ошибка загрузки моделей машин для ComboBox");
        }
    }

    @FXML
    private void onSaveClick() {
        CarModel selectedModel = modelComboBox.getValue();
        String number = numberField.getText();

        if (selectedModel == null || number.trim().isEmpty()) {
            System.out.println("Заполните все поля!");
            return;
        }

        // ТЕСТОВЫЙ ВЫВОД: Проверяем, что всё подцепилось правильно
        System.out.println("ID выбранной модели: " + selectedModel.getIdModel());
        System.out.println("Марка: " + selectedModel.getBrand());
        System.out.println("Гос. номер: " + number);

        // Закрываем окно
        Stage stage = (Stage) numberField.getScene().getWindow();
        stage.close();
    }
}