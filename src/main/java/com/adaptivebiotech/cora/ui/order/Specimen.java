package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;
import org.openqa.selenium.TimeoutException;
import com.adaptivebiotech.test.utils.PageHelper.Anticoagulant;
import com.adaptivebiotech.test.utils.PageHelper.Compartment;
import com.adaptivebiotech.test.utils.PageHelper.DeliveryType;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class Specimen extends Diagnostic {

    public void enterSpecimenDelivery (DeliveryType type) {
        assertTrue (clickAndSelectValue ("[ng-model$='specimenDeliveryType']", "string:" + type));
    }

    public void findSpecimenId (String id) {
        assertTrue (setText ("[ng-model='ctrl.specimenNumber']", id));
        assertTrue (click ("[ng-click='ctrl.reuseSpecimen(ctrl.specimenNumber)']"));
        assertTrue (isTextInElement (popupTitle, "Patient Warning"));
        assertTrue (click ("[ng-click='ctrl.ok()']"));
        moduleLoading ();
        assertTrue (isTextInElement (specimenNumber, id));
    }

    public void clickEnterSpecimenDetails () {
        assertTrue (click ("[ng-click='ctrl.showSpecimen=!ctrl.showSpecimen']"));
    }

    public void enterSpecimenType (SpecimenType type) {
        assertTrue (clickAndSelectValue ("[ng-model='ctrl.orderEntry.specimen.sampleType']", "string:" + type));
        // if a test is selected there is a popup here
        try {
            waitForElementVisible (".modal-footer");
            clickPopupOK ();
        } catch (TimeoutException te) {
            // do nothing
        }
    }

    public void enterSpecimenTypeOther (String type) {
        assertTrue (setText ("[name='otherSampleType']", type));
    }

    public void enterCompartment (Compartment compartment) {
        assertTrue (clickAndSelectValue ("[ng-model='ctrl.orderEntry.specimen.compartment']", "string:" + compartment));
    }

    public void enterAntiCoagulant (Anticoagulant anticoagulant) {
        assertTrue (clickAndSelectValue ("[name='anticoagulant']", "string:" + anticoagulant));
    }

    public void enterAntiCoagulantOther (String anticoagulant) {
        assertTrue (setText ("[name='otherAnticoagulant']", anticoagulant));
    }

    public void enterSpecimenSource (SpecimenSource source) {
        assertTrue (clickAndSelectValue ("[name='specimenSource']", "string:" + source));
    }

    public void enterSpecimenSourceOther (String source) {
        assertTrue (setText ("[name='otherSpecimenSource']", source));
    }

    public void enterCollectionDate (String date) {
        assertTrue (setText ("[name='collectionDate']", date));
    }
    
    public void enterRetrievalDate (String date) {
        String cssRetrievalDate = "#specimen-entry-retrieval-date";
        assertTrue (setText (cssRetrievalDate, date));
    }

    public String getRetrievalDate (OrderStatus orderStatus) {

        String css = "[ng-" + (OrderStatus.Pending.equals (orderStatus) ? "model" : "bind") + "^='ctrl.orderEntry.specimen.retrievalDate']";

        if (isElementVisible (css)) {
            if (OrderStatus.Pending.equals (orderStatus)) {
                return readInput (css);
            } else {
                return getText (css);
            }
        }
        return null;

    }
}
