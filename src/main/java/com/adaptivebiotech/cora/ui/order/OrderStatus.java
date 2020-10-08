package com.adaptivebiotech.cora.ui.order;

public class OrderStatus extends Diagnostic {

    public String getOrderNum () {
        return getText ("[ng-bind='ctrl.orderEntry.order.orderNumber']");
    }

    public String getOrderName () {
        return getText ("[ng-bind='ctrl.orderEntry.order.name']");
    }

    public String getTestName () {
        return getText ("[ng-bind='::orderTest.testName']");
    }

    public boolean kitClonoSEQReportStageDisplayed () {

        return waitForElement ("[class='ordertest-list-stage KitClonoSEQReport']").isDisplayed ();
    }

    public boolean kitReportDeliveryStageDisplayed () {
        return waitForElement ("[class='ordertest-list-stage KitReportDelivery']").isDisplayed ();
    }

}
