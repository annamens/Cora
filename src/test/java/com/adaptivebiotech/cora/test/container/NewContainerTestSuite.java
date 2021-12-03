package com.adaptivebiotech.cora.test.container;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.SlideBox100;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.SlideBox5;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.TubeBox10x10;
import static com.adaptivebiotech.test.utils.TestHelper.randomString;
import static java.lang.String.format;
import static java.lang.String.join;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.ContainerHistory;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.AddContainer;
import com.adaptivebiotech.cora.ui.container.ContainerList;
import com.adaptivebiotech.cora.ui.container.Detail;
import com.adaptivebiotech.cora.ui.container.History;
import com.adaptivebiotech.cora.ui.container.MyCustody;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.GenerateContainerLabels;
import com.adaptivebiotech.cora.utils.DateUtils;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;

@Test (groups = "regression")
public class NewContainerTestSuite extends ContainerTestBase {

    private final String            testFreezer1            = "[Destroyed – xAMPL]";
    private final String            error1                  = "Please select a container type.";
    private final String            error2                  = "Quantity must be between 1 and 50.";
    private final String            error3                  = "Unable to find a location in %s. Choose another storage location.";
    private final String            error4                  = "Container name is not unique. Enter another name.";
    private Login                   login                   = new Login ();
    private OrdersList              ordersList              = new OrdersList ();
    private ContainerList           containerList           = new ContainerList ();
    private AddContainer            addContainer            = new AddContainer ();
    private MyCustody               myCustody               = new MyCustody ();
    private GenerateContainerLabels generateContainerLabels = new GenerateContainerLabels ();
    private Detail                  containerDetails        = new Detail ();
    private History                 history                 = new History ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.selectNewContainer ();
    }

    /**
     * @sdlc_requirements 126.AddNewContainer
     */
    public void add_container_validation () {
        // test: missing container type
        addContainer.clickAdd ();
        addContainer.isFailedValidation (error1);

        // test: missing quantity
        addContainer.pickContainerType (SlideBox5);
        addContainer.clickAdd ();
        addContainer.isFailedValidation (error2);

        // test: negative quantity
        addContainer.enterQuantity (-1);
        addContainer.clearQuantity ();
        addContainer.clickAdd ();
        addContainer.isFailedValidation (error2);

        // test: >50 quantity
        addContainer.enterQuantity (51);
        addContainer.clearQuantity ();
        addContainer.clickAdd ();
        addContainer.isFailedValidation (error2);

        // test: duplicate name
        addContainer.addContainer (SlideBox5, 2);
        addContainer.clearQuantity ();
        addContainer.setContainerName (1, "foo");
        addContainer.setContainerName (2, "foo");
        addContainer.clickSave ();
        addContainer.getNameValErrors ().stream ().forEach (e -> assertEquals (e, error4));
        Containers containers = addContainer.getContainers ();

        // test: verify we have 2 containers in my custody
        addContainer.gotoMyCustody ();
        Containers myContainers = new Containers ();
        myContainers.list = new ArrayList <> ();
        for (Container c : myCustody.getContainers ().list)
            if (containers.findContainerByNumber (c) != null)
                myContainers.list.add (c);

        assertEquals (myContainers.list.size (), 2);
        myContainers.list.parallelStream ().forEach (container -> {
            assertEquals (container.containerType, SlideBox5);
            assertEquals (container.name, "");
            assertEquals (container.location, coraTestUser);
        });
        coraApi.login ();
        coraApi.deactivateContainers (myContainers);

        // test: incompatible freezer
        myCustody.selectNewContainer ();
        addContainer.addContainer (SlideBox100, 1);
        addContainer.setContainersLocation (testFreezer1);
        addContainer.clickSave ();
        assertEquals (addContainer.getLocationValErrors ().get (0), format (error3, testFreezer1));
        containers = addContainer.getContainers ();

        // test: verify we have 1 containers in my custody (duplicate names & incompatible freezer)
        addContainer.gotoMyCustody ();
        myContainers = new Containers ();
        myContainers.list = new ArrayList <> ();
        for (Container c : myCustody.getContainers ().list)
            if (containers.findContainerByNumber (c) != null)
                myContainers.list.add (c);

        assertEquals (myContainers.list.size (), 1);
        Container actual = myContainers.list.get (0);
        assertEquals (actual.containerType, SlideBox100);
        assertEquals (actual.location, coraTestUser);
        coraApi.deactivateContainers (myContainers);
    }

    /**
     * @sdlc_requirements 126.AddNewContainer
     */
    public void add_remove_containers () {
        String name32 = randomString (32);
        String name33 = randomString (33);

        // test: add containers and add another one
        addContainer.addContainer (SlideBox5, 1);
        addContainer.addContainer (TubeBox10x10, 1);

        // test: set name to a string of 33 chars
        addContainer.setContainerName (1, name32);
        addContainer.setContainerName (2, name33);
        addContainer.setContainerLocation (1, dumbwaiter.name);
        addContainer.clickSave ();

        Containers containers = addContainer.getContainers ();
        assertEquals (containers.list.size (), 2);
        assertEquals (containers.list.get (0).containerType, TubeBox10x10);
        assertEquals (containers.list.get (0).name, name32);
        assertTrue (containers.list.get (0).location.startsWith (dumbwaiter.name));
        assertEquals (containers.list.get (1).containerType, SlideBox5);
        assertEquals (containers.list.get (1).name, name33.substring (0, 32));

        // test: container with valid location is assigned to location
        Container c1 = containers.list.get (0);
        Container c2 = containers.list.get (1);
        addContainer.clickContainers ();
        containerList.isCorrectPage ();
        containerList.searchContainerIdOrName (c1.containerNumber);
        containerList.setCurrentLocationFilter (dumbwaiter.name);
        containerList.clickFilter ();

        Containers myContainers = new Containers ();
        myContainers.list = containerList.getContainers ().list.stream ()
                                                               .filter (container -> container.containerNumber.equals (c1.containerNumber))
                                                               .collect (Collectors.toList ());
        assertEquals (myContainers.list.size (), 1);
        assertEquals (myContainers.list.get (0).containerNumber, c1.containerNumber);
        assertEquals (myContainers.list.get (0).location, c1.location);
        coraApi.login ();
        coraApi.deactivateContainers (myContainers);

        // test: verify we have 1 containers in my custody

        addContainer.gotoMyCustody ();
        myContainers = new Containers ();
        myContainers.list = new ArrayList <> ();
        myContainers.list.add (myCustody.getContainers ().findContainerByNumber (c2));
        assertEquals (myContainers.list.size (), 1);
        assertEquals (myContainers.list.get (0).containerType, SlideBox5);
        assertEquals (myContainers.list.get (0).name, name33.substring (0, 32));
        assertEquals (myContainers.list.get (0).location, coraTestUser);
        coraApi.deactivateContainers (myContainers);
    }

    /**
     * NOTE: SR-T2155
     */
    public void createContainer () {
        assertTrue (addContainer.isAddContainerHeaderVisible ());
        assertTrue (addContainer.isContainerTypeVisible ());
        assertTrue (addContainer.isQuantityVisible ());
        assertTrue (addContainer.isAddBtnVisible ());
        Logging.testLog ("STEP 1 - Add Container form is displayed");

        ContainerType newContainerType = ContainerType.TubeBox9x9;
        addContainer.addContainer (newContainerType, 1);

        Containers emptyContainers = addContainer.getContainers ();
        assertTrue (emptyContainers.list.size () == 1);
        assertTrue (emptyContainers.list.get (0).containerNumber.isEmpty ());
        assertTrue (emptyContainers.list.get (0).name.isEmpty ());
        assertTrue (emptyContainers.list.get (0).location.isEmpty ());
        assertEquals (emptyContainers.list.get (0).containerType, newContainerType);
        Logging.testLog ("STEP 2 - Tube box (9x9) is displayed below the Add Container(s) form with the following fields: Adaptive Container ID, Barcode, Initial Storage Location. All fields are blank");

        addContainer.clickSave ();
        assertEquals (addContainer.getContainerSavedMsg (), "Container(s) saved");
        Logging.testLog ("STEP 3.1 - Success message is displayed with text Containers Saved");
        assertFalse (addContainer.isAddContainerHeaderVisible ());
        assertFalse (addContainer.isContainerTypeVisible ());
        assertFalse (addContainer.isQuantityVisible ());
        assertFalse (addContainer.isAddBtnVisible ());
        Logging.testLog ("STEP 3.2 - Add Container(s) field is not displayed");

        assertTrue (addContainer.isGenerateContainerLabelsVisible ());
        Logging.testLog ("STEP 3.3 - Generate Container Labels button is displayed");

        Containers newContainers = addContainer.getContainers ();
        String newContainerNo = newContainers.list.get (0).containerNumber;
        assertTrue (newContainers.list.size () == 1);
        assertTrue (newContainerNo.matches ("CO-\\d{6}"));
        Logging.testLog ("STEP 3.4 - Adaptive Container ID is populated");

        addContainer.clickGenerateContainerLabels ();
        generateContainerLabels.isCorrectPage ();
        List <Map <String, String>> tableData = generateContainerLabels.getGenerateContainerLabelDetails ();
        assertTrue (tableData.size () == 1);
        Map <String, String> firstRowData = tableData.get (0);
        assertTrue (firstRowData.containsKey ("Holding Container Type"));
        assertTrue (firstRowData.containsKey ("Holding Container Name"));
        assertTrue (firstRowData.containsKey ("Holding CO#"));
        assertTrue (firstRowData.containsKey ("Container Type"));
        assertTrue (firstRowData.containsKey ("Container Name"));
        assertTrue (firstRowData.containsKey ("Loc"));
        assertTrue (firstRowData.containsKey ("ASID (SP#)"));
        assertTrue (firstRowData.containsKey ("ACID (CO#)"));
        assertTrue (generateContainerLabels.isPrintersVisible ());
        assertTrue (generateContainerLabels.isPrintVisible ());
        assertTrue (generateContainerLabels.isCloseVisible ());
        assertEquals (firstRowData.get ("Container Type"), newContainerType.label);
        assertEquals (firstRowData.get ("ACID (CO#)"), newContainerNo);
        Logging.testLog ("STEP 4 - Validate Generate Container Labels modal");

        assertTrue (generateContainerLabels.isCopyToClipboardVisible ());
        Logging.testLog ("STEP 5 - Validate Copy to Clipboard");

        generateContainerLabels.clickClose ();
        addContainer.clickContainerNo (newContainerNo);
        containerDetails.isCorrectPage ();
        Logging.testLog ("STEP 6.1 - Container details page for Container1 is displayed");

        Container uiContainerDetails = containerDetails.parsePrimaryDetail ();
        assertEquals (uiContainerDetails.containerNumber, newContainerNo);
        assertEquals (uiContainerDetails.containerType, newContainerType);
        assertEquals (uiContainerDetails.location, join (" : ", coraTestUser, newContainerNo));
        Logging.testLog ("STEP 6.2 - verify Container details");

        List <String> historyRows = containerDetails.getDetailHistory ();
        assertTrue (historyRows.size () == 2, "UI History: " + history);
        String todaysDate = DateUtils.getPastFutureDate (0);
        assertTrue (historyRows.get (0).startsWith (todaysDate));
        assertTrue (historyRows.get (0).endsWith ("Created by " + coraTestUser));
        assertTrue (historyRows.get (1).startsWith (todaysDate));
        assertTrue (historyRows.get (1).endsWith ("Last modified by " + coraTestUser));
        Logging.testLog ("STEP 6.3 - verify Container history rows");

        containerDetails.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> containerHistory = history.getHistories ();
        assertTrue (containerHistory.size () == 1);
        assertTrue (containerHistory.get (0).activityDate.startsWith (todaysDate));
        assertEquals (containerHistory.get (0).activity, "Took Custody");
        assertEquals (containerHistory.get (0).location, coraTestUser);
        assertEquals (containerHistory.get (0).activityBy, coraTestUser);
        Logging.testLog ("STEP 7 - A row has been added in History Tab");

        coraApi.login ();
        coraApi.deactivateContainers (newContainers);
    }
}
