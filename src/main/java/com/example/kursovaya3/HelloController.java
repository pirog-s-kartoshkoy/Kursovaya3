package com.example.kursovaya3;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HelloController {

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    protected void onLoginButtonClick() {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (checkLoginInDatabase(login, password)) {
            openNewWindow();
        } else {
            showErrorAlert("Ошибка", "Неверный логин или пароль!");
        }
    }

    private boolean checkLoginInDatabase(String login, String password) {
        String url = "jdbc:mysql://localhost:3306/carrent";
        String user = "root";
        String dbPassword = ""; // Оставь пустым "" или напиши "root" в зависимости от настроек Open Server

        // Хэшируем введенный пароль алгоритмом SHA-256
        String hashedPassword = hashPasswordSHA256(password);

        String query = "SELECT * FROM user WHERE login = ? AND password_hash = ?";

        try (Connection connection = DriverManager.getConnection(url, user, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, login);
            preparedStatement.setString(2, hashedPassword);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Если нашли пользователя с таким логином и хэшем — возвращаем true
            return resultSet.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //Это писал не я, нейронка постаралась
    private String hashPasswordSHA256(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashInBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void openNewWindow() {

    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
