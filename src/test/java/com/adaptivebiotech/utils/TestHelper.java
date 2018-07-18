package com.adaptivebiotech.utils;

import static com.adaptivebiotech.test.BaseEnvironment.coraConfig;
import static com.adaptivebiotech.test.BaseEnvironment.env;
import static com.adaptivebiotech.test.utils.Logging.error;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.Client;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.CommercialInsurance;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.Medicare;
import static com.adaptivebiotech.test.utils.TestHelper.address;
import static com.adaptivebiotech.test.utils.TestHelper.insurance1;
import static com.adaptivebiotech.test.utils.TestHelper.insurance2;
import static com.adaptivebiotech.test.utils.TestHelper.newPatient;
import static org.testng.Assert.fail;
import java.lang.reflect.Field;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import com.adaptivebiotech.dto.Containers.Container;
import com.adaptivebiotech.dto.Patient;
import com.adaptivebiotech.test.BaseEnvironment.envs;

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
        patient.dateOfBirth = "1999-01-01";
        patient.billingType = Client;
        return patient;
    }

    // address is not required for cora
    public static Patient newInsurancePatient () {
        Patient patient = newPatient ();
        patient.billingType = CommercialInsurance;
        patient.insurance1 = insurance1 ();
        patient.insurance1.hospitalizationStatus = null;
        patient.insurance1.billingInstitution = null;
        patient.insurance1.dischargeDate = null;
        patient.insurance2 = insurance2 ();
        return patient;
    }

    // address is not required for cora
    public static Patient newMedicarePatient () {
        Patient patient = newPatient ();
        patient.billingType = Medicare;
        patient.insurance1 = insurance1 ();
        patient.insurance1.groupNumber = null;
        patient.insurance2 = insurance2 ();
        return patient;
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
