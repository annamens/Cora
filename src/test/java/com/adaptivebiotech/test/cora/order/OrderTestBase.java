package com.adaptivebiotech.test.cora.order;

import static com.adaptivebiotech.test.utils.Logging.error;
import static com.adaptivebiotech.test.utils.PageHelper.formatDt1;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.adaptivebiotech.test.utils.TestHelper.physician1;
import static com.adaptivebiotech.test.utils.TestHelper.physician2;
import static com.adaptivebiotech.test.utils.TestHelper.setDate;
import static com.adaptivebiotech.utils.TestHelper.patientMedicare;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import com.adaptivebiotech.common.dto.Patient;
import com.adaptivebiotech.common.dto.Physician;
import com.adaptivebiotech.common.dto.Orders.Order;
import com.adaptivebiotech.test.cora.CoraBaseBrowser;

public class OrderTestBase extends CoraBaseBrowser {

    protected final Physician physician1      = physician1 ();
    protected final Physician physicianTRF    = physician2 ();
    protected final Patient   patientMedicare = patientMedicare ();
    protected final String    icdCode         = "A01.02";
    protected final String    collectionDt    = formatDt1.format (setDate (-3).getTime ());

    protected void verifyTrfCopied (Order actual, Order expected) {
        try {
            assertEquals (actual.orderEntryType, expected.orderEntryType);
            assertTrue (actual.order_number.startsWith (expected.order_number));
            assertTrue (actual.name.startsWith (expected.name));
            assertEquals (actual.isTrfAttached, expected.isTrfAttached);
            assertEquals (actual.date_signed, expected.date_signed);
            assertEquals (actual.customerInstructions, expected.customerInstructions);
            assertEquals (actual.physician.providerFullName, expected.physician.providerFullName);
            assertEquals (actual.physician.accountName, expected.physician.accountName);
            assertEquals (actual.patient.fullname, expected.patient.fullname);
            assertEquals (actual.patient.dateOfBirth, expected.patient.dateOfBirth);
            assertEquals (actual.patient.gender, expected.patient.gender);
            assertEquals (actual.patient.patientCode, expected.patient.patientCode);
            assertEquals (actual.patient.mrn, expected.patient.mrn);
            assertEquals (actual.patient.notes, expected.patient.notes);
            assertEquals (actual.icdcodes, expected.icdcodes);
            assertEquals (actual.properties.SpecimenDeliveryType, expected.properties.SpecimenDeliveryType);
            assertNull (actual.specimenNumber);
            assertNull (actual.specimenType);
            assertNull (actual.collectionDate);
            assertNull (actual.reconciliationDate);
            assertEquals (actual.expected_test_type, expected.expected_test_type);
            assertEquals (actual.tests.size (), 0);
            assertEquals (actual.properties.BillingType, expected.properties.BillingType);
            assertEquals (actual.patient.abnStatusType, expected.patient.abnStatusType);

            if (expected.patient.billingType != null)
                assertEquals (mapper.writeValueAsString (actual.patient.address),
                              mapper.writeValueAsString (expected.patient.address));
            else
                assertNull (actual.patient.billingType);

            assertEquals (mapper.writeValueAsString (actual.patient.insurance1),
                          mapper.writeValueAsString (expected.patient.insurance1));
            assertEquals (mapper.writeValueAsString (actual.patient.insurance2),
                          mapper.writeValueAsString (expected.patient.insurance2));

            assertEquals (actual.orderAttachments, expected.orderAttachments);
            assertEquals (actual.doraAttachments, expected.doraAttachments);
            assertNull (actual.notes);
        } catch (Exception e) {
            error ("expected order=" + expected.order_number + ", actual order=" + actual.order_number);
            error (String.valueOf (e), e);
            fail (String.valueOf (e));
        }
    }
}
