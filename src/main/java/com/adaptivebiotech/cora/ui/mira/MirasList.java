package com.adaptivebiotech.cora.ui.mira;

import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.Keys.RETURN;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Miras;
import com.adaptivebiotech.cora.dto.Miras.Mira;
import com.adaptivebiotech.cora.utils.PageHelper.MiraPanel;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;
import com.adaptivebiotech.ui.cora.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class MirasList extends CoraPage {

    public MirasList () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".active[title='MIRAs']"));
        pageLoading ();
    }

    public void searchMira (String miraId) {
        assertTrue (setText (".search-orders input", miraId));
        assertTrue (pressKey (RETURN));
    }

    public Miras getMiras () {
        return new Miras (waitForElements (".miras-list > tbody > tr").stream ().map (el -> {
            List <WebElement> columns = el.findElements (locateBy ("td"));
            Mira m = new Mira ();
            m.id = getMiraGuid (getAttribute (columns.get (1), "a", "href"));
            m.miraId = getText (columns.get (1));
            String panel = getText (columns.get (2));
            m.panel = panel != null ? MiraPanel.valueOf (panel) : null;
            m.numPools = Integer.valueOf (getText (columns.get (3)));
            m.asid = getText (columns.get (4));
            m.lastActivity = getText (columns.get (5));
            m.status = OrderStatus.valueOf (getText (columns.get (6)));
            m.stages = getAttributeList (columns.get (7), ".ordertest-list-stage-back", "title");
            m.stageStatus = getText (columns.get (8));
            return m;
        }).collect (toList ()));
    }

    private String getMiraGuid (String href) {
        return href.replaceFirst (".*mira/details/", "");
    }
}
