package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static java.time.ZonedDateTime.parse;
import static java.util.Comparator.comparing;
import java.time.LocalDateTime;
import java.util.List;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
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
    public List <OrderTest> orderTests;
    public Specimen         specimen;
    public Shipment         shipment;
    public Task             task;
    public Stage            fastForwardStatus;

    public OrderTest findOrderTest (Assay assay) {
        return orderTests.parallelStream ()
                         .filter (ot -> ot.testName.equals (assay.test))
                         .sorted (comparing ( (OrderTest ot) -> parse (ot.lastActivity)).reversed ())
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
        public int           version;
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
        public boolean       clinicalTrial;
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

    public static final class Task {

        public String      name;
        public String      description;
        public OrderStatus status;
        public StageName   stageName;
        public StageStatus stageStatus;
        public String      configId;
        public String      configName;

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
