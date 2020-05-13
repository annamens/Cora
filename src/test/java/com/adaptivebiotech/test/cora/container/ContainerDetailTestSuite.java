package com.adaptivebiotech.test.cora.container;

import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.ui.container.AddContainer;
import com.adaptivebiotech.cora.ui.container.Detail;
import com.adaptivebiotech.cora.ui.container.History;
import com.adaptivebiotech.cora.ui.container.MyCustody;
import com.adaptivebiotech.ui.cora.CoraPage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.SlideBox5CS;
import static org.testng.Assert.*;

@Test (groups = { "container", "regression" })
public class ContainerDetailTestSuite extends ContainerTestBase {

    private final String error1 = "Only '.pdf,.jpg,.png,.gif,.xlsx' files allowed";
    private final String error2 = "Please select less than 10 files at a time!";
    private CoraPage     main;
    private Containers   containers;
    private Detail       detail;

    @BeforeMethod
    public void beforeMethod () {
        main = new CoraPage ();
        main.clickNewContainer ();

        AddContainer add = new AddContainer ();
        add.addContainer (SlideBox5CS, 1);
        add.clickSave ();
        containers = add.getContainers ();
        detail = new Detail ();
    }


    /**
     * @sdlc_requirements 126.ContainerDetailsPage
     */
    public void extensionCheck () {
        main.gotoContainerDetail (containers.list.get (0));

        // test: unsupported file ext
        detail.isCorrectPage ();
        detail.uploadAttachments ("attachment.tiff");
        assertEquals (detail.getFileExtErr (), error1);
    }

    /**
     * @sdlc_requirements 126.ContainerDetailsPage
     */
    public void maxFilesCheck () {
        main.gotoContainerDetail (containers.list.get (0));

        // test: >10 files at a time
        String[] files = new String[] {
                "attachment.jpg", "test1.png", "test2.png", "test3.png", "test4.png", "test5.png", "test6.png",
                "test7.png", "test8.png", "test9.png", "test10.png"
        };
        detail.uploadAttachments (files);
        assertEquals (detail.getMaxFileErr (), error2);
    }

    /**
     * @sdlc_requirements 126.ContainerDetailsPage
     */
    public void happyPath () {
        main.gotoContainerDetail (containers.list.get (0));

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
    @Test (enabled = false)
    public void history () {
        Container testFreezer = new Container ();
        testFreezer.name = "Sample Prep -20C";

        main.gotoMyCustody ();

        // test: history section of container detail
        Container c = containers.list.get (0);
        main.gotoContainerDetail (c);
        detail.isCorrectPage ();
        List <String> detailHistories1 = detail.getDetailHistory ();
        assertTrue (detailHistories1.get (0).contains ("Created by " + coraTestUser));
        assertTrue (detailHistories1.get (1).contains ("Last modified by " + coraTestUser));

        // test: history section of history view
        main.gotoContainerHistory (c);
        History history = new History ();
        history.isCorrectPage ();
        List <String> historyHistories1 = history.getDetailHistory ();
        assertEquals (historyHistories1, detailHistories1);

        // test: change modified time
        main.doWait (500);
        main.gotoMyCustody ();
        MyCustody my = new MyCustody ();
        // my.moveToFreezer (c, testFreezer, false, dummyString (200)));
        my.scan (c);
        // my.destroyContainer (c));
        my.scan (c);
        c.comment = "foo";
        my.moveToFreezer (c, testFreezer);

        // test: check history section of container detail one more time
        main.gotoContainerDetail (c);
        detail.isCorrectPage ();
        List <String> detailHistories2 = detail.getDetailHistory ();
        assertEquals (detailHistories2.get (0), detailHistories1.get (0));
        assertNotEquals (detailHistories2.get (1), detailHistories1.get (1));

        // test: check history section of history view one more time
        main.gotoContainerHistory (c);
        history.isCorrectPage ();
        List <String> historyHistories2 = history.getDetailHistory ();
        assertEquals (historyHistories2, detailHistories2);
    }
} 
