package com.adaptivebiotech.cora.ui.order;

import com.adaptivebiotech.cora.dto.Patient.Address;

/**
 * @author jpatel
 *
 */
public class BillingTDetect extends Billing {

    public Address getPatientBillingAddress () {
        Address address = new Address ();
        address.line1 = getAttribute ("[formcontrolname='address1']", "value");
        address.line2 = getAttribute ("[formcontrolname='address2']", "value");
        address.city = getAttribute ("[formcontrolname='locality']", "value");
        address.state = getFirstSelectedText ("[formcontrolname='region']");
        address.postalCode = getAttribute ("[formcontrolname='postCode']", "value");
        address.phone = getAttribute ("[formcontrolname='phone']", "value");
        address.email = getAttribute ("[formcontrolname='email']", "value");
        return address;
    }

}
