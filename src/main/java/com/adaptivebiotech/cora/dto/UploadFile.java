package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.equalsOverride;
import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;

/**
 * @author jpatel
 *
 */
public final class UploadFile {

    public String fileName;
    public String fileNameTitle;
    public String fileUrl;
    public String reportName;
    public String createdBy;
    public String createdDateTime;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    @Override
    public boolean equals (Object o) {
        return equalsOverride (this, (UploadFile) o);
    }
}
