package com.adaptivebiotech.cora.dto.emr;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import java.util.List;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class TokenDataPatient {

    public Record        firstName;
    public Record        lastName;
    public RecordDate    dateOfBirth;
    public Record        mrn;
    public Record        gender;
    public Address       address;
    public String        phone;
    public String        email;
    public PatientSource source;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    public static final class Record {

        public String original;
        public String adjusted;
    }

    public static final class RecordDate {

        public String original;
        public String date;
    }

    public static final class PatientSource {

        public String                         resourceType;
        public String                         id;
        public List <PatientSourceExt>        extension;
        public List <PatientSourceIdentifier> identifier;
        public boolean                        active;
        public List <PatientSourceName>       name;
        public List <PatientSourceTelecom>    telecom;
        public String                         gender;
        public String                         birthDate;
        public boolean                        deceasedBoolean;
        public List <Address>                 address;
        public MaritalStatus                  maritalStatus;
        public List <Communication>           communication;
        public List <CareProvider>            careProvider;
    }

    public static final class PatientSourceExt {

        public String               url;
        public ValueCodeableConcept valueCodeableConcept;

        public static final class ValueCodeableConcept {

            public String        text;
            public List <Coding> coding;

            public static final class Coding {

                public String system;
                public String code;
                public String display;
            }
        }
    }

    public static final class PatientSourceIdentifier {

        public String           use;
        public String           system;
        public String           value;
        public List <Extension> extension;

        public static final class Extension {

            public String url;
            public String valueString;
        }
    }

    public static final class PatientSourceName {

        public String        use;
        public String        text;
        public List <String> family;
        public List <String> given;
    }

    public static final class PatientSourceTelecom {

        public String system;
        public String value;
        public String use;
    }

    public static final class MaritalStatus {

        public String text;
    }

    public static final class Communication {

        public CommunicationLanguage language;
        public boolean               preferred;

        public static final class CommunicationLanguage {

            public List <Coding> coding;
            public String        text;

            public static final class Coding {

                public String system;
                public String code;
                public String display;
            }
        }
    }

    public static final class CareProvider {

        public String display;
        public String reference;
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

    }
}
