package ba.unsa.etf.rpr.controllers;

import ba.unsa.etf.rpr.DAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;

public class LoginController {
    public TextField username;
    public PasswordField password;
    public Label message;
    public ImageView grb;

    private DAO DB;

    public void logIn(ActionEvent actionEvent) {
        String usernameHash = SHA256(username.getText());
        String passwordHash = SHA256(password.getText());

        DB = DAO.getInstance();

        Boolean authenticationResponse = DB.authenticate(usernameHash, passwordHash);
        if(authenticationResponse == null) {
            message.setText("Pogrešni pristupni podaci! Pokušajte ponovo.");
            message.getStyleClass().add("invalidField");
        }
        else if(authenticationResponse.equals(true)) {
            // upravnik
        }
        else if(authenticationResponse.equals(false)) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/inspector_main_menu.fxml"));
                Stage stage = new Stage();
                stage.setTitle("Inspekcijska Kontrola");
                stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
                ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public void resetPassField(MouseEvent mouseEvent) {
        if(!password.getText().isEmpty()) password.setText("");
    }
}
