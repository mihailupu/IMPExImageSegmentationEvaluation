/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.ifs.mihailupu.impex;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
@ManagedBean
@SessionScoped
public class RunEvalBean implements Serializable {

    private UploadedFile runFile;
    private transient ArrayList<Run> runs;
    private String newRunName;
    private String newRunDescription;
    private String currentEvaluationSet;
    private String localFilePath;
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
    //private 
    public RunEvalBean() {
        //init();
    }

    public String getCurrentEvaluationSet() {
        return currentEvaluationSet;
    }

    public void setCurrentEvaluationSet(String currentEvaluationSet) {
        this.currentEvaluationSet = currentEvaluationSet;
    }

    public String getNewRunDescription() {
        return newRunDescription;
    }

    public void setNewRunDescription(String newRunDescription) {
        this.newRunDescription = newRunDescription;
    }

    public String getNewRunName() {
        return newRunName;
    }

    public void setNewRunName(String newRunName) {
        this.newRunName = newRunName;
    }

    /**
     * this method always looks into the database and populates this array with
     * every evaluation set that exists there.
     *
     * @return the set of currently defined evaluation sets in the database
     */
    public ArrayList<Run> getRuns() {
        try {
            Run run = new Run();
            ArrayList<String> existingNames = run.getAllNamesFromDB();
            runs = new ArrayList<Run>();
            for (String name : existingNames) {
                run = new Run();
                run.populateFromDB(name);
                runs.add(run);
            }
        } catch (SQLException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", "Please check Tomcat logs for details.");
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(RunEvalBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (java.net.ConnectException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", "Please check Tomcat logs for details.");
            FacesContext.getCurrentInstance().addMessage(null, error);
            ex.printStackTrace();
            Logger.getLogger(IIEEBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return runs;
    }

    public void setRuns(ArrayList<Run> runs) {
        this.runs = runs;
    }

    public UploadedFile getRunFile() {
        return runFile;
    }

    public void setRunFile(UploadedFile runFile) {
        this.runFile = runFile;
    }

    public void handleFileUpload() {
        ArrayList<String> existingNames;
        try {
            Run run = new Run();

            existingNames = run.getAllNamesFromDB();

            if (existingNames.contains(this.getNewRunName())) {

                FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "The run name already exists!", "");
                FacesContext.getCurrentInstance().addMessage(null, error);

            } else {
                if (getRunFile().getSize() > Math.pow(2, 24)) {
                    FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "The ZIP file is too big. Maximum size: 16MB. Given size: " + new DecimalFormat("#.##").format(getRunFile().getSize() / 1048576.0f) + "MB", "");
                    FacesContext.getCurrentInstance().addMessage(null, error);
                } else {
                    Run newRun = new Run();
                    newRun.setName(newRunName);
                    newRun.setDescription(newRunDescription);
                    newRun.setEvaluation(currentEvaluationSet);
                    newRun.setRunData(getRunFile().getContents());
                    newRun.setProgress(0f);
                    FacesMessage msg = new FacesMessage("Succesful", "Uploaded " + new DecimalFormat("#.##").format(getRunFile().getSize() / 1048576.0f) + " MB.");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    newRun.storeToDB();
                    this.runs.add(newRun);
                }
            }
        } catch (IOException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "IO error", "Please check Tomcat logs for details.");
            FacesContext.getCurrentInstance().addMessage(null, error);
            ex.printStackTrace();
            Logger.getLogger(RunEvalBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", "Please check Tomcat logs for details.");
            FacesContext.getCurrentInstance().addMessage(null, error);
            ex.printStackTrace();
            Logger.getLogger(RunEvalBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
