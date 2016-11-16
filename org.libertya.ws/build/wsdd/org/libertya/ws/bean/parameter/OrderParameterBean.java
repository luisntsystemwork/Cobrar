/**
 * OrderParameterBean.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.libertya.ws.bean.parameter;

public class OrderParameterBean  extends org.libertya.ws.bean.parameter.DocumentParameterBean  implements java.io.Serializable {
    private int invoiceDocTypeTargetID;

    private int invoicePuntoDeVenta;

    private java.lang.String invoiceTipoComprobante;

    public OrderParameterBean() {
    }

    public OrderParameterBean(
           int clientID,
           java.util.HashMap mainTable,
           int orgID,
           java.lang.String password,
           java.lang.String userName,
           java.lang.Object[] documentLines,
           int invoiceDocTypeTargetID,
           int invoicePuntoDeVenta,
           java.lang.String invoiceTipoComprobante) {
        super(
            clientID,
            mainTable,
            orgID,
            password,
            userName,
            documentLines);
        this.invoiceDocTypeTargetID = invoiceDocTypeTargetID;
        this.invoicePuntoDeVenta = invoicePuntoDeVenta;
        this.invoiceTipoComprobante = invoiceTipoComprobante;
    }


    /**
     * Gets the invoiceDocTypeTargetID value for this OrderParameterBean.
     * 
     * @return invoiceDocTypeTargetID
     */
    public int getInvoiceDocTypeTargetID() {
        return invoiceDocTypeTargetID;
    }


    /**
     * Sets the invoiceDocTypeTargetID value for this OrderParameterBean.
     * 
     * @param invoiceDocTypeTargetID
     */
    public void setInvoiceDocTypeTargetID(int invoiceDocTypeTargetID) {
        this.invoiceDocTypeTargetID = invoiceDocTypeTargetID;
    }


    /**
     * Gets the invoicePuntoDeVenta value for this OrderParameterBean.
     * 
     * @return invoicePuntoDeVenta
     */
    public int getInvoicePuntoDeVenta() {
        return invoicePuntoDeVenta;
    }


    /**
     * Sets the invoicePuntoDeVenta value for this OrderParameterBean.
     * 
     * @param invoicePuntoDeVenta
     */
    public void setInvoicePuntoDeVenta(int invoicePuntoDeVenta) {
        this.invoicePuntoDeVenta = invoicePuntoDeVenta;
    }


    /**
     * Gets the invoiceTipoComprobante value for this OrderParameterBean.
     * 
     * @return invoiceTipoComprobante
     */
    public java.lang.String getInvoiceTipoComprobante() {
        return invoiceTipoComprobante;
    }


    /**
     * Sets the invoiceTipoComprobante value for this OrderParameterBean.
     * 
     * @param invoiceTipoComprobante
     */
    public void setInvoiceTipoComprobante(java.lang.String invoiceTipoComprobante) {
        this.invoiceTipoComprobante = invoiceTipoComprobante;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof OrderParameterBean)) return false;
        OrderParameterBean other = (OrderParameterBean) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            this.invoiceDocTypeTargetID == other.getInvoiceDocTypeTargetID() &&
            this.invoicePuntoDeVenta == other.getInvoicePuntoDeVenta() &&
            ((this.invoiceTipoComprobante==null && other.getInvoiceTipoComprobante()==null) || 
             (this.invoiceTipoComprobante!=null &&
              this.invoiceTipoComprobante.equals(other.getInvoiceTipoComprobante())));
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
        _hashCode += getInvoiceDocTypeTargetID();
        _hashCode += getInvoicePuntoDeVenta();
        if (getInvoiceTipoComprobante() != null) {
            _hashCode += getInvoiceTipoComprobante().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(OrderParameterBean.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://parameter.bean.ws.libertya.org", "OrderParameterBean"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("invoiceDocTypeTargetID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "invoiceDocTypeTargetID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("invoicePuntoDeVenta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "invoicePuntoDeVenta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("invoiceTipoComprobante");
        elemField.setXmlName(new javax.xml.namespace.QName("", "invoiceTipoComprobante"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
