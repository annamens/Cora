package com.adaptivebiotech.cora.ui.container;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.dto.ContainerHistory;
import com.adaptivebiotech.ui.cora.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class History extends CoraPage {

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".navbar"));
        assertTrue (waitUntilVisible ("[role='tablist']"));
        assertTrue (isTextInElement ("[role='tablist'] .active", "History"));
    }

    public List <ContainerHistory> getHistories () {
        return waitForElements ("[data-ng-repeat*='containerHistoryTrail']").stream ().map (el -> {
            ContainerHistory ch = new ContainerHistory ();
            ch.activityDate = getText (el, "td:nth-child(1)");
            String[] activities = getText (el, "td:nth-child(2)").split ("\n");
            ch.activity = activities[0];
            ch.comment = (activities.length > 1) ? activities[1] : null;
            ch.location = getText (el, "td:nth-child(3)");
            ch.activityBy = getText (el, "td:nth-child(4)");
            return ch;
        }).collect (toList ());
    }

    public List <String> getDetailHistory () {
        return getTextList (".container-details .ab-panel:nth-child(2) li");
    }
}
