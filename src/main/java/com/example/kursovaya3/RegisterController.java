package com.example.kursovaya3;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class RegisterController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> genderComboBox;

    @FXML
    public void initialize() {
        genderComboBox.setItems(FXCollections.observableArrayList("Мужской", "Женский"));
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onRegisterClick() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String phone = phoneField.getText().trim();
        String gender = genderComboBox.getValue();

        if (login.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty() || gender == null) {
            showErrorAlert("Ошибка: Заполните все поля!");
            return;
        }

        String passwordRegex = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$";
        if (!password.matches(passwordRegex)) {
            showErrorAlert("Ошибка: Пароль слишком простой! Требуется: не менее 8 символов, минимум одна цифра, одна заглавная буква и один спецсимвол (!@#$%^&*).");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showErrorAlert("Ошибка: Пароли не совпадают!");
            return;
        }

        if (isLoginExists(login)) {
            showErrorAlert("Ошибка: Логин занят!");
        } else {
            registerNewUserWithClient(login, hashPasswordSHA256(password), phone, gender);
        }
    }

    private boolean isLoginExists(String login) {
        String query = "SELECT COUNT(*) FROM user WHERE login = ?";
        Connection connection = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, login);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void registerNewUserWithClient(String login, String hashedPassword, String phone, String gender) {
        String insertClientQuery = "INSERT INTO client (last_name, first_name, gender, phone) VALUES (?, '', ?, ?)";
        String insertUserQuery = "INSERT INTO user (login, password_hash, role, id_client) VALUES (?, ?, 'user', ?)";

        Connection connection = DatabaseManager.getInstance().getConnection();
        try {
            connection.setAutoCommit(false);

            int generatedClientId = -1;

            try (PreparedStatement clientStmt = connection.prepareStatement(insertClientQuery, Statement.RETURN_GENERATED_KEYS)) {
                clientStmt.setString(1, login);
                clientStmt.setString(2, gender);
                clientStmt.setString(3, phone);
                clientStmt.executeUpdate();

                try (ResultSet generatedKeys = clientStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedClientId = generatedKeys.getInt(1);
                    }
                }
            }

            if (generatedClientId == -1) {
                throw new Exception("Не удалось получить сгенерированный ID клиента.");
            }

            try (PreparedStatement userStmt = connection.prepareStatement(insertUserQuery)) {
                userStmt.setString(1, login);
                userStmt.setString(2, hashedPassword);
                userStmt.setInt(3, generatedClientId);
                userStmt.executeUpdate();
            }

            connection.commit();

            Stage stage = (Stage) loginField.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            try { connection.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            showErrorAlert("Ошибка при комплексной транзакционной регистрации пользователя.");
        } finally {
            try { connection.setAutoCommit(true); } catch (Exception ex) { ex.printStackTrace(); }
        }
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