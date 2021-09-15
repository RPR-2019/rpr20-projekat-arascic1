package ba.unsa.etf.rpr.controllers;

import ba.unsa.etf.rpr.DAO;
import ba.unsa.etf.rpr.models.Business;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class ManagerController {
    public ListView<Business> LVbusinessList;
    private ObservableList<Business> observableBusinesses = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        observableBusinesses.addAll(DAO.getInstance().getAllBusinesses());
        LVbusinessList.setItems(observableBusinesses);
    }
}
