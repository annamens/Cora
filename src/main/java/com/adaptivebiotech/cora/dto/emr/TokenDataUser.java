package com.adaptivebiotech.cora.dto.emr;

import java.util.List;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class TokenDataUser {

    public String     id;
    public String     email;
    public String     firstName;
    public String     lastName;
    public UserSource source;

    public static class UserSource {

        public String             resourceType;
        public String             id;
        public UserMeta           meta;
        public UserName           name;
        public List <UserTelecom> telecom;
        public List <UserAddress> address;
    }

    public static class UserMeta {

        public String versionId;
        public String lastUpdated;
    }

    public static class UserName {

        public List <String> family;
        public List <String> given;
    }

    public static class UserTelecom {

        public String system;
        public String value;
        public String use;
    }

    public static class UserAddress {

        public String        use;
        public List <String> line;
        public String        city;
        public String        state;
        public String        postalCode;
    }
}
