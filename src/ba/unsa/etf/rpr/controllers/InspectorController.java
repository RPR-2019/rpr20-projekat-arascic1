package ba.unsa.etf.rpr.controllers;
import ba.unsa.etf.rpr.models.Business;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class InspectorController {
    public Button leftArrow;
    public Button rightArrow;
    public GridPane main;
    public Label currentDate;
    public ListView<Business> list;
    public ComboBox<String> options;

    @FXML
    public void initialize() {
        // postavljanje boje
        list.getStyleClass().add("azureColor");
        main.getStyleClass().add("azureColor");

        // populacija ComboBox-a
        ArrayList<String> opcije = new ArrayList<>();
        opcije.add("Sve");
        opcije.add("Preostalo");
        opcije.add("Završeno");
        opcije.forEach(str -> options.getItems().add(str));

        // populacija LISTE - baza
        // TODO

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
        currentDate.setText(date);
    }
}
