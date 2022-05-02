/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.ReportFlag;

/**
 * @author jpatel
 *
 */
public class ReportTDetect extends Report {

    public List <ReportFlag> parseFlags () {
        List <ReportFlag> flags = new ArrayList <> ();
        if (isElementPresent ("//h2[text()='Flags']"))
            for (WebElement td : waitForElements ("//*[h2='Flags']//tbody//td")) {
                ReportFlag flag = new ReportFlag ();
                if (isElementPresent (td, "a")) {
                    flag.name = getText (td, "a");
                    flag.link = getAttribute (td, "a", "href");
                } else
                    flag.name = getText (td);
                flags.add (flag);
            }
        return flags;
    }
}
