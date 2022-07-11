/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
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
import static com.adaptivebiotech.cora.utils.PageHelper.AbnStatus.RequiredIncludedBillMedicare;
import static com.adaptivebiotech.cora.utils.PageHelper.Ethnicity.ASKED;
import static com.adaptivebiotech.cora.utils.PageHelper.Race.AMERICAN_INDIAN;
import static com.adaptivebiotech.pipeline.dto.dx.ClassifierOutput.DiseaseType.COVID19;
import static com.adaptivebiotech.pipeline.utils.TestHelper.DxStatus.NEGATIVE;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt1;
import static com.adaptivebiotech.test.utils.DateHelper.genDate;
import static com.adaptivebiotech.test.utils.DateHelper.genLocalDate;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.disableHiFreqSave;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.disableHiFreqSharing;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.lastAcceptedTsvPath;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.lastFinishedPipelineJobId;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.lastFlowcellId;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.sampleName;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.workspaceName;
import static com.adaptivebiotech.test.utils.TestHelper.randomString;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static java.lang.Boolean.TRUE;
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
import com.adaptivebiotech.pipeline.dto.dx.ClassifierOutput;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;
import com.github.javafaker.Faker;

public class TestHelper {

    public static Container freezerDestroyed () {
        Container container = new Container ();
        container.id = testdata ().get ("freezerDestroyed_id");
        container.containerNumber = testdata ().get ("freezerDestroyed_num");
        container.name = "[Destroyed]";
        container.location = "[Destroyed]";
        return container;
    }

    public static Container dumbwaiter () {
        Container container = new Container ();
        container.id = testdata ().get ("dumbwaiter_id");
        container.containerNumber = testdata ().get ("dumbwaiter_num");
        container.name = "1165 Dumbwaiter";
        container.location = "1165 Dumbwaiter";
        return container;
    }

    public static Container freezerAB018055 () {
        Container container = new Container ();
        container.id = testdata ().get ("AB018055_id");
        container.containerNumber = testdata ().get ("AB018055_num");
        container.name = "AB018055 (4C)";
        container.location = "1551 : RM 258 : AB018055 (4C)";
        return container;
    }

    public static Container freezerAB018078 () {
        Container container = new Container ();
        container.id = testdata ().get ("AB018078_id");
        container.containerNumber = testdata ().get ("AB018078_num");
        container.name = "AB018078 (4C) Right";
        container.location = "1551 : RM 258 : AB018078 (4C) Right";
        return container;
    }

    public static Container freezerAB039003 () {
        Container container = new Container ();
        container.id = testdata ().get ("AB039003_id");
        container.containerNumber = testdata ().get ("AB039003_num");
        container.name = "AB039003 (Ambient)";
        container.location = "RM 255 : AB039003 (Ambient)";
        return container;
    }

    public static Container freezerAB018018 () {
        Container container = new Container ();
        container.id = testdata ().get ("AB018018_id");
        container.containerNumber = testdata ().get ("AB018018_num");
        container.name = "AB018018 (-20C)";
        container.location = "RM 243 : AB018018 (-20C)";
        return container;
    }

    public static Container freezerAB018056 () {
        Container container = new Container ();
        container.id = testdata ().get ("AB018056_id");
        container.containerNumber = testdata ().get ("AB018056_num");
        container.name = "AB018056 : Validation (-20C)";
        container.location = "1551 : RM 258 : AB018056 : Validation (-20C)";
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

    public static Patient newClientPatientTDx () {
        Patient patient = newPatient ();
        patient.billingType = Client;
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

    public static Patient newSelfPayPatientTDx () {
        Patient patient = newPatient ();
        patient.billingType = PatientSelfPay;
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

        getRandomAddress (patient);
        patient.billingType = PatientSelfPay;
        patient.address2 = "Ste 200";
        patient.postCode = "98138"; // has to be a valid zipcode
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
        specimen.collectionDate = genLocalDate (-3);
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
        survey.questionnaires.add (new Questionnaire ("treatmentStateV1", asList ("Post Transplant")));
        survey.questionnaires.add (new Questionnaire ("treatmentOrTherapyV1", asList ("Yes")));
        survey.questionnaires.add (new Questionnaire ("treatmentOrTherapyDateV1", asList (genDate (-3))));
        survey.questionnaires.add (new Questionnaire ("diseaseClinicalEvidenceV1", asList ("Yes")));
        // TODO https://sdlc.dna.corp.adaptivebiotech.com:8443/browse/SR-11795
        // replace Ambulatory Surgery Center with Critical Access Hospital
        survey.questionnaires.add (new Questionnaire ("testOrderLocationV1", asList ("Ambulatory Surgery Center")));
        survey.questionnaires.add (new Questionnaire ("inNetworkV1", asList ("Yes")));
        return survey;
    }

    public static Map <WorkflowProperty, String> covidProperties () {
        Map <WorkflowProperty, String> properties = new HashMap <> ();
        properties.put (lastAcceptedTsvPath,
                        "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/e2e/HCYJNBGXJ_0_CLINICAL-CLINICAL_112770-SN-7929.adap.txt.results.tsv.gz");
        properties.put (sampleName, "112770-SN-7929");
        properties.put (workspaceName, "CLINICAL-CLINICAL");
        properties.put (lastFlowcellId, "HCYJNBGXJ");
        properties.put (lastFinishedPipelineJobId, "8a7a958877a26e74017a213f79fe6d45");
        properties.put (disableHiFreqSave, TRUE.toString ());
        properties.put (disableHiFreqSharing, TRUE.toString ());
        return properties;
    }

    public static ClassifierOutput sample_112770_SN_7929 () {
        ClassifierOutput dxResult = new ClassifierOutput ();
        dxResult.disease = COVID19;
        dxResult.classifierVersion = "v1.0";
        dxResult.dxScore = -9.097219383308602d;
        dxResult.posteriorProbability = 1.1196420300544642E-4d;
        dxResult.countEnhancedSeq = 18;
        dxResult.containerVersion = "dx-classifiers/covid-19:d23228f";
        dxResult.pipelineVersion = "v3.1-385-g1340003";
        dxResult.dxStatus = NEGATIVE;
        dxResult.configVersion = "dx.covid19.rev1";
        dxResult.uniqueProductiveTemplates = 343874;
        dxResult.qcFlags = asList ();
        return dxResult;
    }
}
