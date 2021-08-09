package ba.unsa.etf.rpr.models;

import javafx.scene.image.Image;
import java.util.Date;
import java.util.List;

public class Business {
    private String name, address;
    private String phoneNumber;
    private Image image;
    private Date lastVisit, nextVisit;
    private List<Penalty> penalties;
}
