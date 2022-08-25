/**
* Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
*/
package com.adaptivebiotech.cora.test.specimen;

import com.adaptivebiotech.cora.test.CoraBaseBrowser;

/**
 * @author jpatel
 *
 */
public class SpecimenTestBase extends CoraBaseBrowser {

    protected final String   validateToastErrorMsg  = "Please fix errors in the form";
    protected final String   collectionDateErrorMsg = "Please enter a valid date";
    protected final String   validateSuccessMsg     = "order saved";
    protected final String   trackingNumber         = "12345678";
    protected final String[] icdCodes               = { "B64" };
}
