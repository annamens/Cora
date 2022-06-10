/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto.emr;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;

public class CustomerData {

    public Patient   patient1;
    public Patient   patient2;
    public Patient   patient3;
    public Patient   patient4;
    public Physician provider1;
    public Physician provider2;

    @Override
    public String toString () {
        return toStringOverride (this);
    }
}
