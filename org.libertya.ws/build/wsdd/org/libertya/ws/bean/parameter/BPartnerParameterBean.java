/**
 * BPartnerParameterBean.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.libertya.ws.bean.parameter;

public class BPartnerParameterBean  extends org.libertya.ws.bean.parameter.ParameterBean  implements java.io.Serializable {
    private java.util.HashMap BPartnerColumns;

    private java.util.HashMap location;

    private java.util.HashMap locationColumns;

    public BPartnerParameterBean() {
    }

    public BPartnerParameterBean(
           int clientID,
           java.util.HashMap mainTable,
           int orgID,
           java.lang.String password,
           java.lang.String userName,
           java.util.HashMap BPartnerColumns,
           java.util.HashMap location,
           java.util.HashMap locationColumns) {
        super(
            clientID,
            mainTable,
            orgID,
            password,
            userName);
        this.BPartnerColumns = BPartnerColumns;
        this.location = location;
        this.locationColumns = locationColumns;
    }


    /**
     * Gets the BPartnerColumns value for this BPartnerParameterBean.
     * 
     * @return BPartnerColumns
     */
    public java.util.HashMap getBPartnerColumns() {
        return BPartnerColumns;
    }


    /**
     * Sets the BPartnerColumns value for this BPartnerParameterBean.
     * 
     * @param BPartnerColumns
     */
    public void setBPartnerColumns(java.util.HashMap BPartnerColumns) {
        this.BPartnerColumns = BPartnerColumns;
    }


    /**
     * Gets the location value for this BPartnerParameterBean.
     * 
     * @return location
     */
    public java.util.HashMap getLocation() {
        return location;
    }


    /**
     * Sets the location value for this BPartnerParameterBean.
     * 
     * @param location
     */
    public void setLocation(java.util.HashMap location) {
        this.location = location;
    }


    /**
     * Gets the locationColumns value for this BPartnerParameterBean.
     * 
     * @return locationColumns
     */
    public java.util.HashMap getLocationColumns() {
        return locationColumns;
    }


    /**
     * Sets the locationColumns value for this BPartnerParameterBean.
     * 
     * @param locationColumns
     */
    public void setLocationColumns(java.util.HashMap locationColumns) {
        this.locationColumns = locationColumns;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof BPartnerParameterBean)) return false;
        BPartnerParameterBean other = (BPartnerParameterBean) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.BPartnerColumns==null && other.getBPartnerColumns()==null) || 
             (this.BPartnerColumns!=null &&
              this.BPartnerColumns.equals(other.getBPartnerColumns()))) &&
            ((this.location==null && other.getLocation()==null) || 
             (this.location!=null &&
              this.location.equals(other.getLocation()))) &&
            ((this.locationColumns==null && other.getLocationColumns()==null) || 
             (this.locationColumns!=null &&
              this.locationColumns.equals(other.getLocationColumns())));
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
        if (getBPartnerColumns() != null) {
            _hashCode += getBPartnerColumns().hashCode();
        }
        if (getLocation() != null) {
            _hashCode += getLocation().hashCode();
        }
        if (getLocationColumns() != null) {
            _hashCode += getLocationColumns().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(BPartnerParameterBean.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://parameter.bean.ws.libertya.org", "BPartnerParameterBean"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("BPartnerColumns");
        elemField.setXmlName(new javax.xml.namespace.QName("", "BPartnerColumns"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("location");
        elemField.setXmlName(new javax.xml.namespace.QName("", "location"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("locationColumns");
        elemField.setXmlName(new javax.xml.namespace.QName("", "locationColumns"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
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
