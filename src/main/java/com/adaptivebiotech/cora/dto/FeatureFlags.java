package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author jpatel
 *
 */
public final class FeatureFlags {

    public boolean newShipmentsListPage;
    public boolean diagPortalReminders;
    public boolean newOrderTestsListPage;
    @JsonProperty ("AutoQC")
    public boolean autoQC;
    public boolean newOrdersListPage;
    public boolean diagPortalExpeditedOrder;
    public boolean newTasksListPage;
    public boolean diagPortalDetailedOrderStatus;
    public boolean clonalityOrderAlertForMrd;
    public boolean diagPortalDynamicBilling;
    public boolean locks;
    public boolean newClonalityOrderAlert;
    @JsonProperty ("IgHV")
    public boolean igHV;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

}
