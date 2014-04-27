/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.oauth.config;

import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.tokenprocessor.PlainTextPersistenceProcessor;
import org.wso2.carbon.identity.oauth.tokenprocessor.TokenPersistenceProcessor;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.handlers.ResponseTypeHandler;
import org.wso2.carbon.identity.oauth2.token.handlers.clientauth.ClientAuthenticationHandler;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AuthorizationGrantHandler;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.saml.SAML2TokenCallbackHandler;
import org.wso2.carbon.identity.oauth2.validators.OAuth2ScopeValidator;
import org.wso2.carbon.identity.oauth2.validators.OAuth2TokenValidator;
import org.wso2.carbon.identity.oauth2.validators.TokenValidationHandler;
import org.wso2.carbon.identity.openidconnect.CustomClaimsCallbackHandler;
import org.wso2.carbon.identity.openidconnect.IDTokenBuilder;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Runtime representation of the OAuth Configuration as configured through
 * identity.xml
 */
public class OAuthServerConfiguration {

	private static Log log = LogFactory.getLog(OAuthServerConfiguration.class);

	private static final String CONFIG_ELEM_OAUTH = "OAuth";

	/**
	 * Localpart names for the OAuth configuration in identity.xml.
	 */
	private class ConfigElements {

		// Callback handler related configuration elements
		private static final String OAUTH_CALLBACK_HANDLERS = "OAuthCallbackHandlers";
		private static final String OAUTH_CALLBACK_HANDLER = "OAuthCallbackHandler";
		private static final String CALLBACK_CLASS = "Class";
		private static final String CALLBACK_PRIORITY = "Priority";
		private static final String CALLBACK_PROPERTIES = "Properties";
		private static final String CALLBACK_PROPERTY = "Property";
		private static final String CALLBACK_ATTR_NAME = "Name";
		private static final String TOKEN_VALIDATORS = "TokenValidators";
		private static final String TOKEN_VALIDATOR = "TokenValidator";
		private static final String TOKEN_TYPE_ATTR = "type";
		private static final String TOKEN_CLASS_ATTR = "class";
        private static final String SCOPE_VALIDATOR = "OAuthScopeValidator";
        private static final String SCOPE_CLASS_ATTR = "class";
        private static final String SKIP_SCOPE_ATTR = "scopesToSkip";

		// Default timestamp skew
		private static final String TIMESTAMP_SKEW = "TimestampSkew";

		// Default validity periods
		private static final String AUTHORIZATION_CODE_DEFAULT_VALIDITY_PERIOD = "AuthorizationCodeDefaultValidityPeriod";
		private static final String USER_ACCESS_TOKEN_DEFAULT_VALIDITY_PERIOD = "UserAccessTokenDefaultValidityPeriod";
		private static final String APPLICATION_ACCESS_TOKEN_VALIDATION_PERIOD = "ApplicationAccessTokenDefaultValidityPeriod";
				
		// Enable/Disable cache
		private static final String ENABLE_CACHE = "EnableOAuthCache";

		// TokenPersistenceProcessor
		private static final String TOKEN_PERSISTENCE_PROCESSOR = "TokenPersistenceProcessor";

		// Supported Grant Types
		private static final String SUPPORTED_GRANT_TYPES = "SupportedGrantTypes";
        private static final String SUPPORTED_GRANT_TYPE = "SupportedGrantType";
        private static final String GRANT_TYPE_NAME = "GrantTypeName";
        private static final String GRANT_TYPE_HANDLER_IMPL_CLASS = "GrantTypeHandlerImplClass";

        // Supported Client Authentication Methods
        private static final String CLIENT_AUTH_HANDLERS = "ClientAuthHandlers";
        private static final String CLIENT_AUTH_HANDLER_IMPL_CLASS = "ClientAuthHandlerImplClass";

		// Supported Response Types
		private static final String SUPPORTED_RESP_TYPES = "SupportedResponseTypes";
        private static final String SUPPORTED_RESP_TYPE = "SupportedResponseType";
        private static final String RESP_TYPE_NAME = "ResponseTypeName";
        private static final String RESP_TYPE_HANDLER_IMPL_CLASS = "ResponseTypeHandlerImplClass";

        // SAML2 assertion profile configurations
		private static final String SAML2_GRANT = "SAML2Grant";
		private static final String SAML2_TOKEN_HANDLER = "SAML2TokenHandler";

		// JWT Generator
		public static final String AUTHORIZATION_CONTEXT_TOKEN_GENERATION = "AuthorizationContextTokenGeneration";
		public static final String ENABLED = "Enabled";
		public static final String TOKEN_GENERATOR_IMPL_CLASS = "TokenGeneratorImplClass";
		public static final String CLAIMS_RETRIEVER_IMPL_CLASS = "ClaimsRetrieverImplClass";
		public static final String CONSUMER_DIALECT_URI = "ConsumerDialectURI";
		public static final String SIGNATURE_ALGORITHM = "SignatureAlgorithm";
		public static final String SECURITY_CONTEXT_TTL = "AuthorizationContextTTL";

		public static final String ENABLE_ASSERTIONS = "EnableAssertions";
		public static final String ENABLE_ASSERTIONS_USERNAME = "UserName";
		public static final String ENABLE_ACCESS_TOKEN_PARTITIONING = "EnableAccessTokenPartitioning";
		public static final String ACCESS_TOKEN_PARTITIONING_DOMAINS = "AccessTokenPartitioningDomains";

		// OpenIDConnect configurations
		public static final String OPENID_CONNECT = "OpenIDConnect";
		public static final String OPENID_CONNECT_IDTOKEN_BUILDER = "IDTokenBuilder";
		public static final String OPENID_CONNECT_IDTOKEN_SUB_CLAIM = "IDTokenSubjectClaim";
		public static final String OPENID_CONNECT_IDTOKEN_ISSUER_ID = "IDTokenIssuerID";
		public static final String OPENID_CONNECT_IDTOKEN_EXPIRATION = "IDTokenExpiration";
		public static final String OPENID_CONNECT_SKIP_USER_CONSENT = "SkipUserConsent";
		public static final String OPENID_CONNECT_USERINFO_ENDPOINT_CLAIM_DIALECT = "UserInfoEndpointClaimDialect";
		public static final String OPENID_CONNECT_USERINFO_ENDPOINT_CLAIM_RETRIEVER = "UserInfoEndpointClaimRetriever";
		public static final String OPENID_CONNECT_USERINFO_ENDPOINT_REQUEST_VALIDATOR = "UserInfoEndpointRequestValidator";
		public static final String OPENID_CONNECT_USERINFO_ENDPOINT_ACCESS_TOKEN_VALIDATOR = "UserInfoEndpointAccessTokenValidator";
		public static final String OPENID_CONNECT_USERINFO_ENDPOINT_RESPONSE_BUILDER = "UserInfoEndpointResponseBuilder";
        public static final String OPENID_CONNECT_IDTOKEN_CUSTOM_CLAIM_CALLBACK_HANDLER = "IDTokenCustomClaimsCallBackHandler";
        public static final String SUPPORTED_CLAIMS = "OpenIDConnectClaims";

	}

	private static OAuthServerConfiguration instance;

	private long authorizationCodeValidityPeriodInSeconds = 300;

	private long accessTokenValidityPeriodInSeconds = 3600;

	private long applicationAccessTokenValidityPeriodInSeconds = 3600;
	
	private long timeStampSkewInSeconds = 300;

    private String tokenPersistenceProcessorClassName = "org.wso2.carbon.identity.oauth.tokenprocessor.PlainTextPersistenceProcessor";

	private boolean cacheEnabled = true;

	private boolean assertionsUserNameEnabled = false;

	private boolean accessTokenPartitioningEnabled = false;

	private String accessTokenPartitioningDomains = null;

	private TokenPersistenceProcessor persistenceProcessor = null;

	private Set<OAuthCallbackHandlerMetaData> callbackHandlerMetaData = new HashSet<OAuthCallbackHandlerMetaData>();

    private Map<String,String> supportedGrantTypeClassNames = new Hashtable<String,String>();

	private Map<String,AuthorizationGrantHandler> supportedGrantTypes;

    private Map<String, String> supportedResponseTypeClassNames = new Hashtable<String,String>();

	private Map<String, ResponseTypeHandler> supportedResponseTypes;

	private String[] supportedClaims = null;

    private List<String> supportedClientAuthHandlerClassNames = new ArrayList<String>();

	private List<ClientAuthenticationHandler> supportedClientAuthHandlers;
	
	private String saml2TokenCallbackHandlerName = null;
	
	private SAML2TokenCallbackHandler saml2TokenCallbackHandler = null;

	private boolean isAuthContextTokGenEnabled = false;

	private String tokenGeneratorImplClass = "org.wso2.carbon.identity.oauth2.token.JWTTokenGenerator";

	private String claimsRetrieverImplClass = "org.wso2.carbon.identity.oauth2.token.DefaultClaimsRetriever";

	private String consumerDialectURI = "http://wso2.org/claims";

	private String signatureAlgorithm = "SHA256withRSA";

	private String authContextTTL = "15L";

	// OpenID Connect configurations
	private String openIDConnectIDTokenBuilderClassName = "org.wso2.carbon.identity.openidconnect.DefaultIDTokenBuilder";
	
	private String openIDConnectIDTokenCustomClaimsHanlderClassName = "org.wso2.carbon.identity.openidconnect.SAMLAssertionClaimsCallback";
	
	private IDTokenBuilder openIDConnectIDTokenBuilder = null;
	
	private CustomClaimsCallbackHandler openidConnectIDTokenCustomClaimsCallbackHandler = null;
	
	private String openIDConnectIDTokenIssuerIdentifier = "OIDCAuthzServer";
	
	private String openIDConnectIDTokenSubClaim = "http://wso2.org/claims/fullname";
	
	private String openIDConnectSkipUserConsent = "false";

	private String openIDConnectIDTokenExpiration = "300";

	private String openIDConnectUserInfoEndpointClaimDialect = "http://wso2.org/claims";

	private String openIDConnectUserInfoEndpointClaimRetriever = "org.wso2.carbon.identity.oauth.endpoint.user.impl.UserInfoUserStoreClaimRetriever";
	
	private String openIDConnectUserInfoEndpointRequestValidator = "org.wso2.carbon.identity.oauth.endpoint.user.impl.UserInforRequestDefaultValidator";
	
	private String openIDConnectUserInfoEndpointAccessTokenValidator = "org.wso2.carbon.identity.oauth.endpoint.user.impl.UserInfoISAccessTokenValidator";
	
	private String openIDConnectUserInfoEndpointResponseBuilder = "org.wso2.carbon.identity.oauth.endpoint.user.impl.UserInfoJSONResponseBuilder";

    private OAuth2ScopeValidator oAuth2ScopeValidator;

	private OAuthServerConfiguration() {
		buildOAuthServerConfiguration();
	}

	public static OAuthServerConfiguration getInstance() {
		CarbonUtils.checkSecurity();
		if (instance == null) {
			synchronized (OAuthServerConfiguration.class) {
				if (instance == null) {
					instance = new OAuthServerConfiguration();
				}
			}
		}
		return instance;
	}

	private void buildOAuthServerConfiguration() {
		try {

			IdentityConfigParser configParser = IdentityConfigParser.getInstance();
			OMElement oauthElem = configParser.getConfigElement(CONFIG_ELEM_OAUTH);

			if (oauthElem == null) {
				warnOnFaultyConfiguration("OAuth element is not available.");
				return;
			}

			// read callback handler configurations
			parseOAuthCallbackHandlers(oauthElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OAUTH_CALLBACK_HANDLERS)));

			// get the token validators by type
			parseTokenValidators(oauthElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.TOKEN_VALIDATORS)));

            // Get the configured scope validator
            OMElement scopeValidatorElem = oauthElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SCOPE_VALIDATOR));
            if(scopeValidatorElem != null){
                parseScopeValidator(scopeValidatorElem);
            }

			// read default timeout periods
			parseDefaultValidityPeriods(oauthElem);

			// read caching configurations
			parseCachingConfiguration(oauthElem);

			// read token persistence processor config
			parseTokenPersistenceProcessorConfig(oauthElem);

			// read supported grant types
			parseSupportedGrantTypesConfig(oauthElem);

			// read supported response types
			parseSupportedResponseTypesConfig(oauthElem);

			// read supported response types
			parseSupportedClientAuthHandlersConfig(oauthElem);

			// read SAML2 grant config
			parseSAML2GrantConfig(oauthElem);

			// read JWT generator config
			parseAuthorizationContextTokenGeneratorConfig(oauthElem);

			// read the assertions user name config
			parseEnableAssertionsUserNameConfig(oauthElem);

			// read access token partitioning config
			parseAccessTokenPartitioningConfig(oauthElem);

			// read access token partitioning domains config
			parseAccessTokenPartitioningDomainsConfig(oauthElem);

			// read openid connect configurations
			parseOpenIDConnectConfig(oauthElem);

		} catch (ServerConfigurationException e) {
			log.error("Error when reading the OAuth Configurations. "
			          + "OAuth related functionality might be affected.", e);
		}
	}

	public Set<OAuthCallbackHandlerMetaData> getCallbackHandlerMetaData() {
		return callbackHandlerMetaData;
	}

	public long getAuthorizationCodeValidityPeriodInSeconds() {
		return authorizationCodeValidityPeriodInSeconds;
	}

	public long getUserAccessTokenValidityPeriodInSeconds() {
		return accessTokenValidityPeriodInSeconds;
	}

    public long getApplicationAccessTokenValidityPeriodInSeconds() {
        return applicationAccessTokenValidityPeriodInSeconds;
    }
	
	public long getTimeStampSkewInSeconds() {
		return timeStampSkewInSeconds;
	}

	public boolean isCacheEnabled() {
		return cacheEnabled;
	}

	public Map<String,AuthorizationGrantHandler> getSupportedGrantTypes() {
        if(supportedGrantTypes == null){
            synchronized (this) {
                if(supportedGrantTypes == null){
                    supportedGrantTypes = new Hashtable<String,AuthorizationGrantHandler>();
                    for(Map.Entry<String,String> entry : supportedGrantTypeClassNames.entrySet()){
                        AuthorizationGrantHandler authzGrantHandler = null;
                        try {
                            authzGrantHandler = (AuthorizationGrantHandler)Class.forName(entry.getValue()).newInstance();
                            authzGrantHandler.init();
                        } catch (InstantiationException e) {
                            log.error("Error instantiating " + entry.getValue());
                        } catch (IllegalAccessException e) {
                            log.error("Illegal access to " + entry.getValue());
                        } catch (ClassNotFoundException e) {
                            log.error("Cannot find class: " + entry.getValue());
                        } catch (IdentityOAuth2Exception e) {
                            log.error("Error while initializing "  + entry.getValue());
                        }
                        supportedGrantTypes.put(entry.getKey(), authzGrantHandler);
                    }
                }
            }
        }
		return supportedGrantTypes;
	}

	public Map<String,ResponseTypeHandler> getSupportedResponseTypes() {
        if(supportedResponseTypes == null){
            synchronized (this) {
                if(supportedResponseTypes == null){
                    supportedResponseTypes = new Hashtable<String,ResponseTypeHandler>();
                    for(Map.Entry<String,String> entry : supportedResponseTypeClassNames.entrySet()){
                        ResponseTypeHandler responseTypeHandler = null;
                        try {
                            responseTypeHandler = (ResponseTypeHandler)Class.forName(entry.getValue()).newInstance();
                            responseTypeHandler.init();
                        } catch (InstantiationException e) {
                            log.error("Error instantiating " + entry.getValue());
                        } catch (IllegalAccessException e) {
                            log.error("Illegal access to " + entry.getValue());
                        } catch (ClassNotFoundException e) {
                            log.error("Cannot find class: " + entry.getValue());
                        } catch (IdentityOAuth2Exception e) {
                            log.error("Error while initializing "  + entry.getValue());
                        }
                        supportedResponseTypes.put(entry.getKey(), responseTypeHandler);
                    }
                }
            }
        }
        return supportedResponseTypes;
	}

	public String[] getSupportedClaims() {
		return supportedClaims;
	}

	public List<ClientAuthenticationHandler> getSupportedClientAuthHandlers() {
        if(supportedClientAuthHandlers == null){
            synchronized (this) {
                if(supportedClientAuthHandlers == null){
                    supportedClientAuthHandlers = new ArrayList<ClientAuthenticationHandler>();
                    for(String entry : supportedClientAuthHandlerClassNames){
                        ClientAuthenticationHandler clientAuthenticationHandler = null;
                        try {
                            clientAuthenticationHandler = (ClientAuthenticationHandler)Class.forName(entry).newInstance();
                            clientAuthenticationHandler.init();
                        } catch (InstantiationException e) {
                            log.error("Error instantiating " + entry);
                        } catch (IllegalAccessException e) {
                            log.error("Illegal access to " + entry);
                        } catch (ClassNotFoundException e) {
                            log.error("Cannot find class: " + entry);
                        } catch (IdentityOAuth2Exception e) {
                            log.error("Error while initializing " + entry);
                        }
                        supportedClientAuthHandlers.add(clientAuthenticationHandler);
                    }
                }
            }
        }
        return supportedClientAuthHandlers;
	}
	
	public SAML2TokenCallbackHandler getSAML2TokenCallbackHandler() {

        if (saml2TokenCallbackHandlerName == null || saml2TokenCallbackHandlerName.equals("") ) {
			return null;
		}
		if (saml2TokenCallbackHandler == null) {
			synchronized (SAML2TokenCallbackHandler.class) {
				if (saml2TokenCallbackHandler == null) {
					try {
						Class clazz =
						              Thread.currentThread().getContextClassLoader()
						                    .loadClass(saml2TokenCallbackHandlerName);
						saml2TokenCallbackHandler = (SAML2TokenCallbackHandler) clazz.newInstance();
					} catch (ClassNotFoundException e) {
						log.error("Error while instantiating the SAML2TokenCallbackHandler ", e);
					} catch (InstantiationException e) {
						log.error("Error while instantiating the SAML2TokenCallbackHandler ", e);
					} catch (IllegalAccessException e) {
						log.error("Error while instantiating the SAML2TokenCallbackHandler ", e);
					}
				}
			}
		}
		return saml2TokenCallbackHandler;
	}

	public boolean isAccessTokenPartitioningEnabled() {
		return accessTokenPartitioningEnabled;
	}

	public boolean isUserNameAssertionEnabled() {
		return assertionsUserNameEnabled;
	}

	public String getAccessTokenPartitioningDomains() {
		return accessTokenPartitioningDomains;
	}

	private QName getQNameWithIdentityNS(String localPart) {
		return new QName(IdentityConfigParser.IDENTITY_DEFAULT_NAMESPACE, localPart);
	}

	public boolean isAuthContextTokGenEnabled() {
		return isAuthContextTokGenEnabled;
	}

	public String getTokenGeneratorImplClass() {
		return tokenGeneratorImplClass;
	}

	public String getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public String getConsumerDialectURI() {
		return consumerDialectURI;
	}

	public String getClaimsRetrieverImplClass() {
		return claimsRetrieverImplClass;
	}

	public String getAuthorizationContextTTL() {
		return authContextTTL;
	}
	
	public TokenPersistenceProcessor getPersistenceProcessor() throws IdentityOAuth2Exception {
        if(persistenceProcessor == null){
            synchronized (this) {
                if(persistenceProcessor == null) {
                    try {
                        Class clazz =
                                this.getClass().getClassLoader()
                                        .loadClass(tokenPersistenceProcessorClassName);
                        persistenceProcessor = (TokenPersistenceProcessor) clazz.newInstance();

                        if (log.isDebugEnabled()) {
                            log.debug("An instance of " + tokenPersistenceProcessorClassName +
                                    " is created for OAuthServerConfiguration.");
                        }

                    } catch (Exception e) {
                        String errorMsg =
                                "Error when instantiating the TokenPersistenceProcessor : " +
                                        tokenPersistenceProcessorClassName + ". Defaulting to PlainTextPersistenceProcessor";
                        log.error(errorMsg, e);
                        persistenceProcessor = new PlainTextPersistenceProcessor();
                    }
                }
            }
        }
        return persistenceProcessor;
	}
	
	/**
	 * Return an instance of the IDToken builder 
	 * @return
	 */
	public IDTokenBuilder getOpenIDConnectIDTokenBuilder() {
		if (openIDConnectIDTokenBuilder == null) {
			synchronized (IDTokenBuilder.class) {
				if (openIDConnectIDTokenBuilder == null) {
					try {
						Class clazz =
						              Thread.currentThread().getContextClassLoader()
						                    .loadClass(openIDConnectIDTokenBuilderClassName);
						openIDConnectIDTokenBuilder = (IDTokenBuilder) clazz.newInstance();
					} catch (ClassNotFoundException e) {
						log.error("Error while instantiating the IDTokenBuilder ", e);
					} catch (InstantiationException e) {
						log.error("Error while instantiating the IDTokenBuilder ", e);
					} catch (IllegalAccessException e) {
						log.error("Error while instantiating the IDTokenBuilder ", e);
					}
				}
			}
		}
		return openIDConnectIDTokenBuilder;
	}
	
	/**
	 * Returns the custom claims builder for the IDToken
	 * @return
	 */
	public CustomClaimsCallbackHandler getOpenIDConnectCustomClaimsCallbackHandler() {
		if(openidConnectIDTokenCustomClaimsCallbackHandler == null) {
			synchronized (CustomClaimsCallbackHandler.class) {
				if (openidConnectIDTokenCustomClaimsCallbackHandler == null) {
					try {
						Class clazz =
						              Thread.currentThread().getContextClassLoader()
						                    .loadClass(openIDConnectIDTokenCustomClaimsHanlderClassName);
						openidConnectIDTokenCustomClaimsCallbackHandler = (CustomClaimsCallbackHandler) clazz.newInstance();
					} catch (ClassNotFoundException e) {
						log.error("Error while instantiating the IDTokenBuilder ", e);
					} catch (InstantiationException e) {
						log.error("Error while instantiating the IDTokenBuilder ", e);
					} catch (IllegalAccessException e) {
						log.error("Error while instantiating the IDTokenBuilder ", e);
					}
				}
			}
		}
		return openidConnectIDTokenCustomClaimsCallbackHandler;
	}

	/**
	 * @return the openIDConnectIDTokenIssuer
	 */
	public String getOpenIDConnectIDTokenIssuerIdentifier() {
		return openIDConnectIDTokenIssuerIdentifier;
	}
	
	public String getOpenIDConnectIDTokenSubjectClaim() {
		return openIDConnectIDTokenSubClaim;
	}
	
	/**
	 * Returns if skip user consent enabled or not
	 * @return
	 */
	public boolean getOpenIDConnectSkipeUserConsentConfig() {
		return "true".equalsIgnoreCase(openIDConnectSkipUserConsent);
	}

	/**
	 * @return the openIDConnectIDTokenExpiration
	 */
	public String getOpenIDConnectIDTokenExpiration() {
		return openIDConnectIDTokenExpiration;
	}

	public String getOpenIDConnectUserInfoEndpointClaimDialect() {
		return openIDConnectUserInfoEndpointClaimDialect;
	}
	
	public String getOpenIDConnectUserInfoEndpointClaimRetriever() {
		return openIDConnectUserInfoEndpointClaimRetriever;
	}
	
	public String getOpenIDConnectUserInfoEndpointRequestValidator() {
		return openIDConnectUserInfoEndpointRequestValidator;
	}
	
	public String getOpenIDConnectUserInfoEndpointAccessTokenValidator() {
		return openIDConnectUserInfoEndpointAccessTokenValidator;
	}
	
	public String getOpenIDConnectUserInfoEndpointResponseBuilder() {
		return openIDConnectUserInfoEndpointResponseBuilder;
	}

	   	
	private void parseOAuthCallbackHandlers(OMElement callbackHandlersElem) {
		if (callbackHandlersElem == null) {
			warnOnFaultyConfiguration("OAuthCallbackHandlers element is not available.");
			return;
		}

		Iterator callbackHandlers =
		                            callbackHandlersElem.getChildrenWithLocalName(ConfigElements.OAUTH_CALLBACK_HANDLER);
		int callbackHandlerCount = 0;
		if (callbackHandlers != null) {
			for (; callbackHandlers.hasNext();) {
				OAuthCallbackHandlerMetaData cbHandlerMetadata =
				                                                 buildAuthzCallbackHandlerMetadata((OMElement) callbackHandlers.next());
				if (cbHandlerMetadata != null) {
					callbackHandlerMetaData.add(cbHandlerMetadata);
					if (log.isDebugEnabled()) {
						log.debug("OAuthCallbackHandlerMetadata was added. Class : " +
						          cbHandlerMetadata.getClassName());
					}
					callbackHandlerCount++;
				}
			}
		}
		// if no callback handlers are registered, print a WARN
		if (!(callbackHandlerCount > 0)) {
			warnOnFaultyConfiguration("No OAuthCallbackHandler elements were found.");
		}
	}

	private void parseTokenValidators(OMElement tokenValidators) {
		if (tokenValidators == null) {
			return;
		}

		Iterator validators = tokenValidators.getChildrenWithLocalName(ConfigElements.TOKEN_VALIDATOR);
		if (validators != null) {
			for (; validators.hasNext();) {
				OMElement validator = ((OMElement) validators.next());
				if (validator != null) {
					OAuth2TokenValidator tokenValidator = null;
					String clazzName = null;
					try {
						clazzName =
						            validator.getAttributeValue(getQNameWithIdentityNS(ConfigElements.TOKEN_CLASS_ATTR));
						Class clazz = Thread.currentThread().getContextClassLoader().loadClass(clazzName);
						tokenValidator = (OAuth2TokenValidator) clazz.newInstance();
					} catch (ClassNotFoundException e) {
						log.error("Class not in build path " + clazzName, e);
					} catch (InstantiationException e) {
						log.error("Class initialization error " + clazzName, e);
					} catch (IllegalAccessException e) {
						log.error("Class access error " + clazzName, e);

					}
					String type =
					              validator.getAttributeValue(getQNameWithIdentityNS(ConfigElements.TOKEN_TYPE_ATTR));
					TokenValidationHandler.getInstance().addTokenValidator(type, tokenValidator);
				}
			}
		}
	}

    private void parseScopeValidator(OMElement scopeValidatorElem){

        String scopeValidatorClazz = scopeValidatorElem.getAttributeValue(new QName(ConfigElements.SCOPE_CLASS_ATTR));

        String scopesToSkipAttr = scopeValidatorElem.getAttributeValue(new QName(ConfigElements.SKIP_SCOPE_ATTR));
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(scopeValidatorClazz);
            OAuth2ScopeValidator scopeValidator = (OAuth2ScopeValidator)clazz.newInstance();
            if(scopesToSkipAttr != null && !"".equals(scopesToSkipAttr)){
                //Split the scopes attr by a -space- character and create the set (avoid duplicates).
                Set<String> scopesToSkip = new HashSet<String>(Arrays.asList(scopesToSkipAttr.split(" ")));
                scopeValidator.setScopesToSkip(scopesToSkip);
            }
            setoAuth2ScopeValidator(scopeValidator);
        } catch (ClassNotFoundException e) {
            log.error("Class not found in build path " + scopeValidatorClazz, e);
        } catch (InstantiationException e) {
            log.error("Class initialization error " + scopeValidatorClazz, e);
        } catch (IllegalAccessException e) {
            log.error("Class access error " + scopeValidatorClazz, e);
        }
    }

	private void warnOnFaultyConfiguration(String logMsg) {
		log.warn("Error in OAuth Configuration. " + logMsg);
	}

	private OAuthCallbackHandlerMetaData buildAuthzCallbackHandlerMetadata(OMElement omElement) {
		// read the class attribute which is mandatory
		String className = omElement.getAttributeValue(new QName(ConfigElements.CALLBACK_CLASS));

		if (className == null) {
			log.error("Mandatory attribute \"Class\" is not present in the "
			          + "AuthorizationCallbackHandler element. "
			          + "AuthorizationCallbackHandler will not be registered.");
			return null;
		}

		// read the priority element, if it is not there, use the default
		// priority of 1
		int priority = OAuthConstants.OAUTH_AUTHZ_CB_HANDLER_DEFAULT_PRIORITY;
		OMElement priorityElem =
		                         omElement.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CALLBACK_PRIORITY));
		if (priorityElem != null) {
			priority = Integer.parseInt(priorityElem.getText());
		}

		if (log.isDebugEnabled()) {
			log.debug("Priority level of : " + priority + " is set for the " +
			          "AuthorizationCallbackHandler with the class : " + className);
		}

		// read the additional properties.
		OMElement paramsElem =
		                       omElement.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CALLBACK_PROPERTIES));
		Properties properties = null;
		if (paramsElem != null) {
			Iterator paramItr = paramsElem.getChildrenWithLocalName(ConfigElements.CALLBACK_PROPERTY);
			properties = new Properties();
			if (log.isDebugEnabled()) {
				log.debug("Registering Properties for AuthorizationCallbackHandler class : " + className);
			}
			for (; paramItr.hasNext();) {
				OMElement paramElem = (OMElement) paramItr.next();
				String paramName = paramElem.getAttributeValue(new QName(ConfigElements.CALLBACK_ATTR_NAME));
				String paramValue = paramElem.getText();
				properties.put(paramName, paramValue);
				if (log.isDebugEnabled()) {
					log.debug("Property name : " + paramName + ", Property Value : " + paramValue);
				}
			}
		}
		return new OAuthCallbackHandlerMetaData(className, properties, priority);
	}

	private void parseDefaultValidityPeriods(OMElement oauthConfigElem) {

		// set the authorization code default timeout
		OMElement authzCodeTimeoutElem =
		                                 oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.AUTHORIZATION_CODE_DEFAULT_VALIDITY_PERIOD));

		if (authzCodeTimeoutElem != null) {
			authorizationCodeValidityPeriodInSeconds = Long.parseLong(authzCodeTimeoutElem.getText());
		}

		// set the access token default timeout
		OMElement accessTokTimeoutElem =
		                                 oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.USER_ACCESS_TOKEN_DEFAULT_VALIDITY_PERIOD));
		if (accessTokTimeoutElem != null) {
			accessTokenValidityPeriodInSeconds = Long.parseLong(accessTokTimeoutElem.getText());
		}

		// set the application access token default timeout
        OMElement applicationAccessTokTimeoutElem = oauthConfigElem.getFirstChildWithName(
                getQNameWithIdentityNS(ConfigElements.APPLICATION_ACCESS_TOKEN_VALIDATION_PERIOD));
        if (applicationAccessTokTimeoutElem != null) {
            applicationAccessTokenValidityPeriodInSeconds = Long.parseLong(applicationAccessTokTimeoutElem.getText());
        }

		OMElement timeStampSkewElem = oauthConfigElem.getFirstChildWithName(
                getQNameWithIdentityNS(ConfigElements.TIMESTAMP_SKEW));
		if (timeStampSkewElem != null) {
			timeStampSkewInSeconds = Long.parseLong(timeStampSkewElem.getText());
		}

		if (log.isDebugEnabled()) {
			if (authzCodeTimeoutElem == null) {
				log.debug("\"Authorization Code Default Timeout\" element was not available "
				          + "in identity.xml. Continuing with the default value.");
			}
			if (accessTokTimeoutElem == null) {
				log.debug("\"Access Token Default Timeout\" element was not available "
				          + "in from identity.xml. Continuing with the default value.");
			}
			if (timeStampSkewElem == null) {
				log.debug("\"Default Timestamp Skew\" element was not available "
				          + "in from identity.xml. Continuing with the default value.");
			}
			log.debug("Authorization Code Default Timeout is set to : " +
                    authorizationCodeValidityPeriodInSeconds + "ms.");
			log.debug("Access Token Default Timeout is set to " + accessTokenValidityPeriodInSeconds +
			          "ms.");
			log.debug("Application Access Token Default Timeout is set to " +
                    accessTokenValidityPeriodInSeconds + "ms.");
			log.debug("Default TimestampSkew is set to " + timeStampSkewInSeconds + "ms.");
		}
	}

	private void parseCachingConfiguration(OMElement oauthConfigElem) {
		OMElement enableCacheElem =
		                            oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.ENABLE_CACHE));
		if (enableCacheElem != null) {
			cacheEnabled = Boolean.parseBoolean(enableCacheElem.getText());
		}

		if (log.isDebugEnabled()) {
			log.debug("Enable OAuth Cache was set to : " + cacheEnabled);
		}
	}

	private void parseAccessTokenPartitioningConfig(OMElement oauthConfigElem) {
		OMElement enableAccessTokenPartitioningElem =
		                                              oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.ENABLE_ACCESS_TOKEN_PARTITIONING));
		if (enableAccessTokenPartitioningElem != null) {
			accessTokenPartitioningEnabled =
			                                 Boolean.parseBoolean(enableAccessTokenPartitioningElem.getText());
		}

		if (log.isDebugEnabled()) {
			log.debug("Enable OAuth Access Token Partitioning was set to : " + accessTokenPartitioningEnabled);
		}
	}

	private void parseAccessTokenPartitioningDomainsConfig(OMElement oauthConfigElem) {
		OMElement enableAccessTokenPartitioningElem =
		                                              oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.ACCESS_TOKEN_PARTITIONING_DOMAINS));
		if (enableAccessTokenPartitioningElem != null) {
			accessTokenPartitioningDomains = enableAccessTokenPartitioningElem.getText();
		}

		if (log.isDebugEnabled()) {
			log.debug("Enable OAuth Access Token Partitioning Domains was set to : " +
			          accessTokenPartitioningDomains);
		}
	}

	private void parseEnableAssertionsUserNameConfig(OMElement oauthConfigElem) {
		OMElement enableAssertionsElem =
		                                 oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.ENABLE_ASSERTIONS));
		if (enableAssertionsElem != null) {
			OMElement enableAssertionsUserNameElem =
			                                         enableAssertionsElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.ENABLE_ASSERTIONS_USERNAME));
			if (enableAssertionsUserNameElem != null) {
				assertionsUserNameEnabled = Boolean.parseBoolean(enableAssertionsUserNameElem.getText());
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Enable Assertions-UserName was set to : " + assertionsUserNameEnabled);
		}
	}

	private void parseTokenPersistenceProcessorConfig(OMElement oauthConfigElem) {

        OMElement persistenceprocessorConfigElem =
		                                   oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.TOKEN_PERSISTENCE_PROCESSOR));
		if (persistenceprocessorConfigElem != null && !persistenceprocessorConfigElem.getText().trim().equals("")) {
			tokenPersistenceProcessorClassName = persistenceprocessorConfigElem.getText().trim();
		}

		if (log.isDebugEnabled()) {
			log.debug("Token Persistence Processor was set to : " + tokenPersistenceProcessorClassName);
		}

	}

	private void parseSupportedGrantTypesConfig(OMElement oauthConfigElem) {
		OMElement supportedGrantTypesElem =
		                                    oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SUPPORTED_GRANT_TYPES));

		if (supportedGrantTypesElem != null) {
            Iterator<OMElement> iterator = supportedGrantTypesElem.getChildrenWithName(getQNameWithIdentityNS(ConfigElements.SUPPORTED_GRANT_TYPE));
			while(iterator.hasNext()){
                OMElement supportedGrantTypeElement = iterator.next();
                OMElement grantTypeNameElement = supportedGrantTypeElement.
                        getFirstChildWithName(
                                getQNameWithIdentityNS(ConfigElements.GRANT_TYPE_NAME));
                String grantTypeName = null;
                if(grantTypeNameElement != null){
                    grantTypeName = grantTypeNameElement.getText();
                }
                OMElement authzGrantHandlerClassNameElement =
                        supportedGrantTypeElement.getFirstChildWithName(
                                getQNameWithIdentityNS(ConfigElements.GRANT_TYPE_HANDLER_IMPL_CLASS));
                String authzGrantHandlerImplClass = null;
                if(authzGrantHandlerClassNameElement != null){
                    authzGrantHandlerImplClass = authzGrantHandlerClassNameElement.getText();
                }
                if(grantTypeName != null && !grantTypeName.equals("") &&
                        authzGrantHandlerImplClass != null && !authzGrantHandlerImplClass.equals("")){
                    supportedGrantTypeClassNames.put(grantTypeName, authzGrantHandlerImplClass);

                }
            }
		} else {
			// if this element is not present, assume the default case.
            log.warn("\'SupportedGrantTypes\' element not configured in identity.xml. " +
                    "Therefore instantiating default grant type handlers");

            Map<String,String> defaultGrantTypes = new Hashtable<String,String>(5);
            defaultGrantTypes.put(GrantType.AUTHORIZATION_CODE.toString(), "org.wso2.carbon.identity.oauth2.token.handlers.grant.AuthorizationCodeHandler");
            defaultGrantTypes.put(GrantType.CLIENT_CREDENTIALS.toString(), "org.wso2.carbon.identity.oauth2.token.handlers.grant.ClientCredentialsGrantHandler");
            defaultGrantTypes.put(GrantType.PASSWORD.toString(), "org.wso2.carbon.identity.oauth2.token.handlers.grant.PasswordGrantHandler");
            defaultGrantTypes.put(GrantType.REFRESH_TOKEN.toString(), "org.wso2.carbon.identity.oauth2.token.handlers.grant.RefreshGrantHandler");
            defaultGrantTypes.put(org.wso2.carbon.identity.oauth.common.GrantType.SAML20_BEARER.toString(), "org.wso2.carbon.identity.oauth2.token.handlers.grant.saml.SAML2BearerGrantHandler");
            defaultGrantTypes.put(org.wso2.carbon.identity.oauth.common.GrantType.IWA_NTLM.toString(),"org.wso2.carbon.identity.oauth2.token.handlers.grant.iwa.ntlm.NTLMAuthenticationGrantHandler");
            supportedGrantTypeClassNames.putAll(defaultGrantTypes);
		}
        if(log.isDebugEnabled()){
            for(Map.Entry entry : supportedGrantTypeClassNames.entrySet()){
                String grantTypeName = entry.getKey().toString();
                String authzGrantHandlerImplClass = entry.getValue().toString();
                log.debug(grantTypeName + "supported by" + authzGrantHandlerImplClass);
            }
        }
	}

	private void parseSupportedResponseTypesConfig(OMElement oauthConfigElem) {
		OMElement supportedRespTypesElem =
		                                   oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SUPPORTED_RESP_TYPES));

		if (supportedRespTypesElem != null) {
            Iterator<OMElement> iterator = supportedRespTypesElem.getChildrenWithName(getQNameWithIdentityNS(ConfigElements.SUPPORTED_RESP_TYPE));
            while(iterator.hasNext()){
                OMElement supportedResponseTypeElement = iterator.next();
                OMElement responseTypeNameElement = supportedResponseTypeElement.
                        getFirstChildWithName(
                                getQNameWithIdentityNS(ConfigElements.RESP_TYPE_NAME));
                String responseTypeName = null;
                if(responseTypeNameElement != null){
                    responseTypeName = responseTypeNameElement.getText();
                }
                OMElement responseTypeHandlerImplClassElement =
                        supportedResponseTypeElement.getFirstChildWithName(
                                getQNameWithIdentityNS(ConfigElements.RESP_TYPE_HANDLER_IMPL_CLASS));
                String responseTypeHandlerImplClass = null;
                if(responseTypeHandlerImplClassElement != null){
                    responseTypeHandlerImplClass = responseTypeHandlerImplClassElement.getText();
                }
                if(responseTypeName != null && !responseTypeName.equals("") &&
                        responseTypeHandlerImplClass != null && !responseTypeHandlerImplClass.equals("")){
                    supportedResponseTypeClassNames.put(responseTypeName, responseTypeHandlerImplClass);
                }
            }
		} else {
            // if this element is not present, assume the default case.
            log.warn("\'SupportedResponseTypes\' element not configured in identity.xml. " +
                    "Therefore instantiating default response type handlers");

            Map<String,String> defaultResponseTypes = new Hashtable<String,String>(2);
            defaultResponseTypes.put(ResponseType.CODE.toString(), "org.wso2.carbon.identity.oauth2.authz.handlers.CodeResponseTypeHandler");
            defaultResponseTypes.put(ResponseType.TOKEN.toString(), "org.wso2.carbon.identity.oauth2.authz.handlers.TokenResponseTypeHandler");
            supportedResponseTypeClassNames.putAll(defaultResponseTypes);
		}

        if(log.isDebugEnabled()){
            for(Map.Entry entry : supportedResponseTypeClassNames.entrySet()){
                String responseTypeName = entry.getKey().toString();
                String authzHandlerImplClass = entry.getValue().toString();
                log.debug(responseTypeName + "supported by" + authzHandlerImplClass);
            }
        }
	}

	private void parseSupportedClientAuthHandlersConfig(OMElement oauthConfigElem) {
		OMElement supportedClientAuthHandlersElem =
		                                           oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CLIENT_AUTH_HANDLERS));

		if (supportedClientAuthHandlersElem != null) {
            Iterator<OMElement> iterator = supportedClientAuthHandlersElem.getChildrenWithName(getQNameWithIdentityNS(ConfigElements.CLIENT_AUTH_HANDLER_IMPL_CLASS));
            while(iterator.hasNext()){
                OMElement supportedClientAuthHandler = iterator.next();
                String clientAuthHandlerImplClass = supportedClientAuthHandler.getText();
                if(clientAuthHandlerImplClass != null && !clientAuthHandlerImplClass.equals("")){
                    supportedClientAuthHandlerClassNames.add(clientAuthHandlerImplClass);
                }
            }
		} else {
			// if this element is not present, assume the default case.
            log.warn("\'SupportedClientAuthMethods\' element not configured in identity.xml. " +
                    "Therefore instantiating default client authentication handlers");

            List<String> defaultClientAuthHandlers = new ArrayList<String>(1);
            defaultClientAuthHandlers.add("org.wso2.carbon.identity.oauth2.token.handlers.clientauth.BasicAuthClientAuthHandler");
            supportedClientAuthHandlerClassNames.addAll(defaultClientAuthHandlers);
		}
        if(log.isDebugEnabled()){
            for(String className : supportedClientAuthHandlerClassNames){
                log.debug("Supported client authentication method " + className);
            }
        }
	}

	private void parseSAML2GrantConfig(OMElement oauthConfigElem) {

		OMElement saml2GrantElement =
                oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SAML2_GRANT));
        OMElement saml2TokenHandlerElement = null;
        if(saml2GrantElement != null){
            saml2TokenHandlerElement = saml2GrantElement.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SAML2_TOKEN_HANDLER));
        }
		if(saml2TokenHandlerElement != null && !saml2TokenHandlerElement.getText().trim().equals("")) {
			saml2TokenCallbackHandlerName = saml2TokenHandlerElement.getText().trim();
		}
	}

	private void parseAuthorizationContextTokenGeneratorConfig(OMElement oauthConfigElem) {
		OMElement authContextTokGenConfigElem =
		                                        oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.AUTHORIZATION_CONTEXT_TOKEN_GENERATION));
		if (authContextTokGenConfigElem != null) {
			OMElement enableJWTGenerationConfigElem =
			                                          authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.ENABLED));
			if (enableJWTGenerationConfigElem != null) {
				String enableJWTGeneration = enableJWTGenerationConfigElem.getText().trim();
				if (enableJWTGeneration != null && JavaUtils.isTrueExplicitly(enableJWTGeneration)) {
					isAuthContextTokGenEnabled = true;
					if (authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.TOKEN_GENERATOR_IMPL_CLASS)) != null) {
						tokenGeneratorImplClass =
						                          authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.TOKEN_GENERATOR_IMPL_CLASS))
						                                                     .getText().trim();
					}
					if (authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CLAIMS_RETRIEVER_IMPL_CLASS)) != null) {
						claimsRetrieverImplClass =
						                           authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CLAIMS_RETRIEVER_IMPL_CLASS))
						                                                      .getText().trim();
					}
					if (authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CONSUMER_DIALECT_URI)) != null) {
						consumerDialectURI =
						                     authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CONSUMER_DIALECT_URI))
						                                                .getText().trim();
					}
					if (authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SIGNATURE_ALGORITHM)) != null) {
						signatureAlgorithm =
						                     authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SIGNATURE_ALGORITHM))
						                                                .getText().trim();
					}
					if (authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SECURITY_CONTEXT_TTL)) != null) {
						authContextTTL =
						                 authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SECURITY_CONTEXT_TTL))
						                                            .getText().trim();
					}
				}
			}
		}
		if (log.isDebugEnabled()) {
			if (isAuthContextTokGenEnabled) {
				log.debug("JWT Generation is enabled");
			} else {
				log.debug("JWT Generation is disabled");
			}
		}
	}

	private void parseOpenIDConnectConfig(OMElement oauthConfigElem) {

		OMElement openIDConnectConfigElem =
		                                    oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT));

		if (openIDConnectConfigElem != null) {
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_BUILDER)) != null) {
				openIDConnectIDTokenBuilderClassName =
				                             openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_BUILDER))
				                                                    .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_CUSTOM_CLAIM_CALLBACK_HANDLER)) != null) {
				openIDConnectIDTokenCustomClaimsHanlderClassName =
				                             openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_CUSTOM_CLAIM_CALLBACK_HANDLER))
				                                                    .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_SUB_CLAIM)) != null) {
				openIDConnectIDTokenSubClaim =
				                             openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_SUB_CLAIM))
				                                                    .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_SKIP_USER_CONSENT)) != null) {
				openIDConnectSkipUserConsent =
				                             openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_SKIP_USER_CONSENT))
				                                                    .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_ISSUER_ID)) != null) {
				openIDConnectIDTokenIssuerIdentifier =
				                             openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_ISSUER_ID))
				                                                    .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_EXPIRATION)) != null) {
				openIDConnectIDTokenExpiration =
				                                 openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_EXPIRATION))
				                                                        .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_CLAIM_DIALECT)) != null) {
				openIDConnectUserInfoEndpointClaimDialect =
				                                            openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_CLAIM_DIALECT))
				                                                                   .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_CLAIM_RETRIEVER)) != null) {
				openIDConnectUserInfoEndpointClaimRetriever =
				                                              openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_CLAIM_RETRIEVER))
				                                                                     .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_REQUEST_VALIDATOR)) != null) {
				openIDConnectUserInfoEndpointRequestValidator =
				                                                openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_REQUEST_VALIDATOR))
				                                                                       .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_ACCESS_TOKEN_VALIDATOR)) != null) {
				openIDConnectUserInfoEndpointAccessTokenValidator =
				                                                    openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_ACCESS_TOKEN_VALIDATOR))
				                                                                           .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_RESPONSE_BUILDER)) != null) {
				openIDConnectUserInfoEndpointResponseBuilder =
				                                               openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_RESPONSE_BUILDER))
				                                                                      .getText().trim();
			}
            if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SUPPORTED_CLAIMS)) != null) {
                String supportedClaimStr =
                        openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SUPPORTED_CLAIMS))
                                .getText().trim();
                if (log.isDebugEnabled()) {
                    log.debug("Supported Claims : " + supportedClaimStr);
                }
                if(supportedClaimStr != null && !supportedClaimStr.equals("")){
                    supportedClaims = supportedClaimStr.split(",");
                }
            }
		}
	}

    public OAuth2ScopeValidator getoAuth2ScopeValidator() {
        return oAuth2ScopeValidator;
    }

    public void setoAuth2ScopeValidator(OAuth2ScopeValidator oAuth2ScopeValidator) {
        this.oAuth2ScopeValidator = oAuth2ScopeValidator;
    }

}
