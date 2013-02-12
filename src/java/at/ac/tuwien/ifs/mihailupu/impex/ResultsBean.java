/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.ifs.mihailupu.impex;

import java.io.Serializable;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author mihailupu
 */
@ManagedBean
@SessionScoped
public class ResultsBean implements Serializable {

    @ManagedProperty(value = "#{userManager}")
    private UserManager userManager;
    private String localFilePath;
    private MySQL mysql = MySQL.getInstance();

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager neededBean) {
        this.userManager = neededBean;
        localFilePath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("secure/") + "/";
        Config.init(localFilePath);
    }
    
    private String currentEvaluationSet;
    private String currentRun;
    private double percentageDuplicates;
    private double percentageCandidatesMatched;
    private double percentageGoldMatched;
    private double percentageCandidatesSubImage;
    private double percentageGoldSubImage;
    private double percentageCandidatesSupraImage;
    private double percentageGoldSupraImage;
    private int candidatesInRun;
    private int goldInRun;

    public ResultsBean() {
    }

    public int getCandidatesInRun() {
        try {
            String query = "select count(*) as c from imageExtractionCandidates where "
                    + "evaluation='" + currentEvaluationSet + "' and "
                    + "run='" + currentRun + "'";
            ArrayList<HashMap<String, String>> dbResults = mysql.query(query, new String[]{"c"});
            if (dbResults.size() > 0) {
                candidatesInRun = Integer.valueOf(dbResults.get(0).get("c"));
            }

        } catch (SQLException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(ResultsBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConnectException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(ResultsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return candidatesInRun;
    }

    private void setCandidatesInRun(int candidatesInRun) {
        this.candidatesInRun = candidatesInRun;
    }

    public int getGoldInRun() {
        try {
            String query = "select count(*) as c from imageExtractionGold where "
                    + "evaluation='" + currentEvaluationSet + "' and "
                    + "run='" + currentRun + "'";
            ArrayList<HashMap<String, String>> dbResults = mysql.query(query, new String[]{"c"});
            if (dbResults.size() > 0) {
                goldInRun = Integer.valueOf(dbResults.get(0).get("c"));
            }

        } catch (SQLException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(ResultsBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConnectException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(ResultsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return goldInRun;
    }

    private void setGoldInRun(int goldInRun) {
        this.goldInRun = goldInRun;
    }

    public String getCurrentEvaluationSet() {
        return currentEvaluationSet;
    }

    public void setCurrentEvaluationSet(String currentEvaluationSet) {
        this.currentEvaluationSet = currentEvaluationSet;
    }

    public String getCurrentRun() {
        return currentRun;
    }

    public void setCurrentRun(String currentRun) {
        this.currentRun = currentRun;
    }

    private int countCandidateRelationsInDB(int relationType) {
        int result = 0;
        try {
            String query = "select count(distinct(filename)) as c from imageExtractionResults where "
                    + "evaluation='" + currentEvaluationSet + "' and "
                    + "run='" + currentRun + "' and "
                    + "judgement=" + relationType;
            ArrayList<HashMap<String, String>> dbResults = mysql.query(query, new String[]{"c"});
            if (dbResults.size() > 0) {
                result = Integer.valueOf(dbResults.get(0).get("c"));
            }

        } catch (SQLException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(ResultsBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConnectException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(ResultsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public double getPercentageCandidatesMatched() {
        int matchedCandidates = countCandidateRelationsInDB(1);
        if (candidatesInRun != 0) {
            percentageCandidatesMatched = (double) matchedCandidates / (double) candidatesInRun;
        } else {
            percentageCandidatesMatched = -1;
        }
        return percentageCandidatesMatched;
    }

    private void setPercentageCandidatesMatched(double percentageCandidatesMatched) {
        this.percentageCandidatesMatched = percentageCandidatesMatched;
    }

    public double getPercentageCandidatesSubImage() {
        int matchedCandidates = countCandidateRelationsInDB(2);
        if (candidatesInRun != 0) {
            percentageCandidatesSubImage = (double) matchedCandidates / (double) candidatesInRun;
        } else {
            percentageCandidatesSubImage = -1;
        }
        return percentageCandidatesSubImage;
    }

    private void setPercentageCandidatesSubImage(double percentageCandidatesSubImage) {
        this.percentageCandidatesSubImage = percentageCandidatesSubImage;
    }

    public double getPercentageCandidatesSupraImage() {
        int matchedCandidates = countCandidateRelationsInDB(3);
        if (candidatesInRun != 0) {
            percentageCandidatesSupraImage = (double) matchedCandidates / (double) candidatesInRun;
        } else {
            percentageCandidatesSupraImage = -1;
        }
        return percentageCandidatesSupraImage;
    }

    private void setPercentageCandidatesSupraImage(double percentageCandidatesSupraImage) {
        this.percentageCandidatesSupraImage = percentageCandidatesSupraImage;
    }

    /**
     * This uses the number of elements in the imageExtractionDuplicates table.
     * This may not necessarily be correct, since this table does not record
     * which evaluation these duplicates belong to. @TODO verify and potentially
     * correct this
     *
     * @return
     */
    public double getPercentageDuplicates() {
        int result = 0;
        try {
            String query = "select count(distinct(goldFilename)) as c from imageExtractionDuplicates ";
            ArrayList<HashMap<String, String>> dbResults = mysql.query(query, new String[]{"c"});
            if (dbResults.size() > 0) {
                result = Integer.valueOf(dbResults.get(0).get("c"));
            }

        } catch (SQLException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(ResultsBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConnectException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(ResultsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (goldInRun > 0) {
            percentageDuplicates = (double) result / (double) goldInRun;
        } else {
            percentageDuplicates = -1;
        }
        return percentageDuplicates;
    }

    private void setPercentageDuplicates(double percentageDuplicates) {
        this.percentageDuplicates = percentageDuplicates;
    }

    private int countGoldRelationsInDB(int relationType) {
        int result = 0;
        try {
            String query = "select count(distinct(goldFilename)) as c from imageExtractionResults where "
                    + "evaluation='" + currentEvaluationSet + "' and "
                    + "run='" + currentRun + "' and "
                    + "judgement=" + relationType;
            ArrayList<HashMap<String, String>> dbResults = mysql.query(query, new String[]{"c"});
            if (dbResults.size() > 0) {
                result = Integer.valueOf(dbResults.get(0).get("c"));
            }

        } catch (SQLException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(ResultsBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConnectException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(ResultsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public double getPercentageGoldMatched() {
        int matched = countGoldRelationsInDB(1);
        if (goldInRun != 0) {
            percentageGoldMatched = (double) matched / (double) goldInRun;
        } else {
            percentageGoldMatched = -1;
        }
        return percentageGoldMatched;
    }

    private void setPercentageGoldMatched(double percentageGoldMatched) {
        this.percentageGoldMatched = percentageGoldMatched;
    }

    public double getPercentageGoldSubImage() {
        int matched = countGoldRelationsInDB(2);
        if (goldInRun != 0) {
            percentageGoldSubImage = (double) matched / (double) goldInRun;
        } else {
            percentageGoldSubImage = -1;
        }
        return percentageGoldSubImage;
    }

    private void setPercentageGoldSubImage(double percentageGoldSubImage) {
        this.percentageGoldSubImage = percentageGoldSubImage;
    }

    public double getPercentageGoldSupraImage() {
        int matched = countGoldRelationsInDB(3);
        if (goldInRun != 0) {
            percentageGoldSupraImage = (double) matched / (double) goldInRun;
        } else {
            percentageGoldSupraImage = -1;
        }
        return percentageGoldSupraImage;
    }

    private void setPercentageGoldSupraImage(double percentageGoldSupraImage) {
        this.percentageGoldSupraImage = percentageGoldSupraImage;
    }
}
