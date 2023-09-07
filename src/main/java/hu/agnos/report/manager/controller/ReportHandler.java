/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.report.manager.controller;

import hu.mi.agnos.report.entity.Indicator;
import hu.mi.agnos.report.entity.Measure;
import hu.mi.agnos.report.entity.Report;
import hu.mi.agnos.report.exception.WrongCubeName;

import hu.mi.agnos.report.repository.ReportRepository;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author parisek
 */
public class ReportHandler {

    public List<String> getReportNames(String cubeName) {
        List<String> result = new ArrayList<>();
        
        for(Report r : (new ReportRepository()).findAll()){
            if(r.getCubeName().equals(cubeName)){
                result.add(r.getName());
            }
        }

        return result;
    }

    public Report getReport(String cubeUniqeName, String reportUniqeName, String lang) throws WrongCubeName, SQLException {
        Optional<Report> optReport = (new ReportRepository()).findById(cubeUniqeName, reportUniqeName);
        if (optReport.isPresent()) {
            return optReport.get();
        }
        return null;
    }

    public void setReport(String cubeUniqueName, Report report) {
        ReportRepository repo = new ReportRepository();
        repo.save(report);

//        ResourceHandler resourceHandler = new ResourceHandler();
//        Properties props = null;
//        try {
//            props = resourceHandler.getProperties("settings");
//        } catch (IOException ex) {
//            Logger.getLogger(ReportHandler.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        String path = props.getString("agnosHome");
//
//        if (!path.endsWith("/")) {
//            path = path + "/";
//        }
/*        String path = null;
        final String AGNOS_HOME = System.getenv("AGNOS_HOME");
        if (!AGNOS_HOME.endsWith("/")) {
            path = AGNOS_HOME + "/AgnosReportingServer/";
        } else {
            path = AGNOS_HOME + "AgnosReportingServer/";
        }

        File file = new File(path + "Meta/" + cubeUniqueName + "." + report.getName() + ".report.xml");
        String content = report.toXML(cubeUniqueName);

        try (FileOutputStream fop = new FileOutputStream(file, false)) {

            // if file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // get the content in bytes
            byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(ReportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
         */
    }

    public void deleteExistingReport(String cubeUniqueName, Report report) throws SQLException {

        String path = null;
        final String AGNOS_HOME = System.getenv("AGNOS_HOME");
        if (!AGNOS_HOME.endsWith("/")) {
            path = AGNOS_HOME + "/AgnosReportingServer/";
        } else {
            path = AGNOS_HOME + "AgnosReportingServer/";
        }

        File file = new File(path + "Meta/" + cubeUniqueName + "." + report.getName() + ".report.xml");
        if (file.exists()) {
            file.delete();
        }
    }

    public Set<Measure> getMeasures(Report report) {
        Set<Measure> result = new HashSet<>();
        for (Indicator ind : report.getIndicators()) {
            result.add(ind.getDenominator());
            result.add(ind.getValue());
        }
        return result;
    }
}
