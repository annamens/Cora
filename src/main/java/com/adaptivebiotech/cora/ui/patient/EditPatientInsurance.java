package com.adaptivebiotech.cora.ui.patient;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;

public class EditPatientInsurance extends CoraPage {

    public void setBillingType (String billingType) {
        String css = "#billingType";
        assertTrue (clickAndSelectText (css, billingType));
    }

    public void enterInsuranceProvider (String insuranceProvider) {
        String css = "#primaryInsuranceProvider";
        assertTrue (setText (css, insuranceProvider));
    }

    public void enterGroupNumber (String groupNumber) {
        String css = "#primaryGroupNumber";
        assertTrue (setText (css, groupNumber));
    }

    public void enterPolicyNumber (String policyNumber) {
        String css = "#primaryPolicyNumber";
        assertTrue (setText (css, policyNumber));
    }

    public void setPrimaryInsuredRelationship (String relationship) {
        String css = "#primaryInsuredRelationship";
        assertTrue (clickAndSelectText (css, relationship));
    }

    public void enterPolicyholderName (String name) {
        String css = "#primaryPolicyholder";
        assertTrue (setText (css, name));
    }

    public void clickSave () {
        assertTrue (click ("[type='submit']"));
        waitForAjaxCalls ();
        pageLoading ();
    }

}
