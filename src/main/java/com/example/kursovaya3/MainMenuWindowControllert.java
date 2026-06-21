package com.example.kursovaya3;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MainMenuWindowControllert {

    @FXML
    private TableView<Car> carsTable;
    @FXML
    private TableColumn<Car, Integer> idColumn;
    @FXML
    private TableColumn<Car, String> modelColumn;
    @FXML
    private TableColumn<Car, String> regNumberColumn;

    @FXML
    private TableView<Client> clientsTable;
    @FXML
    private TableColumn<Client, Integer> clientIdColumn;
    @FXML
    private TableColumn<Client, String> clientNameColumn;
    @FXML
    private TableColumn<Client, String> clientPhoneColumn;
    @FXML
    private TableColumn<Client, String> clientGenderColumn;

    @FXML
    private TableView<Trip> tripsTable;
    @FXML
    private TableColumn<Trip, Integer> tripIdColumn;
    @FXML
    private TableColumn<Trip, String> tripClientColumn;
    @FXML
    private TableColumn<Trip, String> tripCarColumn;
    @FXML
    private TableColumn<Trip, Integer> tripDurationColumn;
    @FXML
    private TableColumn<Trip, String> tripDateColumn;
    @FXML
    private TableColumn<Trip, Double> tripPriceColumn;

    @FXML private Button carClick;
    @FXML private Button TripClick;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idCar"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        regNumberColumn.setCellValueFactory(new PropertyValueFactory<>("regNumber"));
        loadCarsFromDatabase();

        clientIdColumn.setCellValueFactory(new PropertyValueFactory<>("idClient"));
        clientNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        clientPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        clientGenderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        loadClientsFromDatabase();

        tripIdColumn.setCellValueFactory(new PropertyValueFactory<>("idTrip"));
        tripClientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        tripCarColumn.setCellValueFactory(new PropertyValueFactory<>("carBrand"));
        tripDurationColumn.setCellValueFactory(new PropertyValueFactory<>("durationDays"));
        tripDateColumn.setCellValueFactory(new PropertyValueFactory<>("tripDate"));
        tripPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        loadTripsFromDatabase();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadCarsFromDatabase() {
        ObservableList<Car> carList = FXCollections.observableArrayList();
        String query = "SELECT car.id_car, car_model.brand, car.reg_number FROM car INNER JOIN car_model ON car.id_model = car_model.id_model";

        Connection connection = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                carList.add(new Car(resultSet.getInt("id_car"), resultSet.getString("brand"), resultSet.getString("reg_number")));
            }
            carsTable.setItems(carList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadClientsFromDatabase() {
        ObservableList<Client> clientList = FXCollections.observableArrayList();
        String query = "SELECT id_client, CONCAT(last_name, ' ', first_name) AS fio, gender, phone FROM client";

        Connection connection = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                clientList.add(new Client(resultSet.getInt("id_client"), resultSet.getString("fio"), resultSet.getString("gender"), resultSet.getString("phone")));
            }
            clientsTable.setItems(clientList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTripsFromDatabase() {
        ObservableList<Trip> tripList = FXCollections.observableArrayList();
        String query = "SELECT t.id_trip, " +
                "CONCAT(cl.last_name, ' ', cl.first_name) AS client_fio, " +
                "cm.brand, " +
                "t.duration_days, " +
                "t.trip_date, " +
                "((cm.price_per_day * t.duration_days) + IFNULL((SELECT SUM(ft.cost) FROM fine f INNER JOIN fine_type ft ON f.id_fine_type = ft.id_fine_type WHERE f.id_trip = t.id_trip), 0)) AS total_price " +
                "FROM trip t " +
                "INNER JOIN client cl ON t.id_client = cl.id_client " +
                "INNER JOIN car c ON t.id_car = c.id_car " +
                "INNER JOIN car_model cm ON c.id_model = cm.id_model";

        Connection connection = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                tripList.add(new Trip(
                        resultSet.getInt("id_trip"),
                        resultSet.getString("client_fio"),
                        resultSet.getString("brand"),
                        resultSet.getInt("duration_days"),
                        resultSet.getString("trip_date"),
                        resultSet.getDouble("total_price")
                ));
            }
            tripsTable.setItems(tripList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRole(String role) {
        if (!"admin".equalsIgnoreCase(role)) {
            if (carClick != null) {
                carClick.setVisible(false);
                carClick.setManaged(false);
            }
            if (TripClick != null) {
                TripClick.setVisible(false);
                TripClick.setManaged(false);
            }
        } else {
            if (carClick != null) {
                carClick.setVisible(true);
                carClick.setManaged(true);
                carClick.toFront();
            }
            if (TripClick != null) {
                TripClick.setVisible(true);
                TripClick.setManaged(true);
                TripClick.toFront();
            }
        }
    }

    @FXML
    private void onAddCarClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AddCar.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 300);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Новый автомобиль");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(carClick.getScene().getWindow());

            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            loadCarsFromDatabase();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void TripClick(javafx.event.ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AddTrip.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 450, 400);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Оформить прокат");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            dialogStage.initOwner(source.getScene().getWindow());

            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            loadTripsFromDatabase();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Ошибка", "Не удалось найти или открыть файл AddTrip.fxml!");
        }
    }

    @FXML
    private void onDeleteCarClick() {
        Car selectedCar = carsTable.getSelectionModel().getSelectedItem();

        if (selectedCar == null) {
            showErrorAlert("Ошибка", "Ошибка: Сначала выберите машину в таблице для удаления!");
            return;
        }

        String query = "DELETE FROM car WHERE id_car = ?";
        Connection connection = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, selectedCar.getIdCar());

            int rowsDeleted = preparedStatement.executeUpdate();
            if (rowsDeleted > 0) {
                loadCarsFromDatabase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onDeleteTripClick() {
        Trip selectedTrip = tripsTable.getSelectionModel().getSelectedItem();

        if (selectedTrip == null) {
            showErrorAlert("Ошибка", "Ошибка: Сначала выберите прокат в таблице для удаления!");
            return;
        }

        String query = "DELETE FROM trip WHERE id_trip = ?";
        Connection connection = DatabaseManager.getInstance().getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, selectedTrip.getIdTrip());

            int rowsDeleted = preparedStatement.executeUpdate();
            if (rowsDeleted > 0) {
                loadTripsFromDatabase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onOpenChangeAuthClick(javafx.event.ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ChangeAuth.fxml"));
            javafx.scene.Scene scene = new Scene(fxmlLoader.load(), 350, 300);

            ChangeAuthController controller = fxmlLoader.getController();
            int currentId = UserSession.getUserId();
            controller.setUserId(currentId);

            Stage stage = new Stage();
            stage.setTitle("Настройки");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            stage.initOwner(source.getScene().getWindow());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddFineClick(javafx.event.ActionEvent event) {
        Trip selectedTrip = tripsTable.getSelectionModel().getSelectedItem();

        if (selectedTrip == null) {
            showErrorAlert("Ошибка", "Сначала выберите прокат в таблице для начисления штрафа!");
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AddFine.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 250);

            AddFineController fineController = fxmlLoader.getController();
            fineController.setTripData(
                    selectedTrip.getIdTrip(),
                    selectedTrip.getClientName(),
                    selectedTrip.getCarBrand()
            );

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Начислить штраф");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            dialogStage.initOwner(source.getScene().getWindow());

            dialogStage.setScene(scene);
            dialogStage.showAndWait();

        } catch (IOException e) {
            showErrorAlert("Ошибка", "Не удалось открыть окно начисления штрафа");
            e.printStackTrace();
        }

        loadTripsFromDatabase();
    }
}