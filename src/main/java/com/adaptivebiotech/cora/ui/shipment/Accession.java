package com.adaptivebiotech.cora.ui.shipment;

import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.utils.PageHelper.DiscrepancyType;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class Accession extends ShipmentHeader {

    public final String[] documents            = new String[] {
            "TwoMatchingIdentifiers", "PatientNameMatch", "DateOfBirthMatch", "MrnMatch", "UniqueSpecimenIdMatch"
    };
    public final String[] specimens            = new String[] {
            "SpecimenTypeMatch", "CollectionDateExists", "CollectionDateInRange", "ShippingCondition"
    };
    private final String  accessionItemLocator = "[ng-click=\"ctrl.setAccessionItemStatus('%s', '%s')\"] .accession-radio";
    private final String  orderNo              = "#order-link";
    private final String  accessionNotes       = "#accession-notes";
    private final String  specimenApprovalPass = "#specimen-pass-button";
    private final String  specimenApprovalFail = "#specimen-fail-button";

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active:nth-child(2)", "ACCESSION"));
        pageLoading ();
    }

    public void clickOrderNumber () {
        assertTrue (click (orderNo));
    }

    public String getOrderNumber () {
        return getText (orderNo);
    }

    public String getIntakeCompleteDate () {
        return getText ("[ng-bind='ctrl.entry.shipment.intakeCompleted | localDateTime']") + ", " + getText ("[ng-bind='ctrl.entry.shipment.intakeCompletedBy']");
    }

    public String getSpecimenApprovedDate () {
        return getText ("[ng-bind='ctrl.entry.specimen.approvedDate | localDateTime']") + ", " + getText ("[ng-bind='ctrl.entry.specimen.approvedBy']");
    }

    public void uploadIntakeManifest (String file) {
        waitForElement ("input[data-ng-model='ctrl.intakeManifestFiles']").sendKeys (file);
        transactionInProgress ();
        assertTrue (click ("[data-ng-click='ctrl.proceedToFullAccessionScreen()']"));
        pageLoading ();
    }

    public void clickIntakeComplete () {
        assertTrue (click ("[data-ng-click*='intake-complete']"));
        assertTrue (isTextInElement (popupTitle, "Intake Complete Confirmation"));
        clickPopupOK ();
        assertTrue (isTextInElement (shipmentStatus, "Intake Complete"));
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
        assertTrue (isTextInElement (shipmentStatus, "Specimen Approved (PASS)"));
    }

    public void clickLabelingComplete () {
        assertTrue (click ("[ng-click='ctrl.setLabelingComplete(container)']"));
        assertTrue (isTextInElement (popupTitle, "Labeling Complete Confirmation"));
        clickPopupOK ();
        assertTrue (isTextInElement (shipmentStatus, "Labeling Complete"));
    }

    public void clickLabelVerificationComplete () {
        assertTrue (click ("[ng-click='ctrl.setLabelVerificationComplete(container)']"));
        assertTrue (isTextInElement (popupTitle, "Label Verification Complete Confirmation"));
        clickPopupOK ();
        assertTrue (isTextInElement (shipmentStatus, "Label Verification Complete"));
    }

    public void clickPassAllDocumentations () {
        for (String document : documents)
            clickAccessionItemPass (document);
    }

    public void clickFailAllSpecimens () {
        for (String specimen : specimens)
            clickAccessionItemFail (specimen);
    }

    public void clickAccessionItemPass (String accessionItem) {
        if (!waitForElement (format (accessionItemLocator, accessionItem, "Pass")).isSelected ())
            assertTrue (click (format (accessionItemLocator, accessionItem, "Pass")));
    }

    public void clickAccessionItemFail (String accessionItem) {
        if (!waitForElement (format (accessionItemLocator, accessionItem, "Fail")).isSelected ())
            assertTrue (click (format (accessionItemLocator, accessionItem, "Fail")));
    }

    public boolean isAccessionItemPass (String accessionItem) {
        return isElementPresent (format (".pass[ng-class*='%s']", accessionItem));
    }

    public boolean isAccessionItemFail (String accessionItem) {
        return isElementPresent (format (".fail[ng-class*='%s']", accessionItem));
    }

    public void enterNotes (String notes) {
        assertTrue (setText (accessionNotes, notes));
    }

    public String getNotes () {
        return readInput (accessionNotes);
    }

    public void clickRevert () {
        assertTrue (click ("[ng-click='ctrl.undoIntakeComplete()']"));
        transactionInProgress ();
        isCorrectPage ();
        assertTrue (isTextInElement (shipmentStatus, "Arrived"));
    }

    public void clickAddContainerSpecimenDiscrepancy () {
        assertTrue (click ("[title='Add Container/Specimen Discrepancy']"));
        assertTrue (isTextInElement (popupTitle, "Discrepancy"));
    }

    public boolean specimenApprovalPassEnabled () {
        return waitForElementVisible (specimenApprovalPass).isEnabled ();
    }

    public boolean specimenApprovalFailEnabled () {
        return waitForElementVisible (specimenApprovalFail).isEnabled ();
    }

    public void clickAccessionComplete () {
        String accessionComplete = "[data-ng-click='ctrl.$scope.$broadcast(\\'research-accession-complete\\')']";
        assertTrue (click (accessionComplete));
        clickPopupOK ();
    }

    public List <String> getSpecimenIds () {
        String specimenIds = "[data-ng-bind='::specimen.specimen.specimenNumber']";
        return getTextList (specimenIds);
    }

    public void completeAccession () {
        isCorrectPage ();
        clickIntakeComplete ();
        clickLabelingComplete ();
        clickLabelVerificationComplete ();
        clickPass ();
        clickOrderNumber ();
    }
}
