package com.adaptivebiotech.cora.utils;

import static com.adaptivebiotech.test.BaseEnvironment.coraDBHost;
import static com.adaptivebiotech.test.BaseEnvironment.coraJumpBox;
import static com.adaptivebiotech.test.BaseEnvironment.jumpboxPass;
import static com.adaptivebiotech.test.BaseEnvironment.jumpboxUser;
import static com.seleniumfy.test.utils.Logging.error;
import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.String.format;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.PublicKey;
import java.util.concurrent.CountDownLatch;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.connection.channel.direct.Parameters;

/**
 * 
 * based on
 * https://github.com/newjam/mysql_ssh_tunnel_example/blob/master/src/main/java/com/github/newjam/test/tunnel/Tunnel.java
 * 
 */
public class Tunnel implements Runnable, Closeable {

    private static Tunnel        tunnel;

    private final CountDownLatch tunnelEstablishedLatch = new CountDownLatch (1);

    private final String         SSH_USERNAME           = jumpboxUser;
    private final String         SSH_PASSWORD           = jumpboxPass;
    private final String         REMOTE_HOST            = coraJumpBox;
    private final String         DB_HOST                = coraDBHost;
    private final int            DB_PORT                = 5432;
    private final String         LOCAL_HOST             = "127.0.0.1";
    private final int            LOCAL_PORT             = 6000;

    private SSHClient            ssh                    = null;
    private ServerSocket         ss                     = null;
    private LocalPortForwarder   forwarder              = null;

    public static synchronized Tunnel getTunnel () {
        if (tunnel == null) {
            tunnel = new Tunnel ();
        }
        return tunnel;
    }

    private Tunnel () {}

    private static boolean hostKeyVerify (String string, int i, PublicKey pk) {
        return true;
    }

    public boolean waitForConnection () {
        try {
            tunnelEstablishedLatch.await ();
            Thread.sleep (50);
        } catch (InterruptedException ex) {
            info ("Error waiting for connection to be established. " + ex.toString ());
            return false;
        }
        return true;
    }

    @Override
    public void run () {
        try {
            info ("Opening " + this.toString ());

            ssh = new SSHClient ();
            ssh.addHostKeyVerifier (Tunnel::hostKeyVerify);
            ssh.connect (REMOTE_HOST);
            ssh.authPassword (SSH_USERNAME, SSH_PASSWORD);
            info (format ("SSH connection %s authenticated", ssh.isAuthenticated () ? "is" : "is not"));

            final Parameters params = new Parameters (LOCAL_HOST, LOCAL_PORT, DB_HOST, DB_PORT);
            ss = new ServerSocket ();
            ss.setReuseAddress (true);
            ss.bind (new InetSocketAddress (params.getLocalHost (), params.getLocalPort ()));
            forwarder = ssh.newLocalPortForwarder (params, ss);
            tunnelEstablishedLatch.countDown ();
            forwarder.listen ();
            info ("Tunnel thread finished.");
        } catch (IOException ex) {
            info ("Error establishing ." + this.toString () + " " + ex.toString ());
        } finally {
            info ("Terminated " + this.toString ());
        }
    }

    @Override
    public String toString () {
        String base = "Tunnel from %s:%d to %s:%d using jumpbox %s";
        return format (base, LOCAL_HOST, LOCAL_PORT, DB_HOST, DB_PORT, REMOTE_HOST);
    }

    @Override
    public void close () {
        try {
            if (forwarder != null) {
                forwarder.close ();
            }

            if (ss != null) {
                info ("Closing ServerSocket.");
                ss.close ();
            }

            // work around for net.schmizz.sshj.transport.TransportException: Disconnected
            // https://github.com/hierynomus/sshj/issues/317
            Thread.sleep (3000);

            if (ssh != null) {
                info ("Closing SSHClient.");
                ssh.disconnect ();
            }
        } catch (Exception e) {
            error ("Failed to close the tunnel", e);
        }
    }
}
