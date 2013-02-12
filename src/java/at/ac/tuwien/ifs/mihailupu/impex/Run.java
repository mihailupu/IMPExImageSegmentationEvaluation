/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.ifs.mihailupu.impex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author mihailupu
 */
public class Run implements Serializable {

    private String name;
    private String description;
    private String evaluation;
    private float progress;
    /**
     * this is the content of the zip file *
     */
    private byte[] runData;
    MySQL mysql = MySQL.getInstance();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(String evaluation) {
        this.evaluation = evaluation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getRunData() {
        return runData;
    }

    public void setRunData(byte[] runData) {
        this.runData = runData;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public ArrayList<String> getAllNamesFromDB() throws SQLException,java.net.ConnectException {
        ArrayList<String> result = new ArrayList<String>();
        String query = "select name from imageExtractionRuns;";
        ArrayList<HashMap<String, String>> dbResult = mysql.query(query, new String[]{"name"});
        for (int i = 0; i < dbResult.size(); i++) {
            result.add(dbResult.get(i).get("name"));
        }
        return result;
    }

    public int populateFromDB(String runName) throws SQLException,java.net.ConnectException {
        String query = "select * from imageExtractionRuns where name='" + runName + "';";
        Connection conn = mysql.getConnection();
        Statement stmt;
        ResultSet rs;
        stmt = conn.createStatement();

        rs = stmt.executeQuery(query);

        while (rs.next()) {
            evaluation = rs.getString("evaluation");
            name = rs.getString("name");
            description = rs.getString("description");
            progress = rs.getFloat("progress");
            Blob zipBlob = rs.getBlob("zip");
            long zipBlobSize = zipBlob.length();

            if (zipBlobSize < Integer.MAX_VALUE) {
                runData = zipBlob.getBytes(1, (int) zipBlobSize);
            } else {
                throw new RuntimeException("Somehow, the run " + name + " managed to store a blob larger than what is allowed in "
                        + "the mysql mediumblob type. The size found was: " +  new DecimalFormat("#.##").format(zipBlobSize/1048576.0f)+"MB");
            }
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

        return 0;

    }

    /**
     * Stores this instance into the database. If the name already exists, an
     * exception will be thrown.
     */
    public void storeToDB() throws SQLException, IOException {
        Connection conn = mysql.getConnection();
        PreparedStatement stmt = conn.prepareStatement("insert into imageExtractionRuns(evaluation,name,description,zip,progress) values (?,?,?,?,?)");

        try {
            conn.setAutoCommit(false);
            stmt.setString(1, evaluation);
            stmt.setString(2, name);
            stmt.setString(3, description);
            stmt.setBlob(4, new ByteArrayInputStream(runData));
            stmt.setFloat(5, progress);
            stmt.executeUpdate();
            conn.commit();
        } finally {
            stmt.close();
            conn.close();
        }
    }
}
