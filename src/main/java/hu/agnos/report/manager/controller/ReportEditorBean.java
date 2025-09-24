package hu.agnos.report.manager.controller;

import hu.agnos.cube.meta.resultDto.*;
import hu.agnos.report.entity.*;
import hu.agnos.report.manager.service.CubeService;
import hu.agnos.report.manager.service.ResourceFilesService;
import hu.agnos.report.repository.ReportRepository;
import hu.agnos.report.repository.KeywordsRepository;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import lombok.Getter;
import lombok.Setter;
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

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportEditorBean.class);   //!< Log kezelő
    private static final String TOKEN = "ééé";

    private String cubeServerUri;
    private String reportServerUri;

    @Setter
    @Getter
    private CubeList cubeList;  // List of available cubes
    @Getter
    private List<Keyword> keywordList;
    @Setter
    @Getter
    private Report report;  // The currently edited report

    @Setter
    @Getter
    private int editedLang;
    @Getter
    private boolean deleteReport;
    @Getter
    private List<String> availableDimensionNames;
    @Getter
    private List<String> availableIndicatorNames;
    @Getter
    private List<String> availableCubeNames;
    @Getter
    private List<String> availableMapFiles;
    @Getter
    private List<String> availableDictionaries;
    @Getter
    private List<String> roles;
    @Getter
    private final List<String> availableControlTypes = Arrays.asList("slider", "radio");
    private final List<String> controlParametersExamples = Arrays.asList(
            "{\"min\": 0, \"max\": 100, \"step\": 10}",
            "{\"values\": [0, 1, 2]}");

    @PostConstruct
    public void init() {

        Config config = ConfigProvider.getConfig();

        this.cubeServerUri = config.getValue("cube.server.uri", String.class);
        this.reportServerUri = config.getValue("report.server.uri", String.class);        
        this.roles = getAllRoles();
        roles.sort(null);
        this.deleteReport = false;
        this.cubeList = CubeService.getCubeList(cubeServerUri);
        this.keywordList = (new KeywordsRepository()).findAll();
        keywordList.sort(Comparator.comparing(Keyword::getName));
        initAvailableCubeNames();
        this.editedLang = 0;
        
        String mapFilesDirectory = config.getValue("map.files.dir", String.class);
        this.availableMapFiles = new ArrayList<>(ResourceFilesService.getResourceFilesSet(mapFilesDirectory));
        availableMapFiles.sort(null);
        String dictionaryDirectory = config.getValue("dictionary.files.dir", String.class);
        this.availableDictionaries = new ArrayList<>(ResourceFilesService.getResourceFilesSet(dictionaryDirectory));
        availableDictionaries.sort(null);

        Object reportParameter = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("reportName");

        if (reportParameter == null) { // Ha új reportról van szó
            this.report = new Report();
            report.addLanguage("");
            report.setCubes(new ArrayList<>(2));
        } else { // Ha régiről
            String reportName = (String) reportParameter;
            Optional<Report> optReport = (new ReportRepository()).findByName(reportName);
            optReport.ifPresent(value -> this.report = value);
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

    private void initAvailableCubeNames() {
        this.availableCubeNames = new ArrayList<>(cubeList.cubeMap().keySet());
        java.util.Collections.sort(availableCubeNames);
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
    
    private void validateControls() {
        Iterator<Control> i = report.getControls().iterator();
        while (i.hasNext()) {
            Control c = i.next();
            if (!availableControlTypes.contains(c.getType())) {
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
        Iterator<Indicator> indicatorIterator = report.getIndicators().iterator();
        while (indicatorIterator.hasNext()) {
            Indicator in = indicatorIterator.next();
            if (in.getCombinedValueName() == null) {
                in.setCombinedValueName("1");
            }
            if (in.getCombinedDenominatorName() == null) {
                in.setCombinedDenominatorName("1");
            }
            if (!availableIndicatorNames.contains(in.getCombinedValueName()) || !availableIndicatorNames.contains(in.getCombinedDenominatorName())) {
                indicatorIterator.remove();
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

    public void moveDimension(int index, int direction) {
        Collections.swap(report.getDimensions(), index, index + direction);
        changeAllVariableIndexes("D", index, direction);
    }

    public void removeDimension(int index) {
        report.getDimensions().remove(index);
        changeAllVariableIndexes("D", index, 0);
        addIfEmpty();
    }

    public void addDimension() {
        report.getDimensions().add(new Dimension(report.getLabels()));
    }
    
    public void moveControl(int index, int direction) {
        Collections.swap(report.getControls(), index, index + direction);
        changeAllVariableIndexes("c", index, direction);
    }

    public void removeControl(int index) {
        report.getControls().remove(index);
        changeAllVariableIndexes("c", index, 0);
        addIfEmpty();
    }

    public void addControl() {
        report.getControls().add(new Control(report.getLabels()));
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
    
    /**
     * Sets a control element's default parameters.
     * 
     * @param index Index of the control to set.
     */
    public void setControlAtIndex(int index) {
        Control halfSetControl = report.getControls().get(index);
        int indexOfSelectedType = availableControlTypes.indexOf(halfSetControl.getType());
        halfSetControl.setParameters(controlParametersExamples.get(indexOfSelectedType));
        halfSetControl.setDefaultValue("0");
    }
       
    public void moveIndicator(int index, int direction) {
        Collections.swap(report.getIndicators(), index, index + direction);
        changeAllVariableIndexes("v", index, direction);
        changeAllVariableIndexes("d", index, direction);
    }

    public void removeIndicator(int index) {
        report.getIndicators().remove(index);
        changeAllVariableIndexes("v", index, 0);
        changeAllVariableIndexes("d", index, 0);
        addIfEmpty();
    }

    public void addIndicator() {
        report.addIndicator(new Indicator(report.getLabels()));
    }

    private void changeAllVariableIndexes(String variableName, int index, int direction) {
        for (Indicator indicator : report.getIndicators()) {
            indicator.setValueFunction(ReportEditorBean.changeVariableIndex(indicator.getValueFunction(), variableName, index, direction));
            indicator.setDenominatorFunction(ReportEditorBean.changeVariableIndex(indicator.getDenominatorFunction(), variableName, index, direction));
        }
    }

    private static String changeVariableIndex(String functionBody, String variableName, int index, int direction) {
        if (direction == 0) {
            String s = functionBody.replaceAll(variableName + "\\[" + index + "\\]", variableName + "[MISSING]");
            for (int i = index + 1; i < 99; i++) {
                s = s.replaceAll(variableName + "\\[" + i + "\\]", variableName + "[" + (i - 1) + "]");
            }
            return s;
        } else {
            return functionBody.replaceAll(variableName + "\\[" + index + "\\]", ReportEditorBean.TOKEN + "[" + (index + direction) + "]")
                    .replaceAll(variableName + "\\[" + (index + direction) + "\\]", ReportEditorBean.TOKEN + "[" + index + "]")
                    .replaceAll(ReportEditorBean.TOKEN + "\\[", variableName + "[");
        }
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
            validateControls();
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
            for (Control control : report.getControls()) {
                control.getMultilingualization().get(i).setLang(lang);
            }            
            for (Indicator indicator : report.getIndicators()) {
                indicator.getMultilingualization().get(i).setLang(lang);
            }
        }
    }

    private void sendRefreshReportRequest() throws java.net.URISyntaxException, IOException, java.net.MalformedURLException, org.apache.http.client.ClientProtocolException {
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
        if (report.getControls().isEmpty()) {
            addControl();
        }        
        if (report.getIndicators().isEmpty()) {
            addIndicator();
        }
    }

}
