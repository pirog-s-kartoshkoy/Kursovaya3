package com.example.kursovaya3;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AddFineController {

    @FXML private Label tripInfoLabel;
    @FXML private ComboBox<FineType> fineTypeComboBox;

    private int currentTripId;

    public void setTripData(int idTrip, String clientName, String carBrand) {
        this.currentTripId = idTrip;
        tripInfoLabel.setText("Начисление штрафа");
        loadFineTypes();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadFineTypes() {
        ObservableList<FineType> types = FXCollections.observableArrayList();
        String query = "SELECT id_fine_type, name, cost FROM fine_type";

        Connection connection = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                types.add(new FineType(
                        resultSet.getInt("id_fine_type"),
                        resultSet.getString("name"),
                        resultSet.getDouble("cost")
                ));
            }
            fineTypeComboBox.setItems(types);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Критическая ошибка", "Ошибка загрузки типов штрафов!");
        }
    }

    @FXML
    private void onSaveFineClick() {
        FineType selectedType = fineTypeComboBox.getValue();

        if (selectedType == null) {
            showErrorAlert("Ошибка", "Ошибка: Выберите тип штрафа из списка!");
            return;
        }

        String checkQuery = "SELECT COUNT(*) FROM fine WHERE id_trip = ? AND id_fine_type = ?";
        String insertFineQuery = "INSERT INTO fine (id_trip, id_fine_type) VALUES (?, ?)";

        Connection connection = DatabaseManager.getInstance().getConnection();

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, currentTripId);
                checkStmt.setInt(2, selectedType.getIdFineType());

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        showErrorAlert("Ошибка", "Ошибка: Этот штраф уже выписан!");
                        connection.rollback();
                        return;
                    }
                }
            }

            try (PreparedStatement insertStmt = connection.prepareStatement(insertFineQuery)) {
                insertStmt.setInt(1, currentTripId);
                insertStmt.setInt(2, selectedType.getIdFineType());
                insertStmt.executeUpdate();
            }

            connection.commit();

            Stage stage = (Stage) fineTypeComboBox.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            try { connection.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            showErrorAlert("Ошибка", "Ошибка при начислении штрафа!");
        } finally {
            try { connection.setAutoCommit(true); } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}