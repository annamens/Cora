package com.adaptivebiotech.cora.utils;

import static com.seleniumfy.test.utils.Logging.info;
import static org.testng.Assert.fail;
import java.util.ArrayList;
import java.util.List;
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
            info("found text : " + text);
            super.selectByVisibleText (text);
        } else {
            fail ("cannot find text: " + text);
        }
    }

    public boolean waitForDropdownText (String s) {
        int count = 0;
        List<String> optionTexts = getAllOptionTexts();
        while (count < 10 && !optionTexts.contains (s)) {
            info ("waiting for text: " + s);
            count++;
            doWait (10000);
            optionTexts = getAllOptionTexts();
            info("got all optionTexts, count is: " + count);
        }
        info("waitForDropdownText returning");
        return optionTexts.contains (s);
    }

    List <String> getAllOptionTexts () {
        info("getAllOptionTexts called");
        List <WebElement> options = this.getOptions ();
        info("number of options is: " + options.size ());
        List <String> optionTexts = new ArrayList <> ();
        for (WebElement option : options) {
            optionTexts.add (option.getText ());
            info (option.getText ());
        }
        info("returning " + optionTexts.size () + " optionTexts");
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
