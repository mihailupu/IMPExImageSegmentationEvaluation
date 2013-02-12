package at.ac.tuwien.ifs.mihailupu.impex;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MySQL extends Config implements Serializable {

    private Connection conn = null;
    public String connString = "jdbc:mysql://";
    private static String connClass = "com.mysql.jdbc.Driver";
    private static MySQL instance;

    private MySQL() {
        try {
            Class.forName(connClass).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        connString += this.getWebHost() + ":" + this.getWebPort() + "/" + this.getWebDB();
        connString += "?user=" + this.getWebUser() + "&password=" + this.getWebPass();
    }

    public static MySQL getInstance() {
        if (instance == null) {
            instance = new MySQL();
        }
        return instance;
    }

    public Boolean checkConnect() throws SQLException ,java.net.ConnectException {
        Boolean result = true;
        connectDB();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException sqlEx) {
            }
            conn = null;
        } else {
            result = false;
        }
        return result;
    }

    private void connectDB() throws SQLException,java.net.ConnectException {
            conn = DriverManager.getConnection(connString);
            if (conn==null){
                throw new SQLException("Connection to database returned NULL");
            }
    }

    public ArrayList<HashMap<String, String>> query(String Query, String[] listField) throws SQLException,java.net.ConnectException {

        ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> row;
        Statement stmt;
        ResultSet rs;
        connectDB();
        stmt = conn.createStatement();

        rs = stmt.executeQuery(Query);

        while (rs.next()) {
            row = new HashMap<String, String>();
            for (int i = 0; i < listField.length; i++) {
                row.put(listField[i], rs.getString(listField[i]));
            }
            result.add(row);
        }
        if (rs != null) {
            rs.close();
        }
        if (stmt != null) {
            stmt.close();
        }
        if (conn != null) {
            conn.close();
        }

        return result;
    }

    public int Update(String Query) throws SQLException,java.net.ConnectException {
        Statement stmt;
        ResultSet rs = null;
        int result;
        connectDB();
        stmt = conn.createStatement();
        result = stmt.executeUpdate(Query);
        if (rs != null) {
            rs.close();
        }
        if (stmt != null) {
            stmt.close();
        }
        if (conn != null) {
            conn.close();
        }
        return result;
    }

    protected Connection getConnection() throws SQLException,java.net.ConnectException {
        if (conn.isClosed()) connectDB();
        return conn;
    }
}
