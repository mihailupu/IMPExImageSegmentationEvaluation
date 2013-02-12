/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.ifs.mihailupu.impex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author mihailupu
 */
@ManagedBean(name = "IIEEBean")
@SessionScoped
public class IIEEBean implements Serializable {

    private transient UploadedFile testSetFile;
    private transient UploadedFile testSetFolder;
    private transient ArrayList<EvaluationSet> evaluationSets;
    private String newEvaluationName;
    private String newEvaluationDescription;
    private String currentEvaluationSet;
    private String localFilePath;
    private Integer dcounter;
    ArrayList<String> descriptionParagraphs1 = new ArrayList<String>();
    ArrayList<String> descriptionParagraphs2 = new ArrayList<String>();
    @ManagedProperty(value = "#{userManager}")
    private UserManager userManager;

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager neededBean) {
        this.userManager = neededBean;
        localFilePath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("secure/") +"/";
        Config.init(localFilePath);
    }

    public int getDcounter() {
        return dcounter;
    }

    public void setDcounter(int dcounter) {
        this.dcounter = dcounter;
        descriptionParagraphs1 = new ArrayList<String>();
        descriptionParagraphs2 = new ArrayList<String>();

    }

    public List<String> getDescriptionParagraphs1() {
        return descriptionParagraphs1;
    }

    public void setDescriptionParagraphs1(ArrayList<String> descriptionParagraphs1) {
        this.descriptionParagraphs1 = descriptionParagraphs1;
    }

    public List<String> getDescriptionParagraphs2() {
        return descriptionParagraphs2;
    }

    public void setDescriptionParagraphs2(ArrayList<String> descriptionParagraphs2) {
        this.descriptionParagraphs2 = descriptionParagraphs2;
    }

    public IIEEBean() {
        //init();
    }

    public String getCurrentEvaluationSet() {
        return currentEvaluationSet;
    }

    public void setCurrentEvaluationSet(String currentEvaluationSet) {
        this.currentEvaluationSet = currentEvaluationSet;
    }

    public String getNewEvaluationDescription() {
        return newEvaluationDescription;
    }

    public void setNewEvaluationDescription(String newEvaluationDescription) {
        this.newEvaluationDescription = newEvaluationDescription;
    }

    public String getNewEvaluationName() {
        return newEvaluationName;
    }

    public void setNewEvaluationName(String newEvaluationName) {
        this.newEvaluationName = newEvaluationName;
    }

    /**
     * this method always looks into the database and populates this array with
     * every evaluation set that exists there.
     *
     * @return the set of currently defined evaluation sets in the database
     */
    public ArrayList<EvaluationSet> getEvaluationSets() {

        try {
            EvaluationSet es = new EvaluationSet();
            ArrayList<String> existingNames = es.getAllNamesFromDB();
            evaluationSets = new ArrayList<EvaluationSet>();
            for (String name : existingNames) {
                es = new EvaluationSet();
                es.populateFromDB(name);
                evaluationSets.add(es);
            }
        } catch (SQLException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    "SQL error", "Please check Tomcat logs for details.");
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(IIEEBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (java.net.ConnectException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", "Please check Tomcat logs for details.");
            FacesContext.getCurrentInstance().addMessage(null, error);
            ex.printStackTrace();
            Logger.getLogger(IIEEBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return evaluationSets;
    }

    public void setEvaluationSets(ArrayList<EvaluationSet> evaluationSets) {
        this.evaluationSets = evaluationSets;
    }

    public UploadedFile getTestSetFile() {
        return testSetFile;
    }

    public void setTestSetFile(UploadedFile testSetFile) {
        this.testSetFile = testSetFile;
    }

    public void handleFileUpload() {

        ArrayList<String> existingNames;
        try {
            EvaluationSet es = new EvaluationSet();

            existingNames = es.getAllNamesFromDB();

            if (existingNames.contains(this.getNewEvaluationName())) {

                FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "The evaluation name already exists!", "");
                FacesContext.getCurrentInstance().addMessage(null, error);

            } else {
                EvaluationSet newES = new EvaluationSet();
                newES.setName(newEvaluationName);
                newES.setDescription(newEvaluationDescription);
                ArrayList<String> ucids = new ArrayList<String>();
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(this.getTestSetFile().getInputstream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().length() > 3) {
                            ucids.add(line.trim());
                        }
                    }
                    newES.setUcids(ucids);
                    br.close();

                    FacesMessage msg = new FacesMessage("Succesful", "Found " + ucids.size() + " ucids to test.");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    newES.storeToDB();
                    this.evaluationSets.add(newES);

                } catch (IOException e) {
                    Logger.getLogger(IIEEBean.class.getName()).log(Level.SEVERE, null, e);

                    FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "The files were not uploaded!", "");
                    FacesContext.getCurrentInstance().addMessage(null, error);
                }
            }
        } catch (SQLException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", "Please check Tomcat logs for details.");
            FacesContext.getCurrentInstance().addMessage(null, error);
            ex.printStackTrace();
            Logger.getLogger(IIEEBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (java.net.ConnectException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", "Please check Tomcat logs for details.");
            FacesContext.getCurrentInstance().addMessage(null, error);
            ex.printStackTrace();
            Logger.getLogger(IIEEBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
