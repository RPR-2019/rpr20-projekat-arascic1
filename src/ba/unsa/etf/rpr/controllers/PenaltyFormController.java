package ba.unsa.etf.rpr.controllers;

import ba.unsa.etf.rpr.DAO;
import ba.unsa.etf.rpr.IllegalPenaltyException;
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
import java.util.Optional;

public class PenaltyFormController {

    public CheckBox OKChecBox;
    public DatePicker deadlinePicker;
    public Spinner<Integer> penaltyAmount, deadlinePenalty, ceaseOperation;
    public TextArea report;
    public Button back, ok, deleteBtn;

    private Inspection inspection;
    private InspectionCellController parentController;
    private boolean inspectionStateChanged = false;

    private boolean changeOccured() {
        return !(inspection.getPenalty().getAmount().equals(penaltyAmount.getValue())
            && inspection.getPenalty().getMissedDeadlinePenalty().equals(deadlinePenalty.getValue())
            && inspection.getPenalty().getReport().equals(report.getText())
            && inspection.getPenalty().getCeaseOperation().equals(ceaseOperation.getValue())
            && inspection.getPenalty().getDeadline().equals(DAO.convertToDateViaInstant(deadlinePicker.getValue()))
            && !inspectionStateChanged);
    }

    private void invalidCurrencyAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setHeaderText("Neispravan unos novčanog iznosa");
        alert.setContentText("Molimo unesite pozitivnu novčanu vrijednost");

        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        deadlinePicker.setDayCellFactory(picker ->
            new DateCell() {
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate today = LocalDate.now();
                    setDisable(empty || date.isBefore(today));
                }
            }
        );
        deadlinePicker.setEditable(false);

        OKChecBox.setOnAction(this::cbListener);

        StringConverter<Integer> missedDeadlineCurrency = new StringConverter<>() {
            @Override
            public String toString(Integer integer) {
                return integer.toString() + " KM";
            }

            @Override
            public Integer fromString(String s) {
                s = s.strip();
                String[] strings = s.split(" ");
                try { return Integer.parseInt(strings[0]); }
                catch (NumberFormatException ignored) {
                    if(!s.equals("")) invalidCurrencyAlert();
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
                s = s.strip();
                String[] strings = s.split(" ");
                try { return Integer.parseInt(strings[0]); }
                catch (NumberFormatException ignored) {
                    if(!s.equals("")) invalidCurrencyAlert();
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
                    s = s.strip();
                    String[] strings = s.split(" ");
                    try { return Integer.parseInt(strings[0]); }
                    catch (NumberFormatException e) {
                        if(!s.equals("")) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Greška");
                            alert.setHeaderText("Pogrešan unos dana zabrane rada");
                            alert.setContentText("Molimo unesite cijeli pozitivni broj.");

                            alert.showAndWait();
                        }

                        ceaseOperation.getEditor().setText("0 KM");
                        return 0;
                    }
                }
            },
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE)
        );

        deleteBtn.setDisable(inspection.getIssuedAt() == null);
        deleteBtn.setOnAction(i -> {
            Optional<ButtonType> result = emitDeletionAlert();
            if(result.isPresent() && result.get() == ButtonType.OK) {
                inspection.setIssuedAt(null);
                inspection.setPenalty(null);

                ((Stage) ((Node) i.getSource()).getScene().getWindow()).close();
            }
        });

        back.setOnAction(i -> ((Stage) ((Node) i.getSource()).getScene().getWindow()).close());
        ok.setOnAction(i -> {
            if(deadlinePicker.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Greška");
                alert.setHeaderText("Zaboravili ste unijeti rok kazne");
                alert.setContentText("Molimo pokušajte ponovo.");

                alert.showAndWait();
            }
            else if(inspection.getPenalty() == null) {
                Optional<ButtonType> result;
                if(inspectionStateChanged) {
                    result = emitConfirmationAlert();
                    if(result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            Penalty newPenalty = readPenalty();

                            inspection.setPenalty(newPenalty);
                            inspection.getAddressedTo().getPenalties().add(newPenalty);

                            registerInspection(i);
                        } catch(IllegalPenaltyException e) { handleException(e); }
                    }
                }
                else {
                    try {
                        Penalty newPenalty = readPenalty();

                        inspection.setPenalty(newPenalty);
                        inspection.getAddressedTo().getPenalties().add(newPenalty);

                        registerInspection(i);
                    } catch (IllegalPenaltyException e) { handleException(e); }
                }
            }
            else if(changeOccured()) {
                emitConfirmationAlertAndRegisterInspection(i);
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
            else {
                OKChecBox.setSelected(true);
                deadlinePicker.setDisable(true);
                penaltyAmount.setDisable(true);
                deadlinePenalty.setDisable(true);
                ceaseOperation.setDisable(true);
                report.setDisable(true);
            }
        }
    }

    private void handleException(IllegalPenaltyException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setHeaderText(e.getMessage());
        alert.setContentText("Molimo pokušajte ponovo.");

        alert.showAndWait();
    }

    private Optional<ButtonType> emitDeletionAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Molimo potvrdite akciju");
        alert.setHeaderText("Ovo će izbrisati već postojeći inspekcijski nalaz");
        alert.setContentText("Da li ste sigurni da to želite?");
        return alert.showAndWait();
    }

    private Optional<ButtonType> emitConfirmationAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Molimo potvrdite akciju");
        alert.setHeaderText("Pokušavate izmijeniti rezultat inspekcije");
        alert.setContentText("Da li ste sigurni da to želite?");
        return alert.showAndWait();
    }

    private void emitConfirmationAlertAndRegisterInspection(ActionEvent actionEvent) {
        Optional<ButtonType> result = emitConfirmationAlert();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Penalty newPenalty = readPenalty();

                if (inspection.getPenalty() != null)
                    inspection.getAddressedTo().getPenalties().remove(inspection.getPenalty());
                inspection.setPenalty(newPenalty);
                inspection.getAddressedTo().getPenalties().add(newPenalty);

                registerInspection(actionEvent);
            } catch (IllegalPenaltyException e) { handleException(e); }
        }
    }

    private Penalty readPenalty() {
        Penalty newPenalty = new Penalty();
        newPenalty.setReport(report.getText());
        newPenalty.setDeadline(DAO.convertToDateViaInstant(deadlinePicker.getValue()));
        newPenalty.setAmount(penaltyAmount.getValue());
        newPenalty.setMissedDeadlinePenalty(deadlinePenalty.getValue());
        newPenalty.setCeaseOperation(ceaseOperation.getValue());

        if (newPenalty.getAmount().equals(0) && newPenalty.getCeaseOperation().equals(0))
            throw new IllegalPenaltyException("Kazna nije ni finansijska ni zabrana rada.");
        if (newPenalty.getAmount().equals(0) && !newPenalty.getMissedDeadlinePenalty().equals(0))
            throw new IllegalPenaltyException("Kazna ima penal za probijeni rok a nema iznos.");

        return newPenalty;
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

    public void cbListener(ActionEvent actionEvent) {
        if(OKChecBox.isSelected()) {
            if(inspection.getPenalty() != null) {
                Optional<ButtonType> result = emitConfirmationAlert();
                if(result.isPresent() && result.get() == ButtonType.OK) {
                    inspection.getAddressedTo().getPenalties().remove(inspection.getPenalty());
                    inspection.setPenalty(null);

                    inspectionStateChanged = true;
                }
            }

            registerInspection(actionEvent);
        }
        else if(!OKChecBox.isSelected()) {
            // CBox was selected - user wants to input a penalty on a previously penalty free inspection
            deadlinePicker.setDisable(false);
            penaltyAmount.setDisable(false);
            deadlinePenalty.setDisable(false);
            ceaseOperation.setDisable(false);
            report.setDisable(false);
            inspectionStateChanged = true;
        }
    }

    private void registerInspection(ActionEvent actionEvent) {
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

    public void setParentController(InspectionCellController parentController) {
        this.parentController = parentController;
    }
}