package ba.unsa.etf.rpr.controllers;

import ba.unsa.etf.rpr.DAO;
import ba.unsa.etf.rpr.models.Inspection;
import ba.unsa.etf.rpr.models.Penalty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.time.LocalDate;

public class PenaltyFormController {

    public CheckBox OKChecBox;
    public DatePicker deadlinePicker;
    public Spinner<Integer> penaltyAmount, deadlinePenalty, ceaseOperation;
    public TextArea report;
    public Button back, ok;

    private Inspection inspection;
    private InspectionCellController parentController;

    private void invalidCurrencyAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setHeaderText("Neispravan unos novčanog iznosa");
        alert.setContentText("Molimo unesite pozitivnu novčanu vrijednost");

        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        deadlinePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.isBefore(today));
            }
        });
        deadlinePicker.setEditable(false);

        OKChecBox.setOnAction(this::checkOK);

        StringConverter<Integer> missedDeadlineCurrency = new StringConverter<>() {
            @Override
            public String toString(Integer integer) {
                return integer.toString() + " KM";
            }

            @Override
            public Integer fromString(String s) {
                String[] strings = s.split(" ");
                try { return Integer.parseInt(strings[0]); }
                catch (NumberFormatException ignored) {
                    invalidCurrencyAlert();
                    deadlinePenalty.getEditor().setText("0 KM");
                    return 0;
                }
            }
        };


        StringConverter<Integer> penaltyCurrency = new StringConverter<>() {
            @Override
            public String toString(Integer integer) {
                return integer.toString() + " KM";
            }

            @Override
            public Integer fromString(String s) {
                String[] strings = s.split(" ");
                try { return Integer.parseInt(strings[0]); }
                catch (NumberFormatException ignored) {
                    invalidCurrencyAlert();
                    penaltyAmount.getEditor().setText("0 KM");
                    return 0;
                }
            }
        };

        initializeSpinner(penaltyAmount, penaltyCurrency,
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0, 10));
        initializeSpinner(deadlinePenalty, missedDeadlineCurrency,
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
                    try { return Integer.parseInt(strings[0]); }
                    catch (NumberFormatException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Greška");
                        alert.setHeaderText("Pogrešan unos dana zabrane rada");
                        alert.setContentText("Molimo unesite cijeli pozitivni broj.");

                        alert.showAndWait();

                        ceaseOperation.getEditor().setText("0 KM");
                        return 0;
                    }
                }
            },
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE)
        );

        back.setOnAction(i -> ((Stage) ((Node) i.getSource()).getScene().getWindow()).close());
        ok.setOnAction(i -> {
            if(deadlinePicker.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Greška");
                alert.setHeaderText("Zaboravili ste unijeti rok kazne");
                alert.setContentText("Molimo pokušajte ponovo.");

                alert.showAndWait();
            } else {
                Penalty newPenalty = new Penalty();
                newPenalty.setReport(report.getText());
                newPenalty.setDeadline(DAO.convertToDateViaInstant(deadlinePicker.getValue()));
                newPenalty.setAmount(penaltyAmount.getValue());
                newPenalty.setMissedDeadlinePenalty(deadlinePenalty.getValue());
                newPenalty.setCeaseOperation(ceaseOperation.getValue());

                inspection.setPenalty(newPenalty);
                inspection.getAddressedTo().getPenalties().add(newPenalty);

                ((Stage) ((Node) i.getSource()).getScene().getWindow()).close();
                parentController.parentController.notifyInspectionDone();
            }
        });

        if(inspection.getIssuedAt() != null) {
            if(inspection.getPenalty() != null) {
                deadlinePicker.setValue(DAO.convertToLocalDateViaInstant(inspection.getIssuedAt()));
                penaltyAmount.getValueFactory().setValue(inspection.getPenalty().getAmount());
                deadlinePenalty.getValueFactory().setValue(inspection.getPenalty().getMissedDeadlinePenalty());
                ceaseOperation.getValueFactory().setValue(inspection.getPenalty().getCeaseOperation());
                report.setText(inspection.getPenalty().getReport());
            }
            else OKChecBox.setSelected(true);
        }
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
        spinner.setRepeatDelay(new Duration(30));
        spinner.getEditor().setOnMouseClicked(i -> spinner.getEditor().setText(""));

        spinner.increment(); spinner.decrement();
    }

    public Inspection getInspection() {
        return inspection;
    }

    public void setInspection(Inspection inspection) {
        this.inspection = inspection;
    }

    public void checkOK(ActionEvent actionEvent) {
        // CheckBox is already selected prior to this callback
        // - therefore the following conditional has the effect of querying the state just prior to the callback.
        if(OKChecBox.isSelected()) {
            inspection.setIssuedAt(DAO.convertToDateViaInstant(LocalDate.now()));

            if (inspection.getIssuedAt().before(inspection.getDeadline())) {
                parentController.parentController.inspectionsByDay
                        .get(DAO.convertToDateViaInstant(LocalDate.now())).add(inspection);
                parentController.parentController.inspectionsByDay
                        .get(DAO.convertToDateViaInstant(parentController.parentController.selectionDate)).remove(inspection);
                parentController.parentController.observableInspections.remove(inspection);
            }

            ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow()).close();
            parentController.parentController.notifyInspectionDone();
        }
    }

    public void setParentController(InspectionCellController parentController) {
        this.parentController = parentController;
    }
}