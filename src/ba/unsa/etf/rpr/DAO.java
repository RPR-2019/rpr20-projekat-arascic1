package ba.unsa.etf.rpr;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class DAO {
    private static DAO instance = null;
    private Connection conn;
    private PreparedStatement getPassword;

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

            getPassword = conn.prepareStatement("SELECT passHash FROM USERS WHERE usernameHash=?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean authenticate(String usernameHash, String passHash) {
        try {
            getPassword.setString(1, usernameHash);
            ResultSet rs = getPassword.executeQuery();
            if(!rs.next()) return false;
            return rs.getString(1).equals(passHash);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return false;
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
