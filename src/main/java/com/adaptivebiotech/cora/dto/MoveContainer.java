/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import com.adaptivebiotech.cora.dto.Containers.Container;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class MoveContainer {

    public Container container;
    public Container previous;
    public Container current;
    public Container parentContainer;
    public Container checkedOutContainer;
    public String    position;
}
