package ba.unsa.etf.rpr.controllers;
import ba.unsa.etf.rpr.DAO;
import ba.unsa.etf.rpr.models.Inspection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
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
    public LocalDate selectionDate;

    private ObservableList<Inspection> observableInspections = FXCollections.observableArrayList();
    Map<Date, ArrayList<Inspection>> inspectionsByDay = new TreeMap<>();

    @FXML
    public void initialize() {
        // color init
        list.getStyleClass().add("azureColor");
        main.getStyleClass().add("azureColor");

        // comboBox init
        ArrayList<String> comboOptions = new ArrayList<>();
        comboOptions.add("Sve");
        comboOptions.add("Preostalo");
        comboOptions.add("Završeno");
        comboOptions.forEach(str -> options.getItems().add(str));
        options.getSelectionModel().selectFirst();
        options.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if(!newVal.equals(oldVal)) {
                switch (newVal) {
                    case "Sve":
                        list.setItems(observableInspections);
                        break;
                    case "Završeno":
                        list.setItems(observableInspections.filtered(i -> i.getIssuedAt() != null));
                        break;
                    case "Preostalo":
                        list.setItems(observableInspections.filtered(i -> i.getIssuedAt() == null));
                        break;
                }
            }
        });

        // listView init
        List<Inspection> inspections = DAO.getInstance().getInspectionsForInspector(DAO.usernameHash);
        inspections.forEach(i -> {
                if(!inspectionsByDay.containsKey(i.getDeadline()))
                    inspectionsByDay.put(i.getDeadline(), new ArrayList<>());
                if(!inspectionsByDay.get(i.getDeadline()).contains(i))
                    inspectionsByDay.get(i.getDeadline()).add(i);
            }
        );

        loadDefaultInspectorView();

        list.setItems(observableInspections);
        list.setSelectionModel(new NoSelectionModel<>());
        list.setCellFactory(param -> {
            InspectionCellController factory = new InspectionCellController();
            factory.setParentController(this);
            return factory;
        });

        // time navigation buttons image init
        ImageView leftArrowImg = new ImageView("/img/leftArrow.png");
        ImageView rightArrowImg = new ImageView("/img/rightArrow.png");
        leftArrowImg.setFitWidth(30); leftArrowImg.setFitHeight(30);
        rightArrowImg.setFitWidth(30); rightArrowImg.setFitHeight(30);
        leftArrow.setGraphic(leftArrowImg);
        rightArrow.setGraphic(rightArrowImg);
        leftArrow.getStyleClass().add("circularButton");
        rightArrow.getStyleClass().add("circularButton");

        // date label init
        selectionDate = LocalDate.now();
        currentDateDisplay.setText(customDateFormatter(LocalDate.now()));
    }

    private String customDateFormatter(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String formattedDate = date.format(formatter);
        if(formattedDate.charAt(0) == '0') formattedDate = formattedDate.substring(1);
        // in the absence of this line, "august" prints as "avgust" - ugly
        if(formattedDate.contains("avgust")) formattedDate = formattedDate.replaceFirst("v", "u");
        return formattedDate;
    }

    private List<Inspection> getLatePendingInspections(Map<Date, ArrayList<Inspection>> inspections) {
        Iterator<Map.Entry<Date, ArrayList<Inspection>>> itr = inspections.entrySet().iterator();
        Map.Entry<Date, ArrayList<Inspection>> entry;
        ArrayList<Inspection> latePendingInspections = new ArrayList<>();

        while(itr.hasNext() && (entry = itr.next()).getKey().before(DAO.convertToDateViaInstant(LocalDate.now()))) {
            latePendingInspections.addAll(
                entry.getValue().stream().filter(i -> i.getIssuedAt() == null).collect(Collectors.toList())
            );
        }

        return latePendingInspections;
    }

    private List<Inspection> getInspectionsFinishedOnDate(Date date) {
        Iterator<Map.Entry<Date, ArrayList<Inspection>>> itr = inspectionsByDay.entrySet().iterator();
        Map.Entry<Date, ArrayList<Inspection>> entry;
        ArrayList<Inspection> finishedOnDate = new ArrayList<>();

        while(itr.hasNext()) {
            entry = itr.next();
            finishedOnDate.addAll(
                entry.getValue().stream()
                    .filter(i -> i.getIssuedAt() != null)
                    .filter(i -> i.getIssuedAt().equals(date)).collect(Collectors.toList())
            );
        }

        return finishedOnDate;
    }

    private void loadDefaultInspectorView() {
        /**
         * Will update data to reflect todays assignments.
         */
        observableInspections.addAll(getLatePendingInspections(inspectionsByDay));
        observableInspections.addAll(inspectionsByDay.get(DAO.convertToDateViaInstant(LocalDate.now())));
        observableInspections.addAll(
            getInspectionsFinishedOnDate(DAO.convertToDateViaInstant(LocalDate.now()))
        );
    }

    private void updateDataOnDateChange() {
        observableInspections.clear();

        if(selectionDate.equals(LocalDate.now()))
            loadDefaultInspectorView();
        else
            if(selectionDate.isBefore(LocalDate.now()))
                observableInspections.addAll(getInspectionsFinishedOnDate(DAO.convertToDateViaInstant(selectionDate)));
            else if(inspectionsByDay.containsKey(DAO.convertToDateViaInstant(selectionDate)))
                observableInspections.addAll(inspectionsByDay.get(DAO.convertToDateViaInstant(selectionDate))
                    .stream().filter(i -> i.getIssuedAt() == null).collect(Collectors.toList()));

        list.setItems(observableInspections);
        options.getSelectionModel().selectFirst();
    }

    public void loadPreviousDayInspections(ActionEvent actionEvent) {
        currentDateDisplay.setText(customDateFormatter(selectionDate = selectionDate.minusDays(1)));
        updateDataOnDateChange();
    }

    public void loadNextDayInspections(ActionEvent actionEvent) {
        currentDateDisplay.setText(customDateFormatter(selectionDate = selectionDate.plusDays(1)));
        updateDataOnDateChange();
    }

    private class NoSelectionModel<T> extends MultipleSelectionModel<T> {

        @Override
        public ObservableList<Integer> getSelectedIndices() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public ObservableList<T> getSelectedItems() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public void selectIndices(int index, int... indices) {}

        @Override
        public void selectAll() {}

        @Override
        public void selectFirst() {}

        @Override
        public void selectLast() {}

        @Override
        public void clearAndSelect(int index) {}

        @Override
        public void select(int index) {}

        @Override
        public void select(T obj) {}

        @Override
        public void clearSelection(int index) {}

        @Override
        public void clearSelection() {}

        @Override
        public boolean isSelected(int index) {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void selectPrevious() {}

        @Override
        public void selectNext() {}
    }
}
