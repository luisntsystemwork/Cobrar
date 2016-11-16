/**
 * DocumentResultBean.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.libertya.ws.bean.result;

public class DocumentResultBean  extends org.libertya.ws.bean.result.ResultBean  implements java.io.Serializable {
    private java.lang.Object[] documentLines;

    public DocumentResultBean() {
    }

    public DocumentResultBean(
           boolean error,
           java.lang.String errorMsg,
           java.util.HashMap mainResult,
           java.lang.Object[] documentLines) {
        super(
            error,
            errorMsg,
            mainResult);
        this.documentLines = documentLines;
    }


    /**
     * Gets the documentLines value for this DocumentResultBean.
     * 
     * @return documentLines
     */
    public java.lang.Object[] getDocumentLines() {
        return documentLines;
    }


    /**
     * Sets the documentLines value for this DocumentResultBean.
     * 
     * @param documentLines
     */
    public void setDocumentLines(java.lang.Object[] documentLines) {
        this.documentLines = documentLines;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentResultBean)) return false;
        DocumentResultBean other = (DocumentResultBean) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.documentLines==null && other.getDocumentLines()==null) || 
             (this.documentLines!=null &&
              java.util.Arrays.equals(this.documentLines, other.getDocumentLines())));
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
        if (getDocumentLines() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDocumentLines());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDocumentLines(), i);
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
        new org.apache.axis.description.TypeDesc(DocumentResultBean.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://result.bean.ws.libertya.org", "DocumentResultBean"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("documentLines");
        elemField.setXmlName(new javax.xml.namespace.QName("", "documentLines"));
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
