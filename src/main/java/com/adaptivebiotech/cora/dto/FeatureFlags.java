/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author jpatel
 *
 */
public final class FeatureFlags {

    public boolean cfDNA;
    @JsonProperty ("Havanese-SpecimenActivation")
    public boolean specimenActivation;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

}
