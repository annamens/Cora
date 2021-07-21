package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.equalsOverride;
import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import com.adaptivebiotech.test.utils.PageHelper.PatientRelationship;
import com.adaptivebiotech.test.utils.PageHelper.PatientStatus;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Insurance {

    public String              provider;
    public String              groupNumber;
    public String              policyNumber;
    public PatientRelationship insuredRelationship;
    public String              policyholder;
    public PatientStatus       hospitalizationStatus;
    public String              billingInstitution;
    public String              dischargeDate;

    public boolean isEmpty () {
        return provider == null && groupNumber == null && policyNumber == null && insuredRelationship == null && policyholder == null && hospitalizationStatus == null && billingInstitution == null && dischargeDate == null;
    }

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    @Override
    public boolean equals (Object o) {
        return equalsOverride (this, (Insurance) o);
    }
}
