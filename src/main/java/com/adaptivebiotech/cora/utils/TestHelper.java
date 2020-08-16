package com.adaptivebiotech.cora.utils;

import static com.adaptivebiotech.test.utils.PageHelper.AbnStatus.RequiredIncludedBillMedicare;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.Client;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.CommercialInsurance;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.Medicare;
import static com.adaptivebiotech.test.utils.TestHelper.address;
import static com.adaptivebiotech.test.utils.TestHelper.insurance1;
import static com.adaptivebiotech.test.utils.TestHelper.insurance2;
import static com.adaptivebiotech.test.utils.TestHelper.newPatient;
import static java.util.UUID.randomUUID;
import java.util.HashMap;
import java.util.Map;
import com.adaptivebiotech.common.dto.Patient;
import com.adaptivebiotech.cora.dto.Containers.Container;

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

    // has medicare, secondary insurance, address, etc.
    public static Patient patientMedicare () {
        Patient patient = new Patient ();
        patient.firstName = "Test1";
        patient.lastName = "Fun";
        patient.fullname = String.join (" ", patient.firstName, patient.lastName);
        patient.dateOfBirth = "07/27/1984";
        patient.gender = "Male";
        patient.patientCode = "1";
        patient.mrn = "mrn-000001";
        patient.insurance1 = insurance1 ();
        patient.insurance1.groupNumber = null;
        patient.insurance2 = insurance2 ();
        patient.address = address ();
        patient.billingType = Medicare;
        return patient;
    }

    // scenario builder takes only client billingType
    public static Patient newScenarioBuilderPatient () {
        Patient patient = newPatient ();
        patient.id = randomUUID ().toString ();
        patient.dateOfBirth = "1999-01-01";
        patient.billingType = Client;
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
