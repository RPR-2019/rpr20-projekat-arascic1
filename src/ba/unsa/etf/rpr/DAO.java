package ba.unsa.etf.rpr;

import ba.unsa.etf.rpr.models.Business;
import ba.unsa.etf.rpr.models.Inspection;
import ba.unsa.etf.rpr.models.Inspector;
import ba.unsa.etf.rpr.models.Penalty;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Date;

public class DAO {
    private static DAO instance = null;
    public static String usernameHash;
    private Connection conn;
    private PreparedStatement getPassword, getBusinessesForInspector, getPenaltiesForBusiness, getInspectorByHash;
    private PreparedStatement getInspectionsForInspector, getPenaltyByID;

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
                    "where b.name in (select addressedTo from inspections ib where ib.sanctionedBy=?)");
            getPenaltiesForBusiness = conn.prepareStatement("select * " +
                    "from inspections i, penalties p" +
                    "where i.penalty = p.id and i.addressedTo=?");
            getInspectorByHash = conn.prepareStatement("select * from users where usernameHash=?");
            getInspectionsForInspector = conn.prepareStatement("select * from inspections where sanctionedBy=?");
            getPenaltyByID = conn.prepareStatement("select * from penalties where id=?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Boolean authenticate(String usernameHash, String passHash) {
        /**
         * If authentication is successful - returns whether the account being authenticated is a manager or not.
         * Returns null if authentication failed.
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

                newPenalty.setReport(rs.getString("report"));
                newPenalty.setDeadline(formatter.parse(rs.getString("penaltyDeadline")));
                newPenalty.setAmount(rs.getInt("amount"));
                newPenalty.setMissedDeadlinePenalty(rs.getInt("missedDeadlinePenalty"));
                newPenalty.setCeaseOperation(rs.getInt("ceaseOperation"));

                businessPenalties.add(newPenalty);
            }

            return businessPenalties;
        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    private Date customDateParser(String date) throws ParseException {
        /**
         *  There are special values in the database for representing dates,
         *  namely: today, yesterday, tomorrow. This is coded together with mocked data
         *  in order to illustrate functionality of the program -
         *  we want mocked data to display meaningfully whenever the program is run.
         *  This function provides proper parsing of these values.
         */

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        if(date.equals("today")) return convertToDateViaInstant(LocalDate.now());
        else if(date.equals("yesterday")) return convertToDateViaInstant(LocalDate.now().minusDays(1));
        else if(date.equals("tomorrow")) return convertToDateViaInstant(LocalDate.now().plusDays(1));
        else return formatter.parse(date);
    }

    public static Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    public List<Business> getBusinessesForInspector(String usernameHash) {
        try {
            getBusinessesForInspector.setString(1, usernameHash);
            ResultSet rs = getBusinessesForInspector.executeQuery();
            List<Business> assignedBusinesses = new ArrayList<>();
            while(rs.next()) {
                Business newBussiness = new Business();

                newBussiness.setName(rs.getString(1));
                newBussiness.setAddress(rs.getString(2));
                newBussiness.setPhoneNumber(rs.getString(3));
                newBussiness.setImage(new Image(rs.getString(4)));
                newBussiness.setLastVisit(customDateParser(rs.getString(5)));
                newBussiness.setNextVisit(customDateParser(rs.getString(6)));
                newBussiness.setPenalties(getPenaltiesForBusiness(newBussiness));

                assignedBusinesses.add(newBussiness);
            }

            return assignedBusinesses;
        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    public Penalty getPenaltyByID(Integer id) {
        try {
            getPenaltyByID.setInt(1, id);
            ResultSet rs = getPenaltyByID.executeQuery();
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            if(!rs.next()) return null;
            else {
                Penalty newPenalty = new Penalty();
                newPenalty.setReport(rs.getString("report"));
                newPenalty.setDeadline(formatter.parse(rs.getString("penaltyDeadline")));
                newPenalty.setAmount(rs.getInt("amount"));
                newPenalty.setMissedDeadlinePenalty(rs.getInt("missedDeadlinePenalty"));
                newPenalty.setCeaseOperation(rs.getInt("ceaseOperation"));

                return newPenalty;
            }
        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    public List<Inspection> getInspectionsForInspector(String usernameHash) {
        try {
            getInspectionsForInspector.setString(1, usernameHash);
            ResultSet rs = getInspectionsForInspector.executeQuery();
            List<Inspection> assignedInspections = new ArrayList<>();
            while(rs.next()) {
                Inspection newInspection = new Inspection();

                newInspection.setSanctionedBy(usernameHash);
                newInspection.setAddressedTo(rs.getString("addressedTo"));
                newInspection.setDeadline(customDateParser(rs.getString("deadline")));
                newInspection.setIssuedAt(customDateParser(rs.getString("finished")));
                newInspection.setPenalty(getPenaltyByID(rs.getInt("penalty")));
            }
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
