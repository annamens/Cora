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

    public static String pipelinePortalTestUrl;
    public static String pipelinePortalTestUser;
    public static String pipelinePortalTestPass;

    public static void initialization () {
        try {
            BaseEnvironment.initialization ();
            coraTestUrl = format (appConfig.getProperty ("cora.test.url"), env);
            coraTestUser = appConfig.getProperty ("cora.test.user");
            coraTestPass = decrypt (appConfig.getProperty ("cora.test.pass"));

            physicianLastName = "Tests";
            physicianFirstName = "Automated";
            physicianAccountName = "SEA_QA Test";
            NYphysicianLastName = "IgHV";
            NYphysicianFirstName = "Selenium";

            pipelinePortalTestUrl = format (appConfig.getProperty ("portal.test.url"), env);
            pipelinePortalTestUser = appConfig.getProperty ("portal.test.user");
            pipelinePortalTestPass = decrypt (appConfig.getProperty ("portal.test.pass"));

        } catch (Exception e) {
            error ("failed to parse the config file", e);
            throw new RuntimeException (e);
        }
    }
}
