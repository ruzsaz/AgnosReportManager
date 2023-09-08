package hu.agnos.report.session;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author ruzsaz
 */
public class StartStopListener implements ServletContextListener {

    private static final Logger LOGGER = LogManager.getLogger(StartStopListener.class);
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info(() -> "contextInitialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info(() -> "contextDestroyed");
    }
    
}
