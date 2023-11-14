package hu.agnos.report.session;

import java.io.Serializable;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ruzsaz
 */
@WebListener
public class SessionTimeoutListener implements HttpSessionListener, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionTimeoutListener.class);
    
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // Nothing to do
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
//        User user = (User) session.getAttribute("loggedInUser");
//        if (user != null) {
//            if (session.getAttribute("logoutRequest") != null) {
//                LOGGER.debug(() -> Log.log(user, "1900"));                
//            } else {
//                LOGGER.info(() -> Log.log(user, "1910"));
//            }
//            session.removeAttribute("loggedInUser");
//            session.removeAttribute("logoutRequest");
//        }
    }

}
