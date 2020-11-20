package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.test.utils.PageHelper.StageName;

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
    
    public void clickPatientNotesIcon () {
        String css = "[ng-click=\"ctrl.showPatientNotesDialog()\"]";
        assertTrue (click (css));
        waitForElementVisible (".patient-notes-modal");
        assertTrue (getText (".modal-title").contains ("Patient Note for Patient "));
    }
    
    
    // patient notes popup
    public String getPatientNotes () {
        String css = "[ng-bind=\"ctrl.patient.notes\"]";
        String text = readInput (css);
        return text;
    }

    public int getClarityStageRequeueCount () {
        String css = ".ordertest-list-stage.Clarity .requeue-count";
        return Integer.valueOf (getText (css));
    }

    public StageName getCurrentWorkflowStage () {
        String css = ".is-current";
        WebElement webElement = waitForElementVisible (css);
        String text = webElement.getAttribute ("title");
        return StageName.valueOf (text);
    }

    public void expandWorkflowHistory () {
        String css = ".ordertest-list-stage.Clarity";
        assertTrue (click (css));
        pageLoading ();
        assertTrue (waitUntilVisible (".table.table-bordered.history"));
    }

    public boolean isWorkflowHistoryPresent (String stage, String status, String subStatus) {
        String css = ".table.table-bordered.history tbody tr";
        List <WebElement> workflowHistories = waitForElementsVisible (css);
        for (WebElement row : workflowHistories) {
            String rowStage = getText (row, "td:nth-child(1)");
            String rowStatus = getText (row, "td:nth-child(2)");
            String rowSubStatus = getText (row, "td:nth-child(3)");
            if (stage.equals (rowStage) && status.equals (rowStatus) && subStatus.equals (rowSubStatus)) {
                return true;
            }
        }
        return false;
    }
    
    public String getOrderStatusText () {
        String status = "[ng-bind='ctrl.orderEntry.order.status']";
        return getText (status);
    }

}
