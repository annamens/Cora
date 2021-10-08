package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.dto.Patient.Address;

/**
 * @author jpatel
 *
 */
public class TDetectBilling extends Billing {

    public void enterPatientAddress1 (String address1) {
        assertTrue (setText ("[formcontrolname='address1']", address1));
    }

    public void enterPatientPhone (String phone) {
        assertTrue (setText ("[formcontrolname='phone']", phone));
    }

    public void enterPatientCity (String city) {
        assertTrue (setText ("[formcontrolname='locality']", city));
    }

    public void enterPatientState (String state) {
        assertTrue (clickAndSelectText ("[formcontrolname='region']",
                                        state == null ? "" : state));
    }

    public void enterPatientZipcode (String zipcode) {
        assertTrue (setText ("[formcontrolname='postCode']", zipcode));
    }

    public void enterPatientAddress (Address address) {
        enterPatientAddress1 (address.line1);
        enterPatientPhone (address.phone);
        enterPatientCity (address.city);
        enterPatientState (address.state);
        enterPatientZipcode (address.postalCode);
    }
}
