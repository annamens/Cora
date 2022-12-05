/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.debug.worklist;

import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.dto.Element;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author Srinivas Annameni
 *         <a href="mailto:sannameni@adaptivebiotech.com">sannameni@adaptivebiotech.com</a>
 */
public class WorkList extends CoraPage {

    public WorkList () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible ("worklist"));
    }

    public Element getStabilizationWindow (String order) {
        String xpath = "//a[text()='%s']/preceding::td[1]//div[contains(@class,'data-cell-padding stability')]";
        String stabilizationWindow = format (xpath, order);
        Element el = new Element ();
        el.text = getText (stabilizationWindow + "//strong");
        el.color = getCssValue (stabilizationWindow, "background-color");
        return el;
    }

    public List <String> getColumnList (String worklistItem) {
        List <String> columnHeaderList = getTextList ("//table[@class='genoTable']/descendant::tr[1]/th");
        return columnHeaderList;
    }
}
