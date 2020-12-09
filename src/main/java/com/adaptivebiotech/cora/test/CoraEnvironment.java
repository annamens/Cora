package com.adaptivebiotech.cora.test;

import static java.lang.String.format;
import com.adaptivebiotech.test.BaseEnvironment;

public class CoraEnvironment extends BaseEnvironment {

    public static void initialization () {
        BaseEnvironment.initialization ();
        coraTestUrl = format (appConfig.getProperty ("cora.test.url"), env);
        coraTestUser = appConfig.getProperty ("cora.test.user");
        coraTestPass = decrypt (appConfig.getProperty ("cora.test.pass"));
    }
}
