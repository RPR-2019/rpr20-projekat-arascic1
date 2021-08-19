package ba.unsa.etf.rpr.controllers;

import ba.unsa.etf.rpr.models.Inspection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class InspectionCellController extends ListCell<Inspection> {
    @FXML
    public RadioButton button;
    @FXML
    public Label name;
    @FXML
    public Label address;
    @FXML
    public BorderPane root;

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
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setGraphic(root);
        }
    }
}
