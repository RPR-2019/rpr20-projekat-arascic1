package ba.unsa.etf.rpr.controllers;

import ba.unsa.etf.rpr.DAO;
import ba.unsa.etf.rpr.models.Business;
import ba.unsa.etf.rpr.models.Inspection;
import ba.unsa.etf.rpr.models.Inspector;
import com.sun.source.tree.Tree;
import javafx.application.Platform;
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

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

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
    public TreeView<String> tvInspections;

    private ObservableList<Business> observableBusinesses = FXCollections.observableArrayList();
    private ObservableList<Inspection> observableInspections = FXCollections.observableArrayList();
    private BusinessPropertyWrapper wrappedBusiness = new BusinessPropertyWrapper();
    private final HashSet<Business> changeLog = new HashSet<>();
    private final HashMap<Business, String> oldNames = new HashMap<>();

    private Map<String, Map<Date, List<Inspection>>> loadData() {

        Map<String, Map<Date, List<Inspection>>> data = new HashMap<>();
        List<Inspector> inspectors = DAO.getInstance().getInspectors();

        for(Inspector i : inspectors) {
            Map<Date, List<Inspection>> value = new HashMap<>();

            List<Inspection> inspections = DAO.getInstance().getInspectionsForInspector(
                DAO.getInstance().getHashForUsername(i.getName())
            );

            inspections.forEach(inspection -> {
                if (!value.containsKey(inspection.getDeadline()))
                    value.put(inspection.getDeadline(), new ArrayList<>());
                if (!value.get(inspection.getDeadline()).contains(inspection))
                    value.get(inspection.getDeadline()).add(inspection);
            });

            data.put(i.getName(), value);
        }

        return data;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private void initializeTreeView() {
        Thread task = new Thread(() -> {
            Map<String, Map<Date, List<Inspection>>> data = loadData();
            TreeItem<String> dummyRoot = new TreeItem<>();

            List<TreeItem<String>> rootItems = new ArrayList<>();
            data.keySet().forEach(i -> {
                rootItems.add(new TreeItem<>(i));
            });

            dummyRoot.getChildren().addAll(rootItems);
            dummyRoot.setExpanded(true);
            Platform.runLater(() -> tvInspections.setShowRoot(false));

            for(TreeItem<String> inspector : rootItems) {
                data.get(inspector.getValue()).keySet().forEach(date -> {
                    inspector.getChildren().add(new TreeItem<>(date.toString()));
                });

                for(TreeItem<String> dateItem : inspector.getChildren()) {
                    try {
                        data.get(inspector.getValue()).get(DAO.stringToDate(dateItem.getValue())).forEach(inspection -> {
                            dateItem.getChildren().add(new TreeItem<>(inspection.getAddressedTo().getName()));
                        });
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            Platform.runLater(() -> tvInspections.setRoot(dummyRoot));
        });

        tvInspections.setRoot(new TreeItem<>("učitava se..."));
        tvInspections.setShowRoot(true);
        task.start();
    }

    @FXML
    public void initialize() {
        observableBusinesses.addAll(DAO.getInstance().getAllBusinesses());
        LVbusinessList.setItems(observableBusinesses);

        initializeTreeView();

        img.setImage(new Image("/img/noImage.png"));

        lastVisitField.setEditable(false);
        nextVisitField.setEditable(false);

        dummyBtn.setVisible(false);
        dummyBtn.setOnAction(this::save);

        LVbusinessList.getSelectionModel().selectedItemProperty().addListener(
            (observableValue, oldValue, newValue) -> {
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
                    if(observableValue != null)
                        wrappedBusiness = new BusinessPropertyWrapper(observableValue.getValue());
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

    @SuppressWarnings("InnerClassMayBeStatic")
    private class BusinessPropertyWrapper {
        private SimpleStringProperty name, address, phoneNumber, url, lastVisit, nextVisit;
        private final Object lock = new Object();

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
