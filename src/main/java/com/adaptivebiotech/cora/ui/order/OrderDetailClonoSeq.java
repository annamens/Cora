package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;

/**
 * @author jpatel
 *
 */
public class OrderDetailClonoSeq extends OrderDetail {

    public BillingClonoSeq billing = new BillingClonoSeq ();

    public String getSpecimenDeliverySelectedOption () {
        String css = "[ng-bind^='ctrl.orderEntry.order.specimenDeliveryType']";
        if (isElementVisible (css)) {
            return getText (css);
        }
        return null;
    }

    public String getRetrievalDate () {
        String css = "[ng-bind^='ctrl.orderEntry.specimen.retrievalDate']";
        if (isElementVisible (css)) {
            return getText (css);
        }
        return null;
    }

    public void transferTrf () {
        assertTrue (click (".transfer-trf-btn"));
        assertTrue (isTextInElement (popupTitle, "Transfer TRF to New Order"));
        assertTrue (click ("[data-ng-click='ctrl.ok()']"));
        moduleLoading ();
        pageLoading ();
    }
}
