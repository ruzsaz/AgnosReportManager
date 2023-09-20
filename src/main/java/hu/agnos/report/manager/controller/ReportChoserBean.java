/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.report.manager.controller;


import hu.agnos.cube.meta.dto.CubeList;
import hu.agnos.cube.meta.dto.CubeNameAndDate;
import hu.agnos.cube.meta.http.CubeClient;
import hu.agnos.report.entity.Report;
import hu.agnos.report.repository.ReportRepository;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

@Named(value = "reportChoserBean")
@SessionScoped
public class ReportChoserBean  implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(ReportChoserBean.class);   //!< Log kezelő

    private String cubeServerUri;
    private List<String> cubeNames;
    private List<Report> reports;
    private Report selectedReport;
    private String selectedCubeName;

    @PostConstruct
    public void init() {      
        Config config = ConfigProvider.getConfig();
        this.cubeServerUri = config.getValue("cube.server.uri", String.class);       
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
        return (new CubeClient(cubeServerUri)).getCubesNameAndDate();
    }
}
