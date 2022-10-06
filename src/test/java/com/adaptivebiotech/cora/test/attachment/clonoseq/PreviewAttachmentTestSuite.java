/**
* Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
*/
package com.adaptivebiotech.cora.test.attachment.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
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
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

/**
 * @author jpatel
 *
 */
@Test (groups = { "clonoSeq", "regression", "golden-retriever" })
public class PreviewAttachmentTestSuite extends AttachmentTestBase {

    private Login               login               = new Login ();
    private OrdersList          ordersList          = new OrdersList ();
    private NewOrderClonoSeq    newOrderClonoSeq    = new NewOrderClonoSeq ();
    private OrderDetailClonoSeq orderDetailClonoSeq = new OrderDetailClonoSeq ();
    private NewShipment         shipment            = new NewShipment ();
    private Accession           accession           = new Accession ();

    /**
     * NOTE: SR-T4205
     * 
     * @sdlc.requirements SR-11381, SR-9398
     */
    @Test (groups = "irish-wolfhound")
    public void previewOrderShipmentAttachment () {
        login.doLogin ();
        ordersList.isCorrectPage ();

        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            ID_BCell2_CLIA,
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

        newOrderClonoSeq.gotoOrderEntry (order.id);
        newOrderClonoSeq.uploadAttachments (uploadPreviewFiles);

        previewFilesPendingOrder ("Orders", previewFiles);
        validateAttachments (newOrderClonoSeq.getCoraAttachments (), previewFiles, Pending);

        previewFilesPendingOrder ("Shipments", previewFiles);
        validateAttachments (newOrderClonoSeq.getShipmentAttachments (), previewFiles, Pending);

        accession.gotoAccession (shipmentId);
        accession.completeAccession ();
        newOrderClonoSeq.activateOrder ();
        orderDetailClonoSeq.gotoOrderDetailsPage (order.id);

        previewFilesActiveOrder ("Orders", previewFiles);
        validateAttachments (orderDetailClonoSeq.getCoraAttachments (), previewFiles, Active);

        previewFilesActiveOrder ("Shipments", previewFiles);
        validateAttachments (orderDetailClonoSeq.getShipmentAttachments (), previewFiles, Active);
    }

    private void previewFilesPendingOrder (String attachmentSection, List <String> previewFiles) {
        for (String file : previewFiles) {
            newOrderClonoSeq.clickFilePreviewLink (attachmentSection, file);
            newOrderClonoSeq.closeFilePreview ();
        }
    }

    private void previewFilesActiveOrder (String attachmentSection, List <String> previewFiles) {
        for (String file : previewFiles) {
            orderDetailClonoSeq.clickFilePreviewLink (attachmentSection, file);
            orderDetailClonoSeq.closeFilePreview ();
        }
    }
}
