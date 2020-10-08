package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
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
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    @Override
    public boolean equals (Object o) {

        if (o == this)
            return true;

        if (! (o instanceof Insurance))
            return false;

        Insurance i = (Insurance) o;
        return new EqualsBuilder ().append (provider, i.provider)
                                   .append (groupNumber, i.groupNumber)
                                   .append (policyNumber, i.policyNumber)
                                   .append (insuredRelationship, i.insuredRelationship)
                                   .append (policyholder, i.policyholder)
                                   .append (hospitalizationStatus, i.hospitalizationStatus)
                                   .append (billingInstitution, i.billingInstitution)
                                   .append (dischargeDate, i.dischargeDate)
                                   .isEquals ();
    }
}
