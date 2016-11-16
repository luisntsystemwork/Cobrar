/**
 * AllocationParameterBean.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.libertya.ws.bean.parameter;

public class AllocationParameterBean  extends org.libertya.ws.bean.parameter.ParameterBean  implements java.io.Serializable {
    private java.lang.Object[] invoices;

    private java.lang.Object[] payments;

    public AllocationParameterBean() {
    }

    public AllocationParameterBean(
           int clientID,
           java.util.HashMap mainTable,
           int orgID,
           java.lang.String password,
           java.lang.String userName,
           java.lang.Object[] invoices,
           java.lang.Object[] payments) {
        super(
            clientID,
            mainTable,
            orgID,
            password,
            userName);
        this.invoices = invoices;
        this.payments = payments;
    }


    /**
     * Gets the invoices value for this AllocationParameterBean.
     * 
     * @return invoices
     */
    public java.lang.Object[] getInvoices() {
        return invoices;
    }


    /**
     * Sets the invoices value for this AllocationParameterBean.
     * 
     * @param invoices
     */
    public void setInvoices(java.lang.Object[] invoices) {
        this.invoices = invoices;
    }


    /**
     * Gets the payments value for this AllocationParameterBean.
     * 
     * @return payments
     */
    public java.lang.Object[] getPayments() {
        return payments;
    }


    /**
     * Sets the payments value for this AllocationParameterBean.
     * 
     * @param payments
     */
    public void setPayments(java.lang.Object[] payments) {
        this.payments = payments;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AllocationParameterBean)) return false;
        AllocationParameterBean other = (AllocationParameterBean) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.invoices==null && other.getInvoices()==null) || 
             (this.invoices!=null &&
              java.util.Arrays.equals(this.invoices, other.getInvoices()))) &&
            ((this.payments==null && other.getPayments()==null) || 
             (this.payments!=null &&
              java.util.Arrays.equals(this.payments, other.getPayments())));
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
        if (getInvoices() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getInvoices());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getInvoices(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getPayments() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getPayments());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getPayments(), i);
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
        new org.apache.axis.description.TypeDesc(AllocationParameterBean.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://parameter.bean.ws.libertya.org", "AllocationParameterBean"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("invoices");
        elemField.setXmlName(new javax.xml.namespace.QName("", "invoices"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("payments");
        elemField.setXmlName(new javax.xml.namespace.QName("", "payments"));
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
