package com.example.kursovaya3;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AddCarController {

    @FXML
    private ComboBox<CarModel> modelComboBox;

    @FXML
    private TextField numberField;

    @FXML
    public void initialize() {
        loadCarModels();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadCarModels() {
        String query = "SELECT id_model, brand, price_per_day FROM car_model";
        ObservableList<CarModel> models = FXCollections.observableArrayList();

        Connection connection = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
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
            showErrorAlert("Ошибка", "Ошибка загрузки моделей машин для ComboBox");
        }
    }

    @FXML
    private void onSaveClick() {
        CarModel selectedModel = modelComboBox.getValue();
        String number = numberField.getText();

        if (selectedModel == null || number.trim().isEmpty()) {
            showErrorAlert("Внимание", "Заполните все поля!");
            return;
        }

        String query = "INSERT INTO car (id_model, reg_number) VALUES (?, ?)";
        Connection connection = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, selectedModel.getIdModel());
            preparedStatement.setString(2, number.trim());

            preparedStatement.executeUpdate();

            Stage stage = (Stage) numberField.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Ошибка", "Ошибка при сохранении автомобиля в базу данных!");
        }
    }
}