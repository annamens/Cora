/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.equalsOverride;
import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import static java.util.EnumSet.allOf;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Insurance {

    @JsonAlias ("insuranceProvider")
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

    public enum PatientRelationship {
        Self, Spouse, Child, Other;
    }

    public enum PatientStatus {
        Inpatient ("Inpatient - discharged"),
        InpatientNotDischarged ("Inpatient - not discharged"),
        Outpatient ("Outpatient"),
        NonHospital ("Non-Hospital");

        public String label;

        private PatientStatus (String label) {
            this.label = label;
        }

        public static PatientStatus getPatientStatus (String label) {
            return allOf (PatientStatus.class).parallelStream ().filter (st -> st.label.equals (label)).findAny ()
                                              .orElse (null);
        }
    }
}
