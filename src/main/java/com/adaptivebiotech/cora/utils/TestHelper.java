package com.adaptivebiotech.cora.utils;

import static com.adaptivebiotech.cora.dto.Insurance.PatientRelationship.Child;
import static com.adaptivebiotech.cora.dto.Insurance.PatientRelationship.Other;
import static com.adaptivebiotech.cora.dto.Insurance.PatientRelationship.Spouse;
import static com.adaptivebiotech.cora.dto.Insurance.PatientStatus.Inpatient;
import static com.adaptivebiotech.cora.dto.Insurance.PatientStatus.NonHospital;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.Client;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.CommercialInsurance;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.InternalPharmaBilling;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.Medicare;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.NoCharge;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.PatientSelfPay;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.TrialProtocol;
import static com.adaptivebiotech.cora.dto.Specimen.Anticoagulant.EDTA;
import static com.adaptivebiotech.cora.utils.DateUtils.getPastFutureDate;
import static com.adaptivebiotech.cora.utils.PageHelper.AbnStatus.RequiredIncludedBillMedicare;
import static com.adaptivebiotech.cora.utils.PageHelper.Ethnicity.ASKED;
import static com.adaptivebiotech.cora.utils.PageHelper.Race.AMERICAN_INDIAN;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt1;
import static com.adaptivebiotech.test.utils.TestHelper.randomString;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.adaptivebiotech.cora.dto.BillingSurvey;
import com.adaptivebiotech.cora.dto.BillingSurvey.Questionnaire;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Insurance;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.utils.PageHelper.Ethnicity;
import com.adaptivebiotech.cora.utils.PageHelper.Race;
import com.github.javafaker.Faker;

public class TestHelper {

    public static Container freezerDestroyed () {
        Container container = new Container ();
        container.id = testdata ().get ("freezerDestroyed_id");
        container.containerNumber = testdata ().get ("freezerDestroyed_num");
        container.name = "[Destroyed]";
        return container;
    }

    public static Container dumbwaiter () {
        Container container = new Container ();
        container.id = testdata ().get ("dumbwaiter_id");
        container.containerNumber = testdata ().get ("dumbwaiter_num");
        container.name = "1165 Dumbwaiter";
        return container;
    }

    public static Container freezerAB018055 () {
        Container container = new Container ();
        container.id = testdata ().get ("AB018055_id");
        container.containerNumber = testdata ().get ("AB018055_num");
        container.name = "AB018055 (4C)";
        return container;
    }

    public static Container freezerAB018078 () {
        Container container = new Container ();
        container.id = testdata ().get ("AB018078_id");
        container.containerNumber = testdata ().get ("AB018078_num");
        container.name = "AB018078 (4C) Right";
        return container;
    }

    public static Container freezerAB018082 () {
        Container container = new Container ();
        container.id = testdata ().get ("AB018082_id");
        container.containerNumber = testdata ().get ("AB018082_num");
        container.name = "AB018082 (-20C)";
        return container;
    }

    public static Container freezerAB039003 () {
        Container container = new Container ();
        container.id = testdata ().get ("AB039003_id");
        container.containerNumber = testdata ().get ("AB039003_num");
        container.name = "AB039003 (Ambient)";
        return container;
    }

    public static Container freezerAB018018 () {
        Container container = new Container ();
        container.id = testdata ().get ("AB018018_id");
        container.containerNumber = testdata ().get ("AB018018_num");
        container.name = "AB018018 (-20C)";
        return container;
    }

    public static Container freezerAB018056 () {
        Container container = new Container ();
        container.id = testdata ().get ("AB018056_id");
        container.containerNumber = testdata ().get ("AB018056_num");
        container.name = "AB018056 : Validation (-20C)";
        return container;
    }

    // clean: no insurance, medicare, address, etc.
    public static Patient newPatient () {
        Patient patient = new Patient ();
        patient.firstName = "selenium" + randomWords (1);
        patient.middleName = randomWords (1) + "test";
        patient.lastName = randomUUID ().toString ().replace ("-", "");
        patient.fullname = String.join (" ", patient.firstName, patient.middleName, patient.lastName);
        patient.dateOfBirth = "01/01/1999";
        patient.gender = "Male";
        patient.mrn = randomString (30);
        patient.race = Race.ASKED;
        patient.ethnicity = Ethnicity.ASKED;
        return patient;
    }

    // Bill my Institution
    public static Patient newClientPatient () {
        Patient patient = newPatient ();
        patient.billingType = Client;
        patient.insurance1 = new Insurance ();
        patient.insurance1.hospitalizationStatus = NonHospital;
        return patient;
    }

    // Bill my Study Protocol
    public static Patient newTrialProtocolPatient () {
        Patient patient = newPatient ();
        patient.billingType = TrialProtocol;
        return patient;
    }

    // Patient Self-Pay
    public static Patient newSelfPayPatient () {
        Patient patient = newPatient ();
        patient.billingType = PatientSelfPay;
        patient.insurance1 = new Insurance ();
        patient.insurance1.hospitalizationStatus = NonHospital;
        return patient;
    }

    // address is not required for cora
    public static Patient newInsurancePatient () {
        Patient patient = newPatient ();
        patient.billingType = CommercialInsurance;
        patient.insurance1 = insurance1 ();
        patient.insurance2 = insurance2 ();
        patient.insurance3 = insurance3 ();
        return patient;
    }

    // address is not required for cora
    public static Patient newMedicarePatient () {
        Patient patient = newPatient ();
        patient.billingType = Medicare;
        patient.abnStatusType = RequiredIncludedBillMedicare;
        patient.insurance1 = insurance1 ();
        patient.insurance1.groupNumber = null;
        patient.insurance2 = insurance2 ();
        patient.insurance3 = insurance3 ();
        return patient;
    }

    public static Patient newNoChargePatient () {
        Patient patient = newPatient ();
        patient.billingType = NoCharge;
        return patient;
    }

    public static Patient newInternalPharmaPatient () {
        Patient patient = newPatient ();
        patient.billingType = InternalPharmaBilling;
        return patient;
    }

    // scenario builder takes only client billingType
    public static Patient scenarioBuilderPatient () {
        Patient patient = new Patient ();
        patient.id = randomUUID ().toString ();
        patient.firstName = "Jane";
        patient.middleName = "Selenium";
        patient.lastName = "ClonoSeq";
        patient.fullname = String.join (" ", patient.firstName, patient.middleName, patient.lastName);
        patient.gender = "Female";
        patient.mrn = "sel-123456";
        patient.dateOfBirth = "1999-01-01";
        patient.race = AMERICAN_INDIAN;
        patient.ethnicity = ASKED;
        patient.billingType = Client;
        patient.insurance1 = null;
        patient.insurance2 = null;
        patient.insurance3 = null;
        patient.address = null;
        return patient;
    }

    /**
     * Generate random address
     * 
     * @return Address
     */
    public static Patient getRandomAddress (Patient patient) {
        Faker faker = new Faker ();
        patient.address = faker.address ().streetAddress ();
        patient.phone = faker.phoneNumber ().cellPhone ();
        patient.locality = faker.address ().city ();
        patient.region = faker.address ().stateAbbr ();
        patient.postCode = faker.address ().zipCodeByState (patient.region);
        return patient;
    }

    public static Insurance insurance1 () {
        Insurance insurance = new Insurance ();
        insurance.provider = "Blue Cross";
        insurance.groupNumber = "B5299";
        insurance.policyNumber = "101010";
        insurance.insuredRelationship = Child;
        insurance.policyholder = "Moana";
        insurance.hospitalizationStatus = Inpatient;
        insurance.billingInstitution = "Swedish Hospital";
        insurance.dischargeDate = formatDt1.format (LocalDate.now ().minusDays (7l));
        return insurance;
    }

    public static Insurance insurance2 () {
        Insurance insurance = new Insurance ();
        insurance.provider = "Farmers";
        insurance.groupNumber = "C9000";
        insurance.policyNumber = "202020";
        insurance.insuredRelationship = Spouse;
        insurance.policyholder = "Fauna";
        return insurance;
    }

    public static Insurance insurance3 () {
        Insurance insurance = new Insurance ();
        insurance.provider = "Blue Cross Blue Shield";
        insurance.groupNumber = "4008158";
        insurance.policyNumber = "BTG 602274221 01";
        insurance.insuredRelationship = Other;
        insurance.policyholder = "Elsa of Arendelle";
        return insurance;
    }

    private static Map <String, String> testdata () {
        Map <String, String> data = new HashMap <> ();
        data.put ("freezerDestroyed_id", "c182b9e5-bdbe-44ae-8dfd-12c957cc1fc8");
        data.put ("freezerDestroyed_num", "CO-139956");
        data.put ("AB018055_id", "d8907b46-5e21-403f-bc32-adb149e5c467");
        data.put ("AB018055_num", "CO-100001");
        data.put ("AB018078_id", "53ddaaf6-eeeb-46c5-8f58-4dfbfa83146e");
        data.put ("AB018078_num", "CO-297770");
        data.put ("AB018082_id", "7be0a979-ea12-4452-a9bd-987fe03474c7");
        data.put ("AB018082_num", "CO-100162");
        data.put ("AB039003_id", "8fba58e9-6d78-4e0f-993a-63c7b9450494");
        data.put ("AB039003_num", "CO-166946");
        data.put ("dumbwaiter_id", "eec8c896-0cbe-4531-83a6-da958c79c368");
        data.put ("dumbwaiter_num", "CO-724045");
        data.put ("AB018018_id", "2c37b67f-45c0-427d-8fa6-9d469d673fff");
        data.put ("AB018018_num", "CO-522480");
        data.put ("AB018056_id", "8e2805d5-805b-403b-937b-561837d34ea6");
        data.put ("AB018056_num", "CO-206634");
        return data;
    };

    /**
     * Set Physician object with given properties
     * 
     * @param lastName
     *            last name
     * @param firstName
     *            first name
     * @param accountName
     *            account name
     * @return Physician
     */
    public static Physician setPhysician (String lastName, String firstName, String accountName) {
        Physician physician = new Physician ();
        physician.lastName = lastName;
        physician.firstName = firstName;
        physician.accountName = accountName;
        return physician;
    }

    public static Specimen bloodSpecimen () {
        Specimen specimen = new Specimen ();
        specimen.sampleType = Blood;
        specimen.anticoagulant = EDTA;
        specimen.collectionDate = getPastFutureDate (-3);
        return specimen;
    }

    public static BillingSurvey covidSurvey () {
        BillingSurvey survey = new BillingSurvey ();
        survey.status = "Eligible for Insurance";
        survey.questionnaires = new ArrayList <> ();
        survey.questionnaires.add (new Questionnaire ("symptomsV1", asList ("Yes")));
        survey.questionnaires.add (new Questionnaire ("covidTestV1", asList ("Yes")));
        survey.questionnaires.add (new Questionnaire ("antibodyTestV1", asList ("No")));
        survey.questionnaires.add (new Questionnaire ("justificationV1", asList ("selenium test")));
        survey.questionnaires.add (new Questionnaire ("testOrderLocationV1", asList ("Critical Access Hospital")));
        survey.questionnaires.add (new Questionnaire ("inNetworkV1", asList ("Unknown")));
        return survey;
    }

    public static BillingSurvey cdxSurvey () {
        BillingSurvey survey = new BillingSurvey ();
        survey.questionnaires = new ArrayList <> ();
        survey.questionnaires.add (new Questionnaire ("hadTransplant1V1", asList ("Yes, a bone marrow transplant")));
        survey.questionnaires.add (new Questionnaire ("transplant1DateV1", asList ("Transplant date", getPastFutureDate (-3))));
        survey.questionnaires.add (new Questionnaire ("hadTransplant3V1", asList ("Yes")));
        survey.questionnaires.add (new Questionnaire ("courseOfTherapyV1", asList ("No")));
        survey.questionnaires.add (new Questionnaire ("testOrderLocationV1", asList ("Critical Access Hospital")));
        survey.questionnaires.add (new Questionnaire ("inNetworkV1", asList ("Yes")));
        return survey;
    }
}
