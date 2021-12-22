package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.equalsOverride;
import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import java.time.LocalDateTime;
import java.util.List;
import com.adaptivebiotech.cora.dto.Orders.ChargeType;
import com.adaptivebiotech.cora.utils.PageHelper.AbnStatus;
import com.adaptivebiotech.cora.utils.PageHelper.Ethnicity;
import com.adaptivebiotech.cora.utils.PageHelper.Race;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Patient {

    public String        id;
    public String        firstName;
    public String        middleName;
    public String        lastName;
    public String        fullname;
    public String        gender;
    public String        dateOfBirth;
    public String        mrn;
    public Race          race;
    public Ethnicity     ethnicity;
    public Insurance     insurance1;
    public Insurance     insurance2;
    public Insurance     insurance3;
    public String        address;
    public String        address2;
    public String        locality;
    public String        region;
    public String        postCode;
    public String        country;
    public String        phone;
    public String        email;
    public int           patientCode;
    public int           externalPatientCode;
    public int           calibrationPatientCode;
    public boolean       deceased;
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

    public static final class Address {

        public String        use;
        public String        line1;
        public String        line2;
        public List <String> line;
        public String        phone;
        public String        email;
        public String        city;
        public String        state;
        public String        postalCode;
        public String        country;

        @Override
        public String toString () {
            return toStringOverride (this);
        }

        @Override
        public boolean equals (Object o) {
            return equalsOverride (this, (Address) o);
        }
    }
}
