/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test;

import static com.adaptivebiotech.pipeline.test.PipelineEnvironment.portalTestPass;
import static com.adaptivebiotech.pipeline.test.PipelineEnvironment.portalTestUrl;
import static com.adaptivebiotech.pipeline.test.PipelineEnvironment.portalTestUser;
import static com.seleniumfy.test.utils.Logging.error;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import com.adaptivebiotech.common.dto.Server;
import com.adaptivebiotech.test.BaseEnvironment;

public class CoraEnvironment extends BaseEnvironment {

    public static String portalCliaTestUrl;
    public static String portalIvdTestUrl;
    public static String limsTestUrl;
    public static Server jumpbox;
    public static Server coraDbInfo;

    public static void initialization () {
        try {
            BaseEnvironment.initialization ();
            coraTestUrl = format (getProperty ("cora.test.url"), env);
            coraTestUser = getProperty ("cora.test.user");
            coraTestPass = getPropertyEncrypted ("cora.test.pass");

            portalCliaTestUrl = format (getProperty ("portal.clia.test.url"), env);
            portalIvdTestUrl = format (getProperty ("portal.ivd.test.url"), env);
            portalTestUrl = portalCliaTestUrl;
            portalTestUser = getProperty ("portal.test.user");
            portalTestPass = getPropertyEncrypted ("portal.test.pass");

            limsTestUrl = format (getProperty ("lims.test.url"), env);

            jumpboxServer = format (getProperty ("jumpbox.server"), env);
            jumpboxUser = getProperty ("jumpbox.user");
            jumpboxPass = getProperty ("jumpbox.pass");
            jumpbox = new Server (jumpboxServer, jumpboxUser, jumpboxPass);
            jumpbox.localHost = "localhost";
            jumpbox.localPort = 6000;
            jumpbox.remoteHost = format (getProperty ("cora.db.host"), env);
            jumpbox.remotePort = 5432;

            coraDbInfo = new Server ();
            coraDbInfo.databaseUrl = format ("jdbc:postgresql://%s:5432/coradb", jumpbox.remoteHost);
            coraDbInfo.user = getProperty ("cora.db.user");
            coraDbInfo.pass = getPropertyEncrypted ("cora.db.pass");

            useDbTunnel = parseBoolean (getProperty ("db.tunnel"));
            if (useDbTunnel)
                coraDbInfo.databaseUrl = format ("jdbc:postgresql://%s:%s/coradb",
                                                 jumpbox.localHost,
                                                 jumpbox.localPort);
        } catch (Exception e) {
            error ("failed to parse the config file", e);
            throw new RuntimeException (e);
        }
    }
}
