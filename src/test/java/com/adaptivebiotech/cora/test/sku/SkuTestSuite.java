/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.sku;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertTrue;
import java.io.File;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;

/**
 * @author Srinivas Annameni
 *         <a href="mailto:sannameni@adaptivebiotech.com">sannameni@adaptivebiotech.com</a>
 */
@Test (groups = { "regression", "irish-wolfhound" })
public class SkuTestSuite extends CoraBaseBrowser {

    private Login          login      = new Login ();
    private OrdersList     ordersList = new OrdersList ();
    private OrderTestsList otlist     = new OrderTestsList ();

    /**
     * Note:SR-T4303
     * 
     * @sdlc.requirements SR-12650:R5
     */
    public void CsvDownloadButton () {
        String csvFile = otlist.getDownloadsDir () + "skuProperties.csv";
        File f1 = new File (csvFile);
        f1.delete ();

        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.clickOrderTests ();
        otlist.clickQueriesButton ();
        otlist.clickSKUproperties ();
        otlist.clickCSVdownloadButton ();
        testLog ("Clicked CSV download button");
        assertTrue ( (otlist.isFileDownloaded (csvFile)));
        testLog ("The downloaded file is: skuProperties.csv");

    }
}
