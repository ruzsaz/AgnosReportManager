/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.report.manager.controller;

import hu.agnos.report.controller.auth.Authenticator;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import org.primefaces.context.RequestContext;



/**
 *
 * @author parisek
 */
@ManagedBean
@SessionScoped
public class Login {

    private String username;
    private String password;
    private boolean loggedIn;
    private String token;

    public Login() {
        this.loggedIn = true;
        this.username="EZT_MAJD_KISZEDJUK...";
    }

    public boolean isLoggedIn() {
        return loggedIn;
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

    public String getToken() {
        return token;
    }

    public String getRealName() {
        String result = this.username;
//        if (username != null) {
//            try {
//                result = UserPrincipalStorageSingleton.getInstance().get(username).getRealName();
//            } catch (IOException ex) {
//                Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
        return result;
    }

    public boolean isPermitted(String roleName) {
        return isPermitted(roleName, false);
    }

    public boolean isPermitted(String roleName, Boolean redirectIfNotLoggedIn) {
//        boolean result = false;
////        System.out.println("Felhasználó: " + username + " password: " + password + " loggedin: " + loggedIn);
//        if (username == null) {
//            if (redirectIfNotLoggedIn) {
//                FacesContext facesContext = FacesContext.getCurrentInstance();
//                NavigationHandler navigationHandler = facesContext.getApplication().getNavigationHandler();
//                navigationHandler.handleNavigation(facesContext, null, "/expiredError.xhtml");
//                facesContext.renderResponse();
//            }
//        } else {
//            result = Authorizator.hasPermission(username, roleName);
//        }
//        return (this.loggedIn && result);
          return true;
    }

    public void login(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String remoteIp = request.getHeader("X-FORWARDED-FOR");
        if (remoteIp == null) {
            remoteIp = request.getRemoteAddr();
        }
        FacesMessage message = null;
        token = null;
        if (username != null && password != null) {
            token = login(username, password, remoteIp);
            password = null;
        }

        if (token != null) {
            //message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Welcome", username);
            //storeLoggedInUser(username);
            this.loggedIn = true;
        } else {
            //message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Loggin Error", "Invalid credentials");
            this.loggedIn = false;
            //deleteLoggedInUser();
        }

        //FacesContext.getCurrentInstance().addMessage(null, message);
        //context.addCallbackParam("loggedIn", loggedIn);
        //return "/index.xhtml?faces-redirect=true";
    }

    public String login(String username, String password, String remoteIP) {        
        String token = null;
        try {
            Authenticator auth = new Authenticator();
            Logger.getLogger(Login.class.getName()).log(Level.INFO, "Successful login: {0}", username);
            token = auth.login(username, password, remoteIP);
        } catch (IOException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        }

        return token;
    }

    public String logout() {
        this.loggedIn = false;
        this.username = null;
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/reportChoser.xhtml?faces-redirect=true";
    }

//    public void storeLoggedInUser(String username) {
//        try {
//            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("user", UserPrincipalStorageSingleton.getInstance().get(username));      
//        } catch (IOException ex) {
//            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//    
//    public void deleteLoggedInUser() {
//        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("user");
//    }
//    
//    public User getLoggedInUser() {
//        return (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
//    }
}
