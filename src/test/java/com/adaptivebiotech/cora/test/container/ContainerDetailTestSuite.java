package com.adaptivebiotech.cora.test.container;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.SlideBox5CS;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.ContainerHistory;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.Detail;
import com.adaptivebiotech.cora.ui.container.History;
import com.adaptivebiotech.cora.ui.container.MyCustody;
import com.adaptivebiotech.cora.ui.order.OrdersList;

@Test (groups = { "regression" })
public class ContainerDetailTestSuite extends ContainerTestBase {

    private final String             error1     = "Only '.pdf,.jpg,.png,.gif,.xlsx' files allowed";
    private Login                    login      = new Login ();
    private OrdersList               orderList  = new OrdersList ();
    private Detail                   detail     = new Detail ();
    private MyCustody                myCustody  = new MyCustody ();
    private ThreadLocal <Containers> containers = new ThreadLocal <> ();

    @BeforeMethod
    public void beforeMethod () {
        containers.set (coraApi.addContainers (new Containers (asList (container (SlideBox5CS)))));

        login.doLogin ();
        orderList.isCorrectPage ();
    }

    @AfterMethod
    public void afterMethod () {
        coraApi.deactivateContainers (containers.get ());
    }

    /**
     * @sdlc_requirements 126.ContainerDetailsPage
     */
    public void extensionCheck () {
        orderList.gotoContainerDetail (containers.get ().list.get (0));

        // test: unsupported file ext
        detail.isCorrectPage ();
        detail.uploadAttachments ("attachment.tiff");
        assertEquals (detail.getFileExtErr (), error1);
    }

    /**
     * @sdlc_requirements 126.ContainerDetailsPage
     */
    public void happyPath () {
        orderList.gotoContainerDetail (containers.get ().list.get (0));

        // test: view attachment
        String[] files = new String[] { "attachment.gif", "attachment.jpg", "attachment.pdf", "attachment.png" };
        detail.uploadAttachments (files);
        detail.viewAttachment (2);
        detail.closePopup ();
        detail.deleteAttachment (2);
    }

    /**
     * @sdlc_requirements 126.ContainerDetailsPage
     */
    public void history () {
        orderList.gotoMyCustody ();

        // test: history section of container detail
        Container testContainer = containers.get ().list.get (0);
        myCustody.gotoContainerDetail (testContainer);
        detail.isCorrectPage ();
        List <String> detailHistories1 = detail.getDetailHistory ();
        assertTrue (detailHistories1.get (0).contains ("Created by " + coraTestUser));
        assertTrue (detailHistories1.get (1).contains ("Last modified by " + coraTestUser));
        ContainerHistory activity1 = new ContainerHistory ();
        activity1.activityDate = detailHistories1.get (1).replace (" Last modified by " + coraTestUser, "");
        activity1.activity = "Took Custody";
        activity1.location = coraTestUser;
        activity1.activityBy = coraTestUser;

        // test: history section of history view
        detail.gotoContainerHistory (testContainer);
        History history = new History ();
        history.isCorrectPage ();
        List <ContainerHistory> acivities = history.getActivities ();
        assertEquals (acivities, asList (activity1));

        // test: change modified time
        history.gotoMyCustody ();
        MyCustody my = new MyCustody ();
        my.moveToFreezer (testContainer, freezerAB039003);

        // test: check history section of container detail one more time
        my.gotoContainerDetail (testContainer);
        detail.isCorrectPage ();
        List <String> detailHistories2 = detail.getDetailHistory ();
        assertTrue (detailHistories2.get (1).contains ("Last modified by " + coraTestUser));
        ContainerHistory activity2 = new ContainerHistory ();
        activity2.activityDate = detailHistories2.get (1).replace (" Last modified by " + coraTestUser, "");
        activity2.activity = "Moved to Location";
        activity2.location = join (" : ", freezerAB039003.name, "5-Slide boxes");
        activity2.activityBy = coraTestUser;

        // test: check history section of history view one more time
        detail.gotoContainerHistory (testContainer);
        history.isCorrectPage ();
        acivities = history.getActivities ();
        assertEquals (acivities, asList (activity2, activity1));
    }
}
