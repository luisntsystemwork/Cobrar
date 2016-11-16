/**
 * LibertyaWSServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package ws.libertya.org;

public class LibertyaWSServiceLocator extends org.apache.axis.client.Service implements ws.libertya.org.LibertyaWSService {

    public LibertyaWSServiceLocator() {
    }


    public LibertyaWSServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public LibertyaWSServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for LibertyaWS
    private java.lang.String LibertyaWS_address = "http://localhost:8080/axis/services/LibertyaWS";

    public java.lang.String getLibertyaWSAddress() {
        return LibertyaWS_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String LibertyaWSWSDDServiceName = "LibertyaWS";

    public java.lang.String getLibertyaWSWSDDServiceName() {
        return LibertyaWSWSDDServiceName;
    }

    public void setLibertyaWSWSDDServiceName(java.lang.String name) {
        LibertyaWSWSDDServiceName = name;
    }

    public ws.libertya.org.LibertyaWS getLibertyaWS() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(LibertyaWS_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getLibertyaWS(endpoint);
    }

    public ws.libertya.org.LibertyaWS getLibertyaWS(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            ws.libertya.org.LibertyaWSSoapBindingStub _stub = new ws.libertya.org.LibertyaWSSoapBindingStub(portAddress, this);
            _stub.setPortName(getLibertyaWSWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setLibertyaWSEndpointAddress(java.lang.String address) {
        LibertyaWS_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (ws.libertya.org.LibertyaWS.class.isAssignableFrom(serviceEndpointInterface)) {
                ws.libertya.org.LibertyaWSSoapBindingStub _stub = new ws.libertya.org.LibertyaWSSoapBindingStub(new java.net.URL(LibertyaWS_address), this);
                _stub.setPortName(getLibertyaWSWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("LibertyaWS".equals(inputPortName)) {
            return getLibertyaWS();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("org.libertya.ws", "LibertyaWSService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("org.libertya.ws", "LibertyaWS"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("LibertyaWS".equals(portName)) {
            setLibertyaWSEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
