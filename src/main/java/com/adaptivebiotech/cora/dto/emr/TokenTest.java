/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto.emr;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;

/**
 * @author jpatel
 *
 */
public final class TokenTest {

    public String tokenId;
    public String tempTokenId;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

}
