package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.PageHelper.Assay.getAssay;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static java.util.stream.Collectors.toList;
import java.util.ArrayList;
import java.util.List;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.utils.PageHelper.OrderType;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
import com.adaptivebiotech.test.utils.PageHelper.DeliveryType;
import com.adaptivebiotech.test.utils.PageHelper.OrderCategory;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Orders {

    public List <Order> list = new ArrayList <> ();

    public Orders () {}

    public Orders (List <Order> list) {
        this.list = list;
    }

    public Order findOrderByNumber (String number) {
        return list.parallelStream ().filter (o -> o.order_number.equals (number)).findAny ().get ();
    }

    public Orders findOriginals () {
        return new Orders (
                list.parallelStream ().filter (o -> !o.order_number.matches (".+-.{1}?$"))
                    .collect (toList ()));
    }

    public List <Order> findTestOrders () {
        return list.parallelStream ()
                   .filter (o -> o.patient != null && o.patient.firstName != null && o.patient.lastName != null)
                   .collect (toList ());
    }

    @Override
    public String toString () {
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public static final class Order {

        public String           id;
        public String           category_id;
        public String           name;
        public OrderStatus      status;
        public String           salesforceOrderId;
        public String           salesforceOrderNumber;
        public Object           mrn;
        public OrderProperties  properties;
        public String           version;
        public String           patient_snapshot;
        public String           manifest_file_name;
        public String           data_analysis_group;
        public String           trf_file_name;
        public String           order_number;
        public String           calibrated_receptors;
        public String           date_signed;
        public String           project_id;
        public String           orderEntryType;
        public Boolean          isTrfAttached;
        public String           customerInstructions;
        public Physician        physician;
        public Patient          patient;
        public List <String>    icdcodes;
        public Specimen         specimenDto;
        public String           reportDate;
        public String           expectedTestType;
        public List <OrderTest> tests = new ArrayList <> ();
        public List <String>    doraAttachments;
        public List <String>    orderAttachments;
        public List <String>    shipmentAttachments;
        public String           notes;
        public Alert            alert;
        public Workflow         workflow;
        public OrderType        orderType;
        public Boolean          postToImmunoSEQ;
        public ChargeType       billingType;
        public DeliveryType     specimenDeliveryType;

        @Override
        public String toString () {
            try {
                return mapper.writeValueAsString (this);
            } catch (Exception e) {
                throw new RuntimeException (e);
            }
        }
    }

    public static final class OrderTest {

        public OrderTest () {}

        public OrderTest (String testId, Assay assay, boolean selected) {
            this.testId = testId;
            this.assay = assay;
            this.selected = selected;
        }

        public OrderTest (String testId, String tsvPath) {
            this.testId = testId;
            this.tsvPath = tsvPath;
        }

        public OrderTest (String testId) {
            this.testId = testId;
        }

        public String         id;
        public String         key;
        public String         orderId;
        public String         orderName;
        public String         orderNumber;
        public String         displayOrderNumber;
        public String         testCode;
        public String         testName;
        public String         testId;
        public String         tsvPath;
        public Assay          assay;
        public Boolean        selected;
        public String         name;
        public String         workflowName;
        public String         workflowId;
        public OrderCategory  category;
        public String         customerName;
        public OrderStatus    status;
        public StageName      stage;
        public StageStatus    stageStatus;
        public StageSubstatus subStatusCode;
        public String         subStatusMessage;
        public String         dueDate;
        public String         drilldownUrl;
        public String         specimenNumber;
        public String         patient_code;
        public String         regulationLevel;
        public String         lastActivity;
        public String         finished;
        public List <Stage>   stages;
        public Specimen       specimen;
        public Workflow       workflowProperties;
        public OrderType      diagnosticOrderType;
        public Integer        numTests;
        public Integer        percentComplete;
        public String         salesforceOrderNumber;

        public Assay findAssay () {
            return assay == null ? name == null ? null : getAssay (name) : assay;
        }

        @Override
        public String toString () {
            try {
                return mapper.writeValueAsString (this);
            } catch (Exception e) {
                throw new RuntimeException (e);
            }
        }
    }

    public static final class OrderProperties {

        public ChargeType   BillingType;
        public DeliveryType SpecimenDeliveryType;
        public String       Icd10Codes;

        public OrderProperties () {}

        public OrderProperties (ChargeType BillingType, DeliveryType SpecimenDeliveryType) {
            this.BillingType = BillingType;
            this.SpecimenDeliveryType = SpecimenDeliveryType;
        }

        @Override
        public String toString () {
            try {
                return mapper.writeValueAsString (this);
            } catch (Exception e) {
                throw new RuntimeException (e);
            }
        }
    }

    public static final class Alert {

        public String           href;
        public String           linkText;
        public Order            order;
        public Physician        physician;
        public Patient          patient;
        public Specimen         specimen;
        public List <OrderTest> tests = new ArrayList <> ();
        public String           alertTypeId;
        public String           referencedEntityId;
        public List <String>    recipients;

        public Alert () {}

        public Alert (String href) {
            this.href = href;
        }

        @Override
        public String toString () {
            try {
                return mapper.writeValueAsString (this);
            } catch (Exception e) {
                throw new RuntimeException (e);
            }
        }
    }
}
