package ba.unsa.etf.rpr.controllers;

import ba.unsa.etf.rpr.DAO;
import ba.unsa.etf.rpr.models.Business;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.w3c.dom.Text;

public class ManagerController {
    public ListView<Business> LVbusinessList;
    public TextField nameField;
    public TextField addressField;
    public TextField phoneNumberField;
    public TextField URLField;
    public TextField lastVisitField;
    public TextField nextVisitField;
    public ImageView img;

    private ObservableList<Business> observableBusinesses = FXCollections.observableArrayList();
    private BusinessPropertyWrapper wrappedBusiness = new BusinessPropertyWrapper();

    @FXML
    public void initialize() {
        observableBusinesses.addAll(DAO.getInstance().getAllBusinesses());
        LVbusinessList.setItems(observableBusinesses);

        LVbusinessList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if(observableValue != null) wrappedBusiness = new BusinessPropertyWrapper(observableValue.getValue());
            if(oldValue != null) {
                nameField.textProperty().unbindBidirectional(wrappedBusiness.name);
                addressField.textProperty().unbindBidirectional(wrappedBusiness.address);
                phoneNumberField.textProperty().unbindBidirectional(wrappedBusiness.phoneNumber);
                URLField.textProperty().unbindBidirectional(wrappedBusiness.url);
                lastVisitField.textProperty().unbindBidirectional(wrappedBusiness.lastVisit);
                nextVisitField.textProperty().unbindBidirectional(wrappedBusiness.nextVisit);
            }
            if(newValue == null) {
                nameField.setText("");
                addressField.setText("");
                phoneNumberField.setText("");
                URLField.setText("");
                lastVisitField.setText("");
                nextVisitField.setText("");
                img.setImage(new Image("/img/noImage.png"));
            }
            else {
                nameField.textProperty().bindBidirectional(wrappedBusiness.name);
                addressField.textProperty().bindBidirectional(wrappedBusiness.address);
                phoneNumberField.textProperty().bindBidirectional(wrappedBusiness.phoneNumber);
                URLField.textProperty().bindBidirectional(wrappedBusiness.url);
                lastVisitField.textProperty().bindBidirectional(wrappedBusiness.lastVisit);
                nextVisitField.textProperty().bindBidirectional(wrappedBusiness.nextVisit);
            }
        });
    }

    private static class BusinessPropertyWrapper {
        private SimpleStringProperty name, address, phoneNumber, url, lastVisit, nextVisit;

        public BusinessPropertyWrapper() {}

        public BusinessPropertyWrapper(Business b) {
            name = new SimpleStringProperty(b.getName());
            address = new SimpleStringProperty(b.getAddress());
            phoneNumber = new SimpleStringProperty(b.getPhoneNumber());
            url = new SimpleStringProperty(b.getImage().getUrl());
            lastVisit = new SimpleStringProperty(DAO.dateToString(b.getLastVisit()));
            nextVisit = new SimpleStringProperty(DAO.dateToString(b.getNextVisit()));
        }
    }
}
