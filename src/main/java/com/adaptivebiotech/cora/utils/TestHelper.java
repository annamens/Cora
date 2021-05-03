package com.adaptivebiotech.cora.utils;

import static com.adaptivebiotech.cora.utils.PageHelper.Ethnicity.ASKED;
import static com.adaptivebiotech.cora.utils.PageHelper.Race.AMERICAN_INDIAN;
import static com.adaptivebiotech.test.utils.PageHelper.AbnStatus.RequiredIncludedBillMedicare;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.Client;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.CommercialInsurance;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.Medicare;
import static com.adaptivebiotech.test.utils.PageHelper.PatientRelationship.Child;
import static com.adaptivebiotech.test.utils.PageHelper.PatientRelationship.Other;
import static com.adaptivebiotech.test.utils.PageHelper.PatientRelationship.Spouse;
import static com.adaptivebiotech.test.utils.PageHelper.PatientStatus.Inpatient;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt1;
import static com.adaptivebiotech.test.utils.TestHelper.randomString;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Diagnostic.Account;
import com.adaptivebiotech.cora.dto.Insurance;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Patient.Address;
import com.adaptivebiotech.cora.dto.Physician;

public class TestHelper {

    public static Container freezerDestroyed () {
        Container container = new Container ();
        container.id = testdata ().get ("freezerDestroyed_id");
        container.containerNumber = testdata ().get ("freezerDestroyed_num");
        container.name = "[Destroyed]";
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

    public static Account account () {
        Account account = new Account ();
        account.id = "4a8d76af-2273-4d7f-8853-ba80467b570f";
        account.parent_id = "09ba0e40-4274-486e-86a6-8305ac7f05cc";
        account.name = "Dorsey Testola";
        account.description = "So f'n chill";
        account.accountTypes = "Hospital";
        account.billingAddress = "411 Whatsup Ave";
        account.billingCity = "Portland";
        account.billingState = "OR";
        account.billingZip = "98101";
        account.billingCountry = "United States";
        account.billingPhone = "(000) NON-ONON";
        account.billingEmail = "forest@lakes.com";
        account.billingContact = "Momma";
        account.billingName = "Holla Back";
        return account;
    }

    public static Physician physician1 () {
        Physician physician = new Physician ();
        physician.id = "dfb8acb3-37af-474b-bb07-0dc8c6c10668";
        physician.firstName = "Selenium";
        physician.lastName = "Test1";
        physician.accountName = "Dorsey Testola";
        physician.providerFullName = format ("%s %s", physician.firstName, physician.lastName);
        physician.address1 = "1234 Main St";
        physician.city = "Seattle";
        physician.state = "WA";
        physician.zip = "98111";
        physician.phone = "(206) 555-1212";
        physician.allowInternalOrderUpload = false;
        physician.email = "selenium.test1@1secmail.com";
        physician.notificationEmails = "selenium.test1@1secmail.com";
        physician.portal_emails = "selenium.test1@1secmail.com";
        physician.password = "password123";
        return physician;
    }

    // AllowInternalOrderUpload flag enabled in SalesForce
    public static Physician physician2 () {
        Physician physician = new Physician ();
        physician.id = "a1461f9d-29e0-464c-8bf6-a383079f1d62";
        physician.firstName = "Automated";
        physician.lastName = "Tests";
        physician.accountName = "Dorsey Testola";
        physician.providerFullName = format ("%s %s", physician.firstName, physician.lastName);
        physician.address1 = "1234 Main St";
        physician.city = "Seattle";
        physician.state = "WA";
        physician.zip = "98111";
        physician.phone = "(206) 555-1212";
        physician.allowInternalOrderUpload = true;
        physician.email = "automated.test@adaptivebiotech.com";
        physician.notificationEmails = "automated.test@adaptivebiotech.com";
        physician.portal_emails = "automated.test@adaptivebiotech.com";
        physician.password = "password123";
        return physician;
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
        return patient;
    }

    // has medicare, secondary insurance, address, etc.
    public static Patient patientMedicare () {
        Patient patient = new Patient ();
        patient.firstName = "Test1";
        patient.lastName = "Fun";
        patient.fullname = String.join (" ", patient.firstName, patient.lastName);
        patient.dateOfBirth = "07/27/1984";
        patient.gender = "Male";
        patient.patientCode = 1;
        patient.mrn = "mrn-000001";
        patient.billingType = Medicare;
        patient.insurance1 = insurance1 ();
        patient.insurance1.groupNumber = null;
        patient.insurance2 = insurance2 ();

        Address address = address ();
        patient.address = address.line1;
        patient.phone = address.phone;
        patient.locality = address.city;
        patient.region = address.state;
        patient.postCode = address.postalCode;
        return patient;
    }

    // address is not required for cora
    public static Patient newInsurancePatient () {
        Patient patient = newPatient ();
        patient.billingType = CommercialInsurance;
        patient.insurance1 = insurance1 ();
        patient.insurance2 = insurance2 ();
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

    public static Address address () {
        Address address = new Address ();
        address.line1 = "1551 Eastlake Ave E";
        address.phone = "206-201-1868";
        address.city = "Seattle";
        address.state = "WA";
        address.postalCode = "98104";
        return address;
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
        return data;
    };
}
