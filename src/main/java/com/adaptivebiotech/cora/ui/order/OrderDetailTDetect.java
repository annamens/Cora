package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.utils.PageHelper.Ethnicity;
import com.adaptivebiotech.cora.utils.PageHelper.Race;

/**
 * @author jpatel
 *
 */
public class OrderDetailTDetect extends OrderDetail {

    public BillingOrderDetailTDetect billing    = new BillingOrderDetailTDetect ();

    private final String             dateSigned = "[ng-bind='ctrl.originalDate']";
    private final String             orderNotes = "[ng-bind='ctrl.originalNotes']";

    public String getSpecimenDeliverySelectedOption () {
        String css = "[formcontrolname='specimenDeliveryType']";
        if (isElementVisible (css)) {
            return getText (css);
        }
        return null;
    }

    public String getDateSigned () {
        return isElementPresent (dateSigned) && isElementVisible (dateSigned) ? getText (dateSigned) : null;
    }

    public String getOrderNotes () {
        return isElementPresent (orderNotes) && isElementVisible (orderNotes) ? getText (orderNotes) : null;
    }

    public void clickEditPatientDemographic () {
        String xpath = "//button[text()='Edit Patient Demographics']";
        assertTrue (click (xpath));
        String expectedTitle = "Edit Patient Demographics";
        assertTrue (isTextInElement (popupTitle, expectedTitle));
    }

    public List <String> getTextDangerText () {
        String css = ".text-danger";
        List <String> text = getTextList (css);
        return text;
    }

    public Race getPatientRace () {
        return Race.getRace (getText ("//label[text()='Race']/../div[1]"));
    }

    public Ethnicity getPatientEthnicity () {
        return Ethnicity.getEthnicity (getText ("//label[text()='Ethnicity']/../div[1]"));
    }

    public String getOrderName () {
        return getText ("[ng-bind='ctrl.orderEntry.order.name']");
    }

    public String getBillingAddressLine1 () {
        String xpath = "input[formcontrolname='address1']";
        return readInput (xpath);
    }

    public String getBillingAddressLine2 () {
        String xpath = "input[formcontrolname='address2']";
        return readInput (xpath);
    }

    public String getBillingAddressCity () {
        String xpath = "input[formcontrolname='locality']";
        return readInput (xpath);
    }

    public String getBillingAddressState () {
        String xpath = "select[formcontrolname='region']";
        return getFirstSelectedText (xpath);
    }

    public String getBillingZipcode () {
        String xpath = "input[formcontrolname='postCode']";
        return readInput (xpath);
    }

    public String getBillingType () {
        String css = "#billing-type";
        return getFirstSelectedText (css);
    }

    public String getPhlebotomySelection () {
        String xpath = "//h3[text()='Phlebotomy Selection']/../div";
        return getText (xpath);
    }

    public String getBillingPhone () {
        String xpath = "input[formcontrolname='phone']";
        return readInput (xpath);
    }

    public String getBillingEmail () {
        String xpath = "input[formcontrolname='email']";
        return readInput (xpath);
    }

    public String getPatientName () {
        return getText ("[ng-bind$='patientFullName']");
    }

    public String getPatientDOB () {
        return getText ("[ng-bind^='ctrl.orderEntry.order.patient.dateOfBirth']");
    }

}
