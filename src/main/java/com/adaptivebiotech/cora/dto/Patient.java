/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.equalsOverride;
import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import static java.util.Arrays.asList;
import static java.util.EnumSet.allOf;
import static org.testng.util.Strings.isNotNullAndNotEmpty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.adaptivebiotech.cora.dto.Orders.ChargeType;
import com.adaptivebiotech.cora.utils.PageHelper.AbnStatus;
import com.adaptivebiotech.cora.utils.PageHelper.Ethnicity;
import com.adaptivebiotech.cora.utils.PageHelper.Race;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Patient {

    @JsonAlias ("emrId")
    public UUID          id;
    public String        firstName;
    public String        middleName;
    public String        lastName;
    public String        fullname;
    public String        gender;
    @JsonAlias ("dob")
    public String        dateOfBirth;
    public String        mrn;
    public Race          race;
    public Ethnicity     ethnicity;
    public Insurance     insurance1;
    public Insurance     insurance2;
    public Insurance     insurance3;
    @JsonAlias ("address1")
    public String        address;
    public String        address2;
    public String        locality;
    public String        region;
    @JsonAlias ("postalCode")
    public String        postCode;
    public String        country;
    public String        phone;
    public String        email;
    public Integer       patientCode;
    public Integer       externalPatientCode;
    public Integer       calibrationPatientCode;
    public Boolean       deceased;
    public String        notes;
    public ChargeType    billingType;
    public AbnStatus     abnStatusType;
    public Physician     requestingPhysician;
    public LocalDateTime modified;
    public String        modifiedBy;
    public LocalDateTime created;
    public String        createdBy;
    public String        testStatus;
    public boolean       isSelectPatientVisible;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    @Override
    public boolean equals (Object o) {
        return equalsOverride (this, (Patient) o);
    }

    public boolean hasAddress () {
        return isNotNullAndNotEmpty (address) || isNotNullAndNotEmpty (address2) || isNotNullAndNotEmpty (locality) || isNotNullAndNotEmpty (region) || isNotNullAndNotEmpty (postCode) || isNotNullAndNotEmpty (phone) || isNotNullAndNotEmpty (email);
    }

    @JsonIgnore
    public List <String> getNameDob () {
        return asList (this.lastName, this.firstName, this.dateOfBirth);
    }

    @JsonIgnore
    public List <String> getPatientAddress () {
        return asList (this.address,
                       this.address2,
                       this.locality,
                       this.region,
                       this.postCode,
                       this.phone,
                       this.email,
                       this.country);
    }

    public enum PatientTestStatus {
        Pending ("Pending"),
        ClonalityProcessing ("Clonality (ID) Processing"),
        MrdEnabled ("Tracking (MRD) Enabled"),
        Deceased ("Deceased"),
        NoClonesFound ("No Calibrated Clones Found");

        public String label;

        private PatientTestStatus (String label) {
            this.label = label;
        }

        public static PatientTestStatus getPatientStatus (String label) {
            return allOf (PatientTestStatus.class).parallelStream ().filter (st -> st.label.equals (label)).findAny ()
                                                  .get ();
        }
    }
}
