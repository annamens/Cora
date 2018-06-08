package com.adaptivebiotech.test.cora.order.specimen;

import static com.adaptivebiotech.utils.PageHelper.Anticoagulant.EDTA;
import static com.adaptivebiotech.utils.PageHelper.Anticoagulant.Other;
import static com.adaptivebiotech.utils.PageHelper.Assay.Clonality_BCell_2;
import static com.adaptivebiotech.utils.PageHelper.ChargeType.Client;
import static com.adaptivebiotech.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.utils.PageHelper.DeliveryType.PathRequest;
import static com.adaptivebiotech.utils.PageHelper.DiscrepancyType.Specimen;
import static com.adaptivebiotech.utils.PageHelper.ShippingCondition.Ambient;
import static com.adaptivebiotech.utils.PageHelper.SpecimenSource.BCells;
import static com.adaptivebiotech.utils.PageHelper.SpecimenSource.BoneMarrow;
import static com.adaptivebiotech.utils.PageHelper.SpecimenSource.LymphNode;
import static com.adaptivebiotech.utils.PageHelper.SpecimenSource.PBMC;
import static com.adaptivebiotech.utils.PageHelper.SpecimenSource.Skin;
import static com.adaptivebiotech.utils.PageHelper.SpecimenSource.TCells;
import static com.adaptivebiotech.utils.PageHelper.SpecimenSource.Tissue;
import static com.adaptivebiotech.utils.PageHelper.SpecimenType.Blood;
import static com.adaptivebiotech.utils.PageHelper.SpecimenType.BoneMarrowAspirateSlide;
import static com.adaptivebiotech.utils.PageHelper.SpecimenType.CellPellet;
import static com.adaptivebiotech.utils.PageHelper.SpecimenType.CellSuspension;
import static com.adaptivebiotech.utils.PageHelper.SpecimenType.FFPEScrolls;
import static com.adaptivebiotech.utils.PageHelper.SpecimenType.FFPESlides;
import static com.adaptivebiotech.utils.PageHelper.SpecimenType.FreshBoneMarrow;
import static com.adaptivebiotech.utils.PageHelper.SpecimenType.gDNA;
import static com.adaptivebiotech.utils.TestHelper.newPatient;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.test.cora.order.OrderTestBase;
import com.adaptivebiotech.ui.cora.CoraPage;
import com.adaptivebiotech.ui.cora.order.Billing;
import com.adaptivebiotech.ui.cora.order.Diagnostic;
import com.adaptivebiotech.ui.cora.order.Specimen;
import com.adaptivebiotech.ui.cora.shipment.Accession;
import com.adaptivebiotech.ui.cora.shipment.Shipment;
import com.adaptivebiotech.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.utils.PageHelper.SpecimenType;

@Test (groups = { "cora" })
public class AdaptiveAssistsTestSuite extends OrderTestBase {

    private Specimen specimen;

    @BeforeMethod
    public void beforeMethod () {
        new CoraPage ().clickNewDiagnosticOrder ();
        Diagnostic diagnostic = new Diagnostic ();
        diagnostic.isCorrectPage ();
        diagnostic.selectPhysician (physicianTRF);
        diagnostic.createNewPatient (newPatient ());
        diagnostic.enterPatientICD_Codes (icdCode);
        diagnostic.clickSave (); // have to Save first before we can set Specimen info

        specimen = new Specimen ();
        specimen.enterSpecimenDelivery (PathRequest);
        specimen.clickEnterSpecimenDetails ();
    }

    public void specimenType_Blood_EDTA () {
        specimen.enterSpecimenType (Blood);
        specimen.enterAntiCoagulant (EDTA);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_Blood_Citrate () {
        specimen.enterSpecimenType (Blood);
        specimen.enterAntiCoagulant (Other);
        specimen.enterAntiCoagulantOther ("Citrate");
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate (true);
    }

    public void specimenType_BoneMarrowAspirateSlide () {
        specimen.enterSpecimenType (BoneMarrowAspirateSlide);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_CellPellet_BoneMarrow () {
        specimen.enterSpecimenType (CellPellet);
        specimen.enterSpecimenSource (BoneMarrow);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_CellPellet_PBMC () {
        specimen.enterSpecimenType (CellPellet);
        specimen.enterSpecimenSource (PBMC);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_CellPellet_BCells () {
        specimen.enterSpecimenType (CellPellet);
        specimen.enterSpecimenSource (BCells);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_CellPellet_TCells () {
        specimen.enterSpecimenType (CellPellet);
        specimen.enterSpecimenSource (TCells);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_CellPellet_LymphNode () {
        specimen.enterSpecimenType (CellPellet);
        specimen.enterSpecimenSource (SpecimenSource.Other);
        specimen.enterSpecimenSourceOther ("LymphNode");
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_CellSuspension_BoneMarrow () {
        specimen.enterSpecimenType (CellSuspension);
        specimen.enterSpecimenSource (BoneMarrow);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_CellSuspension_PBMC () {
        specimen.enterSpecimenType (CellSuspension);
        specimen.enterSpecimenSource (PBMC);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_CellSuspension_BCells () {
        specimen.enterSpecimenType (CellSuspension);
        specimen.enterSpecimenSource (BCells);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_CellSuspension_TCells () {
        specimen.enterSpecimenType (CellSuspension);
        specimen.enterSpecimenSource (TCells);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_CellSuspension_Skin () {
        specimen.enterSpecimenType (CellSuspension);
        specimen.enterSpecimenSource (SpecimenSource.Other);
        specimen.enterSpecimenSourceOther ("Skin");
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_FFPEScrolls_Skin () {
        specimen.enterSpecimenType (FFPEScrolls);
        specimen.enterSpecimenSource (Skin);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_FFPEScrolls_LymphNode () {
        specimen.enterSpecimenType (FFPEScrolls);
        specimen.enterSpecimenSource (LymphNode);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_FFPEScrolls_BoneMarrow () {
        specimen.enterSpecimenType (FFPEScrolls);
        specimen.enterSpecimenSource (BoneMarrow);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_FFPEScrolls_PBMC () {
        specimen.enterSpecimenType (FFPEScrolls);
        specimen.enterSpecimenSource (SpecimenSource.Other);
        specimen.enterSpecimenSourceOther ("PBMC");
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_FFPESlides_Skin () {
        specimen.enterSpecimenType (FFPESlides);
        specimen.enterSpecimenSource (Skin);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_FFPESlides_LymphNode () {
        specimen.enterSpecimenType (FFPESlides);
        specimen.enterSpecimenSource (LymphNode);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_FFPESlides_BoneMarrow () {
        specimen.enterSpecimenType (FFPESlides);
        specimen.enterSpecimenSource (BoneMarrow);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_FFPESlides_BCells () {
        specimen.enterSpecimenType (FFPESlides);
        specimen.enterSpecimenSource (SpecimenSource.Other);
        specimen.enterSpecimenSourceOther ("BCells");
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_FreshBoneMarrow_EDTA () {
        specimen.enterSpecimenType (FreshBoneMarrow);
        specimen.enterAntiCoagulant (EDTA);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_FreshBoneMarrow_Oxalate () {
        specimen.enterSpecimenType (FreshBoneMarrow);
        specimen.enterAntiCoagulant (Other);
        specimen.enterAntiCoagulantOther ("Oxalate");
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate (true);
    }

    public void specimenType_gDNA_Skin () {
        specimen.enterSpecimenType (gDNA);
        specimen.enterSpecimenSource (Skin);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_gDNA_Tissue () {
        specimen.enterSpecimenType (gDNA);
        specimen.enterSpecimenSource (Tissue);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_gDNA_BoneMarrow () {
        specimen.enterSpecimenType (gDNA);
        specimen.enterSpecimenSource (BoneMarrow);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_gDNA_PBMC () {
        specimen.enterSpecimenType (gDNA);
        specimen.enterSpecimenSource (PBMC);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_gDNA_TCells () {
        specimen.enterSpecimenType (gDNA);
        specimen.enterSpecimenSource (SpecimenSource.Other);
        specimen.enterSpecimenSourceOther ("TCells");
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_Tissue_Skin () {
        specimen.enterSpecimenType (SpecimenType.Tissue);
        specimen.enterSpecimenSource (Skin);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_Tissue_LymphNode () {
        specimen.enterSpecimenType (SpecimenType.Tissue);
        specimen.enterSpecimenSource (LymphNode);
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_Tissue_BCells () {
        specimen.enterSpecimenType (SpecimenType.Tissue);
        specimen.enterSpecimenSource (SpecimenSource.Other);
        specimen.enterSpecimenSourceOther ("BCells");
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate ();
    }

    public void specimenType_Saliva () {
        specimen.enterSpecimenType (SpecimenType.Other);
        specimen.enterSpecimenTypeOther ("Saliva");
        specimen.enterCollectionDate (collectionDt);
        addDiagnosticShipment_and_Activate (true);
    }

    private void addDiagnosticShipment_and_Activate () {
        addDiagnosticShipment_and_Activate (false);
    }

    private void addDiagnosticShipment_and_Activate (boolean doManualPass) {
        Billing billing = new Billing ();
        billing.selectBilling (Client);
        billing.clickSave ();
        String orderNum = billing.getOrderNum ();

        Shipment shipment = new Shipment ();
        shipment.clickNewDiagnosticShipment ();
        shipment.isCorrectPage ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (orderNum);
        shipment.enterDiagnosticSpecimenContainerType (Tube);
        shipment.clickSave ();
        shipment.gotoAccession ();

        Accession accession = new Accession ();
        accession.isCorrectPage ();
        accession.clickIntakeComplete ();
        if (doManualPass)
            accession.manualPass (Specimen);
        accession.clickPass ();
        accession.gotoOrderDetail ();

        Diagnostic diagnostic = new Diagnostic ();
        diagnostic.isCorrectPage ();
        diagnostic.clickAssayTest (Clonality_BCell_2);
        diagnostic.clickActivateOrder ();
        diagnostic.clickCancel ();
        diagnostic.clickCancelOrder ();
    }
}
