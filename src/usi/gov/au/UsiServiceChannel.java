package usi.gov.au;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.BindingProvider;

import au.gov.abr.akm.credential.store.ABRProperties;
//import com.sun.xml.internal.ws.client.BindingProviderProperties;
import com.sun.xml.ws.addressing.W3CAddressingConstants;
import com.sun.xml.ws.api.security.trust.client.STSIssuedTokenConfiguration;
import com.sun.xml.ws.api.security.trust.client.SecondaryIssuedTokenParameters;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.STSIssuedTokenFeature;
import com.sun.xml.ws.security.trust.WSTrustVersion;
import com.sun.xml.ws.security.trust.impl.client.DefaultSTSIssuedTokenConfiguration;
import com.sun.xml.ws.security.trust.impl.client.SecondaryIssuedTokenParametersImpl;
import com.sun.xml.wss.XWSSConstants;

import au.gov.abr.akm.credential.store.ABRCredential;
import au.gov.abr.akm.credential.store.ABRKeyStore;
import au.gov.usi._2018.ws.servicepolicy.IUSIService;
import au.gov.usi._2018.ws.servicepolicy.USIService;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.saml.util.SAMLUtil;
import org.w3c.dom.Element;


public class UsiServiceChannel {
	public static String strOrganisation = "USI";
	public static String strProduct = "UsiSampleCode";
	public static String strVersion = "0.1";
	public static String strSoftwareTimeStamp = "27 Mar 2020 01:00:00";

	private static final int CONNECT_TIMEOUT = 60000;
	private static final int REQUEST_TIMEOUT = 60000;

	//private static String M2M_KEYSTORE = "keystore/KeyStore.xml";
	//private static String M2M_ALIAS = "ABRD:12300000059_TestDevice03";
	private static String M2M_PASSWORD = "Password1!";
	private static String M2M_KEYSTORE = "keystore/keystore-usi.xml";
	private static String M2M_ALIAS_CLOUD= "ABRD:11000002568_INGLETON153"; // orgcode VA1802 - use with ActAs
	private static String M2M_ALIAS_LOCAL = "ABRD:27809366375_USIMachine"; // orgcode VA1803 - do not use with ActAs
    private static String ORGCODE_CLOUD = "VA1802";
    private static String ORGCODE_LOCAL = "VA1803";

	private static boolean useActAs = false;
	private static String M2M_ALIAS = useActAs ? M2M_ALIAS_CLOUD : M2M_ALIAS_LOCAL;
    private static String ORGCODE = useActAs ? ORGCODE_CLOUD : ORGCODE_LOCAL;

	final private static String ENDPOINT = "https://softwareauthorisations.acc.ato.gov.au/R3.0/S007v1.3/service.svc";
	final private static String WSDL_LOCATION = ENDPOINT;
	final private static String STS_NAMESPACE ="http://schemas.microsoft.com/ws/2008/06/identity/securitytokenservice";
	final private static String STS_SERVICE_NAME = "SecurityTokenService";
	final private static String STS_PORT_NAME = "S007SecurityTokenServiceEndpoint";
	final private static String STS_PROTOCOL = WSTrustVersion.WS_TRUST_13.getNamespaceURI(); //STSIssuedTokenConfiguration.PROTOCOL_13;

    public static String getOrgCode() {
        return ORGCODE;
    }
	public static IUSIService GetNewClient(String orgCode){
		try {
		skipSSLVerification();
		EnableProxy_FOR_DEBUG_ONLY();

		PrivateKey privateKey = GetAUSkey_PrivateKey();
		X509Certificate certificate = GetAUSkey_Cert();

		USIService service = new USIService();

		DefaultSTSIssuedTokenConfiguration config = new DefaultSTSIssuedTokenConfiguration();
		Map<String, Object> otherOptions = config.getOtherOptions();

        config.setSignatureAlgorithm("SHA256withRSA");

	    //config.setKeySize(256);
		if (useActAs) {
			// can put the ActAs token here or below
			Token actAs = getActAs();
			otherOptions.put(STSIssuedTokenConfiguration.ACT_AS, actAs);
		}
			//otherOptions.put(STSIssuedTokenConfiguration.STS_ENDPOINT, ENDPOINT);
			//otherOptions.put(STSIssuedTokenConfiguration.STS_WSDL_LOCATION, WSDL_LOCATION);
			//otherOptions.put(STSIssuedTokenConfiguration.STS_NAMESPACE, STS_NAMESPACE);
			//otherOptions.put(STSIssuedTokenConfiguration.STS_SERVICE_NAME, STS_SERVICE_NAME);
			//otherOptions.put(STSIssuedTokenConfiguration.STS_PORT_NAME, STS_PORT_NAME);
			//config.setSTSInfo(x, ENDPOINT, WSDL_LOCATION, STS_SERVICE_NAME, STS_PORT_NAME, STS_NAMESPACE);
			STSIssuedTokenFeature feature = new STSIssuedTokenFeature(config);
		IUSIService endpoint = service.getWS2007FederationHttpBindingIUSIService(feature);
		//IUSIService endpoint = service.getWS2007FederationHttpBindingIUSIService();

		SetupRequestContext(endpoint, certificate, privateKey);
		return endpoint;

		} catch (Exception ex)	{
			ex.printStackTrace();
			return null;
		}
	}

	// DO NOT USE FOR PRODUCTION CODE
	private static void skipSSLVerification() throws NoSuchAlgorithmException, KeyManagementException {
			// cert and url dont always say the same thing, ergo you gotta be able tell it to ignore it
		final HostnameVerifier hv = new HostnameVerifier()
			{
				public boolean verify(String urlHostName, SSLSession session)
				{
					if(urlHostName!=null && session.getPeerHost()!=null)
					{
						if(!(urlHostName.equals(session.getPeerHost())))
						{
							//if they didn't match log it.
							//LoggingUtilities.log(LoggingUtilities.Level.WARNING, m_logger, "SSL certificate and given URL host name do not match. URL Host: " + urlHostName + "  vs " + session.getPeerHost());
						}
					}
					//we're ignoring the verification if it failed anyway so return true.
					return true;

				}
			};

			// Create a trust manager that does not validate certificate chains like the default TrustManager
			TrustManager[] trustAllCerts = new TrustManager[] {
					new X509TrustManager() {
						public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return null;
						}

						public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
							//No need to implement.
						}

						public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
							//No need to implement.
						}
					}
			};

			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			SSLSocketFactory factory = sc.getSocketFactory();
			HttpsURLConnection.setDefaultSSLSocketFactory(factory);
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
		}

	private static X509Certificate GetAUSkey_Cert() {
		try
		{
			File keystorefile = new File(M2M_KEYSTORE).getAbsoluteFile();
			FileInputStream fileInputStreamKeystore = new FileInputStream(keystorefile);

			if (!keystorefile.exists()) {
				throw new FileNotFoundException(keystorefile.getCanonicalPath());
			}

			ABRKeyStore keyStore = new ABRKeyStore(fileInputStreamKeystore);
	
			ABRCredential abrCredential = keyStore.getCredential(M2M_ALIAS);
			X509Certificate[] certificate = abrCredential.getCertificateChain();
			return certificate[0];

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	private static PrivateKey GetAUSkey_PrivateKey() {
		try
		{
			File keystorefile = new File(M2M_KEYSTORE).getAbsoluteFile();
			FileInputStream fileInputStreamKeystore = new FileInputStream(keystorefile);

			if (!keystorefile.exists()) {
				throw new FileNotFoundException(keystorefile.getCanonicalPath());
			}

			ABRProperties.setSoftwareInfo(strOrganisation, strProduct, strVersion, strSoftwareTimeStamp);
			ABRProperties keystoreProperties = new ABRProperties();
			ABRKeyStore keyStore = new ABRKeyStore(fileInputStreamKeystore, keystoreProperties);
			ABRCredential abrCredential = keyStore.getCredential(M2M_ALIAS);

			if (abrCredential.isReadyForRenewal()) {
				System.out.println("credential is ready for renewal");
			}

			return abrCredential.getPrivateKey(M2M_PASSWORD.toCharArray());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	private static void SetupRequestContext(IUSIService endpoint, X509Certificate certificate, PrivateKey privateKey) throws XMLStreamException, XWSSecurityException {

		Map<String, Object> requestContext = ((BindingProvider)endpoint).getRequestContext();

		requestContext.put(XWSSConstants.CERTIFICATE_PROPERTY, certificate);
		requestContext.put(XWSSConstants.PRIVATEKEY_PROPERTY, privateKey);

		if (useActAs) {
			// or can do above
			//Token actAs = getActAs();
			//requestContext.put(STSIssuedTokenConfiguration.ACT_AS, actAs);
		}


		requestContext.put(STSIssuedTokenConfiguration.LIFE_TIME, 20*60*1000); // minutes*60*1000 (milliseconds). This will override the WSDL
		/*
		requestContext.put(STSIssuedTokenConfiguration.STS_ENDPOINT, ENDPOINT);
		requestContext.put(STSIssuedTokenConfiguration.STS_NAMESPACE, STS_NAMESPACE);
		requestContext.put(STSIssuedTokenConfiguration.STS_WSDL_LOCATION, WSDL_LOCATION);
		requestContext.put(STSIssuedTokenConfiguration.STS_SERVICE_NAME, STS_SERVICE_NAME);
		requestContext.put(STSIssuedTokenConfiguration.STS_PORT_NAME, STS_PORT_NAME);
		*/
		requestContext.put(BindingProviderProperties.REQUEST_TIMEOUT, REQUEST_TIMEOUT);
		requestContext.put(BindingProviderProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
	}
    private static Token getActAs() throws XMLStreamException, XWSSecurityException {
	    return getActAs("11000002568","96312011219","0000123400");
    }
	private static Token getActAs(String FirstParty, String SecondParty, String SSID) throws XMLStreamException, XWSSecurityException {
		// Sure we COULD build a DOM to construct the ActAs. Or not.
		String actAs =  "" +
				"        <v13:RelationshipToken xmlns:v13=\"http://vanguard.business.gov.au/2016/03\" ID=\"35e6d176-bcf0-c7ac-c98d-5eae177e414d\">\n" +
				"          <v13:Relationship v13:Type=\"OSPfor\">\n" +
				"            <v13:Attribute v13:Name=\"SSID\" v13:Value=\"${SSID}\"/>\n" +
				"          </v13:Relationship>\n" +
				"          <v13:FirstParty v13:Scheme=\"uri://abr.gov.au/ABN\" v13:Value=\"${FirstParty}\"/>\n" +
				"          <v13:SecondParty v13:Scheme=\"uri://abr.gov.au/ABN\" v13:Value=\"${SecondParty}\"/>\n" +
				"        </v13:RelationshipToken>\n" +
				"";
        actAs = actAs.replace("${FirstParty}", FirstParty);
        actAs = actAs.replace("${SecondParty}", SecondParty);
        actAs = actAs.replace("${SSID}", SSID);
		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(actAs));
		Element actAsElt = SAMLUtil.createSAMLAssertion(reader);
		return new GenericToken(actAsElt);
	}
	
	public static void EnableProxy_FOR_DEBUG_ONLY(){
		String MY_PROXY_HOST = "localhost";
		String MY_PROXY_PORT = "8080";
		//This method assumes:
		//	* you're running the proxy on localhost:8080 and your've added its SSL inspection cert to the java keystore
		System.out.println("***********************************************************");
		System.out.println("WARNING: ***** Using proxy [" + MY_PROXY_HOST + ":" + MY_PROXY_PORT + "] *****");
		System.out.println("***********************************************************");
		System.setProperty("https.proxyHost", MY_PROXY_HOST);
		System.setProperty("https.proxyPort", MY_PROXY_PORT);
		System.setProperty("https.nonProxyHosts", "localhost|127.0.0.1");
		//System.setProperty("javax.net.ssl.trustStore", "E:\\pf\\Java\\jdk\\jre\\lib\\security\\cacerts");
		//System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
	}
	
}