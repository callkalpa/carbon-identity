DROP TABLE IF EXISTS IDP_BASE_TABLE;
CREATE TABLE IDP_BASE_TABLE (
            NAME VARCHAR(20),
            PRIMARY KEY (NAME)
);

INSERT INTO IDP_BASE_TABLE values ('IdP');

DROP TABLE IF EXISTS SP_IDP;
DROP SEQUENCE IF EXISTS SP_IDP_PK_SEQ;
CREATE SEQUENCE SP_IDP_PK_SEQ;
CREATE TABLE SP_IDP (
            SP_IDP_ID INTEGER DEFAULT NEXTVAL('SP_IDP_PK_SEQ'),
            SP_TENANT_ID INTEGER,
            SP_IDP_NAME VARCHAR(254) NOT NULL,
            SP_IDP_PRIMARY CHAR(1) NOT NULL,
            SP_IDP_HOME_REALM_ID VARCHAR(254),
            SP_IDP_IMAGE BYTEA,
            SP_IDP_THUMBPRINT VARCHAR(2048),
            SP_IDP_TOKEN_EP_ALIAS VARCHAR(254),
            SP_IDP_PROVISION CHAR (1) NOT NULL,
            SP_IDP_PROVISION_USER_STORE_ID VARCHAR(254),
            PRIMARY KEY (SP_IDP_ID),
            CONSTRAINT CON_IDP_KEY UNIQUE (SP_TENANT_ID, SP_IDP_NAME));

DROP TABLE IF EXISTS SP_IDP_ROLES;
DROP SEQUENCE IF EXISTS SP_IDP_ROLES_PK_SEQ;
CREATE SEQUENCE SP_IDP_ROLES_PK_SEQ;
CREATE TABLE SP_IDP_ROLES (
            SP_IDP_ROLE_ID INTEGER NEXTVAL('SP_IDP_ROLES_PK_SEQ'),
            SP_IDP_ID INTEGER,
            SP_IDP_ROLE VARCHAR(254),
            PRIMARY KEY (SP_IDP_ROLE_ID),
            CONSTRAINT CON_ROLES_KEY UNIQUE (SP_IDP_ID, SP_IDP_ROLE),
            FOREIGN KEY (SP_IDP_ID) REFERENCES SP_IDP(SP_IDP_ID) ON DELETE CASCADE);

DROP TABLE IF EXISTS SP_IDP_ROLE_MAPPINGS;
DROP SEQUENCE IF EXISTS SP_IDP_ROLE_MAPPINGS_PK_SEQ;
CREATE SEQUENCE SP_IDP_ROLE_MAPPINGS_PK_SEQ;
CREATE TABLE SP_IDP_ROLE_MAPPINGS (
            SP_IDP_ROLE_MAPPING_ID INTEGER NEXTVAL('SP_IDP_ROLE_MAPPINGS_PK_SEQ'),
            SP_IDP_ROLE_ID INTEGER,
            SP_TENANT_ID INTEGER,
            SP_USER_STORE_ID VARCHAR (253),
            SP_LOCAL_ROLE VARCHAR(253), 
            PRIMARY KEY (SP_IDP_ROLE_MAPPING_ID),
            CONSTRAINT CON_ROLE_MAPPINGS_KEY UNIQUE (SP_IDP_ROLE_ID, SP_TENANT_ID, SP_USER_STORE_ID, SP_LOCAL_ROLE),
            FOREIGN KEY (SP_IDP_ROLE_ID) REFERENCES SP_IDP_ROLES(SP_IDP_ROLE_ID) ON DELETE CASCADE);

DROP TABLE IF EXISTS SP_IDP_CLAIMS;
DROP SEQUENCE IF EXISTS SP_IDP_CLAIMS_PK_SEQ;
CREATE SEQUENCE SP_IDP_CLAIMS_PK_SEQ;
CREATE TABLE SP_IDP_CLAIMS (
            SP_IDP_CLAIM_ID INTEGER NEXTVAL('SP_IDP_CLAIM_MAPPINGS_PK_SEQ'),
            SP_IDP_ID INTEGER,
            SP_IDP_CLAIM VARCHAR(254),
            SP_IDP_IS_USER_ID_CLAIM CHAR (1) NOT NULL,
            SP_IDP_IS_ROLE_CLAIM CHAR (1) NOT NULL,
            SP_IDP_PROV_USER_STORE_CLAIM CHAR (1) NOT NULL,
            PRIMARY KEY (SP_IDP_CLAIM_ID),
            CONSTRAINT CON_CLAIMS_KEY UNIQUE (SP_IDP_ID, SP_IDP_CLAIM),
            FOREIGN KEY (SP_IDP_ID) REFERENCES SP_IDP(SP_IDP_ID) ON DELETE CASCADE);

DROP TABLE IF EXISTS SP_IDP_CLAIM_MAPPINGS;
DROP SEQUENCE IF EXISTS SP_IDP_CLAIM_MAPPINGS_PK_SEQ;
CREATE SEQUENCE SP_IDP_CLAIM_MAPPINGS_PK_SEQ;
CREATE TABLE SP_IDP_CLAIM_MAPPINGS (
            SP_IDP_CLAIM_MAPPING_ID,
            SP_IDP_CLAIM_ID INTEGER NEXTVAL('SP_IDP_CLAIM_MAPPINGS_PK_SEQ'),
            SP_TENANT_ID INTEGER,           
            SP_LOCAL_CLAIM VARCHAR(253),
            PRIMARY KEY (SP_IDP_CLAIM_MAPPING_ID),
            CONSTRAINT CON_CLAIM_MAPPINGS_KEY UNIQUE (SP_IDP_CLAIM_ID, SP_TENANT_ID, SP_LOCAL_CLAIM),
            FOREIGN KEY (SP_IDP_CLAIM_ID) REFERENCES SP_IDP_CLAIMS(SP_IDP_CLAIM_ID) ON DELETE CASCADE);

DROP TABLE IF EXISTS SP_IDP_OPENID;
DROP SEQUENCE IF EXISTS SP_IDP_OPENID_PK_SEQ;
CREATE SEQUENCE SP_IDP_OPENID_PK_SEQ;
CREATE TABLE SP_IDP_OPENID (
            SP_IDP_OPENID_ID INTEGER NEXTVAL('SP_IDP_OPENID_PK_SEQ'),
            SP_IDP_ID INTEGER,              
            SP_IDP_OPENID_ENABLED CHAR(1) NOT NULL,
            SP_IDP_OPENID_DEFAULT CHAR(1) NOT NULL,
            SP_IDP_OPENID_URL VARCHAR(2048),
            SP_IDP_OPENID_UID_IN_CLAIMS CHAR (1) DEFAULT '0',
            PRIMARY KEY (SP_IDP_OPENID_ID),
            FOREIGN KEY (SP_IDP_ID) REFERENCES SP_IDP(SP_IDP_ID) ON DELETE CASCADE);

DROP TABLE IF EXISTS SP_IDP_SAMLSSO;
DROP SEQUENCE IF EXISTS SP_IDP_SAMLSSO_PK_SEQ;
CREATE SEQUENCE SP_IDP_SAMLSSO_PK_SEQ;
CREATE TABLE SP_IDP_SAMLSSO (
            SP_IDP_SAMLSSO_ID INTEGER NEXTVAL('SP_IDP_SAMLSSO_PK_SEQ'),
            SP_IDP_ID INTEGER,              
            SP_IDP_SAMLSSO_ENABLED CHAR(1)  NOT NULL,
            SP_IDP_SAMLSSO_DEFAULT CHAR(1)  NOT NULL,
            SP_IDP_IDP_ENTITY_ID VARCHAR(512),
            SP_IDP_SP_ENTITY_ID VARCHAR(512),
                    SP_IDP_SSO_URL VARCHAR(2048),
            SP_IDP_AUTHN_REQ_SIGNED CHAR(1) NOT NULL,
            SP_IDP_LOGOUT_ENABLED CHAR(1) NOT NULL,
            SP_IDP_LOGOUT_URL VARCHAR(2048),
            SP_IDP_LOGOUT_REQ_SIGNED CHAR(1) NOT NULL,
            SP_IDP_AUTHN_RES_SIGNED CHAR(1) NOT NULL,
            SP_IDP_SAMLSSO_UID_IN_CLAIMS CHAR (1) DEFAULT '0',
            PRIMARY KEY (SP_IDP_SAMLSSO_ID),
            FOREIGN KEY (SP_IDP_ID) REFERENCES SP_IDP(SP_IDP_ID) ON DELETE CASCADE);

DROP TABLE IF EXISTS SP_IDP_OIDC;
DROP SEQUENCE IF EXISTS SP_IDP_OIDC_PK_SEQ;
CREATE SEQUENCE SP_IDP_OIDC_PK_SEQ;
CREATE TABLE SP_IDP_OIDC (
            SP_IDP_OIDC_ID INTEGER NEXTVAL('SP_IDP_OIDC_PK_SEQ'),
            SP_IDP_ID INTEGER,              
            SP_IDP_OIDC_ENABLED CHAR(1) NOT NULL,
            SP_IDP_OIDC_DEFAULT CHAR(1) NOT NULL,
            SP_IDP_CONSUMER_KEY VARCHAR (512),
                    SP_IDP_CONSUMER_SECRET VARCHAR (512),
            SP_IDP_AUTHZ_URL VARCHAR(2048),
            SP_IDP_TOKEN_URL VARCHAR(2048),
            SP_IDP_IS_OIDC_UID_IN_CLAIMS CHAR (1) DEFAULT '0',
            PRIMARY KEY (SP_IDP_OIDC_ID),
            FOREIGN KEY (SP_IDP_ID) REFERENCES SP_IDP(SP_IDP_ID) ON DELETE CASCADE);

DROP TABLE IF EXISTS SP_IDP_FB_AUTH;
DROP SEQUENCE IF EXISTS SP_IDP_FB_AUTH_PK_SEQ;
CREATE SEQUENCE SP_IDP_FB_AUTH_PK_SEQ;
CREATE TABLE IF NOT EXISTS SP_IDP_FB_AUTH (
            SP_IDP_FB_AUTH_ID INTEGER NEXTVAL('SP_IDP_FB_AUTH_PK_SEQ'),
            SP_IDP_ID INTEGER,              
            SP_IDP_FB_AUTH_ENABLED CHAR(1) NOT NULL,
            SP_IDP_FB_AUTH_DEFAULT CHAR(1) NOT NULL,
            SP_IDP_FB_CLIENT_ID VARCHAR (512),
                    SP_IDP_FB_CLIENT_SECRET VARCHAR (512),
            SP_IDP_IS_FB_UID_IN_CLAIMS CHAR (1) DEFAULT '0',
            PRIMARY KEY (SP_IDP_FB_AUTH_ID),
            FOREIGN KEY (SP_IDP_ID) REFERENCES SP_IDP(SP_IDP_ID) ON DELETE CASCADE);

DROP TABLE IF EXISTS SP_IDP_PASSIVE_STS;
DROP SEQUENCE IF EXISTS SP_IDP_PASSIVE_STS_PK_SEQ;
CREATE SEQUENCE SP_IDP_PASSIVE_STS_PK_SEQ;
CREATE TABLE SP_IDP_PASSIVE_STS (
            SP_IDP_PASSIVE_STS_ID INTEGER NEXTVAL('SP_IDP_PASSIVE_STS_PK_SEQ'),
            SP_IDP_ID INTEGER,
            SP_IDP_PASSIVE_STS_ENABLED CHAR(1) NOT NULL,
            SP_IDP_PASSIVE_STS_DEFAULT CHAR(1) NOT NULL,
            SP_IDP_PASSIVE_STS_URL VARCHAR(2048),
            SP_IDP_PASSIVE_STS_REALM VARCHAR(512),
            SP_IDP_PSVE_STS_UID_IN_CLAIMS CHAR (1) DEFAULT '0',
            PRIMARY KEY (SP_IDP_PASSIVE_STS_ID),
            FOREIGN KEY (SP_IDP_ID) REFERENCES SP_IDP(SP_IDP_ID) ON DELETE CASCADE);

DROP TABLE IF EXISTS SP_IDP_PROVISIONING_CONFIGS;
DROP SEQUENCE IF EXISTS SP_IDP_PROVISIONING_CONFIGS_PK_SEQ;
CREATE SEQUENCE SP_IDP_PROVISIONING_CONFIGS_PK_SEQ;
CREATE TABLE SP_IDP_PROVISIONING_CONFIGS (
                        SP_IDP_PROV_CONFIG_ID INTEGER NEXTVAL('SP_IDP_PROVISIONING_CONFIGS_PK_SEQ'),
                        SP_IDP_ID INTEGER,
                        SP_IDP_PROV_CONNECTOR_TYPE CHAR(255) NOT NULL,
                        SP_IDP_PROV_CONFIG_KEY VARCHAR(512) NOT NULL,
                        SP_IDP_PROV_CONFIG_VALUE VARCHAR(2048),
                        SP_IDP_PROV_CONFIG_VALUE_TYPE CHAR(32) NOT NULL,
                        SP_IDP_PROV_CONFIG_IS_SECRET CHAR (1) DEFAULT '0',
                        PRIMARY KEY (SP_IDP_PROV_CONFIG_ID),
                        FOREIGN KEY (SP_IDP_ID) REFERENCES SP_IDP(SP_IDP_ID) ON DELETE CASCADE);

