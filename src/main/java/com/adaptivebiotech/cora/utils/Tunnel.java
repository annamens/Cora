package com.adaptivebiotech.cora.utils;

import static com.seleniumfy.test.utils.Logging.info;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.PublicKey;
import java.util.concurrent.CountDownLatch;
import com.adaptivebiotech.cora.test.CoraEnvironment;
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

    private final String         SSH_USERNAME           = CoraEnvironment.jumpboxUser;
    private final String         SSH_PASSWORD           = CoraEnvironment.jumpboxPass;
    private final String         REMOTE_HOST            = CoraEnvironment.coraJumpBox;
    private final String         DB_HOST                = CoraEnvironment.coraDBHost;
    private final int            DB_PORT                = 5432;
    private final String         LOCAL_HOST             = "127.0.0.1";
    private final int            LOCAL_PORT             = 6000;

    private SSHClient            ssh                    = null;
    private ServerSocket         ss                     = null;

    public static synchronized Tunnel getTunnel () {
        if (tunnel == null) {
            tunnel = new Tunnel ();
        }
        return tunnel;
    }

    private Tunnel () {

    }

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
            ss = new ServerSocket ();

            ssh.addHostKeyVerifier (Tunnel::hostKeyVerify);
            ssh.connect (REMOTE_HOST);
            ssh.authPassword (SSH_USERNAME, SSH_PASSWORD);

            info (String.format ("SSH connection %s authenticated", ssh.isAuthenticated () ? "is" : "is not"));

            final Parameters params = new Parameters (LOCAL_HOST, LOCAL_PORT,
                    DB_HOST, DB_PORT);

            ss.setReuseAddress (true);
            ss.bind (new InetSocketAddress (params.getLocalHost (), params.getLocalPort ()));

            LocalPortForwarder forwarder = ssh.newLocalPortForwarder (params, ss);

            tunnelEstablishedLatch.countDown ();
            forwarder.listen ();
        } catch (IOException ex) {
            info ("Error establishing ." + this.toString () + " " + ex.toString ());
        } finally {
            info ("Terminated " + this.toString ());
        }
    }

    @Override
    public String toString () {
        String base = "Tunnel from %s:%d to %s:%d using jumpbox %s";
        return String.format (base, LOCAL_HOST, LOCAL_PORT, DB_HOST, DB_PORT, REMOTE_HOST);
    }

    @Override
    public void close () throws IOException {
        if (ssh != null || ss != null) {
            info ("Closing " + this.toString ());
        } else {
            info (this.toString () + " is already closed.");
        }

        if (ssh != null) {
            info ("Closing SSHClient.");
            if (ssh.isConnected ()) {
                ssh.disconnect ();
            }
            ssh.close ();
            ssh = null;
        }

        if (ss != null) {
            info ("Closing ServerSocket.");
            ss.close ();
            ss = null;
        }

    }

}
