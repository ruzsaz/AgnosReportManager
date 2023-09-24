package hu.agnos.report.manager.controller;

import hu.agnos.cube.meta.dto.CubeList;
import hu.agnos.cube.meta.dto.CubeNameAndDate;
import hu.agnos.cube.meta.http.CubeClient;
import hu.agnos.report.entity.Report;
import hu.agnos.report.repository.ReportRepository;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

@Named(value = "reportChoserBean")
@ViewScoped
public class ReportChoserBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(ReportChoserBean.class);   //!< Log kezelő

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
        Optional<CubeList> cubeList = getCubeList();
        if (cubeList.isPresent()) {
            for (CubeNameAndDate cnad : cubeList.get().getCubesNameAndDate()) {
                this.cubeNames.add(cnad.getName());
            }
        }
        this.reports = (new ReportRepository()).findAll();
        for (Report r : reports) {
            r.setBroken(!cubeList.isPresent() || !cubeList.get().containsCubeWithName(r.getCubeName()));
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

    public String sendRefreshCubesRequest() {
        try {
            HttpPost CubesRefreshRequest = new HttpPost(new URL((new URL(cubeServerUri)).toExternalForm() + "/refresh").toURI());
            System.out.println("Első kérés beküldve");
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                httpClient.execute(CubesRefreshRequest);
            }
            System.out.println("2x lefutott");
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

    private Optional<CubeList> getCubeList() {
        return (new CubeClient(cubeServerUri)).getCubesNameAndDate();
    }
}
