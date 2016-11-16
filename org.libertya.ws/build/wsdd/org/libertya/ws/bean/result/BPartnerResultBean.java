/**
 * BPartnerResultBean.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.libertya.ws.bean.result;

public class BPartnerResultBean  extends org.libertya.ws.bean.result.ResultBean  implements java.io.Serializable {
    private java.util.HashMap billAddress;

    private boolean moreAddresses;

    private java.util.HashMap userContact;

    public BPartnerResultBean() {
    }

    public BPartnerResultBean(
           boolean error,
           java.lang.String errorMsg,
           java.util.HashMap mainResult,
           java.util.HashMap billAddress,
           boolean moreAddresses,
           java.util.HashMap userContact) {
        super(
            error,
            errorMsg,
            mainResult);
        this.billAddress = billAddress;
        this.moreAddresses = moreAddresses;
        this.userContact = userContact;
    }


    /**
     * Gets the billAddress value for this BPartnerResultBean.
     * 
     * @return billAddress
     */
    public java.util.HashMap getBillAddress() {
        return billAddress;
    }


    /**
     * Sets the billAddress value for this BPartnerResultBean.
     * 
     * @param billAddress
     */
    public void setBillAddress(java.util.HashMap billAddress) {
        this.billAddress = billAddress;
    }


    /**
     * Gets the moreAddresses value for this BPartnerResultBean.
     * 
     * @return moreAddresses
     */
    public boolean isMoreAddresses() {
        return moreAddresses;
    }


    /**
     * Sets the moreAddresses value for this BPartnerResultBean.
     * 
     * @param moreAddresses
     */
    public void setMoreAddresses(boolean moreAddresses) {
        this.moreAddresses = moreAddresses;
    }


    /**
     * Gets the userContact value for this BPartnerResultBean.
     * 
     * @return userContact
     */
    public java.util.HashMap getUserContact() {
        return userContact;
    }


    /**
     * Sets the userContact value for this BPartnerResultBean.
     * 
     * @param userContact
     */
    public void setUserContact(java.util.HashMap userContact) {
        this.userContact = userContact;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof BPartnerResultBean)) return false;
        BPartnerResultBean other = (BPartnerResultBean) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.billAddress==null && other.getBillAddress()==null) || 
             (this.billAddress!=null &&
              this.billAddress.equals(other.getBillAddress()))) &&
            this.moreAddresses == other.isMoreAddresses() &&
            ((this.userContact==null && other.getUserContact()==null) || 
             (this.userContact!=null &&
              this.userContact.equals(other.getUserContact())));
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
        if (getBillAddress() != null) {
            _hashCode += getBillAddress().hashCode();
        }
        _hashCode += (isMoreAddresses() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getUserContact() != null) {
            _hashCode += getUserContact().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(BPartnerResultBean.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://result.bean.ws.libertya.org", "BPartnerResultBean"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("billAddress");
        elemField.setXmlName(new javax.xml.namespace.QName("", "billAddress"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("moreAddresses");
        elemField.setXmlName(new javax.xml.namespace.QName("", "moreAddresses"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userContact");
        elemField.setXmlName(new javax.xml.namespace.QName("", "userContact"));
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
