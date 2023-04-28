package com.seleniumfy.utils;

import static org.testng.xml.XmlSuite.FailurePolicy.CONTINUE;
import static org.testng.xml.XmlSuite.FailurePolicy.SKIP;
import java.util.List;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

/**
 * @author Annameni Srinivas
 *         <a href="mailto:sannameni@gmail.com">sannameni@gmail.com</a>
 */
public class ConfigureFailurePolicyListener implements IAlterSuiteListener {

    @Override
    public void alter (List <XmlSuite> suites) {
        for (XmlSuite suite : suites) {
            if (suite.getConfigFailurePolicy ().equals (SKIP))
                suite.setConfigFailurePolicy (CONTINUE);
        }
    }

}
