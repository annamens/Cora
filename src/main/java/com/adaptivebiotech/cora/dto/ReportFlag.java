/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;

public final class ReportFlag {

    public String name;
    public String link;

    @Override
    public String toString () {
        return toStringOverride (this);
    }
}
