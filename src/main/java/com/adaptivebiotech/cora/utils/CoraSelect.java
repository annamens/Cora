/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.utils;

import static com.seleniumfy.test.utils.Logging.info;
import static org.testng.Assert.fail;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class CoraSelect extends Select {

    public CoraSelect (WebElement element) {
        super (element);
    }

    // seeing that sometimes the page loads and the selector is there but the text is not present
    // yet
    @Override
    public void selectByVisibleText (String text) {
        if (this.waitForDropdownText (text)) {
            info ("found text : " + text);
            super.selectByVisibleText (text);
        } else {
            fail ("cannot find text: " + text);
        }
    }

    @Override
    public WebElement getFirstSelectedOption () {
        try {
            return super.getFirstSelectedOption ();
        } catch (StaleElementReferenceException e) {
            info ("caught exception: " + e.toString ());
            return super.getFirstSelectedOption ();
        }
    }

    private boolean waitForDropdownText (String s) {
        int count = 0;
        List <String> optionTexts = getAllOptionTexts ();
        while (count < 10 && !optionTexts.contains (s)) {
            info ("waiting for text: " + s);
            count++;
            doWait (10000);
            optionTexts = getAllOptionTexts ();
        }
        return optionTexts.contains (s);
    }

    private List <String> getAllOptionTexts () {
        List <WebElement> options = this.getOptions ();
        List <String> optionTexts = new ArrayList <> ();
        for (WebElement option : options) {
            try { // sometimes it refreshes and you get a stale element reference
                optionTexts.add (option.getText ());
            } catch (StaleElementReferenceException sere) {
                info ("caught exception " + sere);
                break;
            }
        }
        return optionTexts;
    }

    private void doWait (long milliSeconds) {
        try {
            Thread.sleep (milliSeconds);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

}
