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
            azureLogin = appConfig.getProperty ("azure.login");
            azurePassword = decrypt (appConfig.getProperty ("azure.password"));
            
            
        } catch (Exception e) {
            error ("failed to parse the config file", e);
            throw new RuntimeException (e);
        }
    }
}
