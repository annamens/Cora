package com.adaptivebiotech.cora.ui.shipment;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.Discrepancy;
import com.adaptivebiotech.cora.utils.PageHelper.DiscrepancyAssignee;
import com.adaptivebiotech.cora.utils.PageHelper.DiscrepancyType;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class Accession extends CoraPage {

    private String accessionNotes = "#accession-notes";
    private String specimenApprovalPass = "#specimen-pass-button";
    private String specimenApprovalFail = "#specimen-fail-button";
       
    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active:nth-child(2)", "ACCESSION"));
        pageLoading ();
    }

    public void uploadIntakeManifest (String file) {
        waitForElement ("input[data-ng-model='ctrl.intakeManifestFiles']").sendKeys (file);
        moduleLoading ();
        assertTrue (click ("[data-ng-click='ctrl.proceedToFullAccessionScreen()']"));
        pageLoading ();
    }

    public void clickIntakeComplete () {
        assertTrue (click ("[data-ng-click*='intake-complete']"));
        assertTrue (isTextInElement (popupTitle, "Intake Complete Confirmation"));
        clickPopupOK ();
    }

    public void manualPass (DiscrepancyType type) {
        String conditionType = "[discrepancy-type='" + type + "'] #conditionType";
        if (!waitForElement (".specimen-approval [ng-click='ctrl.approveSpecimen(true)']").isEnabled ()) {
            assertTrue (clickAndSelectValue (conditionType, "ResolvedYes"));
            assertTrue (click ("[data-ng-click*='accession-save']"));
        }
    }

    public void clickPass () {
        assertTrue (click ("[ng-click='ctrl.approveSpecimen(true)']"));
        assertTrue (isTextInElement (popupTitle, "Specimen Approval Confirmation"));
        clickPopupOK ();
    }

    public void verifyLabels () {
        assertTrue (click ("[ng-click='ctrl.setLabelingComplete(container)']"));
        assertTrue (isTextInElement (popupTitle, "Labeling Complete Confirmation"));
        clickPopupOK ();
    }

    public void labelingComplete () {
        assertTrue (click ("[ng-click='ctrl.setLabelingComplete(container)']"));
        assertTrue (isTextInElement (popupTitle, "Labeling Complete Confirmation"));
        clickPopupOK ();
    }

    public void labelVerificationComplete () {
        assertTrue (click ("[ng-click='ctrl.setLabelVerificationComplete(container)']"));
        assertTrue (isTextInElement (popupTitle, "Label Verification Complete Confirmation"));
        clickPopupOK ();
    }

    public void gotoOrderDetail () {
        assertTrue (click ("[data-ng-bind='ctrl.entry.order.orderNumber']"));
        pageLoading ();
    }

    public void gotoShipment () {
        assertTrue (click ("[role='presentation'] [data-ng-click*='shipment']"));
        pageLoading ();
    }
    
    public void clickPassAllDocumentation () {
        assertTrue (click ("[ng-click=\"ctrl.setAccessionItemStatus('TwoMatchingIdentifiers', 'Pass')\"]"));
        assertTrue (click ("[ng-click=\"ctrl.setAccessionItemStatus('PatientNameMatch', 'Pass')\"]"));
        assertTrue (click ("[ng-click=\"ctrl.setAccessionItemStatus('DateOfBirthMatch', 'Pass')\"]"));
        assertTrue (click ("[ng-click=\"ctrl.setAccessionItemStatus('MrnMatch', 'Pass')\"]"));
        assertTrue (click ("[ng-click=\"ctrl.setAccessionItemStatus('UniqueSpecimenIdMatch', 'Pass')\"]"));
    }
    
    public void clickPassSpecimenType () {
        assertTrue (click ("[ng-click=\"ctrl.setAccessionItemStatus('SpecimenTypeMatch', 'Pass')\"]"));
    }

    public void clickPassShippingConditions () {
        assertTrue (click ("[ng-click=\"ctrl.setAccessionItemStatus('ShippingCondition', 'Pass')\"]"));
    }

    public void clickFailSpecimenType () {
        assertTrue (click ("[ng-click=\"ctrl.setAccessionItemStatus('SpecimenTypeMatch', 'Fail')\"]"));
    }

    public void clickFailShippingConditions () {
        assertTrue (click ("[ng-click=\"ctrl.setAccessionItemStatus('ShippingCondition', 'Fail')\"]"));
    }

    public void enterNotes (String notes) {
        assertTrue (setText (accessionNotes, notes));
    }

    public String getNotes () {
        return readInput (accessionNotes);
    }

    public void clickRevert () {
        String css = "[ng-click=\"ctrl.undoIntakeComplete()\"]";
        assertTrue (click (css));
        isCorrectPage ();
    }

    public void clickAddContainerSpecimenDiscrepancy () {
        String button = "button[title=\"Add Container/Specimen Discrepancy\"]"; // doesn't work
        assertTrue (click (button));
        pageLoading();
        String title = ".modal-title";
        String expectedTitle = "Discrepancy";
        assertEquals (waitForElementVisible(title).getText (), expectedTitle);
    }

    public boolean specimenApprovalPassEnabled () {
        return waitForElementVisible (specimenApprovalPass).isEnabled ();
    }

    public boolean specimenApprovalFailEnabled () {
        return waitForElementVisible (specimenApprovalFail).isEnabled ();
    }
    
    public void waitForStatus (String expectedStatus) {
        String status = "[ng-bind='ctrl.entry | shipmentEntryStatus']";
        assertTrue (isTextInElement (status, expectedStatus));
    }
    
    // discrepancy pop up methods
    
    public void addDiscrepancy (Discrepancy discrepancy, String notes,
                                DiscrepancyAssignee assignee) {
        String cssAdd = "#dropdownDiscrepancy";
        assertTrue (click (cssAdd));

        String menuItemFmtString = "//*[@class='discrepancies-options']/ul/li[text()='%s']";
        String menuItem = String.format (menuItemFmtString, discrepancy.text);
        
        assertTrue (click (menuItem));
        String cssTextArea = "[ng-repeat='discrepancy in ctrl.discrepancies'] textarea";
        assertTrue (setText (cssTextArea, notes));
        String cssAssignee = "[ng-repeat='discrepancy in ctrl.discrepancies'] select";
        assertTrue (clickAndSelectText (cssAssignee, assignee.text));
    }

    public void clickSave () {
        String cssSave = "[ng-click='ctrl.save()'";
        assertTrue (click (cssSave));
    }

}
