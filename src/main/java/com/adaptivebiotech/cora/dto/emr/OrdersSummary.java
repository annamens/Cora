/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto.emr;

import com.adaptivebiotech.cora.dto.Patient.PatientTestStatus;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class OrdersSummary {

    public PatientTestStatus patientTestStatus;
    public int               pendingOrders;
    public int               activeOrders;
    public int               completedOrders;
    public int               alerts;
}
