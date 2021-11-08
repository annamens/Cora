package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.utils.TestHelper.insurance1;
import static com.adaptivebiotech.cora.utils.TestHelper.newPatient;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.ID_BCell2_IVD;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.MRD_BCell2_IVD;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.Client;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.test.utils.PageHelper.PatientStatus.NonHospital;
import static com.adaptivebiotech.test.utils.PageHelper.PatientStatus.Outpatient;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.BoneMarrowAspirateSlide;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.utils.DateUtils;
import com.adaptivebiotech.cora.utils.TestHelper;

@Test (groups = "regression", enabled = false)
public class NewOrderTestSuite extends CoraBaseBrowser {

    private OrdersList       oList      = new OrdersList ();
    private NewOrderClonoSeq diagnostic = new NewOrderClonoSeq ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        new Login ().doLogin ();
        oList.isCorrectPage ();
    }

    public void clonality_eos_ivd () {
        Patient patient = newPatient ();
        patient.billingType = Client;
        patient.insurance1.hospitalizationStatus = NonHospital;

        // test: new order for Clonality EOD IVD
        prep_new_order (patient);
        diagnostic.isCorrectPage ();
        diagnostic.clickAssayTest (ID_BCell2_IVD);
        diagnostic.activateOrder ();
    }

    public void tracking_eos_ivd () {
        Patient patient = newPatient ();
        patient.billingType = Client;
        patient.insurance1 = insurance1 ();
        patient.insurance1.hospitalizationStatus = Outpatient;

        // test: new order for Tracking EOD IVD
        prep_new_order (patient);
        diagnostic.isCorrectPage ();
        diagnostic.clickAssayTest (MRD_BCell2_IVD);
        diagnostic.activateOrder ();
    }

    private void prep_new_order (Patient patient) {
        diagnostic.selectNewClonoSEQDiagnosticOrder ();
        diagnostic.isCorrectPage ();
        diagnostic.selectPhysician (TestHelper.physicianTRF ());
        diagnostic.createNewPatient (patient);
        diagnostic.enterPatientICD_Codes ("A01.02");
        diagnostic.billing.enterBill (patient);
        diagnostic.clickSave (); // have to Save first before we can set Specimen info

        diagnostic.enterSpecimenDelivery (CustomerShipment);
        diagnostic.clickEnterSpecimenDetails ();
        diagnostic.enterSpecimenType (BoneMarrowAspirateSlide);
        diagnostic.enterCollectionDate (DateUtils.getPastFutureDate (-3));
        diagnostic.clickSave ();

        add_shipment_and_accession (diagnostic.getOrderNum ());
    }

    private void add_shipment_and_accession (String orderNum) {
        // test: add diagnostic shipment
        new Shipment ().createShipment (orderNum, Tube);

        // test: accession complete
        new Accession ().completeAccession ();
    }
}
