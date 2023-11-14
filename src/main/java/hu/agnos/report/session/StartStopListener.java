package hu.agnos.report.session;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ruzsaz
 */
public class StartStopListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartStopListener.class);
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("contextInitialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("contextDestroyed");
    }
    
}
