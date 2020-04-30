package com.adaptivebiotech.test.cora.shipment;

import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentList;
import com.adaptivebiotech.test.cora.order.OrderTestBase;
import com.adaptivebiotech.ui.cora.order.Diagnostic;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static com.seleniumfy.test.utils.Environment.webdriverWait;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
import static org.testng.Assert.assertTrue;

@Test (groups = { "regression" })
public class DoubleClickSaveTest extends OrderTestBase {

    private Diagnostic diagnostic;

    public void doubleClickSave () {
        diagnostic = new Diagnostic ();
        diagnostic.clickNewDiagnosticOrder ();
        diagnostic.isCorrectPage ();
        diagnostic.selectPhysician (physicianTRF);
        diagnostic.enterPatientICD_Codes (icdCode);
        diagnostic.clickSave();
        String orderNum = diagnostic.getOrderNum();

        Shipment shipment = new Shipment ();
        shipment.clickNewDiagnosticShipment ();
        shipment.isCorrectPage ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (orderNum);
        shipment.enterDiagnosticSpecimenContainerType (Tube);

        By saveButtonBy = By.cssSelector("[data-ng-click*='shipment-save']");
        WebElement saveButton = shipment.waitForElementVisible(saveButtonBy);
        saveButton.click();
        saveButton.click();
        ShipmentList shipmentList = new ShipmentList();
        shipmentList.goToShipments();
        Set<String> orders = new HashSet<>();
        shipmentList.getAllShipments().forEach(s -> {
            Assert.assertFalse(orders.contains(s.link));
            orders.add(s.link);
        });
    }
}
