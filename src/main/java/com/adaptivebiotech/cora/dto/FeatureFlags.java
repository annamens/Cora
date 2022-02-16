package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;

/**
 * @author jpatel
 *
 */
public final class FeatureFlags {

    public boolean newShipmentsListPage;
    public boolean diagPortalReminders;
    public boolean newOrderTestsListPage;
    public boolean AutoQC;
    public boolean newOrdersListPage;
    public boolean diagPortalExpeditedOrder;
    public boolean newTasksListPage;
    public boolean diagPortalDetailedOrderStatus;
    public boolean clonalityOrderAlertForMrd;
    public boolean diagPortalDynamicBilling;
    public boolean locks;
    public boolean newClonalityOrderAlert;
    public boolean IgHV;
    public boolean pathologyUpload;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

}
