// TÖRÖLHETŐ

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.report.manager.converters;

import hu.mi.agnos.report.entity.Visualization;
import java.util.regex.Pattern;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author ruzsaz
 */
@FacesConverter("VisualizationConverter")
public class VisualizationConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String string) {
        int pos = string.indexOf(":");
        int order = -1;
        if (pos >= 0) {
            String firstPart = string.substring(0, pos);
            if (firstPart.matches("[-0-9]+")) {
                order = Integer.parseInt(firstPart);
                string = string.substring(pos+1);
            }
        }
        System.out.println("Converter: " + string + ", " + order);
        return new Visualization(string.replaceAll("\"","").trim(), order);
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        System.out.println("vissza: " + o.toString());        
        return ((Visualization) o).getOrder() + ": " + o.toString().replaceAll("\"","");
    }

}
