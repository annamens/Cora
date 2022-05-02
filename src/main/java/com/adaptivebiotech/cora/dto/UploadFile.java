/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.equalsOverride;
import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author jpatel
 *
 */
public final class UploadFile {

    public String        fileName;
    public String        fileNameTitle;
    public String        fileUrl;
    public String        reportName;
    public String        createdBy;
    @JsonFormat (shape = JsonFormat.Shape.STRING)
    public LocalDateTime createdDateTime;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    @Override
    public boolean equals (Object o) {
        return equalsOverride (this, (UploadFile) o);
    }
}
