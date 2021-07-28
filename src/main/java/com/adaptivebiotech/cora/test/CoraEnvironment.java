package com.adaptivebiotech.cora.test;

import static com.adaptivebiotech.test.utils.Logging.error;
import static java.lang.String.format;
import com.adaptivebiotech.test.BaseEnvironment;

public class CoraEnvironment extends BaseEnvironment {
    
    public static String physicianLastName;
    public static String physicianFirstName;
    public static String physicianAccountName;
    public static String NYphysicianLastName;
    public static String NYphysicianFirstName;
    
    public static void initialization () {
        try {
            BaseEnvironment.initialization ();
            coraTestUrl = format (appConfig.getProperty ("cora.test.url"), env);
            coraTestUser = appConfig.getProperty ("cora.test.user");
            coraTestPass = decrypt (appConfig.getProperty ("cora.test.pass"));
            
            physicianLastName = getProperty ("physician.last.name");
            physicianFirstName = getProperty ("physician.first.name");
            physicianAccountName = getProperty ("physician.account.name");
            NYphysicianLastName = getProperty ("nyphysician.last.name");
            NYphysicianFirstName = getProperty ("nyphysician.first.name");
            
        } catch (Exception e) {
            error ("failed to parse the config file", e);
            throw new RuntimeException (e);
        }
    }
}
