package com.adaptivebiotech.cora.utils;

import static org.junit.Assert.assertTrue;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import com.adaptivebiotech.cora.dto.KitOrder;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.seleniumfy.test.utils.Timeout;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

public class SftpServerHelper {

    private Session     session;
    private ChannelSftp sftpChannel;
    private String      userName;
    private String      password;
    private String      serverHost;

    public SftpServerHelper (String uName, String pwd, String shost) {

        userName = uName;
        password = pwd;
        serverHost = shost;
    }
    
    public ChannelSftp getChannel() {
        return sftpChannel;
    }

    public void startSftpChannel () {
        JSch jsch = new JSch ();
        session = null;
        try {
            session = jsch.getSession (userName, serverHost, 22);
            session.setConfig ("StrictHostKeyChecking", "no");
            session.setPassword (password);
            session.connect ();

            Channel channel = session.openChannel ("sftp");
            channel.connect ();
            sftpChannel = (ChannelSftp) channel;

        } catch (Exception e) {
            throw new RuntimeException (e);
        }

    }

    public void verifyDirOrFileInSftpServer (String path, int retry, int waitTime) {

        Timeout timer = new Timeout (retry, waitTime);
        boolean result = false;
        do {
            timer.Wait ();
            try {
                String currdir = sftpChannel.pwd ();
                SftpATTRS attrs = null;
                attrs = sftpChannel.stat (currdir + path);
                result = (attrs != null);
            } catch (Exception e) {
                // redo loop again
            }
        } while (!timer.Timedout () && !result);
        assertTrue (result);
    }

    public void disconnectsftpChannel () {
        sftpChannel.exit ();
        session.disconnect ();
    }

    public void verifyCorrectDataInReportTrackingTsv (String path, KitOrder orderInformation,
                                                      String status, int retry, int waitTime) {  
        try {
            TsvParserSettings settings = new TsvParserSettings();
            TsvParser parser = new TsvParser(settings);
            Timeout timer = new Timeout (retry, waitTime);
            
            settings.getFormat().setLineSeparator("\n");
            boolean result = false;
            // retry loop
            do {
                timer.Wait ();
                String tsvTempFile = "target/test.tsv";
                sftpChannel.get (path, tsvTempFile);
           
                List<Record >  allRecords = parser.parseAllRecords(new FileReader("target/test.tsv"));
                for (Record record : allRecords) {
                    if (record.getString ("Order number").equals (orderInformation.orderNum)){
                        assertTrue(record.getString ("Subject ID").equals(orderInformation.externalSubjectId));
                        assertTrue(record.getString ("Order date").equals(orderInformation.orderDate_ISO_DATE));                   
                        assertTrue(record.getString ("Order number").equals(orderInformation.orderNum));                     
                        assertTrue(record.getString ("Sample name").equals(orderInformation.sampleName));                        
                        assertTrue(record.getString ("Report number").equals(orderInformation.reportNum));                       
                        assertTrue(record.getString ("Pipeline version").equals(orderInformation.pipelineVersion));                      
                        assertTrue(record.getString ("Status").equals(status));
                        // Close the file once all data has been read.
                        Files.deleteIfExists (Paths.get (tsvTempFile));
                        result = true;
                        break;
                    } // if 
                } // for  
            } while (!timer.Timedout () && !result); // end of retry loop
            assertTrue (result);
            return;
        } catch (SftpException e) {
            throw new RuntimeException (e);
        } catch (IOException e) {
            throw new RuntimeException (e);
        } 
        
        
        /* try {
            Timeout timer = new Timeout (retry, waitTime);
            boolean result = false;
            // retry loop
            do {
                timer.Wait ();
                String tsvTempFile = "target/test.tsv";
                sftpChannel.get (path, tsvTempFile);
                StringTokenizer st;
                BufferedReader TSVFile = new BufferedReader (new FileReader (tsvTempFile));
                String dataRow = TSVFile.readLine ();

                while (dataRow != null) {
                    st = new StringTokenizer (dataRow, "\t");
                    List <String> dataArray = new ArrayList <String> ();
                    while (st.hasMoreElements ()) {
                        dataArray.add (st.nextElement ().toString ());
                    }

                    String orderNum = dataArray.get (2);
                    // if find the order
                    if (orderNum.equals (orderInformation.orderNum)) {
                        assertEquals (dataArray.get (0), orderInformation.externalSubjectId);
                        assertEquals (dataArray.get (1), orderInformation.orderDate_ISO_DATE);
                        assertEquals (dataArray.get (2), orderInformation.orderNum);
                        assertEquals (dataArray.get (3), orderInformation.sampleName);
                        assertEquals (dataArray.get (4), orderInformation.reportNum);
                        assertEquals (dataArray.get (5), orderInformation.pipelineVersion);
                        assertEquals (dataArray.get (6), status);
                        // Close the file once all data has been read.
                        TSVFile.close ();
                        Files.deleteIfExists (Paths.get (tsvTempFile));
                        result = true;
                        break;
                    }
                    dataRow = TSVFile.readLine (); // Read next line of data.
                } // end of while loop: readline in tsv file
                  // do not find the order in the file
                TSVFile.close ();
                Files.deleteIfExists (Paths.get (tsvTempFile));

            } while (!timer.Timedout () && !result); // end of retry loop
            assertTrue (result);
            return;
        } catch (SftpException e) {
            throw new RuntimeException (e);
        } catch (IOException e) {
            throw new RuntimeException (e);
        } */
    } 

}
