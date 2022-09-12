/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.sku;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertTrue;
import java.io.File;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;

/**
 * @author Srinivas Annameni
 *         <a href="mailto:sannameni@adaptivebiotech.com">sannameni@adaptivebiotech.com</a>
 */
@Test
public class SkuTestSuite extends CoraBaseBrowser {

    private Login          login      = new Login ();
    private OrdersList     ordersList = new OrdersList ();
    private CoraPage       cora       = new CoraPage ();
    private OrderTestsList otlist     = new OrderTestsList ();

    /**
     * Note:SR-T4303
     * sdlc.requirements SR-12650, SR-12749:R5
     */
    public void CsvDownloadButton () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        cora.clickOrderTests ();
        otlist.clickQueriesButton ();
        otlist.clickSkuProperties ();
        otlist.clickCsvDownloadButton ();
        testLog ("Clicked CSV download button");
        String path = login.getDownloadsDir ();
        assertTrue (login.isFileDownloaded (path));
        File f = new File (path);
        String filenames[] = f.list ();
        for (String filename : filenames) {
            if (filename.contains ("skuProperties"))
                testLog ("The downloaded file is: " + filename);
        }
    }
}
