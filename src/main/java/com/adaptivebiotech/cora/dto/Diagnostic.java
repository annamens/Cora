package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static java.util.Comparator.comparing;
import java.time.LocalDateTime;
import java.util.List;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.utils.PageHelper.OrderType;
import com.adaptivebiotech.pipeline.dto.dx.ClassifierOutput;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
import com.adaptivebiotech.test.utils.PageHelper.DeliveryType;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Diagnostic {

    public Account          account;
    public Patient          patient;
    public Physician        provider;
    public Order            order;
    public Specimen         specimen;
    public Shipment         shipment;
    public Task             task;
    public Stage            fastForwardStatus;
    public Boolean          contaminated;
    public Boolean          waitForResults;
    public List <OrderTest> orderTests;
    public ClassifierOutput dxResults;

    public OrderTest findOrderTest (Assay assay) {
        return orderTests.parallelStream ()
                         .filter (ot -> assay.test.equals (ot.test.name))
                         .sorted (comparing ( (OrderTest ot) -> ot.modified).reversed ())
                         .findFirst ().get ();
    }

    @Override
    public String toString () {
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public static final class Account {

        public String        id;
        public Integer       version;
        @JsonFormat (shape = JsonFormat.Shape.STRING)
        public LocalDateTime created;
        @JsonFormat (shape = JsonFormat.Shape.STRING)
        public LocalDateTime modified;
        public String        createdBy;
        public String        modifiedBy;
        public String        parent_id;
        public Account       parent;
        public String        name;
        public String        description;
        public String        accountTypes;
        public String        billingAddress;
        public String        billingCity;
        public String        billingState;
        public String        billingZip;
        public String        billingCountry;
        public String        billingPhone;
        public String        billingEmail;
        public String        billingContact;
        public String        billingName;
        public Boolean       clinicalTrial;
        public String        key;

        public Account () {}

        public Account (String id) {
            this.id = id;
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

    public static final class Order {

        public String          name;
        public OrderStatus     status;
        public String          salesforceOrderId;
        public String          salesforceOrderNumber;
        public String          mrn;
        public Boolean         postToImmunoSEQ;
        public OrderProperties properties;
        public List <CoraTest> tests;
        public OrderType       orderType;
        public ChargeType      billingType;
        public DeliveryType    specimenDeliveryType;
        public Specimen        specimenDto;
        public List <Panel>    panels;
    }

    public static final class Task {

        public String      name;
        public String      description;
        public OrderStatus status;
        public StageName   stageName;
        public StageStatus stageStatus;
        public String      configId;
        public String      configName;
    }

    public static final class Panel {

        public String id;

        public Panel () {}

        public Panel (String id) {
            this.id = id;
        }
    }
}
