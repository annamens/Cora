/**
* Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
*/
package com.adaptivebiotech.cora.test.attachment.tdetect;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Pending;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_trial;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import java.util.List;
import java.util.UUID;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.test.attachment.AttachmentTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrderDetailTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

/**
 * @author jpatel
 *
 */
@Test (groups = { "tDetect", "regression", "golden-retriever" })
public class PreviewAttachmentTestSuite extends AttachmentTestBase {

    private Login              login              = new Login ();
    private OrdersList         ordersList         = new OrdersList ();
    private NewOrderTDetect    newOrderTDetect    = new NewOrderTDetect ();
    private OrderDetailTDetect orderDetailTDetect = new OrderDetailTDetect ();
    private NewShipment        shipment           = new NewShipment ();
    private Accession          accession          = new Accession ();

    /**
     * NOTE: SR-T4205
     * 
     * @sdlc.requirements SR-11381, SR-9398
     */
    public void previewOrderShipmentAttachment () {
        login.doLogin ();
        ordersList.isCorrectPage ();

        Order order = newOrderTDetect.createTDetectOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                          newTrialProtocolPatient (),
                                                          icdCodes,
                                                          COVID19_DX_IVD,
                                                          bloodSpecimen ());
        testLog ("Order created: " + order.orderNumber);

        shipment.createShipment (order.orderNumber, Tube);
        shipment.clickShipmentTab ();
        UUID shipmentId = shipment.getShipmentId ();
        shipment.uploadAttachments (uploadPreviewFiles);

        for (String file : previewFiles) {
            shipment.clickFilePreviewLink (file);
            shipment.closeFilePreview ();
        }
        testLog ("Shipment attachments can be previewed");

        newOrderTDetect.gotoOrderEntry (order.id);
        newOrderTDetect.uploadAttachments (uploadPreviewFiles);
        newOrderTDetect.gotoOrderEntry (order.id);

        previewFilesPendingOrder ("Orders", previewFiles);
        validateAttachments (newOrderTDetect.getCoraAttachments (), previewFiles, Pending);

        previewFilesPendingOrder ("Shipments", previewFiles);
        validateAttachments (newOrderTDetect.getShipmentAttachments (), previewFiles, Pending);

        accession.gotoAccession (shipmentId);
        accession.completeAccession ();
        newOrderTDetect.activateOrder ();
        orderDetailTDetect.gotoOrderDetailsPage (order.id);

        previewFilesActiveOrder ("Orders", previewFiles);
        validateAttachments (orderDetailTDetect.getCoraAttachments (), previewFiles, Active);

        previewFilesActiveOrder ("Shipments", previewFiles);
        validateAttachments (orderDetailTDetect.getShipmentAttachments (), previewFiles, Active);

    }

    private void previewFilesPendingOrder (String attachmentSection, List <String> previewFiles) {
        for (String file : previewFiles) {
            newOrderTDetect.clickFilePreviewLink (attachmentSection, file);
            newOrderTDetect.closeFilePreview ();
        }
    }

    private void previewFilesActiveOrder (String attachmentSection, List <String> previewFiles) {
        for (String file : previewFiles) {
            orderDetailTDetect.clickFilePreviewLink (attachmentSection, file);
            orderDetailTDetect.closeFilePreview ();
        }
    }

}
