/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Insurance.PatientStatus.getPatientStatus;
import static java.lang.String.format;
import static org.apache.commons.lang3.EnumUtils.getEnum;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.dto.Insurance.PatientRelationship;
import com.adaptivebiotech.cora.dto.Insurance.PatientStatus;
import com.adaptivebiotech.cora.dto.Orders.ChargeType;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.AbnStatus;

/**
 * @author jpatel
 *
 */
public class BillingOrderDetail extends CoraPage {

    private final String billing                  = "#billing-type";
    private final String primaryInsurance         = "insurance";
    private final String secondaryInsurance       = "secondaryInsurance";
    private final String tertiaryInsurance        = "tertiaryInsurance";
    private final String insuranceProvider        = "[ng-bind*='ctrl.orderEntry.orderBilling.%s.insuranceProvider']";
    private final String priorAuthorizationNumber = "[ng-bind*='ctrl.orderEntry.orderBilling.%s.priorAuthorizationNumber']";
    private final String groupNumber              = "[ng-bind*='ctrl.orderEntry.orderBilling.%s.groupNumber']";
    private final String policyNumber             = "[ng-bind*='ctrl.orderEntry.orderBilling.%s.policyNumber']";
    private final String insuredRelationship      = "[ng-bind*='ctrl.orderEntry.orderBilling.%s.insuredRelationship']";
    private final String policyholder             = "[ng-bind*='ctrl.orderEntry.orderBilling.%s.policyholder']";
    private final String patientStatus            = "[ng-bind*='ctrl.orderEntry.orderBilling.%s.hospitalizationStatus']";
    private final String hospital                 = "[ng-bind*='ctrl.orderEntry.orderBilling.%s.institution']";
    private final String dischargeDate            = "[ng-bind*='ctrl.orderEntry.orderBilling.%s.dischargeDate']";

    public void selectBilling (ChargeType type) {
        assertTrue (clickAndSelectText (billing, type.label));
    }

    public void editBilling (ChargeType type) {
        assertTrue (click ("[ng-click='ctrl.editBilling()']"));
        selectBilling (type);
        assertTrue (click ("[ng-click='ctrl.saveBilling()']"));
    }

    public ChargeType getBillingType () {
        String css = "[ng-bind^='ctrl.orderEntry.order.billingType']";
        return ChargeType.getChargeType (getText (css));
    }

    public AbnStatus getAbnStatus () {
        String css = "[ng-bind^='ctrl.orderEntry.order.abnStatusType']";
        return isElementVisible (css) ? AbnStatus.getAbnStatus (getText (css)) : null;
    }

    protected boolean isPrimaryInsurancePresent () {
        String css = format (insuranceProvider, primaryInsurance);
        return isElementPresent (css);
    }

    protected String getInsurance1Provider () {
        String css = format (insuranceProvider, primaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getInsurance1AuthorizationNumber () {
        String css = format (priorAuthorizationNumber, primaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getInsurance1GroupNumber () {
        String css = format (groupNumber, primaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getInsurance1Policy () {
        String css = format (policyNumber, primaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected PatientRelationship getInsurance1Relationship () {
        String css = format (insuredRelationship, primaryInsurance);
        return getEnum (PatientRelationship.class, isElementPresent (css) ? getText (css) : null);
    }

    protected String getInsurance1PolicyHolder () {
        String css = format (policyholder, primaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected PatientStatus getInsurance1PatientStatus () {
        String css = format (patientStatus, primaryInsurance);
        return isElementPresent (css) ? getPatientStatus (getText (css)) : null;
    }

    protected String getInsurance1Hospital () {
        String css = format (hospital, primaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getInsurance1DischargeDate () {
        String css = format (dischargeDate, primaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected boolean isSecondaryInsurancePresent () {
        String css = format (insuranceProvider, secondaryInsurance);
        return isElementPresent (css);
    }

    protected String getInsurance2Provider () {
        String css = format (insuranceProvider, secondaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getInsurance2AuthorizationNumber () {
        String css = format (priorAuthorizationNumber, secondaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getInsurance2GroupNumber () {
        String css = format (groupNumber, secondaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getInsurance2Policy () {
        String css = format (policyNumber, secondaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected PatientRelationship getInsurance2Relationship () {
        String css = format (insuredRelationship, secondaryInsurance);
        return isElementPresent (css) ? PatientRelationship.valueOf (getText (css)) : null;
    }

    protected String getInsurance2PolicyHolder () {
        String css = format (policyholder, secondaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected boolean isTertiaryInsurancePresent () {
        String css = format (insuranceProvider, tertiaryInsurance);
        return isElementPresent (css);
    }

    protected String getInsurance3Provider () {
        String css = format (insuranceProvider, tertiaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getInsurance3AuthorizationNumber () {
        String css = format (priorAuthorizationNumber, tertiaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getInsurance3GroupNumber () {
        String css = format (groupNumber, tertiaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected String getInsurance3Policy () {
        String css = format (policyNumber, tertiaryInsurance);
        return isElementPresent (css) ? getText (css) : null;
    }

    protected PatientRelationship getInsurance3Relationship () {
        String css = format (insuredRelationship, tertiaryInsurance);
        return isElementPresent (css) ? PatientRelationship.valueOf (getText (css)) : null;
    }

    protected String getInsurance3PolicyHolder () {
        String css = format (policyholder, tertiaryInsurance);
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
