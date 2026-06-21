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
import java.time.LocalDate;

public class AddTripController {

    @FXML private ComboBox<Client> clientComboBox;
    @FXML private ComboBox<CarComboItem> carComboBox;
    @FXML private TextField durationField;

    @FXML
    public void initialize() {
        loadClients();
        loadCars();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadClients() {
        ObservableList<Client> clientList = FXCollections.observableArrayList();
        String query = "SELECT id_client, CONCAT(last_name, ' ', first_name) AS fio, gender, phone FROM client";

        Connection connection = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                clientList.add(new Client(
                        resultSet.getInt("id_client"),
                        resultSet.getString("fio"),
                        resultSet.getString("gender"),
                        resultSet.getString("phone")
                ));
            }
            clientComboBox.setItems(clientList);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Ошибка", "Не удалось загрузить список клиентов!");
        }
    }

    private void loadCars() {
        ObservableList<CarComboItem> carList = FXCollections.observableArrayList();
        String query = "SELECT c.id_car, cm.brand, c.reg_number FROM car c INNER JOIN car_model cm ON c.id_model = cm.id_model";

        Connection connection = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                carList.add(new CarComboItem(
                        resultSet.getInt("id_car"),
                        resultSet.getString("brand"),
                        resultSet.getString("reg_number")
                ));
            }
            carComboBox.setItems(carList);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Ошибка", "Не удалось загрузить список автомобилей!");
        }
    }

    @FXML
    private void onSaveClick() {
        Client selectedClient = clientComboBox.getValue();
        CarComboItem selectedCar = carComboBox.getValue();
        String durationStr = durationField.getText();

        if (selectedClient == null || selectedCar == null || durationStr.trim().isEmpty()) {
            showErrorAlert("Ошибка", "Заполните все поля!");
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr.trim());
            LocalDate currentDate = LocalDate.now(); // Текущая дата проката

            String query = "INSERT INTO trip (id_client, id_car, duration_days, trip_date) VALUES (?, ?, ?, ?)";
            Connection connection = DatabaseManager.getInstance().getConnection();

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, selectedClient.getIdClient());
                preparedStatement.setInt(2, selectedCar.getIdCar());
                preparedStatement.setInt(3, duration);
                preparedStatement.setString(4, currentDate.toString()); // Формат YYYY-MM-DD

                preparedStatement.executeUpdate();

                Stage stage = (Stage) durationField.getScene().getWindow();
                stage.close();
            }

        } catch (NumberFormatException e) {
            showErrorAlert("Ошибка", "Дни проката должны быть числом!");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Ошибка", "Не удалось сохранить запись о прокате!");
        }
    }
}