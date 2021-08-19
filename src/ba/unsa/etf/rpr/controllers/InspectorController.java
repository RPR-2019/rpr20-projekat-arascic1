package ba.unsa.etf.rpr.controllers;
import ba.unsa.etf.rpr.DAO;
import ba.unsa.etf.rpr.models.Inspection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class InspectorController {
    public Button leftArrow;
    public Button rightArrow;
    public GridPane main;
    public Label currentDateDisplay;
    public ListView<Inspection> list;
    public ComboBox<String> options;

    private ObservableList<Inspection> observableInspections = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // postavljanje boje
        list.getStyleClass().add("azureColor");
        main.getStyleClass().add("azureColor");

        // populacija ComboBox-a
        ArrayList<String> comboOptions = new ArrayList<>();
        comboOptions.add("Sve");
        comboOptions.add("Preostalo");
        comboOptions.add("Završeno");
        comboOptions.forEach(str -> options.getItems().add(str));
        options.getSelectionModel().selectFirst();

        // populacija LISTE - baza
        List<Inspection> inspections = DAO.getInstance().getInspectionsForInspector(DAO.usernameHash);
        Map<Date, ArrayList<Inspection>> inspectionsByDay = new TreeMap<>();
        inspections.forEach(i -> {
                inspectionsByDay.put(i.getDeadline(), new ArrayList<>());
                if(!inspectionsByDay.get(i.getDeadline()).contains(i))
                    inspectionsByDay.get(i.getDeadline()).add(i);
            }
        );
        observableInspections.addAll(getLatePendingInspections(inspectionsByDay));
        observableInspections.addAll(inspectionsByDay.get(DAO.convertToDateViaInstant(LocalDate.now())));
        list.setItems(observableInspections);
        list.setCellFactory(new Callback<ListView<Inspection>, ListCell<Inspection>>() {
            @Override
            public ListCell<Inspection> call(ListView<Inspection> param) {
                return new InspectionCellController();
            }
        });

        // inicijalizacija slika na buttone sa obje strane labela za datum
        ImageView leftArrowImg = new ImageView("/img/leftArrow.png");
        ImageView rightArrowImg = new ImageView("/img/rightArrow.png");
        leftArrowImg.setFitWidth(30); leftArrowImg.setFitHeight(30);
        rightArrowImg.setFitWidth(30); rightArrowImg.setFitHeight(30);
        leftArrow.setGraphic(leftArrowImg);
        rightArrow.setGraphic(rightArrowImg);
        leftArrow.getStyleClass().add("circularButton");
        rightArrow.getStyleClass().add("circularButton");

        // inicijalizacija labele za datum na trenutni sistemski datum
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String date = LocalDate.now().format(formatter);
        if(date.charAt(0) == '0') date = date.substring(1);
        // ako je datum august a ne uradim ovu liniju, ispisaće aVgust
            if(date.contains("avgust")) date = date.replaceFirst("v", "u");
        currentDateDisplay.setText(date);
    }

    private List<Inspection> getLatePendingInspections(Map<Date, ArrayList<Inspection>> inspections) {
        Iterator<Map.Entry<Date, ArrayList<Inspection>>> itr = inspections.entrySet().iterator();
        Map.Entry<Date, ArrayList<Inspection>> entry;
        ArrayList<Inspection> latePendingInspections = new ArrayList<>();

        while(itr.hasNext() && (entry = itr.next()).getKey().before(DAO.convertToDateViaInstant(LocalDate.now()))) {
            latePendingInspections.addAll(
                    entry.getValue().stream().filter(i -> i.getIssuedAt() != null).collect(Collectors.toList())
            );
        }

        return latePendingInspections;
    }
}
