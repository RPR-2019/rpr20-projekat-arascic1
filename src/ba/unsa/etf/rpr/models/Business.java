package ba.unsa.etf.rpr.models;

import javafx.scene.image.Image;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Business {
    private String name, address;
    private String phoneNumber;
    private Image image;
    private Date lastVisit, nextVisit;
    private List<Penalty> penalties;

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setLastVisit(Date lastVisit) {
        this.lastVisit = lastVisit;
    }

    public void setNextVisit(Date nextVisit) {
        this.nextVisit = nextVisit;
    }

    public void setPenalties(List<Penalty> penalties) {
        this.penalties = penalties;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Image getImage() {
        return image;
    }

    public Date getLastVisit() {
        return lastVisit;
    }

    public Date getNextVisit() {
        return nextVisit;
    }

    public List<Penalty> getPenalties() {
        return penalties;
    }

    @Override
    public String toString() {
        return name;
    }
}
