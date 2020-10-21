package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static java.lang.String.format;
import java.time.LocalDateTime;
import com.adaptivebiotech.cora.dto.Diagnostic.Account;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Physician {

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
    public String        email;
    public String        psychePhysicianCode;
    public Boolean       allowInternalOrderUpload;
    public Boolean       medicareEnrolled;
    public Boolean       needsCLEPApproval;
    public String        password;
    public String        key;

    public Physician () {}

    public Physician (String id) {
        this.id = id;
    }

    public String displayName () {
        return format ("%s, %s -- %s", this.lastName, this.firstName, this.accountName);
    }

    public String shortName () {
        return format ("%s%s", this.firstName.charAt (0), this.lastName);
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
