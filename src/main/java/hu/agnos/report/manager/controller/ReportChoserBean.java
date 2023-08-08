/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.report.manager.controller;

import hu.mi.agnos.report.repository.ReportRepository;
import hu.agnos.report.util.Properties;
import hu.agnos.report.util.ResourceHandler;
import hu.agnos.cube.meta.http.CubeClient;
import hu.mi.agnos.report.entity.Report;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

@ManagedBean
public class ReportChoserBean {

    private List<String> cubeNames;
    private List<Report> reports;
    private Report selectedReport;
    private String selectedCubeName;
    private final String cubeServerUri;

    public ReportChoserBean() throws IOException {
        ResourceHandler resourceHandler = new ResourceHandler();
        Properties props = resourceHandler.getProperties("settings");
        String propertyUri = props.getString("cubeServerUri"); 
        this.cubeServerUri = propertyUri.endsWith("/") ? propertyUri : propertyUri+"/";

    }

    @PostConstruct
    public void init() {
        System.out.println("ReportChoserBean INIT");
        
        this.reports = (new ReportRepository()).findAll();

        
        Optional<List<String>> optionalCubeList = new CubeClient()
                .getCubesName(this.cubeServerUri);
        
        if (optionalCubeList.isPresent()) {

            this.cubeNames = optionalCubeList.get();
            /*
            this.reports = new ArrayList<>();
            for (String cubeName : this.cubeNames) {
                Cube c = ModelSingleton.getInstance().get(cubeName);
                //ha a kockához létezik riport
                if (c != null) {
                    for (String reportName : ModelSingleton.getInstance().get(cubeName).keySet()) {
                        try {
                            System.out.println("ZOLIKA: " + cubeName + " " + reportName);
                            // TODO: nyelvkód "" helyett.
                            Report report = ModelSingleton.getInstance().getReport(cubeName, reportName);
                            this.reports.add(report);
                        } catch (WrongCubeName ex) {
                            Logger.getLogger(ReportEditorBean.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }
                    }
                }
            }
             */
        }
    }

    public void setOldReport() {
        FacesContext context = FacesContext.getCurrentInstance();
        Flash flash = context.getExternalContext().getFlash();
        flash.put("cubeName", selectedReport.getCubeName());
        flash.put("reportName", selectedReport.getName());
        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(context, null, "reportEditor.xhtml?faces-redirect=true");
    }

    public void setNewReportSource() {
        FacesContext context = FacesContext.getCurrentInstance();
        Flash flash = context.getExternalContext().getFlash();
        flash.put("cubeName", selectedCubeName);
        flash.put("reportName", null);
        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(context, null, "reportEditor.xhtml?faces-redirect=true");
    }

    public List<String> getCubeNames() {
        return cubeNames;
    }

    public List<Report> getReports() {
        return reports;
    }

    public Report getSelectedReport() {
        return selectedReport;
    }

    public void setSelectedReport(Report selectedReport) {
        this.selectedReport = selectedReport;
    }

    public String getSelectedCubeName() {
        return selectedCubeName;
    }

    public void setSelectedCubeName(String selectedCubeName) {
        this.selectedCubeName = selectedCubeName;
    }

}
