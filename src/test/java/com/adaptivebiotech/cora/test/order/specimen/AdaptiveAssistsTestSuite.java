package com.adaptivebiotech.cora.test.order.specimen;

import static com.adaptivebiotech.cora.utils.TestHelper.newPatient;
import static com.adaptivebiotech.test.utils.PageHelper.Anticoagulant.CfdRoche;
import static com.adaptivebiotech.test.utils.PageHelper.Anticoagulant.EDTA;
import static com.adaptivebiotech.test.utils.PageHelper.Anticoagulant.Other;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.NoCharge;
import static com.adaptivebiotech.test.utils.PageHelper.Compartment.CellFree;
import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.PathRequest;
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
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Plasma;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.gDNA;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.utils.DateUtils;
import com.adaptivebiotech.cora.utils.TestHelper;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;

@Test (groups = "regression")
public class AdaptiveAssistsTestSuite extends CoraBaseBrowser {

    private OrdersList       oList            = new OrdersList ();
    private NewOrderClonoSeq newOrderClonoSeq = new NewOrderClonoSeq ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        new Login ().doLogin ();
        oList.isCorrectPage ();
        oList.selectNewClonoSEQDiagnosticOrder ();

        newOrderClonoSeq.isCorrectPage ();
        newOrderClonoSeq.selectPhysician (TestHelper.physicianTRF ());
        newOrderClonoSeq.createNewPatient (newPatient ());
        newOrderClonoSeq.enterPatientICD_Codes ("A01.02");
        newOrderClonoSeq.clickSave (); // have to Save first before we can set Specimen info

        newOrderClonoSeq.enterSpecimenDelivery (PathRequest);
        newOrderClonoSeq.clickEnterSpecimenDetails ();
    }

    public void specimenType_Blood_EDTA () {
        newOrderClonoSeq.enterSpecimenType (Blood);
        newOrderClonoSeq.enterAntiCoagulant (EDTA);
        saveOrder ();
    }

    public void specimenType_Blood_Citrate () {
        newOrderClonoSeq.enterSpecimenType (Blood);
        newOrderClonoSeq.enterAntiCoagulant (Other);
        newOrderClonoSeq.enterAntiCoagulantOther ("Citrate");
        saveOrder ();
    }

    public void specimenType_Blood_Cellfree () {
        newOrderClonoSeq.enterSpecimenType (Blood);
        newOrderClonoSeq.enterCompartment (CellFree);
        newOrderClonoSeq.enterAntiCoagulant (CfdRoche);
        saveOrder ();
    }

    public void specimenType_BoneMarrowAspirateSlide () {
        newOrderClonoSeq.enterSpecimenType (BoneMarrowAspirateSlide);
        saveOrder ();
    }

    public void specimenType_CellPellet_BoneMarrow () {
        newOrderClonoSeq.enterSpecimenType (CellPellet);
        newOrderClonoSeq.enterSpecimenSource (BoneMarrow);
        saveOrder ();
    }

    public void specimenType_CellPellet_PBMC () {
        newOrderClonoSeq.enterSpecimenType (CellPellet);
        newOrderClonoSeq.enterSpecimenSource (PBMC);
        saveOrder ();
    }

    public void specimenType_CellPellet_BCells () {
        newOrderClonoSeq.enterSpecimenType (CellPellet);
        newOrderClonoSeq.enterSpecimenSource (BCells);
        saveOrder ();
    }

    public void specimenType_CellPellet_TCells () {
        newOrderClonoSeq.enterSpecimenType (CellPellet);
        newOrderClonoSeq.enterSpecimenSource (TCells);
        saveOrder ();
    }

    public void specimenType_CellPellet_LymphNode () {
        newOrderClonoSeq.enterSpecimenType (CellPellet);
        newOrderClonoSeq.enterSpecimenSource (SpecimenSource.Other);
        newOrderClonoSeq.enterSpecimenSourceOther (LymphNode.label);
        saveOrder ();
    }

    public void specimenType_CellSuspension_BoneMarrow () {
        newOrderClonoSeq.enterSpecimenType (CellSuspension);
        newOrderClonoSeq.enterSpecimenSource (BoneMarrow);
        saveOrder ();
    }

    public void specimenType_CellSuspension_PBMC () {
        newOrderClonoSeq.enterSpecimenType (CellSuspension);
        newOrderClonoSeq.enterSpecimenSource (PBMC);
        saveOrder ();
    }

    public void specimenType_CellSuspension_BCells () {
        newOrderClonoSeq.enterSpecimenType (CellSuspension);
        newOrderClonoSeq.enterSpecimenSource (BCells);
        saveOrder ();
    }

    public void specimenType_CellSuspension_TCells () {
        newOrderClonoSeq.enterSpecimenType (CellSuspension);
        newOrderClonoSeq.enterSpecimenSource (TCells);
        saveOrder ();
    }

    public void specimenType_CellSuspension_Skin () {
        newOrderClonoSeq.enterSpecimenType (CellSuspension);
        newOrderClonoSeq.enterSpecimenSource (SpecimenSource.Other);
        newOrderClonoSeq.enterSpecimenSourceOther (Skin.label);
        saveOrder ();
    }

    public void specimenType_FFPEScrolls_Skin () {
        newOrderClonoSeq.enterSpecimenType (FFPEScrolls);
        newOrderClonoSeq.enterSpecimenSource (Skin);
        saveOrder ();
    }

    public void specimenType_FFPEScrolls_LymphNode () {
        newOrderClonoSeq.enterSpecimenType (FFPEScrolls);
        newOrderClonoSeq.enterSpecimenSource (LymphNode);
        saveOrder ();
    }

    public void specimenType_FFPEScrolls_BoneMarrow () {
        newOrderClonoSeq.enterSpecimenType (FFPEScrolls);
        newOrderClonoSeq.enterSpecimenSource (BoneMarrow);
        saveOrder ();
    }

    public void specimenType_FFPEScrolls_PBMC () {
        newOrderClonoSeq.enterSpecimenType (FFPEScrolls);
        newOrderClonoSeq.enterSpecimenSource (SpecimenSource.Other);
        newOrderClonoSeq.enterSpecimenSourceOther (PBMC.label);
        saveOrder ();
    }

    public void specimenType_FFPESlides_Skin () {
        newOrderClonoSeq.enterSpecimenType (FFPESlides);
        newOrderClonoSeq.enterSpecimenSource (Skin);
        saveOrder ();
    }

    public void specimenType_FFPESlides_LymphNode () {
        newOrderClonoSeq.enterSpecimenType (FFPESlides);
        newOrderClonoSeq.enterSpecimenSource (LymphNode);
        saveOrder ();
    }

    public void specimenType_FFPESlides_BoneMarrow () {
        newOrderClonoSeq.enterSpecimenType (FFPESlides);
        newOrderClonoSeq.enterSpecimenSource (BoneMarrow);
        saveOrder ();
    }

    public void specimenType_FFPESlides_BCells () {
        newOrderClonoSeq.enterSpecimenType (FFPESlides);
        newOrderClonoSeq.enterSpecimenSource (SpecimenSource.Other);
        newOrderClonoSeq.enterSpecimenSourceOther (BCells.label);
        saveOrder ();
    }

    public void specimenType_FreshBoneMarrow_EDTA () {
        newOrderClonoSeq.enterSpecimenType (FreshBoneMarrow);
        newOrderClonoSeq.enterAntiCoagulant (EDTA);
        saveOrder ();
    }

    public void specimenType_FreshBoneMarrow_Oxalate () {
        newOrderClonoSeq.enterSpecimenType (FreshBoneMarrow);
        newOrderClonoSeq.enterAntiCoagulant (Other);
        newOrderClonoSeq.enterAntiCoagulantOther ("Oxalate");
        saveOrder ();
    }

    public void specimenType_gDNA_Skin () {
        newOrderClonoSeq.enterSpecimenType (gDNA);
        newOrderClonoSeq.enterSpecimenSource (Skin);
        saveOrder ();
    }

    public void specimenType_gDNA_Tissue () {
        newOrderClonoSeq.enterSpecimenType (gDNA);
        newOrderClonoSeq.enterSpecimenSource (Tissue);
        saveOrder ();
    }

    public void specimenType_gDNA_BoneMarrow () {
        newOrderClonoSeq.enterSpecimenType (gDNA);
        newOrderClonoSeq.enterSpecimenSource (BoneMarrow);
        saveOrder ();
    }

    public void specimenType_gDNA_PBMC () {
        newOrderClonoSeq.enterSpecimenType (gDNA);
        newOrderClonoSeq.enterSpecimenSource (PBMC);
        saveOrder ();
    }

    public void specimenType_gDNA_TCells () {
        newOrderClonoSeq.enterSpecimenType (gDNA);
        newOrderClonoSeq.enterSpecimenSource (SpecimenSource.Other);
        newOrderClonoSeq.enterSpecimenSourceOther (TCells.label);
        saveOrder ();
    }

    public void specimenType_Plasma () {
        newOrderClonoSeq.enterSpecimenType (Plasma);
        saveOrder ();
    }

    public void specimenType_Tissue_Skin () {
        newOrderClonoSeq.enterSpecimenType (SpecimenType.Tissue);
        newOrderClonoSeq.enterSpecimenSource (Skin);
        saveOrder ();
    }

    public void specimenType_Tissue_LymphNode () {
        newOrderClonoSeq.enterSpecimenType (SpecimenType.Tissue);
        newOrderClonoSeq.enterSpecimenSource (LymphNode);
        saveOrder ();
    }

    public void specimenType_Tissue_BCells () {
        newOrderClonoSeq.enterSpecimenType (SpecimenType.Tissue);
        newOrderClonoSeq.enterSpecimenSource (SpecimenSource.Other);
        newOrderClonoSeq.enterSpecimenSourceOther (BCells.label);
        saveOrder ();
    }

    public void specimenType_Saliva () {
        newOrderClonoSeq.enterSpecimenType (SpecimenType.Other);
        newOrderClonoSeq.enterSpecimenTypeOther ("Saliva");
        saveOrder ();
    }

    private void saveOrder () {
        newOrderClonoSeq.enterCollectionDate (DateUtils.getPastFutureDate (-3));
        newOrderClonoSeq.billing.selectBilling (NoCharge);
        newOrderClonoSeq.clickSave ();
    }
}
