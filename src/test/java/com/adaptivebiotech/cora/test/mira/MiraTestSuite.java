package com.adaptivebiotech.cora.test.mira;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertTrue;

import java.util.List;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.CoraPage;
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

        MiraLab miraLab = MiraLab.AntigenMapProduction;
        MiraType miraType = MiraType.MIRA;
        MiraPanel miraPanel = MiraPanel.Minor;
        MiraExpansionMethod miraExpansionMethod = MiraExpansionMethod.AntiCD3;

        loginToCora ();

        List <String> specimenIds = createGeneralShipmentFromIntakeManifest ("MIRA/cora-intakemanifest_28JUL2020.xlsx");

        String miraId = createNewMira (miraLab,
                                       miraType,
                                       miraPanel,
                                       miraExpansionMethod,
                                       specimenIds.get (0));

        gotoMiraByLabAndId (miraId, miraLab);

        waitForStageAndStatus (MiraStage.PoolExtraction, MiraStatus.Ready);

        createSampleManifest (miraId, MiraLab.AntigenMapProduction);
    }

    private void loginToCora () {
        Login login = new Login ();
        login.doLogin ();
        OrdersList ordersList = new OrdersList ();
        ordersList.isCorrectPage ();
    }

    private void waitForStageAndStatus (MiraStage miraStage, MiraStatus miraStatus) {
        Mira mira = new Mira ();
        mira.clickStatusTab ();
        assertTrue (mira.waitForStage (miraStage));
        assertTrue (mira.waitForStatus (miraStatus));
    }

    private void gotoMiraByLabAndId (String miraId, MiraLab miraLab) {
        CoraPage coraPage = new CoraPage ();
        coraPage.clickMiras ();
        MirasList mirasList = new MirasList ();

        mirasList.isCorrectPage ();
        mirasList.searchAndClickMira (miraId, miraLab);
    }

    private String createSampleManifest (String miraId, MiraLab miraLab) {
        CoraPage coraPage = new CoraPage ();
        coraPage.clickMiras ();
        MirasList mirasList = new MirasList ();

        mirasList.isCorrectPage ();
        mirasList.searchForMira (miraId, miraLab);
        mirasList.clickSelect ();
        mirasList.selectMiraInList (miraId);

        String downloadedFileName = mirasList.clickCreateSampleManifest ();
        testLog ("downloaded sample manifest " + downloadedFileName);

        mirasList.clickMira (miraId);

        waitForStageAndStatus (MiraStage.immunoSEQ, MiraStatus.Awaiting);
        testLog ("mira " + miraId + " now in ImmunoSEQ/Awaiting stage");

        return downloadedFileName;
    }

    private String createNewMira (MiraLab miraLab, MiraType miraType, MiraPanel miraPanel,
                                  MiraExpansionMethod miraEM, String specimenId) {
        CoraPage coraPage = new CoraPage ();
        coraPage.selectNewMira ();

        Mira mira = new Mira ();
        mira.isCorrectPage ();
        mira.selectLab (miraLab);
        mira.selectType (miraType);
        mira.selectPanel (miraPanel);
        mira.selectExpansionMethod (miraEM);
        mira.enterSpecimenAndFind (specimenId);
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

        return miraId;
    }

    private List <String> createGeneralShipmentFromIntakeManifest (String intakeManifest) {
        CoraPage coraPage = new CoraPage ();
        coraPage.selectNewGeneralShipment ();

        Shipment shipment = new Shipment ();
        shipment.isBatchOrGeneral ();
        shipment.enterShippingCondition (ShippingCondition.Ambient);
        shipment.clickSave ();
        testLog ("saved general shipment " + shipment.getShipmentNum ());

        shipment.gotoAccession ();

        Accession accession = new Accession ();
        accession.isCorrectPage ();
        String fullPath = ClassLoader.getSystemResource (intakeManifest).getPath ();
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

        return specimenIds;
    }

}
