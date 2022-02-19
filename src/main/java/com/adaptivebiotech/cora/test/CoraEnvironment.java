package com.adaptivebiotech.cora.test;

import static com.adaptivebiotech.test.utils.Logging.error;
import static java.lang.String.format;
import com.adaptivebiotech.test.BaseEnvironment;

public class CoraEnvironment extends BaseEnvironment {

    public static String portalCliaTestUrl;
    public static String portalIvdTestUrl;
    public static String pipelinePortalTestUser;
    public static String pipelinePortalTestPass;
    public static String limsTestUrl;

    public static void initialization () {
        try {
            BaseEnvironment.initialization ();
            coraTestUrl = format (getProperty ("cora.test.url"), env);
            coraTestUser = getProperty ("cora.test.user");
            coraTestPass = decrypt (getProperty ("cora.test.pass"));

            portalCliaTestUrl = format (getProperty ("portal.clia.test.url"), env);
            portalIvdTestUrl = format (getProperty ("portal.ivd.test.url"), env);
            pipelinePortalTestUser = getProperty ("portal.test.user");
            pipelinePortalTestPass = decrypt (getProperty ("portal.test.pass"));
            
            limsTestUrl = format (getProperty ("lims.test.url"), env);

            coraDBHost = format (getProperty ("cora.db.host"), env);
            coraDBUser = getProperty ("cora.db.user");
            coraDBPass = decrypt (getProperty ("cora.db.pass"));
            coraJumpBox = getProperty ("cora.db.jumpbox");

            jumpboxUser = getProperty ("ssh.user");
            jumpboxPass = getProperty ("ssh.pass");

            useDbTunnel = Boolean.parseBoolean (getProperty ("db.tunnel"));

        } catch (Exception e) {
            error ("failed to parse the config file", e);
            throw new RuntimeException (e);
        }
    }
}
