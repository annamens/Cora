package com.adaptivebiotech.cora.ui.shipment;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;
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
    
    public void clickPassAllDocumentation() {
        assertTrue(click("[ng-click=\"ctrl.setAccessionItemStatus('TwoMatchingIdentifiers', 'Pass')\"]"));
        assertTrue(click("[ng-click=\"ctrl.setAccessionItemStatus('PatientNameMatch', 'Pass')\"]"));
        assertTrue(click("[ng-click=\"ctrl.setAccessionItemStatus('DateOfBirthMatch', 'Pass')\"]"));
        assertTrue(click("[ng-click=\"ctrl.setAccessionItemStatus('MrnMatch', 'Pass')\"]"));
        assertTrue(click("[ng-click=\"ctrl.setAccessionItemStatus('UniqueSpecimenIdMatch', 'Pass')\"]"));
    }
    
    public void clickFailSpecimenType() {
        assertTrue(click("[ng-click=\"ctrl.setAccessionItemStatus('SpecimenTypeMatch', 'Fail')\"]"));
    }
    
    public void clickFailShippingConditions() {
        assertTrue(click("[ng-click=\"ctrl.setAccessionItemStatus('ShippingCondition', 'Fail')\"]"));
    }
    
    public void enterNotes(String notes) {
        assertTrue(setText (accessionNotes, notes));
    }
    
    public String getNotes() {
        return readInput(accessionNotes);
    }
    
    public void clickRevert() {
        String css = "[ng-click=\"ctrl.undoIntakeComplete()\"]";
        assertTrue(click(css));
        isCorrectPage ();
    }
    
    public void clickAddContainerSpecimenDiscrepancy() {
        String css = "[ng-click=\"ctrl.addDiscrepancies()\"]";
        assertTrue(click(css));
    }
    
    public void specimenApprovalPassFailDisabled() {
        assertFalse(waitForElementVisible(specimenApprovalPass).isEnabled ());
        assertFalse(waitForElementVisible(specimenApprovalFail).isEnabled ());
    }
    
    public void specimenApprovalPassFailEnabled() {
        assertTrue(waitForElementVisible(specimenApprovalPass).isEnabled ());
        assertTrue(waitForElementVisible(specimenApprovalFail).isEnabled ());
    }
    
}
