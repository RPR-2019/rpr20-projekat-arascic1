package ba.unsa.etf.rpr;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import javax.xml.transform.Result;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoginController {
    public TextField username;
    public PasswordField password;
    public Label message;

    private DAO DB = DAO.getInstance();

    public void logIn(ActionEvent actionEvent) {
        String usernameHash = SHA256(username.getText());
        String passwordHash = SHA256(password.getText());

        if(DB.authenticate(usernameHash, passwordHash)) {
            // login successful
        }
        else {
            message.setText("Pogrešni pristupni podaci! Pokušajte ponovo.");
            message.getStyleClass().add("poljeNijeIspravno");
        }
    }

    private String SHA256(String msg) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(msg.getBytes("UTF-8"));
            final StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                final String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public void resetPassField(MouseEvent mouseEvent) {
        if(!password.getText().isEmpty() && message.getText().contains("Pogrešni")) password.setText("");
    }
}
