package ba.unsa.etf.rpr.controllers;

import ba.unsa.etf.rpr.DAO;
import ba.unsa.etf.rpr.models.Inspection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.time.LocalDate;

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

    private InspectorController parentController;

    public InspectionCellController() { loadFXML(); }

    public void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/inspection_cell.fxml"));
            loader.setController(this);
            loader.load();
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
                if(!button.isSelected()) button.fire();
            }
            else {
                name.getStyleClass().removeAll("strikethrough");
                if(button.isSelected()) button.fire();
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
}
