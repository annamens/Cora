package com.adaptivebiotech.cora.test.mira;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.List;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.mira.Mira;
import com.adaptivebiotech.cora.ui.mira.MirasList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.utils.PageHelper.MiraExpansionMethod;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;
import com.adaptivebiotech.cora.utils.PageHelper.MiraPanel;
import com.adaptivebiotech.cora.utils.PageHelper.MiraStage;
import com.adaptivebiotech.cora.utils.PageHelper.MiraStatus;
import com.adaptivebiotech.cora.utils.PageHelper.MiraType;
import com.adaptivebiotech.test.utils.PageHelper.ShippingCondition;

public class MiraTestSuite extends CoraBaseBrowser {

    @Test
    public void testCreateAndActivateMIRA () {

        Login login = new Login ();
        login.doLogin ();
        OrdersList ordersList = new OrdersList ();
        ordersList.isCorrectPage ();
        ordersList.selectNewGeneralShipment ();

        Shipment shipment = new Shipment ();
        shipment.isBatchOrGeneral ();
        shipment.enterShippingCondition (ShippingCondition.Ambient);
        shipment.clickSave ();
        testLog ("saved general shipment " + shipment.getShipmentNum ());

        shipment.gotoAccession ();

        Accession accession = new Accession ();
        String fullPath = ClassLoader.getSystemResource ("MIRA/cora-intakemanifest_28JUL2020.xlsx").getPath ();
        accession.uploadIntakeManifest (fullPath);
        accession.clickIntakeComplete ();
        accession.labelingComplete ();
        accession.labelVerificationComplete ();
        accession.clickAccessionComplete ();
        accession.waitForStatus ("Accession Complete");
        testLog ("accession complete");

        List <String> specimenIds = accession.getSpecimenIds ();
        // these should all be the same
        testLog ("specimen ids are: ");
        for (String specimenId : specimenIds) {
            testLog (specimenId);
        }

        accession.selectNewMira ();
        Mira mira = new Mira ();
        mira.isCorrectPage ();
        mira.selectLab (MiraLab.AntigenMapProduction);
        mira.selectType (MiraType.MIRA);
        mira.selectPanel (MiraPanel.Minor);
        mira.selectExpansionMethod (MiraExpansionMethod.AntiCD3);
        mira.enterSpecimenAndFind (specimenIds.get (0));
        mira.clickSave (false);

        String miraId = mira.getMiraId ();
        testLog ("mira id is: " + miraId);

        List <String> containerIds = mira.getContainerIds ();
        testLog ("container Ids are: ");
        for (String id : containerIds) {
            testLog (id);
        }

        mira.verifyContainerId (containerIds.get (0));
        testLog ("verified container id: " + containerIds.get (0));

        String batchRecord = mira.createNewBatchRecord (miraId);
        testLog ("new batch record is: " + batchRecord);

        testLog ("about to upload batch record");
        mira.uploadBatchRecord (batchRecord);
        testLog ("uploaded batch record");

        mira.clickUploadAndSave (miraId);
        testLog ("clicked upload and save");
        mira.clickSave (true);
        testLog ("clicked save");
        mira.clickMiraPrepComplete ();

        testLog ("mira prep complete");
        mira.refresh ();

        testLog ("prep complete for mira " + miraId);

        mira.clickMiras ();
        MirasList mirasList = new MirasList ();
        mirasList.isCorrectPage ();
        mirasList.selectLab (MiraLab.AntigenMapProduction);
        mirasList.searchAndClickMira (miraId);

        mira.clickStatusTab ();
        assertTrue (mira.waitForStage (MiraStage.PoolExtraction));
        assertTrue (mira.waitForStatus (MiraStatus.Ready));

        mira.clickMiras ();
        mirasList.isCorrectPage ();
        mirasList.selectLab (MiraLab.AntigenMapProduction);
        mirasList.searchForMira (miraId);
        mirasList.clickSelect ();
        mirasList.selectMiraInList (miraId);

        String downloadedFileName = mirasList.clickCreateSampleManifest ();
        testLog ("downloaded sample manifest " + downloadedFileName);

        mirasList.clickMira (miraId);

        mira.clickStatusTab ();
        assertTrue (mira.waitForStage (MiraStage.immunoSEQ));
        assertTrue (mira.waitForStatus (MiraStatus.Awaiting));
        testLog ("mira " + miraId + " now in ImmunoSEQ/Awaiting stage");

        deleteFile (batchRecord);
        testLog ("deleted batch record " + batchRecord);

    }

    private void deleteFile (String filename) {
        File f = new File (filename);
        if (f.delete () == false) {
            testLog ("failed to delete tmp file " + filename);
        }
    }

}
