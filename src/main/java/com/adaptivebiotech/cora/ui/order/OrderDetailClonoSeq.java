/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

/**
 * @author jpatel
 *
 */
public class OrderDetailClonoSeq extends OrderDetail {

    public BillingOrderDetail billing = new BillingOrderDetail ();

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

    public boolean getSpecimenCoordinationFlagVisibility () {
        String css = "[formcontrolname='specimenCoordination']";
        return isElementPresent (css);
    }

    public boolean getSpecimenCoordinationFlagSelected () {
        String css = "[formcontrolname='specimenCoordination']";
        return findElement (css).isSelected ();
    }
}
