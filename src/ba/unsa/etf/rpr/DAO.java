package ba.unsa.etf.rpr;

import ba.unsa.etf.rpr.models.Business;
import ba.unsa.etf.rpr.models.InspectionFinding;
import ba.unsa.etf.rpr.models.Inspector;
import ba.unsa.etf.rpr.models.Penalty;
import javafx.scene.image.Image;

import javax.xml.transform.Result;
import java.beans.PropertyEditorSupport;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DAO {
    private static DAO instance = null;
    public static String usernameHash;
    private Connection conn;
    private PreparedStatement getPassword, getBusinessesForInspector, getPenaltiesForBusiness, getInspectorByHash;

    private DAO() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:inspection.db");
            PreparedStatement dbExistenceCheck;
            try {
                dbExistenceCheck = conn.prepareStatement("INSERT INTO USERS VALUES (?, ?, ?, ?)");
            } catch (SQLException e) {
                initializeDatabase();
                try {
                    dbExistenceCheck = conn.prepareStatement("INSERT INTO USERS VALUES (?, ?, ?, ?)");
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }

            getPassword = conn.prepareStatement("SELECT passHash, manager FROM USERS WHERE usernameHash=?");
            getBusinessesForInspector = conn.prepareStatement("select * from businesses b " +
                    "where b.name in (select businessName from inspector_business ib where ib.usernameHash=?)");
            getPenaltiesForBusiness = conn.prepareStatement("select * " +
                    "from penalties p, inspection_findings if " +
                    "where p.addressedTo=? and p.details=if.id");
            getInspectorByHash = conn.prepareStatement("select * from users where usernameHash=?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Boolean authenticate(String usernameHash, String passHash) {
        /**
         * If authentication is successful - returns whether the account being authenticated is a manager or not.
         * Otherwise returns null.
         */
        try {
            getPassword.setString(1, usernameHash);
            ResultSet rs = getPassword.executeQuery();
            if(!rs.next()) return null;
            if(rs.getString(1).equals(passHash)) {
                DAO.usernameHash = usernameHash;
                if (rs.getInt(2) == 1) return true;
                else if (rs.getInt(2) == 0) return false;
            }
            else return null;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    public Inspector getInspectorName(String usernameHash) {
        try {
            getInspectorByHash.setString(1, usernameHash);
            return new Inspector(getInspectorByHash.executeQuery().getString(4));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    public List<Penalty> getPenaltiesForBusiness(Business business) {
        try {
            getPenaltiesForBusiness.setString(1, business.getName());
            ResultSet rs = getPenaltiesForBusiness.executeQuery();
            List<Penalty> businessPenalties = new ArrayList<>();
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            while (rs.next()) {
                Penalty newPenalty = new Penalty();

                newPenalty.setSanctionedBy(getInspectorName(rs.getString(2)));
                newPenalty.setAddressedTo(business);
                newPenalty.setFinding(
                    new InspectionFinding(formatter.parse(rs.getString(5)), rs.getString(11))
                );
                newPenalty.setDeadline(formatter.parse(rs.getString(6)));
                newPenalty.setAmount(rs.getInt(7));
                newPenalty.setMissedDeadlinePenalty(8);

                businessPenalties.add(newPenalty);
            }

            return businessPenalties;
        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    public List<Business> getBusinessesForInspector(String usernameHash) {
        try {
            getBusinessesForInspector.setString(1, usernameHash);
            ResultSet rs = getBusinessesForInspector.executeQuery();
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            List<Business> assignedBusinesses = new ArrayList<>();
            while(rs.next()) {
                Business newBussiness = new Business();

                newBussiness.setName(rs.getString(1));
                newBussiness.setAddress(rs.getString(2));
                newBussiness.setPhoneNumber(rs.getString(3));
                newBussiness.setImage(new Image(rs.getString(4)));
                newBussiness.setLastVisit(formatter.parse(rs.getString(5)));
                newBussiness.setNextVisit(formatter.parse(rs.getString(6)));
                newBussiness.setPenalties(getPenaltiesForBusiness(newBussiness));

                assignedBusinesses.add(newBussiness);
            }

            return assignedBusinesses;
        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    private void initializeDatabase() {
        Scanner input;
        try {
            input = new Scanner(new FileInputStream("resources/sql/init.sql"));
            StringBuilder sqlStatement = new StringBuilder();
            while (input.hasNext()) {
                sqlStatement.append(input.nextLine());
                if (sqlStatement.charAt(sqlStatement.length() - 1) == ';') {
                    try {
                        Statement stmt = conn.createStatement();
                        stmt.execute(sqlStatement.toString());
                        sqlStatement = new StringBuilder();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static DAO getInstance() {
        if (instance == null) instance = new DAO();
        return instance;
    }

    public static void removeInstance() {
        if (instance == null) return;
        instance.close();
        instance = null;
    }

    private void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
