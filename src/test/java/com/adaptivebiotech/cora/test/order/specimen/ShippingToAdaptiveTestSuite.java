package com.adaptivebiotech.cora.test.order.specimen;

import static com.adaptivebiotech.cora.utils.TestHelper.newPatient;
import static com.adaptivebiotech.test.utils.PageHelper.Anticoagulant.EDTA;
import static com.adaptivebiotech.test.utils.PageHelper.Anticoagulant.Other;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.NoCharge;
import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.BCells;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.BoneMarrow;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.LymphNode;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.PBMC;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.Skin;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.TCells;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.Tissue;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.BoneMarrowAspirateSlide;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.CellPellet;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.CellSuspension;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.FFPEScrolls;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.FFPESlides;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.FreshBoneMarrow;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.gDNA;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.order.OrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.Billing;
import com.adaptivebiotech.cora.ui.order.Diagnostic;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.Specimen;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;

@Test (groups = "regression")
public class ShippingToAdaptiveTestSuite extends OrderTestBase {

    private OrdersList oList;
    private Specimen   specimen;

    @BeforeMethod
    public void beforeMethod () {
        new Login ().doLogin ();
        oList = new OrdersList ();
        oList.isCorrectPage ();
        oList.selectNewDiagnosticOrder ();

        Diagnostic diagnostic = new Diagnostic ();
        diagnostic.isCorrectPage ();
        diagnostic.selectPhysician (physicianTRF);
        diagnostic.createNewPatient (newPatient ());
        diagnostic.enterPatientICD_Codes (icdCode);
        diagnostic.clickSave (); // have to Save first before we can set Specimen info

        specimen = new Specimen ();
        specimen.enterSpecimenDelivery (CustomerShipment);
        specimen.clickEnterSpecimenDetails ();
    }

    public void specimenType_Blood_EDTA () {
        specimen.enterSpecimenType (Blood);
        specimen.enterAntiCoagulant (EDTA);
        saveOrder ();
    }

    public void specimenType_Blood_Citrate () {
        specimen.enterSpecimenType (Blood);
        specimen.enterAntiCoagulant (Other);
        specimen.enterAntiCoagulantOther ("Citrate");
        saveOrder ();
    }

    public void specimenType_BoneMarrowAspirateSlide () {
        specimen.enterSpecimenType (BoneMarrowAspirateSlide);
        saveOrder ();
    }

    public void specimenType_CellPellet_BoneMarrow () {
        specimen.enterSpecimenType (CellPellet);
        specimen.enterSpecimenSource (BoneMarrow);
        saveOrder ();
    }

    public void specimenType_CellPellet_PBMC () {
        specimen.enterSpecimenType (CellPellet);
        specimen.enterSpecimenSource (PBMC);
        saveOrder ();
    }

    public void specimenType_CellPellet_BCells () {
        specimen.enterSpecimenType (CellPellet);
        specimen.enterSpecimenSource (BCells);
        saveOrder ();
    }

    public void specimenType_CellPellet_TCells () {
        specimen.enterSpecimenType (CellPellet);
        specimen.enterSpecimenSource (TCells);
        saveOrder ();
    }

    public void specimenType_CellPellet_LymphNode () {
        specimen.enterSpecimenType (CellPellet);
        specimen.enterSpecimenSource (SpecimenSource.Other);
        specimen.enterSpecimenSourceOther (LymphNode.label);
        saveOrder ();
    }

    public void specimenType_CellSuspension_BoneMarrow () {
        specimen.enterSpecimenType (CellSuspension);
        specimen.enterSpecimenSource (BoneMarrow);
        saveOrder ();
    }

    public void specimenType_CellSuspension_PBMC () {
        specimen.enterSpecimenType (CellSuspension);
        specimen.enterSpecimenSource (PBMC);
        saveOrder ();
    }

    public void specimenType_CellSuspension_BCells () {
        specimen.enterSpecimenType (CellSuspension);
        specimen.enterSpecimenSource (BCells);
        saveOrder ();
    }

    public void specimenType_CellSuspension_TCells () {
        specimen.enterSpecimenType (CellSuspension);
        specimen.enterSpecimenSource (TCells);
        saveOrder ();
    }

    public void specimenType_CellSuspension_Skin () {
        specimen.enterSpecimenType (CellSuspension);
        specimen.enterSpecimenSource (SpecimenSource.Other);
        specimen.enterSpecimenSourceOther (Skin.label);
        saveOrder ();
    }

    public void specimenType_FFPEScrolls_Skin () {
        specimen.enterSpecimenType (FFPEScrolls);
        specimen.enterSpecimenSource (Skin);
        saveOrder ();
    }

    public void specimenType_FFPEScrolls_LymphNode () {
        specimen.enterSpecimenType (FFPEScrolls);
        specimen.enterSpecimenSource (LymphNode);
        saveOrder ();
    }

    public void specimenType_FFPEScrolls_BoneMarrow () {
        specimen.enterSpecimenType (FFPEScrolls);
        specimen.enterSpecimenSource (BoneMarrow);
        saveOrder ();
    }

    public void specimenType_FFPEScrolls_PBMC () {
        specimen.enterSpecimenType (FFPEScrolls);
        specimen.enterSpecimenSource (SpecimenSource.Other);
        specimen.enterSpecimenSourceOther (PBMC.label);
        saveOrder ();
    }

    public void specimenType_FFPESlides_Skin () {
        specimen.enterSpecimenType (FFPESlides);
        specimen.enterSpecimenSource (Skin);
        saveOrder ();
    }

    public void specimenType_FFPESlides_LymphNode () {
        specimen.enterSpecimenType (FFPESlides);
        specimen.enterSpecimenSource (LymphNode);
        saveOrder ();
    }

    public void specimenType_FFPESlides_BoneMarrow () {
        specimen.enterSpecimenType (FFPESlides);
        specimen.enterSpecimenSource (BoneMarrow);
        saveOrder ();
    }

    public void specimenType_FFPESlides_BCells () {
        specimen.enterSpecimenType (FFPESlides);
        specimen.enterSpecimenSource (SpecimenSource.Other);
        specimen.enterSpecimenSourceOther (BCells.label);
        saveOrder ();
    }

    public void specimenType_FreshBoneMarrow_EDTA () {
        specimen.enterSpecimenType (FreshBoneMarrow);
        specimen.enterAntiCoagulant (EDTA);
        saveOrder ();
    }

    public void specimenType_FreshBoneMarrow_Oxalate () {
        specimen.enterSpecimenType (FreshBoneMarrow);
        specimen.enterAntiCoagulant (Other);
        specimen.enterAntiCoagulantOther ("Oxalate");
        saveOrder ();
    }

    public void specimenType_gDNA_Skin () {
        specimen.enterSpecimenType (gDNA);
        specimen.enterSpecimenSource (Skin);
        saveOrder ();
    }

    public void specimenType_gDNA_Tissue () {
        specimen.enterSpecimenType (gDNA);
        specimen.enterSpecimenSource (Tissue);
        saveOrder ();
    }

    public void specimenType_gDNA_BoneMarrow () {
        specimen.enterSpecimenType (gDNA);
        specimen.enterSpecimenSource (BoneMarrow);
        saveOrder ();
    }

    public void specimenType_gDNA_PBMC () {
        specimen.enterSpecimenType (gDNA);
        specimen.enterSpecimenSource (PBMC);
        saveOrder ();
    }

    public void specimenType_gDNA_TCells () {
        specimen.enterSpecimenType (gDNA);
        specimen.enterSpecimenSource (SpecimenSource.Other);
        specimen.enterSpecimenSourceOther (TCells.label);
        saveOrder ();
    }

    public void specimenType_Tissue_Skin () {
        specimen.enterSpecimenType (SpecimenType.Tissue);
        specimen.enterSpecimenSource (Skin);
        saveOrder ();
    }

    public void specimenType_Tissue_LymphNode () {
        specimen.enterSpecimenType (SpecimenType.Tissue);
        specimen.enterSpecimenSource (LymphNode);
        saveOrder ();
    }

    public void specimenType_Tissue_BCells () {
        specimen.enterSpecimenType (SpecimenType.Tissue);
        specimen.enterSpecimenSource (SpecimenSource.Other);
        specimen.enterSpecimenSourceOther (BCells.label);
        saveOrder ();
    }

    public void specimenType_Saliva () {
        specimen.enterSpecimenType (SpecimenType.Other);
        specimen.enterSpecimenTypeOther ("Saliva");
        saveOrder ();
    }

    private void saveOrder () {
        specimen.enterCollectionDate (collectionDt);
        Billing billing = new Billing ();
        billing.selectBilling (NoCharge);
        billing.clickSave ();
    }
}
