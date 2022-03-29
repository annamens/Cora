package com.adaptivebiotech.cora.test.container;

import java.util.List;
import java.util.stream.Collectors;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.MyCustody;
import com.adaptivebiotech.cora.ui.order.OrdersList;

@Test (groups = "regression")
public class FooTestSuite extends ContainerTestBase {

    private Login      login      = new Login ();
    private OrdersList ordersList = new OrdersList ();
    private MyCustody  myCustody  = new MyCustody ();

    /**
     * @sdlc_requirements 126.MoveMetadata
     */
    public void cleanup () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.gotoMyCustody ();
        myCustody.isCorrectPage ();

        Containers containers = myCustody.getContainers ();
        List <String> allContainerIDs = containers.list.stream ().map (container -> container.containerNumber)
                                                       .collect (Collectors.toList ());

        myCustody.bulkMoveToFreezer (allContainerIDs, freezerDestroyed, "cleanup");
//        myCustody.sendAllMyCustody (freezerDestroyed);
//
//        coraApi.login ();
//        coraApi.storeContainer (containers, freezerDestroyed);
    }
}
