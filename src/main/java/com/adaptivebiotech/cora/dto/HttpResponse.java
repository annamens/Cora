/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import java.util.List;
import java.util.UUID;
import com.adaptivebiotech.cora.dto.Containers.Container;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class HttpResponse {

    public UUID             orderId;
    public UUID             accountId;
    public UUID             patientId;
    public UUID             specimenId;
    public UUID             providerId;
    public UUID             projectId;
    public boolean          received;
    public Errors           errors;
    public List <Container> containers;
    public List <String>    testIds;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    public static final class Errors {

        public List <String> error;
    }

    public static final class Meta {

        public int numTotal;
        public int numReturned;
        public int limit;
        public int offset;

        @Override
        public String toString () {
            return toStringOverride (this);
        }
    }
}
