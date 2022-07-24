/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class AlertType {

    public UUID          id;
    public int           version;
    public LocalDateTime created;
    public LocalDateTime modified;
    public String        createdBy;
    public String        modifiedBy;
    public String        name;
    public String        subjectLine;
    public String        key;
}
