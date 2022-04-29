/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto.emr;

import java.util.Map;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class EmrConfig {

    public String     emrConfigId;
    public String     emrType;
    public String     displayName;
    public String     ISS;
    public boolean    trustEmail;
    public Map <?, ?> properties;
}
