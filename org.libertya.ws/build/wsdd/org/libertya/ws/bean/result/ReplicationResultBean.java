/**
 * ReplicationResultBean.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.libertya.ws.bean.result;

public class ReplicationResultBean  extends org.libertya.ws.bean.result.ResultBean  implements java.io.Serializable {
    private java.util.Vector eventLog;

    public ReplicationResultBean() {
    }

    public ReplicationResultBean(
           boolean error,
           java.lang.String errorMsg,
           java.util.HashMap mainResult,
           java.util.Vector eventLog) {
        super(
            error,
            errorMsg,
            mainResult);
        this.eventLog = eventLog;
    }


    /**
     * Gets the eventLog value for this ReplicationResultBean.
     * 
     * @return eventLog
     */
    public java.util.Vector getEventLog() {
        return eventLog;
    }


    /**
     * Sets the eventLog value for this ReplicationResultBean.
     * 
     * @param eventLog
     */
    public void setEventLog(java.util.Vector eventLog) {
        this.eventLog = eventLog;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ReplicationResultBean)) return false;
        ReplicationResultBean other = (ReplicationResultBean) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.eventLog==null && other.getEventLog()==null) || 
             (this.eventLog!=null &&
              this.eventLog.equals(other.getEventLog())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getEventLog() != null) {
            _hashCode += getEventLog().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ReplicationResultBean.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://result.bean.ws.libertya.org", "ReplicationResultBean"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eventLog");
        elemField.setXmlName(new javax.xml.namespace.QName("", "eventLog"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Vector"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
