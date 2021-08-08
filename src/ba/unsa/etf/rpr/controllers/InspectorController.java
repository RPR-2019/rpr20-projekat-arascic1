package ba.unsa.etf.rpr.controllers;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class InspectorController {
    public Button leftArrow;
    public Button rightArrow;
    public Label currentDate;

    @FXML
    public void initialize() {
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
