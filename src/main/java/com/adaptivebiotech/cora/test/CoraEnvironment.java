package com.adaptivebiotech.cora.test;

import static com.adaptivebiotech.test.utils.Logging.error;
import static java.lang.String.format;
import com.adaptivebiotech.test.BaseEnvironment;

public class CoraEnvironment extends BaseEnvironment {

    public static String azureLogin;
    public static String azurePassword;
    
    public static void initialization () {
        try {
            BaseEnvironment.initialization ();
            coraTestUrl = format (appConfig.getProperty ("cora.test.url"), env);
            coraTestUser = appConfig.getProperty ("cora.test.user");
            coraTestPass = decrypt (appConfig.getProperty ("cora.test.pass"));
            azureLogin = getProperty ("azure.login");
            error ("got azure login " + azureLogin);
            azurePassword = getPropertyEncryptedInFile ("azure.password");
            error ("got azure password length " + azurePassword.length ());
            
            
        } catch (Exception e) {
            error ("failed to parse the config file", e);
            throw new RuntimeException (e);
        }
    }
    
    private static String getProperty (String propertyName) {
        String property = System.getProperty (propertyName);
        if (property == null || property.length () == 0) {
            property = appConfig.getProperty (propertyName);
        }
        return property;

    }

    private static String getPropertyEncryptedInFile (String propertyName) {
        String property = System.getProperty (propertyName);
        if (property != null && property.length () > 0) {
            return property;
        }
        return decrypt (appConfig.getProperty (propertyName));
    }
}
