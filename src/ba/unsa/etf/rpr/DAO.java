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

import static java.sql.Types.NULL;

public class DAO {
    private static DAO instance = null;
    public static String usernameHash;
    private Connection conn;
    private PreparedStatement getPassword, getBusinessesForInspector, getPenaltiesForBusiness, getInspectorByHash;
    private PreparedStatement getInspectionsForInspector, getPenaltyByID, getBusinessByName;
    private PreparedStatement updateBusiness, updateInspection, updatePenalty, createPenalty, newPenaltyID;
    private PreparedStatement getPenaltyForInspection, deletePenalty, getAllBusinesses, updateInspectionAddressedTo;
    private PreparedStatement getInspectionIDsForBusiness;

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
                    "from inspections i, penalties p " +
                    "where i.penalty = p.id and i.addressedTo=?");
            getInspectorByHash = conn.prepareStatement("select * from users where usernameHash=?");
            getInspectionsForInspector = conn.prepareStatement("select * from inspections where sanctionedBy=?");
            getPenaltyByID = conn.prepareStatement("select * from penalties where id=?");
            getBusinessByName = conn.prepareStatement("select * from businesses where name=?");
            getPenaltyForInspection = conn.prepareStatement("select penalty from inspections where id=?");
            getAllBusinesses = conn.prepareStatement("select * from businesses");
            getInspectionIDsForBusiness = conn.prepareStatement("select id from inspections where addressedTo=?");

            updateBusiness = conn.prepareStatement("update businesses set name=?, address=?, phoneNumber=?, imgURL=? where businesses.name=?");
            updateInspection = conn.prepareStatement("update inspections set penalty=?, finished=? where inspections.id=?");
            updatePenalty = conn.prepareStatement("update penalties set penaltyDeadline=?, amount=?, missedDeadlinePenalty=?, ceaseOperation=?, report=? where penalties.id=?");
            updateInspectionAddressedTo = conn.prepareStatement("update inspections set addressedTo=? where inspections.id=?");

            createPenalty = conn.prepareStatement("insert into penalties values(?,?,?,?,?,?)");
            newPenaltyID = conn.prepareStatement("select max(id)+1 from penalties");

            deletePenalty = conn.prepareStatement("delete from penalties where id=?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Business> getAllBusinesses() {
        try {
            ResultSet rs = getAllBusinesses.executeQuery();
            ArrayList<Business> businesses = new ArrayList<>();
            while(rs.next()) {
                Business newBusiness = new Business();
                newBusiness.setName(rs.getString(1));
                newBusiness.setAddress(rs.getString(2));
                newBusiness.setPhoneNumber(rs.getString(3));
                newBusiness.setImage(new Image(rs.getString(4)));
                newBusiness.setLastVisit(DAO.stringToDate(rs.getString(5)));
                newBusiness.setNextVisit(DAO.stringToDate(rs.getString(6)));

                businesses.add(newBusiness);
            }

            return businesses;
        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    public void writeBusiness(Business b, String oldName) {
        try {
            updateBusiness.setString(1, b.getName());
            updateBusiness.setString(2, b.getAddress());
            updateBusiness.setString(3, b.getPhoneNumber());
            updateBusiness.setString(4, b.getImage().getUrl());
            updateBusiness.setString(5, oldName);

            updateBusiness.executeUpdate();

            getInspectionIDsForBusiness.setString(1, oldName);
            ResultSet IDs = getInspectionIDsForBusiness.executeQuery();
            while(IDs.next()) {
                updateInspectionAddressedTo.setString(1, b.getName());
                updateInspectionAddressedTo.setInt(2, IDs.getInt(1));
                updateInspectionAddressedTo.executeUpdate();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void writeInspection(Inspection i) {
        try {
            getPenaltyForInspection.setInt(1, i.getId());
            ResultSet penaltyIDRS = getPenaltyForInspection.executeQuery();
            if(penaltyIDRS.next() && penaltyIDRS.getInt(1) != NULL) {
                // a penalty is present
                if(i.getPenalty() == null) {
                    // the present penalty has been erased
                    deletePenalty.setInt(1, penaltyIDRS.getInt(1));
                    deletePenalty.executeUpdate();
                }
                else {
                    // the present penalty has been modified
                    updatePenalty.setString(1, DAO.dateToString(i.getPenalty().getDeadline()));
                    updatePenalty.setInt(2, i.getPenalty().getAmount());
                    updatePenalty.setInt(3, i.getPenalty().getMissedDeadlinePenalty());
                    updatePenalty.setInt(4, i.getPenalty().getCeaseOperation());
                    updatePenalty.setString(5, i.getPenalty().getReport());
                    updatePenalty.setInt(6, penaltyIDRS.getInt(1));
                    updatePenalty.executeUpdate();
                }
                updateInspection.setInt(1, penaltyIDRS.getInt(1));
            }
            else if(i.getPenalty() != null) {
                // no penalty was present, but the user has added one
                ResultSet newPenaltyIDRS = newPenaltyID.executeQuery();
                newPenaltyIDRS.next();
                createPenalty.setInt(1, newPenaltyIDRS.getInt(1));
                createPenalty.setString(2, DAO.dateToString(i.getPenalty().getDeadline()));
                createPenalty.setInt(3, i.getPenalty().getAmount());
                createPenalty.setInt(4, i.getPenalty().getMissedDeadlinePenalty());
                createPenalty.setInt(5, i.getPenalty().getCeaseOperation());
                createPenalty.setString(6, i.getPenalty().getReport());
                createPenalty.executeUpdate();

                updateInspection.setInt(1, newPenaltyIDRS.getInt(1));
            }
            else {
                // no penalty present, finished inspection is penalty free
                updateInspection.setNull(1, Types.INTEGER);
            }

            if(i.getIssuedAt() != null)
                updateInspection.setString(2, DAO.dateToString(i.getIssuedAt()));
            else
                updateInspection.setNull(2, Types.VARCHAR);

            updateInspection.setInt(3, i.getId());
            updateInspection.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
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

        if(date == null) return null;

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

    public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
    }

    public List<Business> getBusinessesForInspector(String usernameHash) {
        try {
            getBusinessesForInspector.setString(1, usernameHash);
            ResultSet rs = getBusinessesForInspector.executeQuery();
            List<Business> assignedBusinesses = new ArrayList<>();
            while(rs.next()) {
                Business newBusiness = new Business();

                newBusiness.setName(rs.getString(1));
                newBusiness.setAddress(rs.getString(2));
                newBusiness.setPhoneNumber(rs.getString(3));
                newBusiness.setImage(new Image(rs.getString(4)));
                newBusiness.setLastVisit(customDateParser(rs.getString(5)));
                newBusiness.setNextVisit(customDateParser(rs.getString(6)));
                newBusiness.setPenalties(getPenaltiesForBusiness(newBusiness));

                assignedBusinesses.add(newBusiness);
            }

            return assignedBusinesses;
        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    public Business getBusinessByName(String name) {
        try {
            getBusinessByName.setString(1, name);
            ResultSet rs = getBusinessByName.executeQuery();
            if(!rs.next()) return null;

            Business newBusiness = new Business();
            newBusiness.setName(rs.getString("name"));
            newBusiness.setAddress(rs.getString("address"));
            newBusiness.setPhoneNumber(rs.getString("phoneNumber"));
            newBusiness.setImage(new Image(rs.getString("imgURL")));
            newBusiness.setLastVisit(customDateParser(rs.getString("lastVisit")));
            newBusiness.setNextVisit(customDateParser(rs.getString("nextVisit")));
            newBusiness.setPenalties(getPenaltiesForBusiness(newBusiness));

            return newBusiness;
        } catch (SQLException | ParseException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    public static Date stringToDate(String str) throws ParseException {
        if(str == null) return null;
        else if(str.equals("today")) return convertToDateViaInstant(LocalDate.now());
        else if(str.equals("tomorrow")) return convertToDateViaInstant(LocalDate.now().plusDays(1));
        else if(str.equals("yesterday")) return convertToDateViaInstant(LocalDate.now().minusDays(1));
        return new SimpleDateFormat("dd.MM.yyyy").parse(str);
    }

    public static String dateToString(Date d) {
        if(d == null) return "";
        return new SimpleDateFormat("dd.MM.yyyy").format(d);
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

                newInspection.setId(rs.getInt("id"));
                newInspection.setSanctionedBy(usernameHash);
                newInspection.setAddressedTo(getBusinessByName(rs.getString("addressedTo")));
                newInspection.setDeadline(customDateParser(rs.getString("deadline")));
                newInspection.setIssuedAt(customDateParser(rs.getString("finished")));
                newInspection.setPenalty(getPenaltyByID(rs.getInt("penalty")));

                assignedInspections.add(newInspection);
            }

            return assignedInspections;
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