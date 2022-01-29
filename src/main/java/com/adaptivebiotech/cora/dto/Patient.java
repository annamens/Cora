package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.Logging.info;
import static com.adaptivebiotech.test.utils.TestHelper.equalsOverride;
import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.toMap;
import static org.testng.util.Strings.isNotNullAndNotEmpty;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.builder.EqualsBuilder;
import com.adaptivebiotech.cora.dto.Orders.ChargeType;
import com.adaptivebiotech.cora.utils.PageHelper.AbnStatus;
import com.adaptivebiotech.cora.utils.PageHelper.Ethnicity;
import com.adaptivebiotech.cora.utils.PageHelper.Race;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Patient {

    @JsonAlias ("emrId")
    public String        id;
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
    @JsonFormat (shape = JsonFormat.Shape.STRING)
    public LocalDateTime modified;
    public String        modifiedBy;
    @JsonFormat (shape = JsonFormat.Shape.STRING)
    public LocalDateTime created;
    public String        createdBy;
    public String        testStatus;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    @Override
    public boolean equals (Object o) {
        return equalsOverride (this, (Patient) o);
    }

    public boolean equalsNameDob (Patient patient) {
        Map <String, String> name1 = Stream.of (new String[][] {
                { "lastName", this.lastName },
                { "firstName", this.firstName },
                { "dateOfBirth", this.dateOfBirth }
        }).collect (toMap (data -> data[0], data -> data[1]));
        Map <String, String> name2 = Stream.of (new String[][] {
                { "lastName", patient.lastName },
                { "firstName", patient.firstName },
                { "dateOfBirth", patient.dateOfBirth }
        }).collect (toMap (data -> data[0], data -> data[1]));
        info ("comparing: " + name1);
        info ("against: " + name2);
        return new EqualsBuilder ().append (name1, name2).isEquals ();
    }

    public boolean hasAddress () {
        return isNotNullAndNotEmpty (address) || isNotNullAndNotEmpty (address2) || isNotNullAndNotEmpty (locality) || isNotNullAndNotEmpty (region) || isNotNullAndNotEmpty (postCode) || isNotNullAndNotEmpty (phone) || isNotNullAndNotEmpty (email);
    }

    public boolean equalsAddress (Patient patient) {
        Map <String, String> address1 = Stream.of (new String[][] {
                { "address", this.address },
                { "address2", this.address2 },
                { "locality", this.locality },
                { "region", this.region },
                { "postCode", this.postCode },
                { "phone", this.phone },
                { "email", this.email },
                { "country", this.country }
        }).collect (toMap (data -> data[0], data -> data[1]));
        Map <String, String> address2 = Stream.of (new String[][] {
                { "address", patient.address },
                { "address2", patient.address2 },
                { "locality", patient.locality },
                { "region", patient.region },
                { "postCode", patient.postCode },
                { "phone", patient.phone },
                { "email", patient.email },
                { "country", patient.country }
        }).collect (toMap (data -> data[0], data -> data[1]));
        info ("comparing: " + address1);
        info ("against: " + address2);
        return new EqualsBuilder ().append (address1, address2).isEquals ();
    }

    public enum PatientTestStatus {
        Pending ("Pending"),
        ClonalityProcessing ("Clonality (ID) Processing"),
        TrackingEnabled ("Tracking (MRD) Enabled"),
        Deceased ("Deceased"),
        NoClonesFound ("No Calibrated Clones Found");

        public String label;

        private PatientTestStatus (String label) {
            this.label = label;
        }

        public static PatientTestStatus getCompartment (String label) {
            return allOf (PatientTestStatus.class).parallelStream ().filter (st -> st.label.equals (label)).findAny ()
                                                  .get ();
        }
    }
}
