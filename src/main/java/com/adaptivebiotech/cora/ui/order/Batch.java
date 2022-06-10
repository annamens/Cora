/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Shipment.LimsProjectType.Testing;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.Keys.ENTER;
import static org.testng.Assert.assertTrue;
import java.io.File;
import java.util.List;
import com.adaptivebiotech.cora.dto.Shipment;
import com.adaptivebiotech.cora.dto.Shipment.LimsProjectType;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class Batch extends OrderHeader {

    private final String projectType = "[name='projectType']";

    public Batch () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".salesforce-container"));
        assertTrue (waitUntilVisible (".shipments"));
        assertTrue (waitUntilVisible (projectType));
    }

    public void clickSaveAndActivate () {
        assertTrue (click ("[ng-click='ctrl.submitManifest()']"));
        assertTrue (isTextInElement (popupTitle, "Confirm Order"));
        assertTrue (click ("//button[contains(text(),'Save & Activate Order')]"));
        moduleLoading ();
        waitUntilActivated ();
    }

    public String getOrderNumber () {
        return getText ("//*[*[text()='Order #']]/div");
    }

    public void searchOrder (String ordernum) {
        assertTrue (setText ("[ng-model='ctrl.salesforceId']", ordernum));
        assertTrue (pressKey (ENTER));
        pageLoading ();
    }

    public List <Shipment> getShipments () {
        return waitForElements ("[batch-shipment='shipmentEntry']").stream ().map (el -> {
            Shipment s = new Shipment ();
            s.shipmentNumber = getText (el, "[ng-bind='ctrl.entry.shipment.shipmentNumber']");
            return s;
        }).collect (toList ());
    }

    public void addShipment (String shipmentNumber) {
        assertTrue (setText ("[ng-model='ctrl.shipmentNumber']", shipmentNumber));
        assertTrue (pressKey (ENTER));
    }

    public void selectLimsProjectType (LimsProjectType limsProjectType) {
        assertTrue (clickAndSelectValue (projectType, "string:" + limsProjectType));
    }

    public void uploadPreManifest (String file) {
        waitForElement ("input[name='preManifestFile']").sendKeys (new File (file).getAbsolutePath ());
        assertTrue (click ("[ng-click='ctrl.uploadManifest()']"));
        transactionInProgress ();
    }

    public void createBatchOrder (String sfdcOrder, String shipmentNumber, String preManifest) {
        selectNewBatchOrder ();
        isCorrectPage ();
        searchOrder (sfdcOrder);
        addShipment (shipmentNumber);
        selectLimsProjectType (Testing);
        uploadPreManifest (preManifest);
        clickSaveAndActivate ();
    }
}
