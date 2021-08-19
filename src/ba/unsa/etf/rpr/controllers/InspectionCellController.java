package ba.unsa.etf.rpr.controllers;

import ba.unsa.etf.rpr.DAO;
import ba.unsa.etf.rpr.models.Inspection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.text.DateFormat;

public class InspectionCellController extends ListCell<Inspection> {
    @FXML
    public RadioButton button;
    @FXML
    public Label name;
    @FXML
    public Label address;
    @FXML
    public BorderPane root;
    @FXML
    public Label deadline;

    public InspectionCellController() {
        loadFXML();
    }

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
            deadline.setText(DAO.dateToString(item.getDeadline()));
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setGraphic(root);
        }
    }
}
