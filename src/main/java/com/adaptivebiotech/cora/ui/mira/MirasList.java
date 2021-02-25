package com.adaptivebiotech.cora.ui.mira;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.openqa.selenium.Keys.RETURN;
import static org.testng.Assert.assertTrue;
import static com.seleniumfy.test.utils.Logging.info;

import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Miras;
import com.adaptivebiotech.cora.dto.Miras.Mira;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;
import com.adaptivebiotech.cora.utils.PageHelper.MiraPanel;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;

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

    public void searchAndClickMira (String miraId) {
        String searchField = "input[type='search']";
        String firstResult = "//table[contains(@class, 'mira-table')]/tbody/tr[1]/td[1]/a/span";

        assertTrue (setText (searchField, miraId));
        assertTrue (pressKey (RETURN));
        pageLoading ();
        
        // wait for the search results to populate
        String firstElementText = waitForElement (firstResult).getText ();
        int count = 0;
        while(count < 20 && !firstElementText.equals (miraId)) {
            info("waiting for search result");
            count++;
            doWait(10000);
            firstElementText = waitForElement (firstResult).getText ();
        }
        assertEquals (waitForElement (firstResult).getText (), miraId);
        assertTrue (click (firstResult));
        pageLoading ();
        assertTrue (waitUntilVisible (".mira-header"));
    }

    public void selectLab (MiraLab miraLab) {
        String dropdown = "//dropdown-filter[@label='Lab']/div[@class='dropdown']/button";
        String menu = "//dropdown-filter[@label='Lab']/div[@class='dropdown open']/ul[@class='dropdown-menu']";
        String itemToClick = menu + "/li/a[text()='" + miraLab.text + "']";
        String selectedLab = dropdown + "/span";

        assertTrue (click (dropdown));
        assertTrue (click (itemToClick));
        assertEquals (getText (selectedLab), miraLab.text);
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
