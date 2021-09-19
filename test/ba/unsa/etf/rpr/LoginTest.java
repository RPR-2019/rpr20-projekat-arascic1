package ba.unsa.etf.rpr;
import ba.unsa.etf.rpr.controllers.LoginController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

@ExtendWith(ApplicationExtension.class)
class LoginTest {
    Stage stage;
    LoginController loginCtrl;

    @Start
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        loginCtrl = loader.getController();
        stage.setTitle("Inspekcijska Kontrola");
        stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
        stage.setResizable(false);
        stage.show();

        stage.toFront();

        this.stage = stage;
    }

    @Test
    public void testFailedLogin(FxRobot robot) {
        robot.clickOn("#username");
        robot.write("non-existent username");
        robot.clickOn("#password");
        robot.write("password");
        robot.clickOn("#btnLogin");

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Label message = robot.lookup("#message").queryAs(Label.class);
        PasswordField passwordField = robot.lookup("#password").queryAs(PasswordField.class);

        assertEquals("Pogrešni pristupni podaci! Pokušajte ponovo.", message.getText());

        robot.clickOn("#btnLogin");
        assertEquals("Pokušajte ponovo.", message.getText());

        robot.clickOn("#password");
        assertEquals("", passwordField.getText());

        assertTrue(message.getStyleClass().stream().anyMatch(s -> s.equals("invalidField")));
    }

    @Test
    public void testSucessfulLogin(FxRobot robot) {
        robot.clickOn("#username");
        robot.write("user1");
        robot.clickOn("#password");
        robot.write("123456");
        robot.clickOn("#btnLogin");

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Label message = robot.lookup("#message").queryAs(Label.class);

        assertEquals("Autentifikacija uspješna - podaci se učitavaju", message.getText());
        assertTrue(message.getStyleClass().stream().anyMatch(s -> s.equals("beforeDeadline")));
        assertTrue(message.getStyleClass().stream().noneMatch(s -> s.equals("invalidField")));
        assertTrue(loginCtrl.loading.getText().contains("loading"));
        assertTrue(loginCtrl.btnLogin.isDisabled());
    }

    @AfterEach
    private void restart() {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}