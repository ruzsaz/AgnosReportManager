/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.report.manager.controller;

//import hu.agnos.report.manager.repository.ModelSingleton;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author ruzsaz
 */
public class AutoRun implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
//        System.out.println("Most indul");
//        ModelSingleton.getInstance();
        System.out.println("Elindult zolikaokos");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Leáll");
    }
    
}
