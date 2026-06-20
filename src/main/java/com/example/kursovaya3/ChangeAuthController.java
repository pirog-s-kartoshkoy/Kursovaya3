package com.example.kursovaya3;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ChangeAuthController {

    @FXML private TextField newLoginField;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;

    private int currentUserId = 0; // Изначально 0
    private final String url = "jdbc:mysql://localhost:3306/carrent";
    private final String user = "root";
    private final String dbPassword = "";

    // Метод для передачи ID пользователя из личного кабинета
    public void setUserId(int idUser) {
        this.currentUserId = idUser;
        loadCurrentLogin();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadCurrentLogin() {
        String query = "SELECT login FROM user WHERE id_user = ?";
        try (Connection connection = DriverManager.getConnection(url, user, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, currentUserId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    newLoginField.setText(rs.getString("login"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onChangeAuthClick() {
        String newLogin = newLoginField.getText().trim();
        String oldPassword = oldPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmNewPassword = confirmNewPasswordField.getText().trim();


        if (newLogin.isEmpty() || oldPassword.isEmpty()) {
            showErrorAlert("Ошибка: Логин и текущий пароль обязательны!");
            return;
        }

        // Проверяем текущий пароль
        if (!checkOldPassword(oldPassword)) {
            showErrorAlert("Ошибка: Текущий пароль введен неверно!");
            return;
        }

        boolean isPasswordChanged = !newPassword.isEmpty();
        String updateQuery;

        if (isPasswordChanged) {
            if (!newPassword.equals(confirmNewPassword)) {
                showErrorAlert("Ошибка: Новые пароли не совпадают!");
                return;
            }

            String passwordRegex = "^(?=.*[0-9])(?=.*[A-Z]).{6,}$";
            if (!newPassword.matches(passwordRegex)) {
                showErrorAlert("Ошибка: Новый пароль не соответствует требованиям (от 6 символов, минимум 1 цифра и 1 заглавная буква).");
                return;
            }

            updateQuery = "UPDATE user SET login = ?, password_hash = ? WHERE id_user = ?";
        } else {
            updateQuery = "UPDATE user SET login = ? WHERE id_user = ?";
        }

        try (Connection connection = DriverManager.getConnection(url, user, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

            preparedStatement.setString(1, newLogin);

            if (isPasswordChanged) {
                preparedStatement.setString(2, hashPasswordSHA256(newPassword));
                preparedStatement.setInt(3, currentUserId);
            } else {
                preparedStatement.setInt(2, currentUserId);
            }

            int rows = preparedStatement.executeUpdate();
            if (rows > 0) {
                System.out.println("Данные успешно изменены в БД!");
                Stage stage = (Stage) newLoginField.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Ошибка при сохранении данных в базу. Возможно, логин уже занят.");
        }
    }

    private boolean checkOldPassword(String oldPassword) {
        String query = "SELECT password_hash FROM user WHERE id_user = ?";
        try (Connection connection = DriverManager.getConnection(url, user, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, currentUserId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    String dbHash = rs.getString("password_hash");
                    String inputHash = hashPasswordSHA256(oldPassword);

                    return dbHash.equalsIgnoreCase(inputHash);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String hashPasswordSHA256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}