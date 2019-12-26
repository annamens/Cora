package com.adaptivebiotech.test.cora.smoke;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.adaptivebiotech.common.dto.Physician;

import com.adaptivebiotech.ui.cora.CoraPage;
import com.adaptivebiotech.ui.cora.order.Diagnostic;

@Test (groups = { "smoke" })
public class SmokeTestSuite extends SmokeTestBase{
	 private CoraPage     main;


	    @BeforeMethod
	    public void beforeMethod () {
	        main = new CoraPage ();
	        

	    }

	    @AfterMethod
	    public void afterMethod () {
	       
	    }
	    
	    public void SaveNewDiagnosticOrder () {
	        diagnostic = new Diagnostic();
	        Physician physician = new Physician ();
	        physician.firstName = "";
	        physician.lastName = "UVT-Physician";
	        physician.accountName = "";
	        physician.providerFullName = "";
	        physician.allowInternalOrderUpload = true;
	        
	    	main.clickNewDiagnosticOrder();
	        diagnostic.selectPhysician(physician);
	        verifyPhysicianName("Matt UVT-Physician");
	        diagnostic.clickSave();
	        verifyOrderNumber("D-######");
	    }


}
