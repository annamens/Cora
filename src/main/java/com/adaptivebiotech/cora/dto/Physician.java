package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import static java.lang.String.format;
import java.time.LocalDateTime;
import org.apache.commons.lang3.builder.EqualsBuilder;
import com.adaptivebiotech.cora.dto.Diagnostic.Account;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Physician {

    @JsonAlias ({ "emrId", "ID" })
    public String        id;
    public Integer       version;
    @JsonFormat (shape = JsonFormat.Shape.STRING)
    public LocalDateTime created;
    @JsonFormat (shape = JsonFormat.Shape.STRING)
    public LocalDateTime modified;
    public String        createdBy;
    public String        modifiedBy;
    public String        accountName;
    public Account       account;
    public String        firstName;
    public String        lastName;
    public String        providerFullName;
    public String        npi;
    public String        address1;
    public String        address2;
    public String        city;
    public String        state;
    public String        zip;
    public String        phone;
    public String        secureFax;
    public String        notificationEmails;
    public String        placeOrderEmails;
    public String        seeReportEmails;
    public String        manageAccessEmails;
    public String        shipmentEmails;
    public String        portal_emails;
    public Object        productFamilies;
    public String        email;
    public String        psychePhysicianCode;
    public Boolean       allowInternalOrderUpload;
    public Boolean       medicareEnrolled;
    public Boolean       needsCLEPApproval;
    public String        key;

    public Physician () {}

    public Physician (String id) {
        this.id = id;
    }

    public String displayName () {
        return format ("%s, %s%s", this.lastName, this.firstName, this.accountName);
    }

    public String shortName () {
        return format ("%s%s", this.firstName.charAt (0), this.lastName);
    }

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    public boolean equalsPhysicianName (Physician o) {
        Physician p = (Physician) o;
        return new EqualsBuilder ().append (this.lastName, p.lastName)
                                   .append (this.firstName, p.firstName)
                                   .append (this.accountName, p.accountName)
                                   .isEquals ();
    }

    public enum PhysicianType {
        clonoSEQ_client ("ClonoSEQ", "Bill My Institution", "SEA_QA ClonoSEQ Bill My Institution"),
        clonoSEQ_trial ("ClonoSEQ", "Bill per Study Protocol", "SEA_QA ClonoSEQ Bill per Study Protocol"),
        clonoSEQ_insurance ("ClonoSEQ", "Insurance", "SEA_QA ClonoSEQ Insurance"),
        clonoSEQ_medicare ("ClonoSEQ", "Medicare", "SEA_QA ClonoSEQ Medicare"),
        clonoSEQ_selfpay ("ClonoSEQ", "Patient Self-Pay", "SEA_QA ClonoSEQ Patient Self-Pay"),
        clonoSEQ_all_payments ("ClonoSEQ", "All Payments", "SEA_QA ClonoSEQ All Payments"),
        clonoSEQ_int_order_upload ("ClonoSEQ", "Internal Order Upload", "SEA_QA ClonoSEQ All Payments"),
        TDetect_client ("T-Detect", "Bill My Institution", "SEA_QA T-Detect Bill My Institution"),
        TDetect_trial ("T-Detect", "Bill per Study Protocol", "SEA_QA T-Detect Bill per Study Protocol"),
        TDetect_insurance ("T-Detect", "Insurance", "SEA_QA T-Detect Insurance"),
        TDetect_medicare ("T-Detect", "Medicare", "SEA_QA T-Detect Medicare"),
        TDetect_selfpay ("T-Detect", "Patient Self-Pay", "SEA_QA T-Detect Patient Self-Pay"),
        TDetect_all_payments ("T-Detect", "All Payments", "SEA_QA T-Detect All Payments"),
        TDetect_int_order_upload ("T-Detect", "Internal Order Upload", "SEA_QA T-Detect All Payments"),
        non_CLEP_clonoseq ("Seattle", "ClonoSEQ", "SEA_QA Test"),
        non_CLEP_tdetect_all_tests ("Seattle", "T-Detect-All", "SEA_QA Test"),
        non_CLEP_tdetect_no_tests ("Seattle", "T-Detect", "SEA_QA Test"),
        non_CLEP_lyme ("Seattle", "Lyme", "SEA_QA Test"),
        non_CLEP_covid ("Seattle", "Covid", "SEA_QA Test"),
        CLEP_clonoseq ("CLEP", "ClonoSEQ", "SEA_QA Test"),
        CLEP_tdetect ("CLEP", "T-Detect", "SEA_QA Test"),
        CLEP_lyme ("CLEP", "Lyme", "SEA_QA Test"),
        CLEP_covid ("CLEP", "Covid", "SEA_QA Test"),
        trial_clonoseq ("Trial", "ClonoSEQ", "SEA_QA Test"),
        non_trial_clonoseq ("Non-Trial", "ClonoSEQ", "SEA_QA Test"),
        big_shot ("Big", "Shot", "Test123");

        public String firstName;
        public String lastName;
        public String accountName;

        private PhysicianType (String firstName, String lastName, String accountName) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.accountName = accountName;
        }
    }
}
