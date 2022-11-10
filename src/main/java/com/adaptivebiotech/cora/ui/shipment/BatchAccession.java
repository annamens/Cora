/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.shipment;

import static org.testng.Assert.assertTrue;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class BatchAccession extends Accession {

    private final String intakeRow = "[ng-if='ctrl.entry.shipment.intakeInitialized']";

    public void uploadIntakeManifest (String file) {
        String uploadBeforeContainersAdded = "input[name='intakeManifestFiles']";
        // different locator in UI depending on if containers have been added prior
        if (isElementPresent (uploadBeforeContainersAdded)) {
            uploadFile (uploadBeforeContainersAdded, file);
        } else {
            uploadFile ("input[name='file']", file);
        }
        transactionInProgress ();
        pageLoading ();
    }

    public void clickProceed () {
        assertTrue (click ("#proceed-ui button"));
        pageLoading ();
    }

    public void clickCreateIntakeDetails () {
        assertTrue (click ("[ng-click='ctrl.initializeResearchAccession()']"));
        transactionInProgress ();
    }

    public void clickPassAllSpecimens () {
        waitForElements (intakeRow).forEach (el -> {
            assertTrue (click (el, "[ng-click='ctrl.setAllIsApprovalSelected(container, true)']"));
            assertTrue (click (el, ".accession-item-set-approval-header button"));
            assertTrue (click (el, "[data-ng-click=\"ctrl.setApproval(container, 'Pass')\"]"));
        });
    }

    public void clickLabelingComplete () {
        waitForElements (intakeRow).forEach (el -> {
            assertTrue (click (el, "[ng-click='ctrl.setLabelingComplete(container)']"));
            assertTrue (isTextInElement (popupTitle, "Labeling Complete Confirmation"));
            clickLabelingCompleteButton ();
        });
        assertTrue (isTextInElement (shipmentStatus, "Labeling Complete"));
    }

    public void clickLabelVerificationComplete () {
        waitForElements (intakeRow).forEach (el -> {
            assertTrue (click (el, "[ng-click='ctrl.setLabelVerificationComplete(container)']"));
            assertTrue (isTextInElement (popupTitle, "Label Verification Complete Confirmation"));
            clickLabelVerificationCompleteButton ();
        });
        assertTrue (isTextInElement (shipmentStatus, "Label Verification Complete"));
    }

    public void completeBatchAccession () {
        clickIntakeComplete ();
        clickPassAllSpecimens ();
        clickLabelingComplete ();
        clickLabelVerificationComplete ();
        clickAccessionComplete ();
    }

    public void completeBatchAccession (String intakeManifest) {
        isCorrectPage ();
        uploadIntakeManifest (intakeManifest);
        completeBatchAccession ();
    }
}
