package com.example.kursovaya3;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
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
    protected void onLoginButtonClick() throws ClassNotFoundException {
        String login = loginField.getText();
        String password = passwordField.getText();

        String userRole = checkLoginInDatabase(login, password);

        if (userRole != null) {
            openNewWindow(userRole); // Передаем полученную роль дальше
        } else {
            showErrorAlert("Ошибка", "Неверный логин или пароль!");
        }
    }

    private String checkLoginInDatabase(String login, String password) throws ClassNotFoundException {
        String url = "jdbc:mysql://localhost:3306/carrent";
        String user = "root";
        String dbPassword = "";

        String hashedPassword = hashPasswordSHA256(password);
        String query = "SELECT role FROM user WHERE login = ? AND password_hash = ?";
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(url, user, dbPassword);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, login);
            preparedStatement.setString(2, hashedPassword);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("role"); // Возвращаем роль из БД
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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

    private void openNewWindow(String role) {
        try {
            Stage currentStage = (Stage) loginField.getScene().getWindow();
            currentStage.close();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainMenu.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1000, 600);

            // Получаем контроллер СТРОГО после fxmlLoader.load()
            MainMenuWindowControllert mainMenuController = fxmlLoader.getController();
            if (mainMenuController != null) {
                mainMenuController.setRole(role); // Вызываем наш тестовый метод
            }

            Stage newStage = new Stage();
            newStage.setTitle("Главное меню");
            newStage.setScene(scene);
            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Ошибка", "Не удалось загрузить главное окно приложения!");
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}