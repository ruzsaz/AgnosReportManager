package hu.agnos.report.manager.controller;

//import hu.agnos.molap.Cube;
import hu.agnos.cube.meta.dto.HierarchyDTO;
import hu.agnos.cube.meta.dto.LevelDTO;
import hu.agnos.cube.meta.http.CubeClient;
import hu.agnos.report.util.Properties;
import hu.agnos.report.util.ResourceHandler;
import hu.mi.agnos.report.entity.Hierarchy;
import hu.mi.agnos.report.entity.Measure;
import hu.mi.agnos.report.entity.Indicator;
import hu.mi.agnos.report.entity.Level;
//import hu.agnos.molap.MOLAPCubeSingleton;
import hu.mi.agnos.report.entity.Report;
import hu.mi.agnos.report.entity.Visualization;
import hu.mi.agnos.report.exception.WrongCubeName;
import hu.mi.agnos.report.repository.ReportRepository;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@ViewScoped
public class ReportEditorBean {

    private String cubeUniqeName;
    private String reportUniqeName;
    private Report report;
    private int editedLang;
    private boolean deleteReport;
    private final String cubeServerUri;

    public ReportEditorBean(String cubeServerUri) throws IOException {
        ResourceHandler resourceHandler = new ResourceHandler();
        Properties props = resourceHandler.getProperties("settings");
        String propertyUri = props.getString("cubeServerUri");
        this.cubeServerUri = propertyUri.endsWith("/") ? propertyUri : propertyUri + "/";

    }

    @PostConstruct
    public void init() {
        this.deleteReport = false;
        this.cubeUniqeName = null;
        Map<String, Object> parameters = FacesContext.getCurrentInstance().getExternalContext().getFlash();
        this.cubeUniqeName = parameters.get("cubeName").toString();
        this.editedLang = 0;
        if (parameters.get("reportName") == null) { // Ha új reportról van szó
            this.report = new Report();
            this.report.addLanguage("");
            this.report.setCubeName(cubeUniqeName);
            addHierarchy();
            addIndicator();
            addVisualization();
        } else { // Ha régiről
            this.reportUniqeName = parameters.get("reportName").toString();
            Optional<Report> optReport = (new ReportRepository()).findById(cubeUniqeName, reportUniqeName);
            this.report = optReport.isPresent() ? optReport.get() : null;
            if (report.getHierarchies().isEmpty()) {
                addHierarchy();
            }
            if (report.getIndicators().isEmpty()) {
                addIndicator();
            }
            if (report.getVisualizations().isEmpty()) {
                addVisualization();
            }
        }
    }

    public String getReportUniqeName() {
        return reportUniqeName;
    }

    public void setReportUniqeName(String reportUniqeName) {
        this.reportUniqeName = reportUniqeName;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public int getEditedLang() {
        return editedLang;
    }

    public void setEditedLang(int editedLang) {
        this.editedLang = editedLang;
    }

    public String getCubeUniqeName() {
        return cubeUniqeName;
    }

    public void moveHierarchy(int index, int direction) {
        Collections.swap(report.getHierarchies(), index, index + direction);
    }

    public void removeHierarchy(int index) {
        report.getHierarchies().remove(index);
        if (report.getHierarchies().isEmpty()) {
            addHierarchy();
        }
    }

    public void addHierarchy() {
        report.getHierarchies().add(new Hierarchy(getLanguageCount()));
    }

    /**
     * Beállítja egy hierachia szintjeit a megfelelő kocka-beli hierarchiából. A
     * hiearchia unique-nevének már kitöltve kell lennie, ez alapján keresi ki.
     *
     * @param index Az állítandó hierarchia report-beli sorszáma.
     */
    public void setHierachyFromCube(int index) {
        Hierarchy reportHiererchy = report.getHierarchies().get(index);

        Optional<HierarchyDTO> optionalHierarchy = new CubeClient()
                .getHierarchy(cubeServerUri, cubeUniqeName, reportHiererchy.getHierarchyUniqueName());

        if (optionalHierarchy.isPresent()) {
            reportHiererchy.setLevels(new ArrayList<>());
            for (LevelDTO levelDTO : optionalHierarchy.get().getLevels()) {
                reportHiererchy.getLevels().add(new Level(levelDTO.getDepth(), levelDTO.getIdColumnName(), levelDTO.getCodeColumnName(), levelDTO.getNameColumnName()));
            }
            reportHiererchy.setAllowedDepth(reportHiererchy.getLevels().size() - 1);

        }
    }
 
    public void moveIndicator(int index, int direction) {
        Collections.swap(report.getIndicators(), index, index + direction);
    }

    public void removeIndicator(int index) {
        report.getIndicators().remove(index);
        if (report.getIndicators().isEmpty()) {
            addIndicator();
        }
    }

    public void addIndicator() {
        Measure value = new Measure(getLanguageCount(), "", 0, false);
        Measure denominator = new Measure(getLanguageCount(), "", 0, false);
        //TODO: Jó-e az 1.0 konstans
        report.getIndicators().add(new Indicator(getLanguageCount(), 0, value, denominator, 1.0));
    }

    public int getLanguageCount() {
        return report.getLanguageCount();
    }

    public void moveVisualization(int index, int direction) {
        Collections.swap(report.getVisualizations(), index, index + direction);
    }

    public void removeVisualization(int index) {
        report.getVisualizations().remove(index);
        if (report.getVisualizations().isEmpty()) {
            addVisualization();
        }
    }

    public void addVisualization() {
        report.getVisualizations().add(new Visualization(""));
    }

    /**
     * Beállítja a sorrendet meghatározó id-ket (hierarchiák, indikátorok és
     * megjelenítés) a képernyő-beli sorrend alapján.
     */
    public void setIds() {
        for (int i = 0; i < report.getHierarchies().size(); i++) {
            report.getHierarchies().get(i).setId(i);
        }
        for (int i = 0; i < report.getIndicators().size(); i++) {
            report.getIndicators().get(i).setId(i);
        }
        for (int i = 0; i < report.getVisualizations().size(); i++) {
            report.getVisualizations().get(i).setOrder(i);
        }
    }

    public void removeEditedLanguage() {
        if (editedLang > 0) {
            report.removeLanguage(editedLang);
            editedLang = editedLang - 1;
        }
    }

    public void addLanguage() {
        report.addLanguage("???");
    }

    public String save() throws IOException, SQLException {
        if (deleteReport) {
            (new ReportRepository()).deleteById(cubeUniqeName, reportUniqeName);
        } else {
            setIds();
            (new ReportRepository()).save(report);
        }
        return "reportChoser.xhtml?faces-redirect=true";
    }

    public String cancel() {
        return "reportChoser.xhtml?faces-redirect=true";
    }

    public void selectLang(int id) {
        this.editedLang = id;
    }

    public void toggleDelete() {
        this.deleteReport = !this.deleteReport;
    }

    public boolean isDeleteReport() {
        return deleteReport;
    }

}
