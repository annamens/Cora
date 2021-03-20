package com.adaptivebiotech.cora.ui.mira;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.openqa.selenium.Keys.RETURN;
import static org.testng.Assert.assertTrue;
import static com.seleniumfy.test.utils.Logging.info;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Miras;
import com.adaptivebiotech.cora.dto.Miras.Mira;
import com.adaptivebiotech.cora.test.CoraEnvironment;
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
        searchForMira (miraId);
        clickMira (miraId);
    }

    public void searchAndClickMira (String miraId, MiraLab miraLab) {
        selectLab (miraLab);
        searchAndClickMira (miraId);
    }
    
    public void searchForMira (String miraId, MiraLab miraLab) {
        selectLab (miraLab);
        searchForMira (miraId);
    }
    
    public void searchForMira (String miraId) {
        String searchField = "input[type='search']";
        String firstResult = "//table[contains(@class, 'mira-table')]/tbody/tr[1]/td[1]/a/span";

        assertTrue (setText (searchField, miraId));
        assertTrue (pressKey (RETURN));
        pageLoading ();

        // wait for the search results to populate
        String firstElementText = waitAndGetText (firstResult);
        int count = 0;
        while (count < 20 && !firstElementText.equals (miraId)) {
            info ("waiting for search result");
            count++;
            doWait (10000);
            firstElementText = waitAndGetText (firstResult);
        }
        assertEquals (waitAndGetText (firstResult), miraId);
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

    public void clickSelect () {
        String selectButton = "//button[text()='Select']";
        String selectAllCheckbox = "//table[contains(@class,'mira-table')]/thead/tr/th/input[contains(@type, 'checkbox')]";
        assertTrue (click (selectButton));
        assertTrue (waitUntilVisible (selectAllCheckbox));
    }

    public void selectMiraInList (String miraId) {
        String miraCheckBox = "//td[contains(@class, 'mira-name-description')]/a/span[text()='%s']/../../../td[1]/input[contains(@type, 'checkbox')]";
        assertTrue (click (String.format (miraCheckBox, miraId)));
    }

    public void clickMira (String miraId) {
        String miraLink = "//td[contains(@class, 'mira-name-description')]/a/span[text()='%s']";
        assertTrue (click (String.format (miraLink, miraId)));
        pageLoading ();
        assertTrue (waitUntilVisible (".mira-header"));
    }

    public String clickCreateSampleManifest () {
        String createSampleManifestButton = "//button[text()='Create Sample Manifest']";
        assertTrue (click (createSampleManifestButton));
        assertTrue (waitUntilVisible (".mira-manifest-dialog"));
        assertTrue (click ("//button[text()='Yes, Create Sample Manifest']"));
        pageLoading ();
        if (!CoraEnvironment.useSauceLabs) {
            return getDownloadedSampleManifestName ();
        }
        return "Can't verify file download on saucelabs";
        
    }

    private String getDownloadedSampleManifestName () {
        info ("downloads dir is: " + getDownloadsDir ());
        File downloadDir = new File (getDownloadsDir ());
        String filenameMatch = "Adaptive-AMPL-P01-\\d+.xlsx";

        File[] downloadedFiles = listMatchingFiles (downloadDir, filenameMatch);
        int count = 0;
        while (count < 10 && downloadedFiles == null) {
            info ("waiting for sample manifest to download");
            count++;
            doWait (10000);
            downloadedFiles = listMatchingFiles (downloadDir, filenameMatch);
        }
        assertNotNull (downloadedFiles);
        Arrays.sort (downloadedFiles, Comparator.comparingLong (File::lastModified).reversed ());
        File latestDownload = downloadedFiles[0];
        assertNotNull (latestDownload);
        return latestDownload.getName ();
    }

    private File[] listMatchingFiles (File dir, String filenameMatch) {
        return dir.listFiles ((File f) -> f.getName ().matches (filenameMatch));
    }
    
    private String getMiraGuid (String href) {
        return href.replaceFirst (".*mira/details/", "");
    }
    
    // avoid stale element reference
    private String waitAndGetText (String by) {
        try {
            return waitForElement (by).getText ();
        } catch (StaleElementReferenceException e) {
            info (e.getMessage ());
            return waitForElement (by).getText ();
        }
       
    }
}
