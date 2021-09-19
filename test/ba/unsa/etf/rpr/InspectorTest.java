package ba.unsa.etf.rpr;

import ba.unsa.etf.rpr.controllers.InspectorController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
    NOTICE ::
        These are integration tests, they rely on each other.
        It is ONLY meaningful to run them together all at once.
 */

@ExtendWith(ApplicationExtension.class)
class InspectorTest {
    Stage stage;
    InspectorController inspector;

    @BeforeAll
    static void resetujBazu() throws SQLException {
        DAO.getInstance().resetDatabase();
    }

    @Start
    public void start(Stage stage) throws IOException {
        DAO.usernameHash = "0a041b9462caa4a31bac3567e0b6e6fd9100787db2ab433d96f6d178cabfce90";
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/inspector_main_menu.fxml"));
        Parent root = loader.load();
        inspector = loader.getController();
        stage.setTitle("Inspekcijska Kontrola");
        stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
        stage.setResizable(false);
        stage.show();

        stage.toFront();

        this.stage = stage;
    }

    @Test
    public void test1(FxRobot robot) {
        robot.clickOn((Node) robot.lookup("#button").nth(3).query());
        waitForWindow();

        //noinspection ResultOfMethodCallIgnored
        robot.lookup("#OKCheckBox").tryQuery().isPresent();
        robot.clickOn("#OKCheckBox");
        waitForWindow();

        /*
            For UNKNOWN REASONS, TestFX here changes the ordering of the query items from top-down to bottom-up.
            Therefore the correct item to be tested has index 1 instead of 3.
            There is a total of 5 items displayed on default scrollball position.
        */

        assertTrue(robot.lookup("#name").nth(1).queryAs(Text.class).getStyleClass().contains("strikethrough"));
        assertTrue(robot.lookup("#button").nth(1).queryAs(RadioButton.class).isSelected());

        // POTENTIAL DATABASE LOCK - release the file from other processes.
        robot.clickOn((Node) robot.lookup("#btnSave").query());
    }

    @Test
    public void test2(FxRobot robot) {
        // Mutation persistence check
        assertTrue(robot.lookup("#name").nth(3).queryAs(Text.class).getStyleClass().contains("strikethrough"));
        assertTrue(robot.lookup("#button").nth(3).queryAs(RadioButton.class).isSelected());

        robot.clickOn((Node) robot.lookup("#button").nth(3).query());
        waitForWindow();

        //noinspection ResultOfMethodCallIgnored
        robot.lookup("#OKCheckBox").tryQuery().isPresent();
        robot.clickOn("#deleteBtn");
        waitForWindow();

        robot.press(KeyCode.ENTER); robot.release(KeyCode.ENTER);

        robot.clickOn((Node) robot.lookup("#button").nth(0).query());

        Semaphore semaphore = new Semaphore(1);
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> {
            robot.lookup("#deadlinePicker").queryAs(DatePicker.class).setValue(LocalDate.now().plusDays(1));
            robot.lookup("#penaltyAmount").queryAs(Spinner.class).getEditor().setText("100");
            robot.lookup("#penaltyAmount").queryAs(Spinner.class).commitValue();
            robot.lookup("#deadlinePenalty").queryAs(Spinner.class).getEditor().setText("1000");
            robot.lookup("#deadlinePenalty").queryAs(Spinner.class).commitValue();

            semaphore.release();
        });

        while(semaphore.availablePermits() != 1) waitForWindow();
        robot.clickOn("#ok");
        waitForWindow();
        robot.clickOn("#btnSave");
    }

    @Test
    public void test3(FxRobot robot) throws TimeoutException {
        robot.clickOn((Node) robot.lookup("#button").nth(4).query());
        waitForWindow();

        assertEquals(String.valueOf(inspector.selectionDate.getDayOfMonth() + 1),
            robot.lookup("#deadlinePicker").queryAs(DatePicker.class).getEditor().getText().substring(0, 2)
        );

        assertEquals("100 KM",
            robot.lookup("#penaltyAmount").queryAs(Spinner.class).getEditor().getText()
        );

        assertEquals("1000 KM",
            robot.lookup("#deadlinePenalty").queryAs(Spinner.class).getEditor().getText()
        );

        FxToolkit.cleanupStages();
    }

    private void waitForWindow() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}