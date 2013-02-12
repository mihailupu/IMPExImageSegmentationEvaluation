/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.ifs.mihailupu.impex;

import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.primefaces.context.RequestContext;

/**
 *
 * @author mihailupu
 */
@ManagedBean
@SessionScoped
public class UserManager implements Serializable {

    private String username;
    private String password;
    private User current;
    private MySQL mysql;

    public UserManager() {
        String localFilePath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
        Config.init(localFilePath);
        mysql = MySQL.getInstance();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String login() {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage msg;
        boolean loggedIn = false;
        String fullname = "";
        try {
            if (username != null) {
                String query = "select username,password,fullname from users where username='" + username + "';";
                ArrayList<HashMap<String, String>> dbResult = mysql.query(query, new String[]{"username", "password", "fullname"});
                //only consider the first result, in case there are more
                if (dbResult.size() > 0) {
                    if (dbResult.get(0).get("password").equals(md5(password))) {
                        fullname = dbResult.get(0).get("fullname");
                        loggedIn = true;
                        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Welcome", fullname);

                    } else {
                        loggedIn = false;
                        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Error", "Credentials do not match.");

                    }
                } else {
                    loggedIn = false;
                    msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Error", "No such user");

                }
            } else {
                loggedIn = false;
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Error", "Empty user name");
            }
        } catch (ConnectException ex) {
            msg = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Login Error", ex.getLocalizedMessage());
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            msg = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Login Error", ex.getLocalizedMessage());
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            msg = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Login Error", ex.getLocalizedMessage());
        } catch (SQLException sqlex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, sqlex);
            msg = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Login Error", sqlex.getLocalizedMessage());
        }


        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (loggedIn) {
            current = new User(username, fullname);
            return "secure/evaluationSet?faces-redirect=true";
        } else {
            return (username = password = null);
        }
    }

    private String md5(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(text.getBytes("UTF-8"));
        BigInteger bigInt = new BigInteger(1, digest);
        String hashtext = bigInt.toString(16);
// Now we need to zero pad it if you actually want the full 32 chars.
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/index?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return current != null;
    }

    public User getCurrent() {
        return current;
    }
}
