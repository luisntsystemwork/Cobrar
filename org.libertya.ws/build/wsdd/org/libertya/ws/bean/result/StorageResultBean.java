/**
 * StorageResultBean.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.libertya.ws.bean.result;

public class StorageResultBean  extends org.libertya.ws.bean.result.ResultBean  implements java.io.Serializable {
    private java.lang.Object[] stockList;

    public StorageResultBean() {
    }

    public StorageResultBean(
           boolean error,
           java.lang.String errorMsg,
           java.util.HashMap mainResult,
           java.lang.Object[] stockList) {
        super(
            error,
            errorMsg,
            mainResult);
        this.stockList = stockList;
    }


    /**
     * Gets the stockList value for this StorageResultBean.
     * 
     * @return stockList
     */
    public java.lang.Object[] getStockList() {
        return stockList;
    }


    /**
     * Sets the stockList value for this StorageResultBean.
     * 
     * @param stockList
     */
    public void setStockList(java.lang.Object[] stockList) {
        this.stockList = stockList;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof StorageResultBean)) return false;
        StorageResultBean other = (StorageResultBean) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.stockList==null && other.getStockList()==null) || 
             (this.stockList!=null &&
              java.util.Arrays.equals(this.stockList, other.getStockList())));
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
        if (getStockList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getStockList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getStockList(), i);
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
        new org.apache.axis.description.TypeDesc(StorageResultBean.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://result.bean.ws.libertya.org", "StorageResultBean"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("stockList");
        elemField.setXmlName(new javax.xml.namespace.QName("", "stockList"));
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
