/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto.emr;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Config {

    public String        id;
    public int           version;
    @JsonFormat (shape = JsonFormat.Shape.STRING)
    public LocalDateTime created;
    @JsonFormat (shape = JsonFormat.Shape.STRING)
    public LocalDateTime modified;
    public String        createdBy;
    public String        modifiedBy;
    public String        emrType;
    public String        displayName;
    public String        iss;
    public String        clientId;
    public String        clientSecret;
    public boolean       trustEmail;
    public JsonNode      users;
    public String        key;

    @Override
    public String toString () {
        return toStringOverride (this);
    }
}
