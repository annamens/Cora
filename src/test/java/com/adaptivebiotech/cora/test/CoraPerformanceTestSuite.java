package com.adaptivebiotech.cora.test;

import static com.adaptivebiotech.cora.utils.TestHelper.freezerAB018078;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.AddContainer;
import com.adaptivebiotech.cora.ui.container.ContainerList;
import com.adaptivebiotech.cora.ui.mira.MirasList;
import com.adaptivebiotech.cora.ui.order.Diagnostic;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientsList;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentList;
import com.adaptivebiotech.cora.ui.task.TaskList;

@Test (groups = "performance")
public class CoraPerformanceTestSuite extends CoraBaseBrowser {

    private Login          login;
    private CoraPage       cora;
    private OrdersList     oList;
    private OrderTestsList otList;
    private ShipmentList   shList;
    private ContainerList  cList;
    private TaskList       tList;
    private MirasList      mList;
    private PatientsList   pList;
    private AddContainer   cAdd;
    private Diagnostic     diagnostic;
    private Shipment       shipment;

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login = new Login ();
        cora = new CoraPage ();
        oList = new OrdersList ();
        otList = new OrderTestsList ();
        shList = new ShipmentList ();
        cList = new ContainerList ();
        tList = new TaskList ();
        mList = new MirasList ();
        pList = new PatientsList ();
        cAdd = new AddContainer ();
        diagnostic = new Diagnostic ();
        shipment = new Shipment ();
        login.doLogin ();
    }

    public void loadListOfOrders () {
        oList.isCorrectTopNavRow2 ();
        oList.isCorrectPage ();
    }

    public void loadListOfOrderTests () {
        oList.isCorrectPage ();
        cora.clickOrderTests ();
        otList.isCorrectPage ();
    }

    public void loadListOfShipments () {
        oList.isCorrectPage ();
        cora.clickShipments ();
        shList.isCorrectPage ();
    }

    public void loadListOfContainers () {
        oList.isCorrectPage ();
        cora.clickContainers ();
        cList.isCorrectPage ();
    }

    public void loadListOfTasks () {
        oList.isCorrectPage ();
        cora.clickTasks ();
        tList.isCorrectPage ();
    }

    public void loadListOfMiras () {
        oList.isCorrectPage ();
        cora.clickMiras ();
        mList.isCorrectPage ();
    }

    public void loadListOfPatients () {
        oList.isCorrectPage ();
        cora.clickPatients ();
        pList.isCorrectPage ();
    }

    public void filterContainers () {
        oList.isCorrectPage ();
        cora.searchContainer (freezerAB018078 ());
        cList.isCorrectPage ();
    }

    public void newBatchShipment () {
        oList.isCorrectPage ();
        diagnostic.selectNewBatchShipment ();
        shipment.isBatchOrGeneral ();
        shipment.enterShippingCondition (Ambient);
        shipment.clickSave ();
        String sNum = shipment.getHeaderShipmentNum ();
        assertTrue (sNum.matches ("Batch Shipment # SH-\\d{5,}"));
        cora.clickShipments ();
        shList.isCorrectPage ();
        List <String> shNames = new ArrayList <> ();
        shList.getAllShipments ().stream ().forEach (x -> shNames.add (x.shipmentNumber));
        assertTrue (shNames.contains (sNum.substring (sNum.indexOf ("SH-"))));
    }
}
