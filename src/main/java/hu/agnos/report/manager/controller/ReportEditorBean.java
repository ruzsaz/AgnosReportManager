package hu.agnos.report.manager.controller;

import hu.agnos.cube.meta.dto.HierarchyDTO;
import hu.agnos.cube.meta.dto.LevelDTO;
import hu.agnos.cube.meta.http.CubeClient;
import hu.agnos.report.entity.Measure;

import hu.agnos.report.entity.Report;
import hu.agnos.report.entity.Visualization;
import hu.agnos.report.repository.ReportRepository;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import javax.enterprise.context.RequestScoped;

@Named(value = "reportEditorBean")
@RequestScoped
public class ReportEditorBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(ReportEditorBean.class);   //!< Log kezelő


    private String cubeServerUri;

    private String cubeUniqeName;
    private String reportUniqeName;
    private Report report;

    private int editedLang;
    private boolean deleteReport;
    private String[] hierarchyHeader;
    private String[] measureHeader;

    @PostConstruct
    public void init() {
    
        Config config = ConfigProvider.getConfig();

        String cubeServerUri = config.getValue("cube.server.uri", String.class);
    

        this.deleteReport = false;
        this.cubeUniqeName = null;
        Map<String, Object> parameters = FacesContext.getCurrentInstance().getExternalContext().getFlash();
        this.cubeUniqeName = parameters.get("cubeName").toString();
        this.editedLang = 0;
        Optional<String[]> optHierarchyHeader = getHierarchyHeaderOfCube(cubeServerUri, cubeUniqeName);
        if(optHierarchyHeader.isPresent()){
            this.hierarchyHeader = optHierarchyHeader.get();
        }
       
                
        Optional<String[]> optMeasureHeader = getMeasureHeaderOfCube(cubeServerUri, cubeUniqeName);
        if(optMeasureHeader.isPresent()){
            this.measureHeader = optMeasureHeader.get();
        }        
                
        if (parameters.get("reportName") == null) { // Ha új reportról van szó
            this.report = new Report();
            this.report.addLanguage("");
            this.report.setCubeName(cubeUniqeName);
            this.report.setDatabaseType("AGNOS_MOLAP");
            addHierarchy();
            addIndicator();
            addVisualization();
        } else { // Ha régiről
            this.reportUniqeName = parameters.get("reportName").toString();
            Optional<Report> optReport = (new ReportRepository()).findById(cubeUniqeName, reportUniqeName);
            if (optReport.isPresent()) {
                this.report = optReport.get();
            }
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

    public String[] getHierarchyHeader() {
        return hierarchyHeader;
    }

    public void setHierarchyHeader(String[] hierarchyHeader) {
        this.hierarchyHeader = hierarchyHeader;
    }

    public String[] getMeasureHeader() {
        return measureHeader;
    }

    public void setMeasureHeader(String[] measureHeader) {
        this.measureHeader = measureHeader;
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
        report.getHierarchies().add(new hu.agnos.report.entity.Hierarchy(getLanguageCount()));
    }

    /**
     * Beállítja egy hierachia szintjeit a megfelelő kocka-beli hierarchiából. A
     * hiearchia unique-nevének már kitöltve kell lennie, ez alapján keresi ki.
     *
     * @param index Az állítandó hierarchia report-beli sorszáma.
     */
    public void setHierachyFromCube(int index) {
        hu.agnos.report.entity.Hierarchy h = report.getHierarchies().get(index);
        Optional<HierarchyDTO> optHierarchyDTO = getHierarchy(cubeServerUri, cubeUniqeName, h.getHierarchyUniqueName());
        if (optHierarchyDTO.isPresent()) {
            List<LevelDTO> lFromSet = optHierarchyDTO.get().getLevels();
            h.setLevels(new ArrayList<>());

            for (LevelDTO l : lFromSet) {
                h.getLevels().add(new hu.agnos.report.entity.Level(l.getDepth(), l.getIdColumnName(), l.getCodeColumnName(), l.getNameColumnName()));
            }
            h.setAllowedDepth(h.getLevels().size() - 1);
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
        report.getIndicators().add(new hu.agnos.report.entity.Indicator(getLanguageCount(), 0, value, denominator, 1.0));
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
            (new ReportRepository()).delete(report);
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

    private Optional<HierarchyDTO> getHierarchy(String cubeServerUri, String cubeUniqeName, String hierarchyUniqueName) {
        return (new CubeClient()).getHierarchy(cubeServerUri, cubeUniqeName, hierarchyUniqueName);
    }
    
    private Optional<String[]> getHierarchyHeaderOfCube(String cubeServerUri, String cubeUniqeName) {
        return (new CubeClient()).getHierarchyHeaderOfCube(cubeServerUri,cubeUniqeName);
    }
    
    private Optional<String[]> getMeasureHeaderOfCube(String cubeServerUri, String cubeUniqeName) {
        return (new CubeClient()).getMeasureHeaderOfCube(cubeServerUri,cubeUniqeName);
    }
}
