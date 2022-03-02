package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author jpatel
 *
 */
public final class FeatureFlags {

    @JsonProperty ("Dingo-diagUploadPathologyOrder")
    public boolean dingoDiagUploadPathologyOrder;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

}
