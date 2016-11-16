/**
 * ReplicationParameterBean.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.libertya.ws.bean.parameter;

public class ReplicationParameterBean  extends org.libertya.ws.bean.parameter.ParameterBean  implements java.io.Serializable {
    private byte[] actionsXML;

    private int sourceHostPos;

    private int targetHostPos;

    public ReplicationParameterBean() {
    }

    public ReplicationParameterBean(
           int clientID,
           java.util.HashMap mainTable,
           int orgID,
           java.lang.String password,
           java.lang.String userName,
           byte[] actionsXML,
           int sourceHostPos,
           int targetHostPos) {
        super(
            clientID,
            mainTable,
            orgID,
            password,
            userName);
        this.actionsXML = actionsXML;
        this.sourceHostPos = sourceHostPos;
        this.targetHostPos = targetHostPos;
    }


    /**
     * Gets the actionsXML value for this ReplicationParameterBean.
     * 
     * @return actionsXML
     */
    public byte[] getActionsXML() {
        return actionsXML;
    }


    /**
     * Sets the actionsXML value for this ReplicationParameterBean.
     * 
     * @param actionsXML
     */
    public void setActionsXML(byte[] actionsXML) {
        this.actionsXML = actionsXML;
    }


    /**
     * Gets the sourceHostPos value for this ReplicationParameterBean.
     * 
     * @return sourceHostPos
     */
    public int getSourceHostPos() {
        return sourceHostPos;
    }


    /**
     * Sets the sourceHostPos value for this ReplicationParameterBean.
     * 
     * @param sourceHostPos
     */
    public void setSourceHostPos(int sourceHostPos) {
        this.sourceHostPos = sourceHostPos;
    }


    /**
     * Gets the targetHostPos value for this ReplicationParameterBean.
     * 
     * @return targetHostPos
     */
    public int getTargetHostPos() {
        return targetHostPos;
    }


    /**
     * Sets the targetHostPos value for this ReplicationParameterBean.
     * 
     * @param targetHostPos
     */
    public void setTargetHostPos(int targetHostPos) {
        this.targetHostPos = targetHostPos;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ReplicationParameterBean)) return false;
        ReplicationParameterBean other = (ReplicationParameterBean) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.actionsXML==null && other.getActionsXML()==null) || 
             (this.actionsXML!=null &&
              java.util.Arrays.equals(this.actionsXML, other.getActionsXML()))) &&
            this.sourceHostPos == other.getSourceHostPos() &&
            this.targetHostPos == other.getTargetHostPos();
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
        if (getActionsXML() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getActionsXML());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getActionsXML(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += getSourceHostPos();
        _hashCode += getTargetHostPos();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ReplicationParameterBean.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://parameter.bean.ws.libertya.org", "ReplicationParameterBean"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("actionsXML");
        elemField.setXmlName(new javax.xml.namespace.QName("", "actionsXML"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sourceHostPos");
        elemField.setXmlName(new javax.xml.namespace.QName("", "sourceHostPos"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("targetHostPos");
        elemField.setXmlName(new javax.xml.namespace.QName("", "targetHostPos"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
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
