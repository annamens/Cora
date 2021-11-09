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
            for (WebElement a : waitForElements ("//*[h2='Flags']//tr//a")) {
                ReportFlag flag = new ReportFlag ();
                flag.name = getText (a);
                flag.link = getAttribute (a, "href");
                flags.add (flag);
            }
        return flags;
    }
}
