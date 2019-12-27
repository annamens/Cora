package com.adaptivebiotech.test.cora.smoke;

import static org.testng.Assert.assertEquals;
import com.adaptivebiotech.test.cora.CoraBaseBrowser;
import com.adaptivebiotech.ui.cora.order.Diagnostic;



public class SmokeTestBase extends CoraBaseBrowser{
	protected Diagnostic diagnostic;
	
	protected void verifyPhysicianName(String ExpectedName) {
		 assertEquals (diagnostic.getProviderName(), ExpectedName);
	}
	
	//Verify Diagnostic Order page displays an order number, D-######, in the order header.
	protected void verifyOrderNumber(String expectedOrderNumber) {
		
		String orderNumber = diagnostic.getOrderNum();
		assertEquals(orderNumber.length(),expectedOrderNumber.length());
		assertEquals(orderNumber.substring(0,1),expectedOrderNumber.substring(0,1));
		assertEquals(orderNumber.substring(2,orderNumber.length()).matches("[0-9]+"),true);
	}

}
