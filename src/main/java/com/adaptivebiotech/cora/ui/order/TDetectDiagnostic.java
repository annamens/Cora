package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Pending;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;
import com.seleniumfy.test.utils.Timeout;

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

    public void clickShipment () {
        assertTrue (click ("//*[text()='Shipment']"));
    }

    @Override
    public void clickShowContainers () {
        assertTrue (click ("//*[@class='row']//*[contains(text(),'Containers')]"));
    }

    @Override
    public Containers getContainers () {
        String rows = "//specimen-containers//*[@class='row']/..";
        return new Containers (waitForElements (rows).stream ().map (row -> {
            Container c = new Container ();
            c.id = getConId (getAttribute (row, "//*[text()='Adaptive Container ID']/..//a", "href"));
            c.containerNumber = getText (row, "//*[text()='Adaptive Container ID']/..//a");
            c.location = getText (row, "//*[text()='Current Storage Location']/..//div");

            if (isElementPresent (row, ".container-table")) {
                String css = "tbody tr";
                List <Container> children = row.findElements (locateBy (css)).stream ().map (childRow -> {
                    Container childContainer = new Container ();
                    childContainer.id = getConId (getAttribute (childRow,
                                                                "td:nth-child(1) a",
                                                                "href"));
                    childContainer.containerNumber = getText (childRow,
                                                              "td:nth-child(1) a");
                    childContainer.name = getText (childRow, "td:nth-child(2)");
                    childContainer.integrity = getText (childRow, "td:nth-child(3)");
                    childContainer.root = c;
                    return childContainer;
                }).collect (toList ());
                c.children = children;
            }
            return c;
        }).collect (toList ()));
    }
    
    @Override
    public String getOrderName (OrderStatus state) {
        String locator = Pending.equals (state) ? "//*[@label='Order Name']/parent::div//span" : "[ng-bind='ctrl.orderEntry.order.name']";
        return getText (locator);
    }
}
