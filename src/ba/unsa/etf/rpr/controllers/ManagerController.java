package ba.unsa.etf.rpr.controllers;

import ba.unsa.etf.rpr.DAO;
import ba.unsa.etf.rpr.models.Business;
import ba.unsa.etf.rpr.models.Inspection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public class ManagerController {
    public ListView<Business> LVbusinessList;
    public TextField nameField;
    public TextField addressField;
    public TextField phoneNumberField;
    public TextField URLField;
    public TextField lastVisitField;
    public TextField nextVisitField;
    public ImageView img;
    public Button dummyBtn;

    private ObservableList<Business> observableBusinesses = FXCollections.observableArrayList();
    private BusinessPropertyWrapper wrappedBusiness = new BusinessPropertyWrapper();
    private final HashSet<Business> changeLog = new HashSet<>();
    private final HashMap<Business, String> oldNames = new HashMap<>();

    @FXML
    public void initialize() {
        observableBusinesses.addAll(DAO.getInstance().getAllBusinesses());
        LVbusinessList.setItems(observableBusinesses);
        img.setImage(new Image("/img/noImage.png"));

        lastVisitField.setEditable(false);
        nextVisitField.setEditable(false);

        dummyBtn.setVisible(false);
        dummyBtn.setOnAction(this::save);

        LVbusinessList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if(oldValue != null) {
                if(wrappedBusiness.isMutated(oldValue)) {
                    changeLog.add(oldValue);
                    oldNames.put(oldValue, oldValue.getName());
                    wrappedBusiness.parallelWriteToBusiness(oldValue);
                }

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
                if(observableValue != null) wrappedBusiness = new BusinessPropertyWrapper(observableValue.getValue());
                if(newValue.getImage() != null) {
                    img.setPreserveRatio(true);
                    img.setImage(newValue.getImage());
                }

                nameField.textProperty().bindBidirectional(wrappedBusiness.name);
                addressField.textProperty().bindBidirectional(wrappedBusiness.address);
                phoneNumberField.textProperty().bindBidirectional(wrappedBusiness.phoneNumber);
                URLField.textProperty().bindBidirectional(wrappedBusiness.url);
                lastVisitField.textProperty().bindBidirectional(wrappedBusiness.lastVisit);
                nextVisitField.textProperty().bindBidirectional(wrappedBusiness.nextVisit);
            }
        });
    }

    private void save(ActionEvent actionEvent) {
        for(Business b : changeLog) {
            DAO.getInstance().writeBusiness(b, oldNames.get(b));
        }
        ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
    }

    public void notifyWindowClosing() {
        if(!changeLog.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Pažnja!");
            alert.setHeaderText("Mogući gubitak podataka - imate nespremljene izmjene!");
            alert.setContentText("");

            ButtonType ignoreChanges = new ButtonType("Zanemari izmjene");
            ButtonType writeChanges = new ButtonType("Spasi izmjene");
            ButtonType back = new ButtonType("Nazad");

            alert.getButtonTypes().setAll(writeChanges, ignoreChanges, back);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == writeChanges) {
                dummyBtn.fire();
            }
            else if(result.get() == back) {
                throw new RuntimeException();
            }
        }
    }

    private static class BusinessPropertyWrapper {
        private SimpleStringProperty name, address, phoneNumber, url, lastVisit, nextVisit;
        private static final Object lock = new Object();

        public BusinessPropertyWrapper() {}

        public void parallelWriteToBusiness(Business b) {
            Thread work = new Thread(() -> { synchronized (lock) {
                b.setName(name.get());
                b.setAddress(address.get());
                b.setPhoneNumber(phoneNumber.get());
                b.setImage(new Image(url.get()));
            }});

            work.start();
        }

        public boolean isMutated(Business b) {
            if(b.getName() == null) b.setName("");
            if(b.getAddress() == null) b.setAddress("");
            if(b.getImage() == null) b.setImage(new Image("/img/noImage.png"));
            if(b.getPhoneNumber() == null) b.setPhoneNumber("");

            return !(b.getName().equals(name.get()) && b.getAddress().equals(address.get())
                && b.getPhoneNumber().equals(phoneNumber.get()) && b.getImage().getUrl().equals(url.get()));
        }

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
