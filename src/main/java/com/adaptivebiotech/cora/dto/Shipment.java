/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.toList;
import java.util.List;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Orders.OrderCategory;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Shipment {

    public String            id;
    public String            shipmentNumber;
    public String            link;
    public OrderCategory     category;
    public String            status;
    public Object            arrivalDate;
    public ShippingCondition condition;
    public String            carrier;
    public String            trackingNumber;
    public String            expectedRecordType;
    public List <Container>  containers;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    public enum ShippingCondition {
        Ambient ("Ambient"), WetIce ("Wet ice"), DryIce ("Dry ice"), Refrigerated ("Refrigerated");

        public String label;

        private ShippingCondition (String label) {
            this.label = label;
        }

        public static List <String> getAllShippingConditions () {
            return allOf (ShippingCondition.class).stream ().map (e -> e.label).collect (toList ());
        }
    }

    public enum LimsProjectType {
        PreferredCustomer ("Preferred Customer"),
        Grant ("Grant"),
        RnDScienceProject ("R&amp;D Science Project"),
        FeeForServiceCustomer ("Fee for Service Customer"),
        BetaKitTest ("Beta Kit Test"),
        RnDAssayDevelopment ("R&D Assay Development"),
        Collaboration ("Collaboration"),
        PharmaceuticalBiotech ("Pharmaceutical/Biotech"),
        Testing ("Testing");

        public String label;

        private LimsProjectType (String label) {
            this.label = label;
        }
    }
}
