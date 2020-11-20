package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import java.util.List;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Containers {

    public List <Container> list;

    public Containers () {}

    public Containers (List <Container> list) {
        this.list = list;
    }

    public Container findContainerByNumber (Container container) {
        return list.parallelStream ()
                   .filter (c -> c.containerNumber.equals (container.containerNumber)).findAny ().orElse (null);
    }

    @Override
    public String toString () {
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public static final class Container {

        public String           id;
        public String           containerNumber;
        public ContainerType    containerType;
        public String           contents;
        public String           location;
        public String           name;
        public String           externalId;
        public String           arrivalDate;
        public String           orderId;
        public Boolean          isActive;
        public Boolean          contentsLocked;
        public Boolean          depleted;
        public Boolean          usesBarcodeAsId;
        public String           barcode;
        public String           specimenId;
        public String           specimenName;
        public String           integrity;
        public String           comment;
        public Container        root;
        public Integer          capacity;
        public String           shipmentNumber;
        public List <Container> children;

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
