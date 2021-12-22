package com.adaptivebiotech.cora.dto.emr;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import java.util.List;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class TokenData {

    public String           id;
    public String           configId;
    public LaunchParams     launchParams;
    public PortalUser       portalUser;
    public Auth             auth;
    public TokenDataUser    user;
    public TokenDataPatient patient;
    public IcdCodes         icdCodes;

    @Override
    public String toString () {
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public static final class LaunchParams {

        public String email;
        public String firstName;
        public String lastName;
    }

    public static final class PortalUser {

        public String        email;
        public List <String> permissions;
        public List <String> patientIds;
    }

    public static final class Auth {

        public String userId;
        public String accessToken;
        public String refreshToken;
        public String expires;
        public String patientId;
        public String mrn;
        public String fhirUserUrl;
    }

    public static final class IcdCodes {

        public List <String> codes;
        public IcdCodeSource source;

        public static final class IcdCodeSource {

            public String              resourceType;
            public String              type;
            public int                 total;
            public List <IcdCodeLink>  link;
            public List <IcdCodeEntry> entry;

            public static final class IcdCodeLink {

                public String relation;
                public String url;
            }

            public static final class IcdCodeEntry {

                public List <IcdCodeLink> link;
                public String             fullUrl;
                public IcdCodeResource    resource;
                public IcdCodeSearch      search;

                public static final class IcdCodeResource {

                    public String          resourceType;
                    public String          id;
                    public IcdCodePatient  patient;
                    public IcdCodeAsserter asserter;
                    public String          dateRecorded;
                    public IcdCode         code;
                    public IcdCodeCategory category;
                    public String          clinicalStatus;
                    public String          verificationStatus;
                    public String          onsetDateTime;

                    public static final class IcdCodePatient {

                        public String display;
                        public String reference;
                    }

                    public static final class IcdCodeAsserter {

                        public String display;
                        public String reference;
                    }

                    public static final class IcdCode {

                        public List <IcdCoding> coding;
                        public String           text;
                    }

                    public static final class IcdCodeCategory {

                        public List <IcdCoding> coding;
                        public String           text;
                    }

                    public static final class IcdCoding {

                        public String system;
                        public String code;
                        public String display;
                    }
                }

                public static final class IcdCodeSearch {

                    public String mode;
                }
            }
        }
    }
}
