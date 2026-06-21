package com.example.kursovaya3;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ChangeAuthController {

    @FXML private TextField newLoginField;
    @FXML private TextField newPhoneField; // Текстовое поле для телефона
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;

    private int currentUserId = 0;

    public void setUserId(int idUser) {
        this.currentUserId = idUser;
        loadCurrentUserData();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadCurrentUserData() {
        String query = "SELECT u.login, cl.phone FROM user u " +
                "LEFT JOIN client cl ON u.id_client = cl.id_client " +
                "WHERE u.id_user = ?";

        Connection connection = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, currentUserId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    newLoginField.setText(rs.getString("login"));
                    String currentPhone = rs.getString("phone");
                    if (currentPhone != null) {
                        newPhoneField.setText(currentPhone);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onChangeAuthClick() {
        String newLogin = newLoginField.getText().trim();
        String newPhone = newPhoneField.getText().trim();
        String oldPassword = oldPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmNewPassword = confirmNewPasswordField.getText().trim();

        if (newLogin.isEmpty() || oldPassword.isEmpty() || newPhone.isEmpty()) {
            showErrorAlert("Ошибка: Логин, телефон и текущий пароль обязательны!");
            return;
        }
        if (!checkOldPassword(oldPassword)) {
            showErrorAlert("Ошибка: Текущий пароль введен неверно!");
            return;
        }

        boolean isPasswordChanged = !newPassword.isEmpty();

        String updateQueryUser;
        String updateClientQuery = "UPDATE client SET phone = ? WHERE id_client = (SELECT id_client FROM user WHERE id_user = ?)";

        if (isPasswordChanged) {
            if (!newPassword.equals(confirmNewPassword)) {
                showErrorAlert("Ошибка: Новые пароли не совпадают!");
                return;
            }

            String passwordRegex = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$";
            if (!newPassword.matches(passwordRegex)) {
                showErrorAlert("Ошибка: Пароль должен быть от 8 символов и содержать цифру, заглавную букву и спецсимвол!");
                return;
            }

            updateQueryUser = "UPDATE user SET login = ?, password_hash = ? WHERE id_user = ?";
        } else {
            updateQueryUser = "UPDATE user SET login = ? WHERE id_user = ?";
        }

        Connection connection = DatabaseManager.getInstance().getConnection();

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement clientStmt = connection.prepareStatement(updateClientQuery)) {
                clientStmt.setString(1, newPhone);
                clientStmt.setInt(2, currentUserId);
                clientStmt.executeUpdate();
            }

            try (PreparedStatement userStmt = connection.prepareStatement(updateQueryUser)) {
                userStmt.setString(1, newLogin);
                if (isPasswordChanged) {
                    userStmt.setString(2, hashPasswordSHA256(newPassword));
                    userStmt.setInt(3, currentUserId);
                } else {
                    userStmt.setInt(2, currentUserId);
                }
                userStmt.executeUpdate();
            }

            connection.commit();
            System.out.println("Профиль успешно обновлен!");

            Stage stage = (Stage) newLoginField.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            try { connection.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            showErrorAlert("Ошибка сохранения: Возможно, этот логин уже кем-то занят.");
        } finally {
            try { connection.setAutoCommit(true); } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    private boolean checkOldPassword(String oldPassword) {
        String query = "SELECT password_hash FROM user WHERE id_user = ?";
        Connection connection = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
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