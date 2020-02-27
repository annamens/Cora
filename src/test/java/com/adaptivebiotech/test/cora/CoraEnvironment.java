package com.adaptivebiotech.test.cora;

import static java.lang.String.format;
import com.adaptivebiotech.test.BaseEnvironment;

public class CoraEnvironment extends BaseEnvironment {

    public static String sftpServerPassword;
    public static String sftpServerUserName;
    public static String sftpServerHostName;
    public static String incomingPath;
    public static String projectID;
    public static String projectAccountID;
    public static String projectName;
    public static String reportPrefix;
    public static int    retryTimes;
    public static int    waitTime;

    public static void initialization () {
        BaseEnvironment.initialization ();
        coraTestUrl = format (appConfig.getProperty ("cora.test.url"), env);
        coraTestUser = appConfig.getProperty ("cora.test.user");
        coraTestPass = decrypt (appConfig.getProperty ("cora.test.pass"));
        sftpServerUserName = appConfig.getProperty ("cora.test.sftpserveruser");
        sftpServerPassword = appConfig.getProperty ("cora.test.sftpserverpass");
        sftpServerHostName = appConfig.getProperty ("cora.test.sftpserverhost");
        incomingPath = appConfig.getProperty ("cora.test.incomingpath");
        projectID = appConfig.getProperty ("cora.test.projectid");
        projectAccountID = appConfig.getProperty ("cora.test.projectaccountid");
        projectName = appConfig.getProperty ("cora.test.projectName");
        reportPrefix = appConfig.getProperty ("cora.test.reportprefix");
        retryTimes = Integer.parseInt (appConfig.getProperty ("cora.test.retrytimes"));
        waitTime = Integer.parseInt (appConfig.getProperty ("cora.test.waittime"));
        
    }
}
