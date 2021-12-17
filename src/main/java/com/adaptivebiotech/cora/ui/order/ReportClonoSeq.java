package com.adaptivebiotech.cora.ui.order;

/**
 * @author jpatel
 *
 */
public class ReportClonoSeq extends Report {

    private final String btnCLIAIGHV = "//li//div[text()='CLIA-IGHV']";

    public boolean isCLIAIGHVBtnVisible () {
        return isElementVisible (btnCLIAIGHV);
    }
}
