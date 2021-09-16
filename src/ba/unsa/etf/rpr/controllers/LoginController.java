package ba.unsa.etf.rpr.controllers;

import ba.unsa.etf.rpr.DAO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.Semaphore;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;

public class LoginController {
    public TextField username;
    public PasswordField password;
    public Label message;
    public Button btnLogin;
    public Label loading;

    private DAO DB;

    private void successfulAuthentication(Thread loadingMessage) {
        message.getStyleClass().removeAll("invalidField");
        message.getStyleClass().addAll("beforeDeadline");
        message.setText("Autentifikacija uspješna - podaci se učitavaju");
        loadingMessage.start();
        btnLogin.setDisable(true);
    }

    public void logIn(ActionEvent actionEvent) {
        String usernameHash = SHA256(username.getText());
        String passwordHash = SHA256(password.getText());

        DB = DAO.getInstance();
        Boolean authenticationResponse = DB.authenticate(usernameHash, passwordHash);
        loading.setVisible(true);
        Semaphore semaphore = new Semaphore(1);

        Thread loadingMessage = new Thread(() -> { synchronized (this) {
            while(semaphore.availablePermits() == 1);
            int counter = 0;
            do {
                counter++;
                StringBuilder dots = new StringBuilder();
                dots.append(".".repeat(Math.max(0, counter % 10)));
                try {
                    Platform.runLater(() -> loading.setText("loading" + dots.toString()));
                    wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (semaphore.availablePermits() == 0);
            Platform.runLater(() -> loading.setText(""));
        }});

        Thread authenticationProcess = new Thread(() -> {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (authenticationResponse == null) {
                Platform.runLater(() -> {
                    if(!message.getText().contains("podaci"))
                        message.setText("Pogrešni pristupni podaci! Pokušajte ponovo.");
                    else message.setText("Pokušajte ponovo.");
                    message.getStyleClass().add("invalidField");
                });
            }
            else if (authenticationResponse.equals(true)) {
                Platform.runLater(() -> {
                    successfulAuthentication(loadingMessage);
                });
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manager_main_menu.fxml"));
                    Parent root = loader.load();
                    Platform.runLater(() -> {
                        Stage stage = new Stage();
                        stage.setTitle("Inspekcijska Kontrola");
                        stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
                        stage.setOnCloseRequest(i -> {
                            try {
                                loader.<ManagerController>getController().notifyWindowClosing();
                            } catch (RuntimeException e) {
                                i.consume();
                            }
                        });
                        ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
                        stage.show();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (authenticationResponse.equals(false)) {
                Platform.runLater(() -> {
                    successfulAuthentication(loadingMessage);
                });
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/inspector_main_menu.fxml"));
                    Parent root = loader.load();

                    Platform.runLater(() -> {
                        Stage stage = new Stage();
                        stage.setTitle("Inspekcijska Kontrola");
                        stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
                        stage.setOnCloseRequest(i -> {
                            try {
                                loader.<InspectorController>getController().notifyWindowClosing();
                            } catch (RuntimeException e) {
                                i.consume();
                            }
                        });
                        ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
                        stage.show();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            semaphore.release();
        });

        Platform.runLater(authenticationProcess::start);
    }

    private String SHA256(String msg) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(msg.getBytes(StandardCharsets.UTF_8));
            final StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                final String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void resetPassField(MouseEvent mouseEvent) {
        if(!password.getText().isEmpty()) password.setText("");
    }
}