package com.adaptivebiotech.cora.ui.order;

/**
 * @author jpatel
 *
 */
public class BillingNewOrderClonoSeq extends BillingNewOrder {

    public String getPatientAddress1 () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.guarantor.address1']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    protected String getPatientPhone () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.guarantor.phone']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    protected String getPatientCity () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.guarantor.locality']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    protected String getPatientState () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.guarantor.region']";
        return isElementPresent (css) ? getFirstSelectedText (css) : null;
    }

    protected String getPatientZipcode () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.guarantor.postCode']";
        return isElementPresent (css) ? readInput (css) : null;
    }
}
