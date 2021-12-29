package com.adaptivebiotech.cora.dto.emr;

import com.adaptivebiotech.cora.dto.Insurance;
import com.adaptivebiotech.cora.dto.Patient;

public class CustomerData {
    private CustomerPatient  patient1;
    private CustomerPatient  patient2;
    private CustomerPatient  patient3;
    private CustomerPatient  patient4;
    private CustomerProvider provider1;
    private CustomerProvider provider2;

    public CustomerPatient getPatient1 () {
        return patient1;
    }

    public void setPatient1 (CustomerPatient patient1) {
        this.patient1 = patient1;
    }

    public CustomerPatient getPatient2 () {
        return patient2;
    }

    public void setPatient2 (CustomerPatient patient2) {
        this.patient2 = patient2;
    }

    public CustomerPatient getPatient3 () {
        return patient3;
    }

    public void setPatient3 (CustomerPatient patient3) {
        this.patient3 = patient3;
    }

    public CustomerPatient getPatient4 () {
        return patient4;
    }

    public void setPatient4 (CustomerPatient patient4) {
        this.patient4 = patient4;
    }

    public CustomerProvider getProvider1 () {
        return provider1;
    }

    public void setProvider1 (CustomerProvider provider1) {
        this.provider1 = provider1;
    }

    public CustomerProvider getProvider2 () {
        return provider2;
    }

    public void setProvider2 (CustomerProvider provider2) {
        this.provider2 = provider2;
    }

    public static final class CustomerPatient {
        private String          firstName;
        private String          lastName;
        private String          dob;
        private String          mrn;
        private String          emrId;
        private String          gender;
        private Patient.Address address;
        private String          diagnosis;
        private Insurance       insurance1;
        private Insurance       insurance2;
        private Insurance       insurance3;

        public String getFirstName () {
            return firstName;
        }

        public void setFirstName (String firstName) {
            this.firstName = firstName;
        }

        public String getLastName () {
            return lastName;
        }

        public void setLastName (String lastName) {
            this.lastName = lastName;
        }

        public String getDob () {
            return dob;
        }

        public void setDob (String dob) {
            this.dob = dob;
        }

        public String getMrn () {
            return mrn;
        }

        public void setMrn (String mrn) {
            this.mrn = mrn;
        }

        public String getEmrId () {
            return emrId;
        }

        public void setEmrId (String emrId) {
            this.emrId = emrId;
        }

        public String getGender () {
            return gender;
        }

        public void setGender (String gender) {
            this.gender = gender;
        }

        public Patient.Address getAddress () {
            return address;
        }

        public void setAddress (Patient.Address address) {
            this.address = address;
        }

        public String getDiagnosis () {
            return diagnosis;
        }

        public void setDiagnosis (String diagnosis) {
            this.diagnosis = diagnosis;
        }

        public Insurance getInsurance1 () {
            return insurance1;
        }

        public void setInsurance1 (Insurance insurance1) {
            this.insurance1 = insurance1;
        }

        public Insurance getInsurance2 () {
            return insurance2;
        }

        public void setInsurance2 (Insurance insurance2) {
            this.insurance2 = insurance2;
        }

        public Insurance getInsurance3 () {
            return insurance3;
        }

        public void setInsurance3 (Insurance insurance3) {
            this.insurance3 = insurance3;
        }
    }

    public static final class CustomerProvider {
        private String firstName;
        private String lastName;
        private String accountName;
        private String accountId;
        private String emrId;
        private String ID;

        public String getAccountId () {
            return accountId;
        }

        public void setAccountId (String accountId) {
            this.accountId = accountId;
        }

        public String getFirstName () {
            return firstName;
        }

        public void setFirstName (String firstName) {
            this.firstName = firstName;
        }

        public String getLastName () {
            return lastName;
        }

        public void setLastName (String lastName) {
            this.lastName = lastName;
        }

        public String getEmrId () {
            return emrId;
        }

        public void setEmrId (String emrId) {
            this.emrId = emrId;
        }

        public String getID () {
            return ID;
        }

        public void setID (String ID) {
            this.ID = ID;
        }

        public String getAccountName () {
            return accountName;
        }

        public void setAccountName (String accountName) {
            this.accountName = accountName;
        }
    }
}
