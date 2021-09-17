package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;
import java.util.List;
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

    private final String specimenDeliveryTDx = "[formcontrolname='specimenDeliveryType']";
    private final String specimenDeliveryCDx = "[name='specimenType']";

    public void enterSpecimenDelivery (DeliveryType type) {
        String locator = getCurrentUrl ().contains ("/dx/") ? specimenDeliveryTDx : specimenDeliveryCDx;
        assertTrue (clickAndSelectValue (locator, "string:" + type));
    }

    public List <String> getSpecimenDeliveryOptions () {
        String css = getCurrentUrl ().contains ("/dx/") ? specimenDeliveryTDx : specimenDeliveryCDx;
        return getDropdownOptions (css);
    }

    public String getSpecimenDeliverySelectedOption (OrderStatus orderStatus) {
        String css = "[ng-" + (OrderStatus.Pending.equals (orderStatus) ? "model" : "bind") + "^='ctrl.orderEntry.order.specimenDeliveryType']";
        css = getCurrentUrl ().contains ("/dx/") && OrderStatus.Pending.equals (orderStatus) ? "[formcontrolname='specimenDeliveryType']" : css;
        if (isElementVisible (css)) {
            if (OrderStatus.Pending.equals (orderStatus)) {
                return getFirstSelectedText (css);
            } else {
                return getText (css);
            }
        }
        return null;
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

    public void closeTestSelectionWarningModal () {
        String expectedModalTitle = "Test Selection Warning";
        String modalHeader = "[ng-bind-html=\"ctrl.dialogOptions.headerText\"]";
        assertTrue (isTextInElement (modalHeader, expectedModalTitle));
        clickPopupOK ();
    }

}
