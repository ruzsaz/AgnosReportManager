/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.report.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author parisek
 */
public class ResourceHandler {
    
    public Properties getProperties(String fileName) throws IOException{
        fileName += ".properties";
        InputStream is = getClass().getClassLoader().getResourceAsStream("../resources/"+fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        Properties prop = new Properties();
        String thisLine;
        while ((thisLine = br.readLine()) != null) {
//            System.out.println(thisLine);
            prop.addProperty(thisLine);
         } 
        return prop;
    }
    
}
