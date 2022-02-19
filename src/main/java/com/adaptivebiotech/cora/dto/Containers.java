package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import static java.util.EnumSet.allOf;
import java.io.Serializable;
import java.util.List;

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
        return toStringOverride (this);
    }

    public static final class Container implements Serializable {

        private static final long serialVersionUID = 1L;
        public String             id;
        public String             containerNumber;
        public ContainerType      containerType;
        public String             contents;
        public String             location;
        public String             name;
        public String             externalId;
        public String             arrivalDate;
        public String             orderId;
        public Boolean            isActive;
        public Boolean            contentsLocked;
        public Boolean            depleted;
        public Boolean            usesBarcodeAsId;
        public String             barcode;
        public String             specimenId;
        public String             specimenName;
        public String             integrity;
        public String             comment;
        public Container          root;
        public Integer            capacity;
        public String             shipmentNumber;
        public List <Container>   children;

        @Override
        public String toString () {
            return toStringOverride (this);
        }
    }

    public enum ContainerType {
        Tube ("Tube (<2ml)", "tube", "Tube", "Tube (<2ml)", false),
        MatrixTube5ml ("Matrix tube (5ml)", "matrix46", "MatrixTube5ml", "Matrix tube (5ml)", false),
        MatrixTube ("Matrix tube", "matrix", "MatrixTube", "Matrix tube", false),
        OtherTube ("Other - tube", "othertube", "OtherTube", "Other - tube", false),
        Vacutainer ("Vacutainer", "vacutainer", "Vacutainer", "Vacutainer", false),
        Conical ("Conical", "conical", "Conical", "Conical", false),
        ConicalBox6x6 ("Conical Box (6x6)", "conical", "ConicalBox", "Conical box", true),
        Slide ("Slide w/o coverslip", "slidebox", "Slide", "Slide", false),
        SlideWithCoverslip ("Slide w/coverslip", "slideboxcs", "SlideWithCoverslip", "Slide w/ coverslip", false),
        Plate ("96 well plate", "plate", "Plate", "96 well plate", true),
        TubeBox5x5 ("Tube box (5x5)", "tube", "TubeBox", "Tube box", true),
        TubeBox5x10 ("Tube box (5x10)", "tube", "TubeBox", "Tube box", true),
        TubeBox9x9 ("Tube box (9x9)", "tube", "TubeBox", "Tube box", true),
        TubeBox10x10 ("Tube box (10x10)", "tube", "TubeBox", "Tube box", true),
        MatrixRack4x6 ("Matrix rack (4x6)", "matrix46", "MatrixRack4x6", "Matrix rack (4x6)", true),
        MatrixRack ("Matrix rack (8x12)", "matrix", "MatrixRack", "Matrix rack (8x12)", true),
        VacutainerBox7x7 ("Vacutainer Box (7x7)", "vacutainer", "VacutainerBox", "Vacutainer box", true),
        SlideBox5 ("5-Slide box w/o coverslips", "slidebox", "SlideBox5", "5-Slide box", true),
        SlideBox5CS ("5-Slide box w/coverslips", "slideboxcs", "SlideBox5", "5-Slide box", true),
        SlideBox25 ("25-Slide box w/o coverslips", "slidebox", "SlideBox25", "25-Slide box", true),
        SlideBox25CS ("25-Slide box w/coverslips", "slideboxcs", "SlideBox25", "25-Slide box", true),
        SlideBox100 ("100-Slide box w/o coverslips", "slidebox", "SlideBox100", "100-Slide box", true),
        SlideBox100CS ("100-Slide box w/coverslips", "slideboxcs", "SlideBox100", "100-Slide box", true),
        SlideTube ("Slide tube w/o coverslips", "slidebox", "SlideTube", "Slide Tube", true),
        SlideTubeCS ("Slide tube w/coverslips", "slideboxcs", "SlideTube", "Slide Tube", true),
        OtherSlideBox ("Other - slide box w/o coverslips", "slidebox", "OtherSlideBox", "Other - slide box", true),
        OtherSlideBoxCS ("Other - slide box w/coverslips", "slideboxcs", "OtherSlideBox", "Other - slide box", true),
        Freezer ("Freezer", "freezer", "Freezer", "Freezer", true);

        public String  label;
        public String  type;
        public String  typeError;
        public String  displayText;
        public boolean isHolding;

        private ContainerType (String label, String type, String typeError, String displayText, boolean isHolding) {
            this.label = label;
            this.type = type;
            this.typeError = typeError;
            this.displayText = displayText;
            this.isHolding = isHolding;
        }

        public static ContainerType getContainerType (String label) {
            return allOf (ContainerType.class).parallelStream ().filter (ct -> ct.label.equals (label)).findAny ()
                                              .get ();
        }
    }
}
