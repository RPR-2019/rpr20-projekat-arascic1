package ba.unsa.etf.rpr.controllers;

import ba.unsa.etf.rpr.DAO;
import ba.unsa.etf.rpr.models.Inspection;
import ba.unsa.etf.rpr.models.Penalty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PenaltyFormController {

    public CheckBox OKChecBox;
    public DatePicker deadlinePicker;
    public Spinner<Integer> penaltyAmount, deadlinePenalty, ceaseOperation;
    public TextArea report;
    public Button back, ok;

    private Inspection inspection;
    private InspectionCellController parentController;

    @FXML
    public void initialize() {
        deadlinePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.isBefore(today));
            }
        });

        OKChecBox.setOnAction(this::checkOK);

        StringConverter<Integer> currency = new StringConverter<>() {
            @Override
            public String toString(Integer integer) {
                return integer.toString() + " KM";
            }

            @Override
            public Integer fromString(String s) {
                String[] strings = s.split(" ");
                return Integer.parseInt(strings[0]);
            }
        };

        initializeSpinner(penaltyAmount, currency,
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0, 10));
        initializeSpinner(deadlinePenalty, currency,
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0, 10));
        initializeSpinner(
            ceaseOperation,
            new StringConverter<>() {
                @Override
                public String toString(Integer integer) {
                    if(integer.toString().lastIndexOf("1") == integer.toString().length() - 1 && integer != 11) {
                        return integer.toString() + " dan";
                    }
                    else {
                        return integer.toString() + " dana";
                    }
                }

                @Override
                public Integer fromString(String s) {
                    String[] strings = s.split(" ");
                    return Integer.parseInt(strings[0]);
                }
            },
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE)
        );

        back.setOnAction(i -> ((Stage) ((Node) i.getSource()).getScene().getWindow()).close());
        ok.setOnAction(i -> {
            Penalty newPenalty = new Penalty();
            newPenalty.setReport(report.getText());
            newPenalty.setDeadline(DAO.convertToDateViaInstant(deadlinePicker.getValue()));
            newPenalty.setAmount(penaltyAmount.getValue());
            newPenalty.setMissedDeadlinePenalty(deadlinePenalty.getValue());
            newPenalty.setCeaseOperation(ceaseOperation.getValue());

            inspection.setPenalty(newPenalty);
            inspection.getAddressedTo().getPenalties().add(newPenalty);

            checkOK(i);
        });
    }

    private void initializeSpinner(
        Spinner<Integer> spinner,
        StringConverter<Integer> converter,
        SpinnerValueFactory<Integer> factory
    ) {
        spinner.setEditable(true);
        spinner.setValueFactory(factory);
        spinner.getValueFactory().setConverter(converter);
        spinner.getValueFactory().setValue(0);
        spinner.increment(); spinner.decrement();
    }

    public Inspection getInspection() {
        return inspection;
    }

    public void setInspection(Inspection inspection) {
        this.inspection = inspection;
    }

    public void checkOK(ActionEvent actionEvent) {
        inspection.setIssuedAt(DAO.convertToDateViaInstant(LocalDate.now()));
        if(inspection.getIssuedAt().before(inspection.getDeadline())) {
            parentController.parentController.inspectionsByDay
                .get(DAO.convertToDateViaInstant(LocalDate.now())).add(inspection);
            parentController.parentController.inspectionsByDay
                .get(DAO.convertToDateViaInstant(parentController.parentController.selectionDate)).remove(inspection);
            parentController.parentController.observableInspections.remove(inspection);
        }
        ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
        parentController.parentController.notifyInspectionDone();
    }

    public void setParentController(InspectionCellController parentController) {
        this.parentController = parentController;
    }
}