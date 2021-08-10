package ba.unsa.etf.rpr.models;

import java.sql.ResultSet;

public class Inspector {
    private String name;

    public Inspector(String name) { this.name = name; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}