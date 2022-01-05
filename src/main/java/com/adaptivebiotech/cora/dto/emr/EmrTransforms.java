package com.adaptivebiotech.cora.dto.emr;

import static com.adaptivebiotech.test.utils.TestHelper.equalsOverride;
import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import java.util.List;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class EmrTransforms {

    public IcdCodes          icdCodes;
    public List <EmrPatient> patientName;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    @Override
    public boolean equals (Object o) {
        return equalsOverride (this, (EmrTransforms) o);
    }

    public static final class IcdCodes {

        public List <IcdCode>   icd10;
        public List <Condition> condition;

        @Override
        public boolean equals (Object o) {
            return equalsOverride (this, (IcdCodes) o);
        }
    }

    public static final class IcdCode {

        public String            type;
        public List <String>     systems;
        public List <EmrMapping> mappings;
        public String            toSystem;
        public List <String>     includeCodes;
        public List <String>     includeSystems;

        @Override
        public boolean equals (Object o) {
            return equalsOverride (this, (IcdCode) o);
        }
    }

    public static final class Condition {

        public String type;
        public String exclude;

        public Condition () {}

        public Condition (String type, String exclude) {
            this.type = type;
            this.exclude = exclude;
        }

        @Override
        public boolean equals (Object o) {
            return equalsOverride (this, (Condition) o);
        }
    }

    public static final class EmrMapping {

        public String emrCode;
        public String icdCode;

        public EmrMapping () {}

        public EmrMapping (String emrCode, String icdCode) {
            this.emrCode = emrCode;
            this.icdCode = icdCode;
        }

        @Override
        public boolean equals (Object o) {
            return equalsOverride (this, (EmrMapping) o);
        }
    }

    public static final class EmrPatient {

        public String type;
        public String csv;

        public EmrPatient () {}

        public EmrPatient (String type) {
            this.type = type;
        }

        public EmrPatient (String type, String csv) {
            this.type = type;
            this.csv = csv;
        }

        @Override
        public boolean equals (Object o) {
            return equalsOverride (this, (EmrPatient) o);
        }
    }
}
