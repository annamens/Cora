package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Pending;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;

/**
 * @author jpatel
 *
 */
public class TDetectDiagnostic extends Diagnostic {

    @Override
    public String getOrderNum () {
        return getOrderNum (Pending);
    }

    @Override
    public String getOrderNum (OrderStatus state) {
        String css = Pending.equals (state) ? oEntry + " [formcontrolname='itemDetailsForm'] [label='Order #'] span" : ".detail-sections [ng-bind='ctrl.orderEntry.order.orderNumber']";
        return getText (css);
    }

    @Override
    public void createNewPatient (Patient patient) {
        clickPickPatient ();
        assertTrue (click ("#new-patient"));
        assertTrue (waitForElementInvisible (".ab-panel.matches"));
        assertTrue (isTextInElement (popupTitle, "Create New Patient"));
        assertTrue (setText ("#firstName", patient.firstName));
        assertTrue (setText ("#middleName", patient.middleName));
        assertTrue (setText ("#lastName", patient.lastName));
        assertTrue (setText ("#dateOfBirth", patient.dateOfBirth));
        assertTrue (clickAndSelectText ("#gender", patient.gender));
        if (patient.race != null) {
            assertTrue (clickAndSelectText ("#race", patient.race.text));
        }
        if (patient.ethnicity != null) {
            assertTrue (clickAndSelectText ("#ethnicity", patient.ethnicity.text));
        }
        assertTrue (click ("//button[text()='Save']"));
        assertTrue (setText ("[formcontrolname='mrn']", patient.mrn));
    }

    @Override
    public void clickAssayTest (Assay assay) {
        waitForElements (".test-type-selection .panel-label").forEach (el -> {
            if (el.getText ().equals (assay.test)) {
                click (el, "input");
            }
        });
    }

    @Override
    public void activateOrder () {
        clickActivateOrder ();
        moduleLoading ();
        pageLoading ();
        assertTrue (isTextInElement ("//*[text()='Status']/..//span", "PendingActivation"));
        waitUntilActivated ();
    }

    @Override
    public void clickActivateOrder () {
        clickSaveAndActivate ();
    }
}
