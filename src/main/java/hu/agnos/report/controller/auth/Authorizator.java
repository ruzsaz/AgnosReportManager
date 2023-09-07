/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.report.controller.auth;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author parisek
 */
public class Authorizator {

    //TODO: a static vajon jó-e
    public static boolean hasPermission(String userName, String principalName) {

        return true;
//        try {
//            UserPrincipalStorageSingleton usersPrincipalStorage = UserPrincipalStorageSingleton.getInstance();
//
//            if (usersPrincipalStorage.containsKey(userName)) {
////                System.out.println("Van ilyen user!!!!!!!!!!!!!");
//
//                if (usersPrincipalStorage.get(userName).hasRole(principalName)) {
//                    return true;
//                }
//            }
//
//        } catch (IOException ex) {
//            Logger.getLogger(Authorizator.class.getName()).log(Level.SEVERE, null, ex);
////            System.out.println("ioe: " + ex.getMessage());
//        }
//        return false;
    }
}
