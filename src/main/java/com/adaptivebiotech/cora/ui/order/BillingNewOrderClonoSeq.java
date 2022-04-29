/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Insurance.PatientStatus.NonHospital;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.Medicare;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.BillingSurvey;
import com.adaptivebiotech.cora.dto.BillingSurvey.Questionnaire;
import com.adaptivebiotech.cora.dto.Insurance;
import com.adaptivebiotech.cora.dto.Patient;

/**
 * @author jpatel
 *
 */
public class BillingNewOrderClonoSeq extends BillingNewOrder {

    public BillingNewOrderClonoSeq (int staticNavBarHeight) {
        super.staticNavBarHeight = staticNavBarHeight;
    }

    public BillingSurvey parseBillingQuestions () {
        String container = "[ng-show*='showInsuranceQuestionnaires']";
        assertTrue (waitUntilVisible (billingQuestionnaire));
        if (!isElementPresent (container))
            assertTrue (click (billingQuestionnaire));

        List <Questionnaire> questionnaires = new ArrayList <> ();
        for (WebElement li : waitForElements (".billing-questionnaire li")) {
            Questionnaire q = new Questionnaire ();
            q.name = getAttribute (li, "id");
            q.title = getText (li).split ("\\n")[0].trim ();

            String answerText = getText (li, ".insurance-questionnaire-answer");
            q.answers = asList (answerText.replaceAll ("(\\d{4})-(\\d{2})-(\\d{2})", "$2/$3/$1"));
            questionnaires.add (q);
        }
        return new BillingSurvey (questionnaires);
    }

    public Patient getPatientBilling () {
        return getPatientBilling (new Patient ());
    }

    public Patient getPatientBilling (Patient patient) {
        patient.billingType = getBilling ();
        patient.abnStatusType = Medicare.equals (patient.billingType) ? getAbnStatus () : null;

        switch (patient.billingType) {
        case CommercialInsurance:
        case Medicare:
            patient.insurance1 = new Insurance ();
            patient.insurance1.provider = getInsurance1Provider ();
            patient.insurance1.groupNumber = getInsurance1GroupNumber ();
            patient.insurance1.policyNumber = getInsurance1Policy ();
            patient.insurance1.insuredRelationship = getInsurance1Relationship ();
            patient.insurance1.policyholder = getInsurance1PolicyHolder ();
            patient.insurance1.hospitalizationStatus = getInsurance1PatientStatus ();
            patient.insurance1.billingInstitution = getInsurance1Hospital ();
            patient.insurance1.dischargeDate = getInsurance1DischargeDate ();

            if (hasSecondaryInsurance ()) {
                patient.insurance2 = new Insurance ();
                patient.insurance2.provider = getInsurance2Provider ();
                patient.insurance2.groupNumber = getInsurance2GroupNumber ();
                patient.insurance2.policyNumber = getInsurance2Policy ();
                patient.insurance2.insuredRelationship = getInsurance2Relationship ();
                patient.insurance2.policyholder = getInsurance2PolicyHolder ();
            }

            if (hasTertiaryInsurance ()) {
                patient.insurance3 = new Insurance ();
                patient.insurance3.provider = getInsurance3Provider ();
                patient.insurance3.groupNumber = getInsurance3GroupNumber ();
                patient.insurance3.policyNumber = getInsurance3Policy ();
                patient.insurance3.insuredRelationship = getInsurance3Relationship ();
                patient.insurance3.policyholder = getInsurance3PolicyHolder ();
            }
            break;
        case Client:
        case PatientSelfPay:
            patient.insurance1 = new Insurance ();
            patient.insurance1.hospitalizationStatus = getInsurance1PatientStatus ();
            if (!NonHospital.equals (patient.insurance1.hospitalizationStatus)) {
                patient.insurance1.billingInstitution = getInsurance1Hospital ();
                patient.insurance1.dischargeDate = getInsurance1DischargeDate ();
            }
            break;
        default:
            break;
        }
        getPatientBillingAddress (patient);
        return patient;
    }
}
