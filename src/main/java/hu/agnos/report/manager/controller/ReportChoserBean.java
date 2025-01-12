package hu.agnos.report.manager.controller;

import hu.agnos.cube.meta.resultDto.CubeList;
import hu.agnos.report.entity.Cube;
import hu.agnos.report.entity.Report;
import hu.agnos.report.manager.service.CubeService;
import hu.agnos.report.repository.ReportRepository;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

@Named(value = "reportChoserBean")
@ViewScoped
public class ReportChoserBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportChoserBean.class);   //!< Log kezelő

    private String cubeServerUri;
    private String reportServerUri;
    private List<String> cubeNames;
    private List<Report> reports;
    private Report selectedReport;
    private String selectedCubeName;

    @PostConstruct
    public void init() {
        Config config = ConfigProvider.getConfig();
        this.cubeServerUri = config.getValue("cube.server.uri", String.class);
        this.reportServerUri = config.getValue("report.server.uri", String.class);
        this.cubeNames = new ArrayList<>();
        CubeList cubeList = CubeService.getCubeList(cubeServerUri);
        if (cubeList != null) {
            this.cubeNames.addAll(cubeList.cubeMap().keySet());
            java.util.Collections.sort(cubeNames);            
        }
        this.reports = (new ReportRepository()).findAll();
        for (Report report : reports) {
            report.setBroken(!cubeNames.containsAll(report.getCubeNames()));
        }
    }

    public void setOldReport() {
        FacesContext context = FacesContext.getCurrentInstance();
        Flash flash = context.getExternalContext().getFlash();
        flash.put("reportName", selectedReport.getName());
        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(context, null, "reportEditor.xhtml?faces-redirect=true");
    }

    public void createNewReport() {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(context, null, "reportEditor.xhtml?faces-redirect=true");
    }

    public String sendRefreshCubesRequest() {
        try {
            HttpPost CubesRefreshRequest = new HttpPost(new URL((new URL(cubeServerUri)).toExternalForm() + "/refresh").toURI());
            LOGGER.info("Refresh the cube servers.");
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                httpClient.execute(CubesRefreshRequest);
            }
            LOGGER.info("Refresh the report server.");
            HttpPost ReportServerRefreshRequest = new HttpPost(new URL((new URL(reportServerUri)).toExternalForm() + "/refresh").toURI());
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                httpClient.execute(ReportServerRefreshRequest);
            }
        } catch (IOException | URISyntaxException e) {
            return "reportChoser.xhtml?faces-redirect=true";
        } 
        return "reportChoser.xhtml?faces-redirect=true";
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