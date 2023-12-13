package hu.agnos.report.session;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.security.http.oidc.OidcPrincipal;

/**
 *
 * @author parisek
 */
@Named(value = "login")
@SessionScoped
public class Login implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Login.class);   //!< Log kezelő

    private String userName;
    private String password;
    private boolean loggedIn;
    private String token;

    @PostConstruct
    public void init() {

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

    }
    
    public ExternalContext currentExternalContext() {
        if (FacesContext.getCurrentInstance() == null) {
            throw new RuntimeException("message here ");
        } else {
            return FacesContext.getCurrentInstance().getExternalContext();
         }
    }
    
    public String getLogout() {
        logout();
        return "olikaokos";
    }
    
    public void logout() {
                
        
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        HttpSession session = request.getSession(true);

        ExternalContext externalContext = currentExternalContext();

        try {
            LOGGER.debug("logout");
            request.logout();
        } catch (ServletException ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {        
            LOGGER.debug("redirect");
            externalContext.redirect(externalContext.getRequestContextPath());
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        }

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
        return result;
    }

    public boolean isPermitted(String roleName) {
        return isPermitted(roleName, false);
    }

    public boolean isPermitted(String roleName, Boolean redirectIfNotLoggedIn) {
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
            this.loggedIn = true;
        } else {
            this.loggedIn = false;
        }

    }

    public String login(String username, String password, String remoteIP) {
        String token = null;
        return token;
    }

}
