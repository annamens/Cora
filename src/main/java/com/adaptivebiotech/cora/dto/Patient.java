package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import com.adaptivebiotech.test.utils.PageHelper.AbnStatus;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
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
    public String        race;
    public String        ethnicity;
    public Insurance     insurance1 = new Insurance ();
    public Insurance     insurance2 = new Insurance ();
    public Insurance     insurance3 = new Insurance ();
    public Address       address    = new Address ();
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

    @Override
    public String toString () {
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
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

        public Address nullOrElse () {
            return line1 == null && phone == null && city == null && state == null && postalCode == null ? null : this;
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

            if (! (o instanceof Address))
                return false;

            Address a = (Address) o;
            return new EqualsBuilder ().append (use, a.use)
                                       .append (line1, a.line1)
                                       .append (line2, a.line2)
                                       .append (line, a.line)
                                       .append (phone, a.phone)
                                       .append (email, a.email)
                                       .append (city, a.city)
                                       .append (state, a.state)
                                       .append (postalCode, a.postalCode)
                                       .append (country, a.country)
                                       .isEquals ();
        }
    }
}
