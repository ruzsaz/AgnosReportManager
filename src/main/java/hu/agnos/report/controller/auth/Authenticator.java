/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.report.controller.auth;


import java.io.IOException;

/**
 *
 * @author parisek
 */
public class Authenticator {

    private static Authenticator authenticator = null;
//    private final String LDAP_DOMAIN="";
//    private final String LDAP_SERVER="";

    private final long EXPIRATION_TIME = 100;

    // An authentication token storage which stores <service_key, auth_token>.
//    private static Map<String, Principal> authorizationTokensStorage = new HashMap();
    public Authenticator() throws IOException {

//        ResourceHandler resourceHandler = new ResourceHandler();
//        Properties props = resourceHandler.getProperties("settings");
//        this.LDAP_DOMAIN = props.getString("ldapDomain");
//        this.LDAP_SERVER = props.getString("ldapServer");
//        this.EXPIRATION_TIME = Long.parseLong(props.getString("expirationTime"));
    }

    public static Authenticator getInstance() throws IOException {
        if (authenticator == null) {
            authenticator = new Authenticator();
        }
        return authenticator;
    }

    /**
     * A logint elvégző metódus. Első körben a lokális adatbázisban próbál
     * authentikálni, és ha azt találja, hogy az adott usert AD-ban kell
     * authentikálni, akkor ott próbálja.
     *
     * @param username felhasználó név
     * @param password jelszó
     * @param remoteIp kliens IP-je
     * @return ha sikeres az autehntkáció, akkor egy AUTH token a visszatérési
     * érték, egyébként exception
     * @throws LoginException ha sikertelen az authentikáció.
     */
    public String login(String username, String password, String remoteIp) {
        if (username.equals("agnos.demo") && password.equals("zolikaokos")) {
            return "zsoltikaokos";
        } else {
//            try {
//                try {
//                    findUserByPasswdInLocalDB(username, password);
//                } catch (WrongAuthenticationMethodException ex) {                   
//                    if (this.LDAP_DOMAIN != null) {
//                        System.out.println(username + " " + password);
////                        findUserByPasswdInAD(username, password);                        
//                    } else {
//                        throw new LoginException(ex.getMessage());
//                    }
//                }
//
//                String authToken = UUID.randomUUID().toString().replaceAll(":", "");
//
//                while (AuthTokenStorageSingleton.getInstance().containsKey(authToken)) {
//                    authToken = UUID.randomUUID().toString().replaceAll(":", "");
//                }
//
//                AuthTokenStorageSingleton.getInstance().put(authToken, new Principal(username, remoteIp));
//
//                return authToken;
//
//            } catch (NamingException | IOException ex) {
//                throw new LoginException(ex.getMessage());
//            }
            return "zsoltikaokos";
        }
    }

    public void findUserByPasswdInAD(String login, String password)  {
        //AD login        
//        ActiveDirectory ad = new ActiveDirectory();
//        ad.login(login, password, this.LDAP_DOMAIN, this.LDAP_SERVER);
    }

    public void findUserByPasswdInLocalDB(String login, String password)  {
//        LocalDB lDB = new LocalDB();
//        lDB.login(login, password);
    }

    /**
     * The method that pre-validates if the client which invokes the REST API is
     * from a authorized and authenticated source.
     *
     * @param username User's name
     * @param authToken The authorization token generated after login
     * @param remoteIP IP of the client
     * @return TRUE for acceptance and FALSE for denied.
     * @throws javax.security.auth.login.LoginException
     */
    public boolean authTokenIsValid(String username, String authToken, String remoteIP) throws IOException {
//        String exceptionMsg = "";
//        System.out.println(AuthTokenStorageSingleton.getInstance().toString());
        if (username.equals("agnos.demo") && authToken.equals("zsoltikaokos")) {
            return true;
//        }

//        if (AuthTokenStorageSingleton.getInstance().containsKey(authToken)) {
//            Principal principal = AuthTokenStorageSingleton.getInstance().get(authToken);
//            long now = new java.util.Date().getTime();
//
//            if (!principal.getUser().equals(username)) {
////                exceptionMsg = "Invalid user!";
//                AuthTokenStorageSingleton.getInstance().remove(authToken);
//                return false;
//            } else if (!principal.getRemoteIP().equals(remoteIP)) {
////                exceptionMsg = "Invalid IP!";
//                AuthTokenStorageSingleton.getInstance().remove(authToken);
//                return false;
//            } else if ((now - principal.getCreateTimestampInMillsec()) > this.EXPIRATION_TIME) {
////                exceptionMsg = "Expired authorization token!";
//                AuthTokenStorageSingleton.getInstance().remove(authToken);
//                return false;
//            } else {
//                AuthTokenStorageSingleton.getInstance().remove(authToken);
//                AuthTokenStorageSingleton.getInstance().put(authToken, new Principal(username, remoteIP));
//                return true;
//            }
        } else {
//            exceptionMsg = "Invalid authorization token!";
            return true;
        }
    }

    public void logout(String username, String authToken, String remoteIP) throws  IOException {
        if (!(username.equals("agnos.demo") && authToken.equals("zsoltikaokos"))) {
//            if (AuthTokenStorageSingleton.getInstance().containsKey(authToken)) {
//                Principal principal = AuthTokenStorageSingleton.getInstance().get(authToken);
//                long now = new java.util.Date().getTime();
//                if (!principal.getUser().equals(username)) {
//                    AuthTokenStorageSingleton.getInstance().remove(authToken);
//                    throw new GeneralSecurityException("Invalid user!");
//                } else if (!principal.getRemoteIP().equals(remoteIP)) {
//                    AuthTokenStorageSingleton.getInstance().remove(authToken);
//                    throw new GeneralSecurityException("Invalid IP!");
//                } else {
//                    //Ez csak 1.8 java-ban
////                AuthTokenStorageSingleton.getInstance().replace(authToken, new Principal(username, remuteIP));
//                    AuthTokenStorageSingleton.getInstance().remove(authToken);
//                    AuthTokenStorageSingleton.getInstance().put(authToken, new Principal(username, remoteIP));
//
//                }
//            } else {
//                throw new GeneralSecurityException("Invalid authorization token!");
//            }
        }
    }
}
