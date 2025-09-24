package hu.agnos.report.manager.controller;

import hu.agnos.cube.meta.resultDto.CubeList;
import hu.agnos.report.entity.Cube;
import hu.agnos.report.entity.Report;
import hu.agnos.report.manager.service.CubeService;
import hu.agnos.report.repository.ReportRepository;
import java.io.IOException;
import java.io.Serial;
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
import lombok.Getter;
import lombok.Setter;
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

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportChoserBean.class);   //!< Log kezelő

    private String cubeServerUri;
    private String reportServerUri;
    @Getter
    private List<String> cubeNames;
    @Getter
    private List<Report> reports;
    @Setter
    @Getter
    private Report selectedReport;
    @Setter
    @Getter
    private String selectedCubeName;

    @PostConstruct
    public void init() {
        Config config = ConfigProvider.getConfig();
        this.cubeServerUri = config.getValue("cube.server.uri", String.class);
        this.reportServerUri = config.getValue("report.server.uri", String.class);
        this.cubeNames = new ArrayList<>(5);
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
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("reportName", selectedReport.getName());
        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(context, null, "reportEditor.xhtml?faces-redirect=true");
    }

    public void createNewReport() {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(context, null, "reportEditor.xhtml?faces-redirect=true");
    }

    public String sendRefreshCubesRequest() {
        try {
            HttpPost CubesRefreshRequest = new HttpPost(new URL((new URL(cubeServerUri)).toExternalForm() + "/restart").toURI());
            LOGGER.info("Restart the cube servers.");
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

}