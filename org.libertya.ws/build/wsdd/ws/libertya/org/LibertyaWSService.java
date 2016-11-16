/**
 * LibertyaWSService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package ws.libertya.org;

public interface LibertyaWSService extends javax.xml.rpc.Service {
    public java.lang.String getLibertyaWSAddress();

    public ws.libertya.org.LibertyaWS getLibertyaWS() throws javax.xml.rpc.ServiceException;

    public ws.libertya.org.LibertyaWS getLibertyaWS(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
