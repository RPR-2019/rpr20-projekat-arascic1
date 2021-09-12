package ba.unsa.etf.rpr.controllers;

import ba.unsa.etf.rpr.DAO;
import ba.unsa.etf.rpr.models.Inspection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.stream.Collectors;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;

public class InspectionCellController extends ListCell<Inspection> {
    @FXML
    public RadioButton button;
    @FXML
    public Text name;
    @FXML
    public Label address;
    @FXML
    public BorderPane root;
    @FXML
    public Label deadline;
    @FXML
    public Separator separator;

    public InspectorController parentController;

    public InspectionCellController() { loadFXML(); }

    public void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/inspection_cell.fxml"));
            loader.setController(this);
            loader.load();
            button.setOnAction(this::handle);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(Inspection item, boolean empty) {
        super.updateItem(item, empty);
        if(empty || item == null) {
            setText(null);
            setGraphic(null);
        }
        else {
            name.setText(item.getAddressedTo().getName());
            address.setText(item.getAddressedTo().getAddress());

            if(item.getDeadline().before(DAO.convertToDateViaInstant(parentController.selectionDate))) {
                deadline.setText(DAO.dateToString(item.getDeadline()));
                separator.setVisible(true);
                deadline.getStyleClass().add("invalidField");
            }
            else {
                separator.setVisible(false);
                deadline.setText("");
            }

            if(item.getIssuedAt() != null &&
                    item.getIssuedAt().equals(DAO.convertToDateViaInstant(parentController.selectionDate))) {
                name.getStyleClass().add("strikethrough");

                if(!button.isSelected()) {
                    button.setSelected(true);
                }

                if(item.getIssuedAt().before(item.getDeadline())) {
                    separator.setVisible(true);
                    deadline.setText(DAO.dateToString(item.getDeadline()));
                    deadline.getStyleClass().add("beforeDeadline");
                }
                else {
                    separator.setVisible(false);
                    deadline.setText("");
                    deadline.getStyleClass().removeAll("beforeDeadline");
                }
            }
            else {
                name.getStyleClass().removeAll("strikethrough");
                if(button.isSelected()) {
                    button.setSelected(false);
                }
            }

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setGraphic(root);
        }
    }

    public InspectorController getParentController() {
        return parentController;
    }

    public void setParentController(InspectorController parentController) {
        this.parentController = parentController;
    }

    private void handle(ActionEvent i) {
        /*
        if (button.selectedProperty().get()) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/inspection_form.fxml"));
                PenaltyFormController controller = new PenaltyFormController();
                controller.setInspection(
                    parentController.list.getItems().stream()
                    .filter(q -> q.getAddressedTo().getName().equals(name.getText()))
                    .collect(Collectors.toList()).get(0)
                );
                controller.setParentController(this);
                fxmlLoader.setController(controller);
                Parent root = fxmlLoader.load();

                getParentController().changeLog.add(this.getItem());

                Stage stage = new Stage();
                stage.setTitle(name.getText());
                stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));

                stage.setOnHiding(event -> {
                    if (button.isSelected()) button.setSelected(false);
                });

                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // button.selectedProperty().setValue(true);
        }
        */
        try {
            button.setSelected(!button.isSelected());

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/inspection_form.fxml"));
            PenaltyFormController controller = new PenaltyFormController();
            controller.setInspection(
                parentController.list.getItems().stream()
                    .filter(q -> q.getAddressedTo().getName().equals(name.getText()))
                    .collect(Collectors.toList()).get(0)
            );
            controller.setParentController(this);
            fxmlLoader.setController(controller);
            Parent root = fxmlLoader.load();

            getParentController().changeLog.add(this.getItem());

            Stage stage = new Stage();
            stage.setTitle(name.getText());
            stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));

            stage.setOnHiding(event -> {
                if(this.getItem().getIssuedAt() != null) button.setSelected(true);
                else {
                    button.setSelected(false);
                    name.getStyleClass().removeAll("strikethrough");
                }
            });

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
