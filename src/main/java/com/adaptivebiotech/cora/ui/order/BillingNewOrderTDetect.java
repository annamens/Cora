package com.adaptivebiotech.cora.ui.order;

import com.adaptivebiotech.cora.dto.Patient;

/**
 * @author jpatel
 *
 */
public class BillingNewOrderTDetect extends BillingNewOrder {

    public String getPatientAddress1 () {
        String css = "[formcontrolname='address1']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    public String getPatientAddress2 () {
        String css = "[formcontrolname='address2']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    public String getPatientCity () {
        String css = "[formcontrolname='locality']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    public String getPatientState () {
        String css = "[formcontrolname='region']";
        return isElementPresent (css) ? getFirstSelectedText (css) : null;
    }

    public String getPatientZipcode () {
        String css = "[formcontrolname='postCode']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    public String getPatientPhone () {
        String css = "[formcontrolname='phone']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    public String getPatientEmail () {
        String css = "[formcontrolname='email']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    public Patient getPatientBillingAddress () {
        Patient patient = new Patient ();
        patient.address = getPatientAddress1 ();
        patient.address2 = getPatientAddress2 ();
        patient.locality = getPatientCity ();
        patient.region = getPatientState ();
        patient.postCode = getPatientZipcode ();
        patient.phone = getPatientPhone ();
        patient.email = getPatientEmail ();
        return patient;
    }

}
