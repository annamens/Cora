package com.adaptivebiotech.cora.ui.order;

/**
 * @author jpatel
 *
 */
public class BillingTDetect extends Billing {

    public String getPatientBillingAddress1 () {
        return getAttribute ("[formcontrolname='address1']", "value");
    }

    public String getPatientBillingAddress2 () {
        return getAttribute ("[formcontrolname='address2']", "value");
    }

    public String getPatientBillingCity () {
        return getAttribute ("[formcontrolname='locality']", "value");
    }

    public String getPatientBillingState () {
        return getFirstSelectedText ("[formcontrolname='region']");
    }

    public String getPatientBillingZipCode () {
        return getAttribute ("[formcontrolname='postCode']", "value");
    }

    public String getPatientBillingPhone () {
        return getAttribute ("[formcontrolname='phone']", "value");
    }

    public String getPatientBillingEmail () {
        return getAttribute ("[formcontrolname='email']", "value");
    }
}
