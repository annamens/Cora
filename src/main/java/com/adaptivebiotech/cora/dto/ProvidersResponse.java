/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import java.util.List;
import com.adaptivebiotech.cora.dto.HttpResponse.Meta;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class ProvidersResponse {

    public Meta             meta;
    public List <Physician> objects;

    @Override
    public String toString () {
        return toStringOverride (this);
    }
}
