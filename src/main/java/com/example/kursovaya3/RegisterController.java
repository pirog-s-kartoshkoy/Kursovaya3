package com.example.kursovaya3;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class RegisterController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> genderComboBox;

    private final String url = "jdbc:mysql://localhost:3306/carrent";
    private final String user = "root";
    private final String dbPassword = "";

    @FXML
    public void initialize() {
        genderComboBox.setItems(FXCollections.observableArrayList("Мужской", "Женский"));
    }

    @FXML
    private void onRegisterClick() {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String phone = phoneField.getText().trim();
        String gender = genderComboBox.getValue();

        if (login.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty() || gender == null) {
            System.out.println("Ошибка: Заполните все поля и выберите пол!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            System.out.println("Ошибка: Пароли не совпадают!");
            return;
        }

        if (isLoginExists(login)) {
            System.out.println("Ошибка: Пользователь с таким логином уже существует!");
        } else {
            String hashedPassword = hashPasswordSHA256(password);
            registerNewUserWithClient(login, hashedPassword, phone, gender);
        }
    }

    private boolean isLoginExists(String login) {
        String query = "SELECT COUNT(*) FROM user WHERE login = ?";
        try (Connection connection = DriverManager.getConnection(url, user, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

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

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, user, dbPassword);
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
            if (connection != null) {
                try { connection.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            System.err.println("Ошибка при комплексной регистрации пользователя.");
        } finally {
            if (connection != null) {
                try { connection.close(); } catch (Exception ex) { ex.printStackTrace(); }
            }
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