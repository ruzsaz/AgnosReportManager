package hu.agnos.report.manager.controller;

import hu.agnos.cube.meta.resultDto.CubeList;
import hu.agnos.cube.meta.resultDto.CubeMetaDTO;
import hu.agnos.cube.meta.resultDto.DimensionDTO;
import hu.agnos.cube.meta.resultDto.LevelDTO;
import hu.agnos.cube.meta.resultDto.MeasureDTO;
import hu.agnos.report.entity.Cube;
import hu.agnos.report.entity.Dimension;
import hu.agnos.report.entity.Indicator;
import hu.agnos.report.entity.Keyword;

import hu.agnos.report.entity.Report;
import hu.agnos.report.entity.Visualization;
import hu.agnos.report.manager.service.CubeService;
import hu.agnos.report.repository.ReportRepository;
import hu.agnos.report.repository.KeywordsRepository;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import javax.faces.view.ViewScoped;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RoleRepresentation;

@Named(value = "reportEditorBean")
@ViewScoped
public class ReportEditorBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportEditorBean.class);   //!< Log kezelő

    private String cubeServerUri;
    private String reportServerUri;

    private CubeList cubeList;  // List of available cubes
    private List<Keyword> keywordList;
    private Report report;  // The currently edited report

    private int editedLang;
    private boolean deleteReport;
    private List<String> availableDimensionNames;
    private List<String> availableIndicatorNames;
    private List<String> availableCubeNames;
    private List<String> roles;

    @PostConstruct
    public void init() {

        Config config = ConfigProvider.getConfig();

        this.cubeServerUri = config.getValue("cube.server.uri", String.class);
        this.reportServerUri = config.getValue("report.server.uri", String.class);
        this.roles = getAllRoles();
        this.roles.sort(null);
        this.deleteReport = false;
        Map<String, Object> parameters = FacesContext.getCurrentInstance().getExternalContext().getFlash();
        this.cubeList = CubeService.getCubeList(cubeServerUri);
        this.keywordList = (new KeywordsRepository()).findAll();
        this.keywordList.sort((a, b) -> { return a.getName().compareTo(b.getName()); });
        initAvailableCubeNames();
        this.editedLang = 0;

        if (parameters.get("reportName") == null) { // Ha új reportról van szó
            this.report = new Report();
            this.report.addLanguage("");            
            this.report.setCubes(new ArrayList<>(2));
        } else { // Ha régiről
            String reportName = parameters.get("reportName").toString();
            Optional<Report> optReport = (new ReportRepository()).findByName(reportName);
            if (optReport.isPresent()) {
                this.report = optReport.get();
            }
        }
        initAvailableDimensionNames();
        initAvailableIndicatorNames();
        addIfEmpty();
    }

    public void onCubeChange() {
        initAvailableDimensionNames();
        initAvailableIndicatorNames();
        validateDimensions();
        validateIndicators();
        addIfEmpty();
    }

    public List<String> getAvailableCubeNames() {
        return availableCubeNames;
    }

    private void initAvailableCubeNames() {
        this.availableCubeNames = new ArrayList<>(cubeList.cubeMap().keySet());
        java.util.Collections.sort(availableCubeNames);
    }

    public List<String> getAvailableIndicatorNames() {
        return availableIndicatorNames;
    }

    private Map<String, CubeMetaDTO> getAvailableCubesMap() {
        return cubeList.cubeMap().entrySet()
                .stream()
                .filter(a -> report.getCubeNames().contains(a.getKey()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

    }

    private void initAvailableIndicatorNames() {
        this.availableIndicatorNames = new ArrayList<>(10);
        for (Entry<String, CubeMetaDTO> cubeEntry : getAvailableCubesMap().entrySet()) {
            String cubeName = cubeEntry.getKey();
            for (MeasureDTO indicatorDTO : cubeEntry.getValue().measureHeader()) {              
                if (!indicatorDTO.hidden()) {
                    availableIndicatorNames.add(cubeName + "." + indicatorDTO.name());
                }
            }
        }        
        java.util.Collections.sort(availableIndicatorNames);
        availableIndicatorNames.add(0, "1");
    }

    public List<String> getAvailableDimensionNames() {
        return availableDimensionNames;
    }

    private void initAvailableDimensionNames() {
        Set<String> availableDimensionNamesSet = new HashSet<>(8);
        for (CubeMetaDTO cubeMeta : getAvailableCubesMap().values()) {
            for (DimensionDTO dimDTO : cubeMeta.dimensionHeader()) {
                availableDimensionNamesSet.add(dimDTO.name());
            }
        }
        availableDimensionNames = new ArrayList<>(availableDimensionNamesSet);
        java.util.Collections.sort(availableDimensionNames);
    }

    /**
     * Removes all dimensions whose cube is not present
     */
    private void validateDimensions() {
        Iterator<Dimension> i = report.getDimensions().iterator();
        while (i.hasNext()) {
            Dimension d = i.next();
            if (!availableDimensionNames.contains(d.getName())) {
                i.remove();
            }

        }
    }
    
    /**
     * Removes all cubes whose name are not set.
     */
    private void validateCubes() {
        Iterator<Cube> i = report.getCubes().iterator();
        while (i.hasNext()) {
            Cube c = i.next();
            if (c.getName() == null || c.getName().isEmpty()) {
                i.remove();
            }
        }        
    }    

    /**
     * Removes all indicators whose cube is not present
     */
    private void validateIndicators() {
        Iterator<Indicator> i = report.getIndicators().iterator();
        while (i.hasNext()) {
            Indicator in = i.next();
            if (!availableIndicatorNames.contains(in.getCombinedValueName()) || !availableIndicatorNames.contains(in.getCombinedDenominatorName())) {
                i.remove();
            }
        }
    }
    
    /**
     * Removes all empty visualizations
     */
    private void validateVisualizations() {
        Iterator<Visualization> v = report.getVisualizations().iterator();
        while (v.hasNext()) {
            Visualization vi = v.next();
            if (vi.getInitString() == null || vi.getInitString().length() < 2) {
                v.remove();
            }
        }
    }
    
    public List<String> getRoles() {
        return roles;
    }

    public List<Keyword> getKeywordList() {
        return keywordList;
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

    public CubeList getCubeList() {
        return cubeList;
    }

    public void setCubeList(CubeList cubeList) {
        this.cubeList = cubeList;
    }

    public void moveDimension(int index, int direction) {
        Collections.swap(report.getDimensions(), index, index + direction);
    }

    public void removeDimension(int index) {
        report.getDimensions().remove(index);
        addIfEmpty();
    }

    public void addDimension() {
        report.getDimensions().add(new Dimension(report.getLabels()));
    }

    public void moveCube(int index, int direction) {
        Collections.swap(report.getCubes(), index, index + direction);
    }

    public void removeCube(int index) {
        report.getCubes().remove(index);
        onCubeChange();
    }

    public void addCube() {
        report.getCubes().add(new Cube("", "ZolikaOkos"));
        onCubeChange();
    }

    /**
     * Sets a cube as the report's base cube. The cube's name is already filled
     * when this method starts, but only its name. This method restores the
     * whole cube object.
     *
     * @param index The index of the cube to set.
     */
    public void setCube(int index) {
        onCubeChange();
    }

    /**
     * Beállítja egy hierachia szintjeit a megfelelő kocka-beli hierarchiából. A
     * hiearchia unique-nevének már kitöltve kell lennie, ez alapján keresi ki.
     *
     * @param index Az állítandó hierarchia report-beli sorszáma.
     */
    public void setDimensionAtIndex(int index) {
        Dimension halfSetDimension = report.getDimensions().get(index);
        halfSetDimension.setAllowedDepth(getLevels(halfSetDimension).size() - 1);
    }

    public void moveIndicator(int index, int direction) {
        Collections.swap(report.getIndicators(), index, index + direction);
    }

    public void removeIndicator(int index) {
        report.getIndicators().remove(index);
        addIfEmpty();
    }

    public void addIndicator() {
        report.addIndicator(new Indicator(report.getLabels()));
    }

    public int getLanguageCount() {
        return report.getLanguageCount();
    }

    public void removeEditedLanguage() {
        if (editedLang > 0) {
            report.removeLanguage(editedLang);
            editedLang = editedLang - 1;
        }
    }

    /**
     * Adds a new language to the report, with the default ??? language code.
     */
    public void addLanguage() {
        report.addLanguage("???");
    }

    public String save() throws IOException, SQLException {
        if (deleteReport) {
            (new ReportRepository()).delete(report);
        } else {
            spreadLanguages();
            validateCubes();
            validateDimensions();
            validateIndicators();
            validateVisualizations();
            (new ReportRepository()).save(report);
        }
        try {
            sendRefreshReportRequest();
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(ReportEditorBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "reportChoser.xhtml?faces-redirect=true";
    }

    /**
     * Spreads the report's language codes to the indicators, dimensions, helps,
     * so they will reflect the same.
     */
    private void spreadLanguages() {
        for (int i = 0; i < report.getLanguageCount(); i++) {
            String lang = report.getLabels().get(i).getLang();
            report.getHelps().get(i).setLang(lang);
            for (Dimension dimension : report.getDimensions()) {
                dimension.getMultilingualization().get(i).setLang(lang);
            }
            for (Indicator indicator : report.getIndicators()) {
                indicator.getMultilingualization().get(i).setLang(lang);
            }
        }
    }

    private void sendRefreshReportRequest() throws Exception {
        HttpPost request = new HttpPost(new URL((new URL(reportServerUri)).toExternalForm() + "/refresh").toURI());
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            httpClient.execute(request);
        }
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

    private List<String> getAllRoles() {
        Config config = ConfigProvider.getConfig();
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(config.getValue("keycloak.serverurl", String.class))
                .realm("master")
                .clientId("admin-cli")
                .grantType(OAuth2Constants.PASSWORD)
                .username(config.getValue("keycloak.username", String.class))
                .password(config.getValue("keycloak.password", String.class))
                .build();
        List<RoleRepresentation> roleRepresentation = keycloak.realm(config.getValue("keycloak.realm", String.class)).roles().list();
        return roleRepresentation.stream().map(rr -> rr.getName()).collect(Collectors.toList());
    }

    public int getMaxDepth(Dimension d) {
        return getMatchingDimensionsFromCubes(d)
                .stream()
                .map(DimensionDTO::maxDepth)
                .reduce(Math::max)
                .orElse(-1);
    }

    public List<String> getLevels(Dimension d) {
        List<String> result = new ArrayList<>(4);
        if (d != null) {
            List<DimensionDTO> matchingDimensionsFromCubes = getMatchingDimensionsFromCubes(d);
            for (DimensionDTO dto : matchingDimensionsFromCubes) {
                List<String> newLevels = dto.levels().stream().map(LevelDTO::name).toList();
                for (int i = result.size(); i < newLevels.size(); i++) {
                    result.add(newLevels.get(i));
                }
            }
        }
        return result;
    }
    
    /**
     * Gets the available dimensions' names in a Cube.
     * 
     * @param c Cube to look for dimensions
     * @return List of dimension names
     */
    public String[] getDimensionNames(Cube c) {
        if (c != null) {
            CubeMetaDTO cubeDTO = cubeList.cubeMap().get(c.getName());
            if (cubeDTO != null) {
                return cubeDTO.dimensionHeader().stream().map(DimensionDTO::name).toArray(String[]::new);
            }
        }
        return new String[0];
    }
    
    /**
     * Gets the available measures' names in a Cube.
     * 
     * @param c Cube to look for measures
     * @return List of measure names
     */
    public List<String> getMeasureNames(Cube c) {
        List<String> result = new ArrayList<>();
        if (c != null) {            
            CubeMetaDTO cubeDTO = cubeList.cubeMap().get(c.getName());
            if (cubeDTO != null) {
                for (MeasureDTO measureDTO : cubeDTO.measureHeader()) {              
                    if (!measureDTO.hidden()) {
                        result.add(measureDTO.name());
                    }
                }            
            }
        }
        return result;                
    }
    
    /**
     * Gets the name-matching dimensionDTOs from the cubes currently the report
     * is based on.
     *
     * @param dimension Dimension in the report to process
     * @return List of matching dimensionDTOs.
     */
    private List<DimensionDTO> getMatchingDimensionsFromCubes(Dimension dimension) {
        String dimensionName = dimension.getName();
        List<DimensionDTO> result = new ArrayList<>(report.getCubes().size());
        for (Cube cube : report.getCubes()) {
            CubeMetaDTO cubeMetaDTO = cubeList.cubeMap().get(cube.getName());
            if (cubeMetaDTO != null) {
                for (DimensionDTO dimensionDTO : cubeMetaDTO.dimensionHeader()) {
                    if (dimensionDTO.name().equals(dimensionName)) {
                        result.add(dimensionDTO);
                    }
                }
            }
        }
        return result;
    }

    private void addIfEmpty() {
        if (report.getCubes().isEmpty()) {
            addCube();
        }
        if (report.getDimensions().isEmpty()) {
            addDimension();
        }
        if (report.getIndicators().isEmpty()) {
            addIndicator();
        }
    }

}
