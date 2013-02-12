/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.ifs.mihailupu.impex;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author mihailupu
 */
@ManagedBean
@SessionScoped
public class ImageListsBean implements Serializable {

    private String localFilePath;
    private String currentRun;
    private Run runData;
    private int patentCounter;
    private String currentUcid;
    private EvaluationSet evaluationSet = new EvaluationSet();
    private int evaluationSetSize;
    private int currentCandidate = -1;
    private int currentGold = -1;
    private ArrayList<Image> candidates = new ArrayList<Image>();
    private ArrayList<Image> gold = new ArrayList<Image>();
    private int candidateImageWidth = 150;
    private int goldImageWidth = 150;
    private ArrayList<Results> results = new ArrayList<Results>();
    private ArrayList<Image> duplicatesGold = new ArrayList<Image>();
    private String user = "unknown";
    private String evaluation;
    @ManagedProperty(value = "#{userManager}")
    private UserManager userManager;

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager neededBean) {
        this.userManager = neededBean;
        user = userManager.getCurrent().getUsername();
        localFilePath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("secure/") + "/";
        Config.init(localFilePath);
    }

    public ImageListsBean() {
        //init();
    }

    public String getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(String evaluation) {
        this.evaluation = evaluation;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public ArrayList<Image> getDuplicatesGold() {
        return duplicatesGold;
    }

    public void setDuplicatesGold(ArrayList<Image> duplicatesGold) {
        this.duplicatesGold = duplicatesGold;
    }

    public int getCurrentGold() {
        return currentGold;
    }

    public void setCurrentGold(int currentGold) {
        this.currentGold = currentGold;
    }

    public ArrayList<Results> getResults() {
        return results;
    }

    public void setResults(ArrayList<Results> results) {
        this.results = results;
    }

    public Run getRunData() {
        return runData;
    }

    public void setRunData(Run runData) {
        this.runData = runData;
    }

    public int getCandidateImageWidth() {
        return candidateImageWidth;
    }

    public void setCandidateImageWidth(int candidateImageWidth) {
        this.candidateImageWidth = candidateImageWidth;
    }

    public int getGoldImageWidth() {
        return goldImageWidth;
    }

    public void setGoldImageWidth(int goldImageWidht) {
        this.goldImageWidth = goldImageWidht;
    }

    public int getCurrentCandidate() {
        return currentCandidate;
    }

    public void setCurrentCandidate(int currentCandidate) {
        this.currentCandidate = currentCandidate;
    }

    public void unMatch() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map map = context.getExternalContext().getRequestParameterMap();
        String filename = (String) map.get("filename");
        String goldFilename = (String) map.get("goldFilename");

        for (int i = 0; i < results.size(); i++) {
            try {
                Results r = results.get(i);

                if (r.getFilename().equals(filename)
                        && r.getGoldFilename().equals(goldFilename)) {

                    r.deleteFromDB();
                    results.remove(i);
                    int j = gold.indexOf(new Image(r.getGoldFilename(), "img"));

                    gold.get(j).setStyle("img");
                    gold.get(j).setIsGoldDuplicate(false);
                    gold.get(j).setIsGoldMatch(false);
                    gold.get(j).setIsGoldSubimage(false);
                    gold.get(j).setIsGoldSupraimage(false);
                    gold.get(j).setVisible(true);

                    j = candidates.indexOf(new Image(r.getFilename(), "img"));

                    candidates.get(j).setStyle("img");
                    candidates.get(j).setIsCandidateError(false);
                    candidates.get(j).setIsCandidateSelected(false);
                    candidates.get(j).setVisible(true);

                }
            } catch (SQLException ex) {
                FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "SQL Error",
                        ex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, error);
                Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ConnectException ex) {
                FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "SQL Error",
                        ex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, error);
                Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void unDuplicate() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map map = context.getExternalContext().getRequestParameterMap();
        String source = (String) map.get("javax.faces.source");
        String path = (String) map.get("path");
        for (int i = 0; i < gold.size(); i++) {
            if (gold.get(i).path.equals(path)) {
                gold.get(i).setStyle("img");
                gold.get(i).setIsGoldDuplicate(false);
                gold.get(i).setVisible(true);
                duplicatesGold.remove(gold.get(i));
                try {
                    String query = "delete from imageExtractionDuplicates where goldFilename='"
                            + path + "'and user='"
                            + user + "'and docId='"
                            + currentUcid + "'"
                            + "";
                    MySQL.getInstance().Update(query);
                } catch (SQLException ex) {
                    FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "SQL Error",
                            ex.getLocalizedMessage());
                    FacesContext.getCurrentInstance().addMessage(null, error);
                    Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ConnectException ex) {
                    FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "SQL Error",
                            ex.getLocalizedMessage());
                    FacesContext.getCurrentInstance().addMessage(null, error);
                    Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void processSelection() {
        System.out.println("CurrentGold:" + currentGold);
        System.out.println("CurrentCandidate:" + currentCandidate);
        if (currentGold >= 0 && gold.get(currentGold).isGoldDuplicate) {

            try {
                duplicatesGold.add(gold.get(currentGold));
                gold.get(currentGold).setVisible(false);
                String query = "insert into imageExtractionDuplicates values ('"
                        + gold.get(currentGold).getPath() + "','"
                        + user + "','"
                        + currentUcid + "'"
                        + ")";
                MySQL.getInstance().Update(query);
                currentGold = -1;

            } catch (SQLException ex) {
                FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "SQL Error",
                        ex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, error);
                Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ConnectException ex) {
                FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "SQL Error",
                        ex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, error);
                Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
            }

            return;
        }
        if (currentGold < 0 || currentCandidate < 0) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Empty",
                    "No selection made in either Gold or Run list");
            FacesContext.getCurrentInstance().addMessage(null, error);
        } else {
            try {
                Results r = new Results();
                r.setFilename(candidates.get(currentCandidate).path);
                r.setGoldFilename(gold.get(currentGold).path);
                if (gold.get(currentGold).isGoldMatch) {
                    r.setJudgment(Results.MATCH);
                } else if (gold.get(currentGold).isGoldSubimage) {
                    r.setJudgment(Results.SUBSET);
                } else if (gold.get(currentGold).isGoldSupraimage) {
                    r.setJudgment(Results.SUPRASET);
                } else {
                    FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unknown",
                            "No known relation type indicated.");
                    FacesContext.getCurrentInstance().addMessage(null, error);
                    return;
                }
                r.setDocID(currentUcid);
                r.setUsername(user);
                r.setEvaluation(evaluation);
                r.setRun(currentRun);
                results.add(r);
                r.storeToDB();
                if (r.getJudgment() != Results.SUPRASET) {
                    gold.get(currentGold).setVisible(false);
                    currentGold = -1;

                }
                if (r.getJudgment() != Results.SUBSET) {
                    candidates.get(currentCandidate).setVisible(false);
                    currentCandidate = -1;

                }
            } catch (SQLException ex) {
                FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "SQL Error",
                        ex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, error);
                Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ConnectException ex) {
                FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "SQL Error",
                        ex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, error);
                Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void toggleGoldImage() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map map = context.getExternalContext().getRequestParameterMap();
        String source = (String) map.get("javax.faces.source");
        String path = (String) map.get("cf");
        // source is the anchor id surrounding the image: formForList:candidatesList:0:j_idt14        
        currentGold = Integer.parseInt(source.split(":")[2]);

        if (gold.get(currentGold).isGoldMatch) {
            gold.get(currentGold).setIsGoldMatch(false);
            gold.get(currentGold).setIsGoldDuplicate(true);
            gold.get(currentGold).setStyle("img-goldselectedDuplicate");
        } else if (gold.get(currentGold).isGoldDuplicate) {
            gold.get(currentGold).setIsGoldDuplicate(false);
            gold.get(currentGold).setIsGoldSubimage(true);
            gold.get(currentGold).setStyle("img-goldselectedSubImage");
        } else if (gold.get(currentGold).isGoldSubimage) {
            gold.get(currentGold).setIsGoldSubimage(false);
            gold.get(currentGold).setIsGoldSupraimage(true);
            gold.get(currentGold).setStyle("img-goldselectedSupraImage");
        } else if (gold.get(currentGold).isGoldSupraimage) {
            gold.get(currentGold).setIsGoldSupraimage(false);
            gold.get(currentGold).setStyle("img");
            currentGold = -1;//it was deselected
        } else {
            gold.get(currentGold).setIsGoldMatch(true);
            gold.get(currentGold).setStyle("img-goldselectedMatch");
        }

        for (int i = 0; i < gold.size(); i++) {
            if (i != currentGold) {
                gold.get(i).setStyle("img");
                gold.get(i).setIsCandidateError(false);
                gold.get(i).setIsCandidateSelected(false);
                gold.get(i).setIsGoldDuplicate(false);
                gold.get(i).setIsGoldMatch(false);
                gold.get(i).setIsGoldSubimage(false);
                gold.get(i).setIsGoldSupraimage(false);
            }

        }

        setGold(gold);
//        System.out.println(currentGold + " has path " + path);
//        FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_INFO,
//                currentGold + " ", path);
//        FacesContext.getCurrentInstance().addMessage(null, error);
    }

    public void toggleRunImage() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map map = context.getExternalContext().getRequestParameterMap();
        String source = (String) map.get("javax.faces.source");
        String path = (String) map.get("cf");
        // source is the anchor id surrounding the image: formForList:candidatesList:0:j_idt14        
        currentCandidate = Integer.parseInt(source.split(":")[2]);
        if (candidates.get(currentCandidate).isCandidateSelected) {
            for (int i = 0; i < candidates.size(); i++) {
                candidates.get(i).setStyle("img");
                candidates.get(i).setIsCandidateSelected(false);
                currentCandidate = -1;
            }
        } else {
            for (int i = 0; i < candidates.size(); i++) {
                candidates.get(i).setStyle("img");
                candidates.get(i).setIsCandidateSelected(false);

            }
            candidates.get(currentCandidate).setStyle("img-candidateSelected");
            candidates.get(currentCandidate).setIsCandidateSelected(true);

        }


        setCandidates(candidates);
//        System.out.println(currentCandidate + " has path " + path);
//        FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_INFO,
//                currentCandidate + " ", path);
//        FacesContext.getCurrentInstance().addMessage(null, error);
//        //   return null;
    }

    public int getEvaluationSetSize() {
        return evaluationSet.getUcids().size();
    }

    private void setEvaluationSetSize(int evaluationSetSize) {
        this.evaluationSetSize = evaluationSetSize;
    }

    public String getCurrentRun() {
        return currentRun;
    }

    public void setCurrentRun(String currentRun) {
        if (!currentRun.equals(this.currentRun)) {
            try {
                this.currentRun = currentRun;
                deleteTempFiles(this.currentRun);
                createTempFilesFromZipData();
                evaluationSet.populateFromDB(runData.getEvaluation());
                setEvaluationSetSize(evaluationSet.getUcids().size());
                setPatentCounter(0);
            } catch (SQLException ex) {
                FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "SQL error", ex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, error);
                ex.printStackTrace();
                Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
            } catch (java.net.ConnectException ex) {
                FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "SQL error", ex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, error);
                ex.printStackTrace();
                Logger.getLogger(IIEEBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public int getPatentCounter() {
        return patentCounter;
    }

    public void setPatentCounter(int patentCounter) {
        this.patentCounter = patentCounter;

        if (patentCounter < evaluationSet.getUcids().size()) {
            setCurrentUcid(evaluationSet.getUcids().get(patentCounter));
        }
    }

    public void nextDocument() {
        if (patentCounter < evaluationSetSize) {
            setPatentCounter(patentCounter + 1);
        } else {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Done", "No more documents in this run.");
            FacesContext.getCurrentInstance().addMessage(null, error);
        }
    }

    public void previousDocument() {
        if (patentCounter > 0) {
            setPatentCounter(patentCounter - 1);
        } else {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Done", "No more documents in this run.");
            FacesContext.getCurrentInstance().addMessage(null, error);
        }
    }

    public void done() {
        System.out.println("done function called");
    }

    public String getCurrentUcid() {
        return currentUcid;
    }

    public void setCurrentUcid(String currentUcid) {
        this.currentUcid = currentUcid;
        populateGold(currentUcid);
        populateCandidates(currentUcid);
        updateWithExistingResults();
        updateWithExistingDuplicates(currentUcid);
    }

    public void updateWithExistingDuplicates(String ucid) {
        try {
            String query = "select * from imageExtractionDuplicates where user='" + user + "' and docId='" + currentUcid + "'";
            MySQL mysql = MySQL.getInstance();
            ArrayList<HashMap<String, String>> dbResult = mysql.query(query, new String[]{"goldFilename"});
            duplicatesGold.clear();
            for (int i = 0; i < dbResult.size(); i++) {
                HashMap<String, String> dbRow = dbResult.get(i);
                String goldFilename = dbRow.get("goldFilename");
                //now that i've populated the duplicates list - i must hide them from the displayed lists of images...

                Image im = new Image(goldFilename, "img");
                gold.get(gold.indexOf(im)).setVisible(false);
                duplicatesGold.add(im);

            }

        } catch (SQLException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "SQL Error",
                    ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConnectException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "SQL Error",
                    ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateWithExistingResults() {
        try {
            String query = "select * from imageExtractionResults where user='" + user + "' and evaluation='" + evaluation + "' "
                    + "and run='" + currentRun + "' and docId='" + currentUcid + "'";
            System.out.println(query);
            MySQL mysql = MySQL.getInstance();
            ArrayList<HashMap<String, String>> dbResult = mysql.query(query, new String[]{"filename", "judgement", "goldFilename"});
            results.clear();
            for (int i = 0; i < dbResult.size(); i++) {
                HashMap<String, String> dbRow = dbResult.get(i);
                String filename = dbRow.get("filename");
                String judgement = dbRow.get("judgement");
                String goldFilename = dbRow.get("goldFilename");
                results.add(new Results(currentRun, filename, Integer.valueOf(judgement), goldFilename, evaluation, user, currentUcid));
                //now that i've populated the results list - i must hide them from the displayed lists of images...
                Image im = new Image(filename, "img");
                candidates.get(candidates.indexOf(im)).setVisible(false);
                im = new Image(goldFilename, "img");
                gold.get(gold.indexOf(im)).setVisible(false);
            }

        } catch (SQLException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "SQL Error",
                    ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConnectException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR, "SQL Error",
                    ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    public void populateGold(String ucid) {
        try {
            gold = new ArrayList<Image>();
            byte[] zipData = getZipDataFromLDC(ucid);
            gold = createTempGoldData(ucid, zipData);
            populateGoldDB();
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void populateGoldDB() {

        for (Image goldImage : gold) {
            String query = "insert ignore into imageExtractionGold values ("
                    + "'" + currentRun + "',"
                    + "'" + goldImage.path + "',"
                    + "'" + evaluation + "',"
                    + "'" + currentUcid + "')";

            try {
                MySQL.getInstance().Update(query);
            } catch (IOException ex) {
                FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "IO error", ex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, error);
                ex.printStackTrace();
                Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "SQL error", ex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, error);
                ex.printStackTrace();
                Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private ArrayList<Image> createTempGoldData(String ucid, byte[] zipData) throws IOException {
        ArrayList<Image> result = new ArrayList<Image>();
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData));
        ZipEntry zentry;
        while ((zentry = zis.getNextEntry()) != null) {
            if (zentry.isDirectory()) {
                System.out.println("Directory : "
                        + zentry.getName());
            } else {
                if (zentry.getName().startsWith("__MACOSX") || zentry.getName().startsWith(".")) {
                    System.out.println("Ignoring : "
                            + zentry.getName());
                } else {
                    System.out.println("Name of Entry in Given Zip file : "
                            + zentry.getName());
                    String entryName = cleanEntryName(zentry.getName());
                    System.out.println("Size of Entry in Given Zip file : "
                            + zentry.getCompressedSize() + " byte");
                    System.out.println("Actual Size of Entry : "
                            + zentry.getSize() + " byte");
                    String filename = localFilePath + currentRun + File.separator + "gold" + File.separator + ucid + File.separator + entryName;
                    File f = new File(filename);
                    f.getParentFile().mkdirs();
                    byte[] fileData;
                    if (zentry.getSize() > 0) {
                        fileData = new byte[(int) zentry.getSize()];

                    } else {
                        fileData = IOUtils.toByteArray(zis);
                    }
                    zis.read(fileData, 0, fileData.length);
                    zis.closeEntry();

                    OutputStream output = null;
                    try {
                        output = new BufferedOutputStream(new FileOutputStream(f));
                        output.write(fileData);
                    } finally {
                        output.close();
                    }

                    //if needed, convert TIF to jpg
                    if (filename.matches(".*(?i)(tif|tiff)")) {
                        System.out.println("converting to jpg...");
                        File newFile = new File(tiff2jpg.convert(f.getAbsolutePath()));
                        result.add(new Image(currentRun + File.separator + "gold" + File.separator + ucid + File.separator + newFile.getName(), "img"));
                        f.delete();
                    } else {
                        result.add(new Image(currentRun + File.separator + "gold" + File.separator + ucid + File.separator + f.getName(), "img"));
                    }

                    System.out.println("Created file " + f.getAbsolutePath());
                }
            }
        }
        return result;
    }

    private byte[] getZipDataFromLDC(String ucid) throws IOException {
        Socket dfSocket = null;
        PrintWriter out = null;
        InputStream in = null;

        try {
            dfSocket = new Socket("ldc.ir-facility.org", 4426);
            out = new PrintWriter(dfSocket.getOutputStream(), true);
            in = dfSocket.getInputStream();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: ldc.ir-facility.org.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: ldc.ir-facility.org.");
            System.exit(1);
        }

        byte[] fromServer;
        out.println(ucid);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buffer[] = new byte[1024];
        for (int s; (s = in.read(buffer)) != -1;) {
            baos.write(buffer, 0, s);
        }
        fromServer = baos.toByteArray();


        out.close();
        in.close();
        dfSocket.close();
        return fromServer;
    }

    public void populateCandidates(String ucid) {
        candidates = new ArrayList<Image>();

        String filename = localFilePath + currentRun + File.separator + ucid;
        File f = new File(filename);
        if (f.exists()) {
            File[] images = f.listFiles();
            for (File image : images) {
                candidates.add(new Image(currentRun + File.separator + ucid + File.separator + image.getName(), "img"));
            }
            populateCandidatesDB();
        } else {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "IO error", "Folder " + filename + " does not exist - no candidates to evaluate.");
            FacesContext.getCurrentInstance().addMessage(null, error);
        }

    }

    public void populateCandidatesDB() {
        for (Image candidateImage : candidates) {
            String query = "insert ignore into imageExtractionCandidates values ("
                    + "'" + currentRun + "',"
                    + "'" + candidateImage.path + "',"
                    + "'" + evaluation + "',"
                    + "'" + currentUcid + "');";

            try {
                MySQL.getInstance().Update(query);
            } catch (IOException ex) {
                FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "IO error", ex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, error);
                ex.printStackTrace();
                Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "SQL error", ex.getLocalizedMessage());
                FacesContext.getCurrentInstance().addMessage(null, error);
                ex.printStackTrace();
                Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public ArrayList<Image> getCandidates() {
        return candidates;
    }

    public void setCandidates(ArrayList<Image> candidates) {
        this.candidates = candidates;
    }

    public ArrayList<Image> getGold() {
        return gold;
    }

    public void setGold(ArrayList<Image> gold) {
        this.gold = gold;
    }

    public void deleteTempFiles(String runName) {
        try {
            String filename = localFilePath + currentRun;
            File f = new File(filename);
            delete(f);
            System.out.println("deleted folder" + f.getAbsolutePath());

        } catch (IOException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "IO error", ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            ex.printStackTrace();
            Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void delete(File file)
            throws IOException {

        if (file.isDirectory()) {

            //directory is empty, then delete it
            if (file.list().length == 0) {

                file.delete();
                System.out.println("Directory is deleted : "
                        + file.getAbsolutePath());

            } else {

                //list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);

                    //recursive delete
                    delete(fileDelete);
                }

                //check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                    System.out.println("Directory is deleted : "
                            + file.getAbsolutePath());
                }
            }

        } else {
            //if file, then delete it
            file.delete();
            System.out.println("File is deleted : " + file.getAbsolutePath());
        }
    }

    /**
     * an entry name should be in the form <ucid>/filename. However, often
     * enough, there is another folder in front. This simply removes this extra
     * folder;
     *
     * @param folder
     * @return
     */
    private String cleanEntryName(String folder) {
        String result = folder;
        String[] names = folder.split(File.separator);

        if (names.length == 3) {
            result = names[1] + File.separator + names[2];
        }
        return result;
    }

    /**
     *
     */
    public void createTempFilesFromZipData() {

        try {
            runData = new Run();
            runData.populateFromDB(currentRun);
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(runData.getRunData()));
            ZipEntry zentry;
            while ((zentry = zis.getNextEntry()) != null) {
                if (zentry.isDirectory()) {
                    System.out.println("Directory : "
                            + zentry.getName());
                } else {
                    if (zentry.getName().startsWith("__MACOSX") || zentry.getName().startsWith(".")) {
                        System.out.println("Ignoring : "
                                + zentry.getName());
                    } else {
                        System.out.println("Name of Entry in Given Zip file : "
                                + zentry.getName());
                        String entryName = cleanEntryName(zentry.getName());
                        System.out.println("Size of Entry in Given Zip file : "
                                + zentry.getCompressedSize() + " byte");
                        System.out.println("Actual Size of Entry : "
                                + zentry.getSize() + " byte");
                        String filename = localFilePath + currentRun + File.separator + entryName;
                        File f = new File(filename);
                        f.getParentFile().mkdirs();
                        byte[] fileData;
                        if (zentry.getSize() > 0) {
                            fileData = new byte[(int) zentry.getSize()];

                        } else {
                            fileData = IOUtils.toByteArray(zis);
                        }
                        zis.read(fileData, 0, fileData.length);
                        zis.closeEntry();

                        OutputStream output = null;
                        try {
                            output = new BufferedOutputStream(new FileOutputStream(f));
                            output.write(fileData);
                        } finally {
                            output.close();
                        }

                        //if needed, convert TIF to jpg
                        if (filename.matches(".*(?i)(tif|tiff)")) {
                            System.out.println("converting to jpg...");
                            tiff2jpg.convert(f.getAbsolutePath());
                            f.delete();
                        }

                        System.out.println("Create file " + f.getAbsolutePath());
                    }
                }

            }

        } catch (IOException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "IO error", ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            ex.printStackTrace();
            Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "SQL error", ex.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(null, error);
            ex.printStackTrace();
            Logger.getLogger(ImageListsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
