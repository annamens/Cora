/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import java.util.List;
import com.adaptivebiotech.cora.dto.Containers.Container;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class HttpResponse {

    public String           orderId;
    public String           accountId;
    public String           patientId;
    public String           specimenId;
    public String           providerId;
    public String           projectId;
    public boolean          received;
    public Errors           errors;
    public List <Container> containers;
    public List <String>    testIds;

    @Override
    public String toString () {
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
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
            try {
                return mapper.writeValueAsString (this);
            } catch (Exception e) {
                throw new RuntimeException (e);
            }
        }
    }
}
