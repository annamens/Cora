/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class AlertType {

    public String        id;
    public int           version;
    @JsonFormat (shape = JsonFormat.Shape.STRING)
    public LocalDateTime created;
    @JsonFormat (shape = JsonFormat.Shape.STRING)
    public LocalDateTime modified;
    public String        createdBy;
    public String        modifiedBy;
    public String        name;
    public String        subjectLine;
    public String        key;
}
