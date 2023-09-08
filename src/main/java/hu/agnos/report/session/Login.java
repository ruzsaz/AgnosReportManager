/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.report.session;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.Enumeration;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.wildfly.security.http.oidc.OidcPrincipal;

/**
 *
 * @author parisek
 */
@Named(value = "login")
@SessionScoped
public class Login implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(Login.class);   //!< Log kezelő


    private String userName;
    private String password;
    private boolean loggedIn;
    private String token;

    @PostConstruct
    public void init(){

        this.loggedIn = true;

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        if (request.getUserPrincipal() != null) {

            this.userName = request.getUserPrincipal().getName();

            Principal p = request.getUserPrincipal();
            if (p instanceof OidcPrincipal) {
                OidcPrincipal principal = (OidcPrincipal) p;
                this.userName = principal.getOidcSecurityContext().getToken().getClaimValueAsString("preferred_username");
            }

        } else {
            this.userName = "";
        }

//        String userName = sc.getUserPrincipal().getName();
//
//        if (sc.getUserPrincipal() instanceof KeycloakPrincipal) {
//            KeycloakPrincipal<KeycloakSecurityContext> kp = (KeycloakPrincipal<KeycloakSecurityContext>) sc.getUserPrincipal();
//
//            // this is how to get the real userName (or rather the login name)
//            userName = kp.getKeycloakSecurityContext().getIdToken().getPreferredUsername();
//        }
//        this.username = 
    }

    public String logout() {
//        LOGGER.info(() -> LogMessageFactory.logMessage(FacesContext.getCurrentInstance(), user, "1200"));
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        HttpSession session = request.getSession(true);

        Enumeration attributeNames = session.getAttributeNames();

        //destroy session        
        while (attributeNames.hasMoreElements()) {
            String name = (String) attributeNames.nextElement();
            session.removeAttribute(name);
        }

        this.loggedIn = false;
        this.userName = null;

        try {
            response.sendRedirect("/reportChoser.xhtml?faces-redirect=true");
        } catch (IOException e) {
            context.addMessage(null, new FacesMessage("Logout failed."));
        }
        return "reportChoser.xhtml?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getUsername() {
        return userName;
    }

    public void setUsername(String username) {
        this.userName = username;
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
        String result = this.userName;
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
        if (userName != null && password != null) {
            token = login(userName, password, remoteIp);
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
//        try {
//            Authenticator auth = new Authenticator();
//            Logger.getLogger(Login.class.getName()).log(Level.INFO, "Successful login: {0}", username);
//            token = auth.login(username, password, remoteIP);
//        } catch (IOException ex) {
//            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
//        }

        return token;
    }
//
//    public String logout() {
//        this.loggedIn = false;
//        this.userName = null;
//        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
//        return "/reportChoser.xhtml?faces-redirect=true";
//    }

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
