/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.shipment;

import static org.testng.Assert.assertTrue;
import java.io.File;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class BatchAccession extends Accession {

    public void uploadIntakeManifest (String file) {
        waitForElement ("input[name='intakeManifestFiles']").sendKeys (new File (file).getAbsolutePath ());
        transactionInProgress ();
        assertTrue (isTextInElement ("#proceed-ui span", "Containers successfully created."));
        assertTrue (click ("#proceed-ui button"));
        pageLoading ();
    }

    public void clickPassAllSpecimens () {
        assertTrue (click ("[ng-click='ctrl.setAllIsApprovalSelected(container, true)']"));
        assertTrue (click (".accession-item-set-approval-header button"));
        assertTrue (click ("[data-ng-click=\"ctrl.setApproval(container, 'Pass')\"]"));
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
