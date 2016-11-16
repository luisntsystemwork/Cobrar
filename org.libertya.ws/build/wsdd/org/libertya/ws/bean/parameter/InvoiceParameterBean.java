/**
 * InvoiceParameterBean.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.libertya.ws.bean.parameter;

public class InvoiceParameterBean  extends org.libertya.ws.bean.parameter.DocumentParameterBean  implements java.io.Serializable {
    private java.lang.Object[] otherTaxes;

    public InvoiceParameterBean() {
    }

    public InvoiceParameterBean(
           int clientID,
           java.util.HashMap mainTable,
           int orgID,
           java.lang.String password,
           java.lang.String userName,
           java.lang.Object[] documentLines,
           java.lang.Object[] otherTaxes) {
        super(
            clientID,
            mainTable,
            orgID,
            password,
            userName,
            documentLines);
        this.otherTaxes = otherTaxes;
    }


    /**
     * Gets the otherTaxes value for this InvoiceParameterBean.
     * 
     * @return otherTaxes
     */
    public java.lang.Object[] getOtherTaxes() {
        return otherTaxes;
    }


    /**
     * Sets the otherTaxes value for this InvoiceParameterBean.
     * 
     * @param otherTaxes
     */
    public void setOtherTaxes(java.lang.Object[] otherTaxes) {
        this.otherTaxes = otherTaxes;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof InvoiceParameterBean)) return false;
        InvoiceParameterBean other = (InvoiceParameterBean) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.otherTaxes==null && other.getOtherTaxes()==null) || 
             (this.otherTaxes!=null &&
              java.util.Arrays.equals(this.otherTaxes, other.getOtherTaxes())));
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
        if (getOtherTaxes() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getOtherTaxes());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getOtherTaxes(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(InvoiceParameterBean.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://parameter.bean.ws.libertya.org", "InvoiceParameterBean"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("otherTaxes");
        elemField.setXmlName(new javax.xml.namespace.QName("", "otherTaxes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
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
