package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.test.utils.PageHelper.PatientStatus.getPatientStatus;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt1;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt2;
import static org.apache.commons.lang3.EnumUtils.getEnum;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.AbnStatus;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
import com.adaptivebiotech.test.utils.PageHelper.PatientRelationship;
import com.adaptivebiotech.test.utils.PageHelper.PatientStatus;

/**
 * @author jpatel
 *
 */
public class BillingOrderDetail extends CoraPage {

    private final String billing = "#billing-type";

    public void selectBilling (ChargeType type) {
        assertTrue (clickAndSelectText (billing, type.label));
    }

    public void editBilling (ChargeType type) {
        assertTrue (click ("[ng-click='ctrl.editBilling()']"));
        selectBilling (type);
        assertTrue (click ("[ng-click='ctrl.saveBilling()']"));
    }

    protected ChargeType getBillingType () {
        String css = "[ng-bind^='ctrl.orderEntry.order.billingType']";
        return ChargeType.getChargeType (getText (css));
    }

    public AbnStatus getAbnStatus () {
        String css = "[ng-bind^='ctrl.orderEntry.order.abnStatusType']";
        return AbnStatus.getAbnStatus (getText (css));
    }

    protected String getInsurance1Provider () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.insurance.insuranceProvider']";
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getInsurance1GroupNumber () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.insurance.groupNumber']";
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getInsurance1Policy () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.insurance.policyNumber']";
        return isElementPresent (css) ? getText (css) : null;
    }

    protected PatientRelationship getInsurance1Relationship () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.insurance.insuredRelationship']";
        return getEnum (PatientRelationship.class, isElementPresent (css) ? getText (css) : null);
    }

    protected String getInsurance1PolicyHolder () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.insurance.policyholder']";
        return isElementPresent (css) ? getText (css) : null;
    }

    protected PatientStatus getInsurance1PatientStatus () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.insurance.hospitalizationStatus']";
        return isElementPresent (css) ? getPatientStatus (getText (css)) : null;
    }

    protected String getInsurance1Hospital () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.insurance.institution']";
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getInsurance1DischargeDate () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.insurance.dischargeDate']";
        String dt = isElementPresent (css) ? getText (css) : null;
        return dt != null ? formatDt1.format (formatDt2.parse (dt)) : dt;
    }

    protected String getInsurance2Provider () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.secondaryInsurance.insuranceProvider']";
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getInsurance2GroupNumber () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.secondaryInsurance.groupNumber']";
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getInsurance2Policy () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.secondaryInsurance.policyNumber']";
        return isElementPresent (css) ? getText (css) : null;
    }

    protected PatientRelationship getInsurance2Relationship () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.secondaryInsurance.insuredRelationship']";
        return isElementPresent (css) ? PatientRelationship.valueOf (getText (css)) : null;
    }

    protected String getInsurance2PolicyHolder () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.secondaryInsurance.policyholder']";
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getPatientAddress1 () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.guarantor.address1']";
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getPatientPhone () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.guarantor.phone']";
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getPatientCity () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.guarantor.locality']";
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getPatientState () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.guarantor.region']";
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getPatientZipcode () {
        String css = "[ng-bind*='ctrl.orderEntry.orderBilling.guarantor.postCode']";
        return isElementPresent (css) ? getText (css) : null;
    }

}
