package au.gov.usi._2018.ws.servicepolicy;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * Version: 3.0
 * Environment: 3PT
 *     
 *
 * This class was generated by Apache CXF 3.3.6
 * 2021-02-24T17:30:59.823+11:00
 * Generated source version: 3.3.6
 *
 */
@WebServiceClient(name = "USIService",
                  wsdlLocation = "srcv3/META-INF/wsdl/UsiService_CLIENT.wsdl",
                  targetNamespace = "http://usi.gov.au/2018/ws/servicepolicy")
public class USIService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://usi.gov.au/2018/ws/servicepolicy", "USIService");
    public final static QName WS2007FederationHttpBindingIUSIService = new QName("http://usi.gov.au/2018/ws/servicepolicy", "WS2007FederationHttpBinding_IUSIService");
    static {
        URL url = USIService.class.getResource("srcv3/META-INF/wsdl/UsiService_CLIENT.wsdl");
        if (url == null) {
            url = USIService.class.getClassLoader().getResource("srcv3/META-INF/wsdl/UsiService_CLIENT.wsdl");
        }
        if (url == null) {
            java.util.logging.Logger.getLogger(USIService.class.getName())
                .log(java.util.logging.Level.INFO,
                     "Can not initialize the default wsdl from {0}", "srcv3/META-INF/wsdl/UsiService_CLIENT.wsdl");
        }
        WSDL_LOCATION = url;
    }

    public USIService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public USIService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public USIService() {
        super(WSDL_LOCATION, SERVICE);
    }

    public USIService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    public USIService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    public USIService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }




    /**
     *
     * @return
     *     returns IUSIService
     */
    @WebEndpoint(name = "WS2007FederationHttpBinding_IUSIService")
    public IUSIService getWS2007FederationHttpBindingIUSIService() {
        return super.getPort(WS2007FederationHttpBindingIUSIService, IUSIService.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IUSIService
     */
    @WebEndpoint(name = "WS2007FederationHttpBinding_IUSIService")
    public IUSIService getWS2007FederationHttpBindingIUSIService(WebServiceFeature... features) {
        return super.getPort(WS2007FederationHttpBindingIUSIService, IUSIService.class, features);
    }

}
