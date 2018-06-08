package com.adaptivebiotech.utils;

import static com.adaptivebiotech.test.BaseEnvironment.coraConfig;
import static com.adaptivebiotech.test.BaseEnvironment.env;
import static com.adaptivebiotech.test.utils.Logging.error;
import static com.adaptivebiotech.utils.PageHelper.formatDt1;
import static com.adaptivebiotech.utils.PageHelper.ChargeType.CommercialInsurance;
import static com.adaptivebiotech.utils.PageHelper.ChargeType.Medicare;
import static com.adaptivebiotech.utils.PageHelper.PatientRelationship.Child;
import static com.adaptivebiotech.utils.PageHelper.PatientRelationship.Spouse;
import static com.adaptivebiotech.utils.PageHelper.PatientStatus.Inpatient;
import static java.util.Calendar.DATE;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.fail;
import java.lang.reflect.Field;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.RandomStringGenerator;
import com.adaptivebiotech.dto.Containers.Container;
import com.adaptivebiotech.dto.Insurance;
import com.adaptivebiotech.dto.Patient;
import com.adaptivebiotech.dto.Patient.Address;
import com.adaptivebiotech.dto.Physician;
import com.adaptivebiotech.test.BaseEnvironment.envs;
import com.thedeanda.lorem.LoremIpsum;

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
        container.name = "AB018078 (4C)";
        return container;
    }

    public static Container freezerAB018082 () {
        Container container = new Container ();
        container.id = testdata ().get ("AB018082_id");
        container.containerNumber = testdata ().get ("AB018082_num");
        container.name = "AB018082 (-20C)";
        return container;
    }

    public static Physician physician1 () {
        Physician physician = new Physician ();
        physician.id = testdata ().get ("physician1");
        physician.first = "Selenium";
        physician.last = "Test1";
        physician.accountName = "Test Account 1 x";
        physician.providerFullName = String.format ("%s %s", physician.first, physician.last);
        physician.allowInternalOrderUpload = false;
        return physician;
    }

    // AllowInternalOrderUpload flag enabled in SalesForce
    public static Physician physician2 () {
        Physician physician = new Physician ();
        physician.id = testdata ().get ("physician2");
        physician.first = "Automated";
        physician.last = "Tests";
        physician.accountName = "Test Account 1 x";
        physician.providerFullName = String.format ("%s %s", physician.first, physician.last);
        physician.allowInternalOrderUpload = true;
        return physician;
    }

    // has medicare, secondary insurance, address, etc.
    public static Patient patientMedicare () {
        Patient patient = new Patient ();
        patient.id = testdata ().get ("patientMedicare");
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

    public static Patient newInsurancePatient () {
        Patient patient = newPatient ();
        patient.billingType = CommercialInsurance;
        patient.insurance1 = insurance1 ();
        patient.insurance1.hospitalizationStatus = null;
        patient.insurance1.billingInstitution = null;
        patient.insurance1.dischargeDate = null;
        patient.insurance2 = insurance2 ();
        patient.address = address ();
        return patient;
    }

    public static Patient newMedicarePatient () {
        Patient patient = newPatient ();
        patient.billingType = Medicare;
        patient.insurance1 = insurance1 ();
        patient.insurance1.groupNumber = null;
        patient.insurance2 = insurance2 ();
        patient.address = address ();
        return patient;
    }

    public static Address address () {
        Address address = new Address ();
        address.address1 = "1551 Eastlake Ave E";
        address.phone = "206-201-1868";
        address.city = "Seattle";
        address.state = "WA";
        address.postCode = "98104";
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
        insurance.dischargeDate = formatDt1.format (setDate (-7).getTime ());
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

    public static Calendar setDate (int past) {
        Calendar cal = Calendar.getInstance ();
        cal.add (DATE, past);
        return cal;
    }

    public static String randomWords (int count) {
        return LoremIpsum.getInstance ().getWords (count);
    }

    public static String randomString (int count) {
        return new RandomStringGenerator.Builder ().withinRange ('A', 'z').build ().generate (count);
    }

    public static Object executeQuery (Object object, String sql) {
        try {
            Statement stmt = DriverManager.getConnection (coraConfig).createStatement ();
            Map <String, String> result = processSingleRow (stmt.executeQuery (sql));
            stmt.execute (sql);
            stmt.close ();

            for (Field field : object.getClass ().getFields ())
                field.set (object, result.get (field.getName ()));

            return object;
        } catch (Exception e) {
            error (String.valueOf (e), e);
            fail (String.valueOf (e));
            return null;
        }
    }

    private static Map <String, String> processSingleRow (ResultSet rs) {
        try {
            ResultSetMetaData md = rs.getMetaData ();
            int columns = md.getColumnCount ();
            Map <String, String> row = new HashMap <String, String> (columns);
            if (rs.next ())
                for (int i = 1; i <= columns; ++i)
                    row.put (md.getColumnName (i), rs.getString (i));
            return row;
        } catch (Exception e) {
            error (String.valueOf (e), e);
            fail (String.valueOf (e));
            return null;
        }
    }

    private static Map <String, String> testdata () {
        Map <String, String> data = new HashMap <String, String> ();
        switch (envs.valueOf (env)) {
        case test1:
            data.put ("physician1", "57947490-72d0-4f9f-844c-5816a3d960f8");
            data.put ("physician2", "bae3a80c-a0e5-4211-9b8c-04bebda4afbc");
            data.put ("patientMedicare", "ef13cc8b-485e-4962-8ac8-4d6256821b38");
            data.put ("freezerDestroyed_id", "");
            data.put ("freezerDestroyed_num", "");
            data.put ("AB018055_id", "");
            data.put ("AB018055_num", "");
            data.put ("AB018078_id", "");
            data.put ("AB018078_num", "");
            data.put ("AB018082_id", "");
            data.put ("AB018082_num", "");
            return data;
        case test2:
            data.put ("physician1", "e713dbb0-768e-4606-970f-d97a621508be");
            data.put ("physician2", "b6291a5c-104d-42e4-97d0-5c94cae74774");
            data.put ("patientMedicare", "b9cbb661-6728-4577-8a01-9a5b372c73db");
            data.put ("freezerDestroyed_id", "");
            data.put ("freezerDestroyed_num", "");
            data.put ("AB018055_id", "");
            data.put ("AB018055_num", "");
            data.put ("AB018078_id", "");
            data.put ("AB018078_num", "");
            data.put ("AB018082_id", "");
            data.put ("AB018082_num", "");
            return data;
        case test3:
            data.put ("physician1", "cd160957-00f3-449a-bbe6-de22d7da121e");
            data.put ("physician2", "3e6a72f1-02ad-4f41-a364-a7832c25b7c1");
            data.put ("patientMedicare", "5799817c-dd42-4ad0-8f11-544285a406d0");
            data.put ("freezerDestroyed_id", "c182b9e5-bdbe-44ae-8dfd-12c957cc1fc8");
            data.put ("freezerDestroyed_num", "CO-101825");
            data.put ("AB018055_id", "d8907b46-5e21-403f-bc32-adb149e5c467");
            data.put ("AB018055_num", "CO-100001");
            data.put ("AB018078_id", "a87adc1f-3b6a-4dac-bbdf-0b30afbb0cd4");
            data.put ("AB018078_num", "CO-101422");
            data.put ("AB018082_id", "7be0a979-ea12-4452-a9bd-987fe03474c7");
            data.put ("AB018082_num", "CO-100160");
            return data;
        case stage:
        default:
            data.put ("physician1", "69dafd40-f11e-496d-a7e0-d2123b2a88bd");
            data.put ("physician2", "aa4fd653-f2f1-4487-8296-7f8756f6e0b4");
            data.put ("patientMedicare", "7c775051-3087-4967-9b1b-7bad6694777e");
            data.put ("freezerDestroyed_id", "c182b9e5-bdbe-44ae-8dfd-12c957cc1fc8");
            data.put ("freezerDestroyed_num", "CO-113819");
            data.put ("AB018055_id", "d8907b46-5e21-403f-bc32-adb149e5c467");
            data.put ("AB018055_num", "CO-100001");
            data.put ("AB018078_id", "a87adc1f-3b6a-4dac-bbdf-0b30afbb0cd4");
            data.put ("AB018078_num", "CO-111698");
            data.put ("AB018082_id", "7be0a979-ea12-4452-a9bd-987fe03474c7");
            data.put ("AB018082_num", "CO-100162");
            return data;
        }
    };
}
