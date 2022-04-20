package com.adaptivebiotech.cora.test.billing;

import com.adaptivebiotech.cora.test.CoraBaseBrowser;

public class BillingTestBase extends CoraBaseBrowser {

    protected final String noChargeReasonQuery = "select no_charge_reason from cora.order_billing ob join cora.orders o on ob.order_id = o.id where o.order_number =";
}
