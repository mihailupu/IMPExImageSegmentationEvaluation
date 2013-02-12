/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.ifs.mihailupu.impex;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 *
 * @author mihailupu
 */
public class EvaluationSet implements Serializable{
    
    private String name;
    private String description;
    
    private ArrayList<String> ucids;
    MySQL mysql = MySQL.getInstance();
    
 
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getUcids() {
        return ucids;
    }

    public void setUcids(ArrayList<String> ucids) {
        this.ucids = ucids;
    }
    
    public   ArrayList<String> getAllNamesFromDB() throws SQLException,java.net.ConnectException{
        ArrayList<String> result = new ArrayList<String>();
        String query="select name from imageExtractionEvaluations;";
        ArrayList<HashMap<String,String>> dbResult=mysql.query(query, new String[]{"name"});
        for (int i = 0 ;i <dbResult.size();i++){
            result.add(dbResult.get(i).get("name"));
        }
        return result;
    }
    
    /**
     * Populate all the fields of this class with values from the database, for the specific name. 
     * It destroys everything that already exists.
     * @param name
     * @return the number of matches for this name in the database. Anything other than 1 is an error
     */
    public int populateFromDB(String name) throws SQLException,java.net.ConnectException{
        String query="select * from imageExtractionEvaluations where name='"+name+"';";
         ArrayList<HashMap<String,String>> dbResult=mysql.query(query, new String[]{"name","description","ucids"});
        String newName=dbResult.get(0).get("name");
        String newDescription=dbResult.get(0).get("description");
        String newUcids=dbResult.get(0).get("ucids");
        if (newUcids!=null){
        ArrayList<String>newUcidsAL=new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(newUcids);
        while (st.hasMoreTokens()){
            newUcidsAL.add(st.nextToken());
        }
        this.setName(newName);
        this.setDescription(newDescription);
        this.setUcids(newUcidsAL);
        return dbResult.size();
        }else{
            return 0;
        }
    }
    
    /**
     * Stores this instance into the database. If the name already exists, an exception will be thrown.
     */
    public void storeToDB() throws SQLException,java.net.ConnectException{
        String ucidsString = "";
        for (String ucid:ucids){
            ucidsString = ucidsString.concat(" "+ucid);
        }
        String query="insert into imageExtractionEvaluations values ('"
                + this.getName()+"','"
                + this.getDescription()+"','"
                + ucidsString+"')";
         mysql.Update(query);
    }
    
}
