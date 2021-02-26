package com.adaptivebiotech.cora.test.mira;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertEquals;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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

        /*
         * create antigen map production lab MIRA and take it through completing MIRA prep
         * New → MIRA
         * Lab → Antigen Map Production
         * Type → MIRA
         * Panel → Minor
         * when this is selected it clears the panel selection and appears below
         * Expansion Method → anti-CD3
         * Specimen For Expansion → enter the specimen ID from the shipment (above) and click "Find"
         * there is a yellow message "This specimen is not associated with the selected lab."
         * click "Save"
         * get the MIRA ID from the top of the page and save it
         * scroll to Containers, copy the top Container ID, put in the bar code field and click
         * "Verify"
         * a blue checkmark appears in that container row in the table
         * 
         * 
         * Copy and edit the "M-xx_Batch Record.xslx" file to replace the existing MIRA ID with the
         * MIRA ID found in MIRA details
         * in the first tab
         * how to do this in java...?
         * Click "Upload File" and upload the M-yyyy_Batch Record.xslx that you edited above
         * Click "Upload & Save"
         * Click "Mira Prep Complete", then "Yes, Save changes" on the modal
         * Click "Yes, MIRA Prep Complete" on the modal
         * 
         * 
         * click MIRAs icon, search for Lab : Antigen Map Production and by MIRA ID
         * should return the MIRA
         * select MIRA, go to MIRA status
         * MIRA should be in PoolExtraction/Ready status
         * 
         * complete Pool Extraction
search for MIRA by ID and lab
click "Select" button (in upper right side of search results)
select the MIRA and click "Create Sample Manifest"
confirm in the modal
MIRA goes to ImmunoSEQ/Awaiting phase
         */

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

        String batchRecord = createNewBatchRecord (miraId);
        testLog ("new batch record is: " + batchRecord);

        testLog("about to upload batch record");
        mira.uploadBatchRecord (batchRecord);
        testLog("uploaded batch record");
        testLog ("driving on...");

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
        assertEquals (mira.getCurrentStage (), "PoolExtraction");
        assertEquals (mira.getCurrentStatus (), "Ready");
        
        mira.clickMiras ();
        mirasList.isCorrectPage ();
        mirasList.selectLab (MiraLab.AntigenMapProduction);
        mirasList.searchForMira (miraId);
        mirasList.clickSelect ();
        mirasList.selectMiraInList (miraId);
        
        String downloadedFileName = mirasList.clickCreateSampleManifest ();
        testLog("downloaded sample manifest " + downloadedFileName);
        
        mirasList.clickMira (miraId);
        
        mira.clickStatusTab ();
        assertEquals(mira.getCurrentStage (), "immunoSEQ");
        assertEquals(mira.getCurrentStatus (), "Awaiting");

    }

    private String createNewBatchRecord (String miraId) {
        String baseBatchRecord = "MIRA/M-xx_Batch_Record.xlsx";
        String basePath = ClassLoader.getSystemResource (baseBatchRecord).getPath ();
        String newBatchRecord = miraId + "_Batch_Record.xlsx";
        String newPath = "/Users/mgrossman/" + newBatchRecord;

        testLog ("basePath is: " + basePath);
        testLog ("newPath is: " + newPath);

        try {
            FileInputStream inputStream = new FileInputStream (new File (basePath));
            Workbook workbook = WorkbookFactory.create (inputStream);
            FileOutputStream outputStream = FileUtils.openOutputStream (new File (newPath));
            Sheet sheet = workbook.getSheet ("Experiment Request");
            sheet.protectSheet (null);
            Cell cell = sheet.getRow (2).getCell (0);
            String originalCellValue = cell.getStringCellValue ();
            testLog ("originalCellValue is: " + originalCellValue);
            cell.setBlank ();
            cell.setCellValue (miraId);
            String newCellValue = cell.getStringCellValue ();
            testLog ("newCellValue is: " + newCellValue);

            workbook.getCreationHelper ().createFormulaEvaluator ().evaluateAll ();
            testLog ("evaluated formulas");
            workbook.write (outputStream);
            outputStream.close ();
            inputStream.close ();
        } catch (Exception e) {
            throw new RuntimeException (e);
        }

        return newPath;

    }

}
