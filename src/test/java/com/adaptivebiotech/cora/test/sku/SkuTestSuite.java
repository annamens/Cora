/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.sku;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.SkuProperties;
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
        String csvFile = "skuProperties.csv";
        File f1 = new File (otlist.getDownloadsDir () + csvFile);
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

    /**
     * Note:SR-T4299
     * 
     * @sdlc.requirements SR-12650:R7
     */
    public void verifySkuPropertyColumnNames () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.clickOrderTests ();
        otlist.clickQueriesButton ();
        otlist.clickSKUproperties ();
        List <SkuProperties> skuNameEnumList = otlist.listOfSkuColumnNames ();
        List <SkuProperties> skuEnumList = Arrays.asList (SkuProperties.values ());
        assertEquals (skuNameEnumList, skuEnumList);
        testLog ("All the SKU property column names displayed in Cora SKU Properties page");

    }

}
