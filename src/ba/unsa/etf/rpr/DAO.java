package ba.unsa.etf.rpr;

import ba.unsa.etf.rpr.models.Business;

import javax.xml.transform.Result;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class DAO {
    private static DAO instance = null;
    private static String usernameHash;
    private Connection conn;
    private PreparedStatement getPassword, getBusinessesForInspector;

    private DAO() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:inspection.db");
            PreparedStatement dbExistenceCheck;
            try {
                dbExistenceCheck = conn.prepareStatement("INSERT INTO USERS VALUES (?, ?, ?)");
            } catch (SQLException e) {
                initializeDatabase();
                try {
                    dbExistenceCheck = conn.prepareStatement("INSERT INTO USERS VALUES (?, ?, ?)");
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }

            getPassword = conn.prepareStatement("SELECT passHash, manager FROM USERS WHERE usernameHash=?");
            getBusinessesForInspector = conn.prepareStatement("select * from businesses b " +
                    "where b.name in (select businessName from inspector_business ib where ib.usernameHash=?)");
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
                if (rs.getInt(2) == 1) return true;
                else if (rs.getInt(2) == 0) return false;
            }
            else return null;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    public List<Business> getBusinessesForInspector(String usernameHash) {
        try {
            getBusinessesForInspector.setString(1, usernameHash);
            ResultSet rs = getBusinessesForInspector.executeQuery();
            while(rs.next()) {

            }
        } catch (SQLException throwables) {
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

    public static DAO getInstance(String usernameHash) {
        if (instance == null) instance = new DAO();
        DAO.usernameHash = usernameHash;
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
