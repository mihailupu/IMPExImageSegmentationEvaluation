/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.ifs.mihailupu.impex;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author mihailupu
 */
public class Results {
    /**
     * If the two images match perfectly (except white boundaries)
     */
    public static final int MATCH=1;
    /**
     * the default value
     */
    public static final int UNJUDGED=-1;
    /**
     * if the candidate image is correct, but a subset of the gold image
     */
    public static final int SUBSET=2;
    
    /**
     * if the candidate image is correct, but a supraset of the gold image
     */
    public static final int SUPRASET=3;
    
    /**
     * the candidate image is wrongly segmented
     */
    public static final int ERROR=0;

    private String run;
    private String filename;
    private Integer judgment;
    private String goldFilename;
    private String evaluation;
    private String username;
    private String docID;
    
    MySQL mysql = MySQL.getInstance();

    public Results(String run, String filename, Integer judgment, String goldFilename, String evaluation, String username, String docID) {
        this.run = run;
        this.filename = filename;
        this.judgment = judgment;
        this.goldFilename = goldFilename;
        this.evaluation = evaluation;
        this.username = username;
        this.docID = docID;
    }

    public Results() {
    }

    
    
    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(String evaluation) {
        this.evaluation = evaluation;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    
    
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getGoldFilename() {
        return goldFilename;
    }

    public void setGoldFilename(String goldFilename) {
        this.goldFilename = goldFilename;
    }

    public Integer getJudgment() {
        return judgment;
    }

    public void setJudgment(Integer judgment) {
        this.judgment = judgment;
    }

    public MySQL getMysql() {
        return mysql;
    }

    public void setMysql(MySQL mysql) {
        this.mysql = mysql;
    }

    public String getRun() {
        return run;
    }

    public void setRun(String run) {
        this.run = run;
    }

    /**
     * Populate all the fields of this class with values from the database, for
     * the specific run and candidate filename. It destroys everything that
     * already exists.
     *
     * @param run the run
     * @param filename the candidate image
     * @param user
     * @param evaluation
     * @return the number of matches for this run,filename pair in the database.
     * Anything other than 1 is an error
     */
    public int populateFromDB(String run, String filename, String user, String evaluation) throws SQLException,java.net.ConnectException {
        String query = "select * from imageExtractionEvaluations where evaluation='" + evaluation + "' and user='" + user + "' and name='" + run + "' and filename='"+filename+"';";
        ArrayList<HashMap<String, String>> dbResult = mysql.query(query, new String[]{"name", "description", "ucids"});
        String newRun = dbResult.get(0).get("run");
        String newFilename = dbResult.get(0).get("filename");
        String newJudgment = dbResult.get(0).get("judgment");
        String newGoldFilename = dbResult.get(0).get("goldFilename");
        if (newRun != null) {
            this.setRun(newRun);
            this.setFilename(newFilename);
            this.setJudgment(Integer.parseInt(newJudgment));
            this.setGoldFilename(newGoldFilename);
            return dbResult.size();
        } else {
            return 0;
        }
    }

    /**
     * Stores this instance into the database. If the name already exists, an
     * exception will be thrown.
     */
    public void storeToDB() throws SQLException ,java.net.ConnectException{
        String query = "insert into imageExtractionResults values ('"
                + this.getRun() + "','"
                + this.getFilename() + "','"
                + this.getJudgment() + "','"                
                + this.getGoldFilename() + "','"
                + this.getEvaluation() + "','"
                + this.getUsername() + "','"
                + this.getDocID() + "'"
                + ")";
        mysql.Update(query);
    }
    
        /**
     * Deletes this instance into the database. Every field must match in order to be deleted.
     */
    public void deleteFromDB() throws SQLException ,java.net.ConnectException{
        String query = "delete from imageExtractionResults where run='"
                + this.getRun() + "' and filename='"
                + this.getFilename() + "' and judgement='"
                + this.getJudgment() + "'and goldFilename='"                
                + this.getGoldFilename() + "'and evaluation='"
                + this.getEvaluation() + "'and user='"
                + this.getUsername() + "'and docId='"
                + this.getDocID() + "'"
                + "";
        mysql.Update(query);
    }
    
   
}
