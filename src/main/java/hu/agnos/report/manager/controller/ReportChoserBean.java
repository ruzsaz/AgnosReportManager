/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.report.manager.controller;


import hu.agnos.cube.meta.dto.CubeList;
import hu.agnos.cube.meta.dto.CubeNameAndDate;
import hu.agnos.cube.meta.http.CubeClient;
import hu.mi.agnos.report.entity.Report;
import hu.mi.agnos.report.repository.ReportRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

@ManagedBean
public class ReportChoserBean {

    //TODO: ezt kiszervezni property-be
    private String cubeServerUri = "http://localhost:7979/acs";
    private List<String> cubeNames;
    private List<Report> reports;
    private Report selectedReport;
    private String selectedCubeName;

    @PostConstruct
    public void init() {
        System.out.println("ReportChoserBean INIT");
        
        
        Config config = ConfigProvider.getConfig();

        String cubeServerUri = config.getValue("cube.server.uri", String.class);
        
        this.cubeNames = new ArrayList<>();
        Optional<CubeList> cubeList = getCubeList();
        if(cubeList.isPresent()){
            for(CubeNameAndDate cnad : cubeList.get().getCubesNameAndDate()){
                this.cubeNames.add(cnad.getName());
            }
        }
        this.reports = (new ReportRepository()).findAll();
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

    private Optional<CubeList> getCubeList() {
        return (new CubeClient()).getCubesNameAndDate(cubeServerUri);
    }
}
