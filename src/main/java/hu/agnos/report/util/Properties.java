/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.report.util;

import java.util.Vector;

/**
 *
 * @author parisek
 */
public class Properties extends Vector<String> {

    public Properties() {
        super();
    }
    
    public void addProperty(String property ){
        this.add(property);
    }

    public String getString(String keyWord){        
        keyWord += "=";
        for(int i =0; i < this.size(); i++){
            if(this.elementAt(i).startsWith(keyWord)){
                return this.elementAt(i).replaceFirst(keyWord, "");
            }
        }
        return null;
    }

    
    
    
}
