/**
 * CustomServiceParameterBean.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.libertya.ws.bean.parameter;

public class CustomServiceParameterBean  extends org.libertya.ws.bean.parameter.ParameterBean  implements java.io.Serializable {
    private java.lang.String className;

    private java.lang.String methodName;

    private org.libertya.wse.common.ListedMap[] rawArguments;

    public CustomServiceParameterBean() {
    }

    public CustomServiceParameterBean(
           int clientID,
           java.util.HashMap mainTable,
           int orgID,
           java.lang.String password,
           java.lang.String userName,
           java.lang.String className,
           java.lang.String methodName,
           org.libertya.wse.common.ListedMap[] rawArguments) {
        super(
            clientID,
            mainTable,
            orgID,
            password,
            userName);
        this.className = className;
        this.methodName = methodName;
        this.rawArguments = rawArguments;
    }


    /**
     * Gets the className value for this CustomServiceParameterBean.
     * 
     * @return className
     */
    public java.lang.String getClassName() {
        return className;
    }


    /**
     * Sets the className value for this CustomServiceParameterBean.
     * 
     * @param className
     */
    public void setClassName(java.lang.String className) {
        this.className = className;
    }


    /**
     * Gets the methodName value for this CustomServiceParameterBean.
     * 
     * @return methodName
     */
    public java.lang.String getMethodName() {
        return methodName;
    }


    /**
     * Sets the methodName value for this CustomServiceParameterBean.
     * 
     * @param methodName
     */
    public void setMethodName(java.lang.String methodName) {
        this.methodName = methodName;
    }


    /**
     * Gets the rawArguments value for this CustomServiceParameterBean.
     * 
     * @return rawArguments
     */
    public org.libertya.wse.common.ListedMap[] getRawArguments() {
        return rawArguments;
    }


    /**
     * Sets the rawArguments value for this CustomServiceParameterBean.
     * 
     * @param rawArguments
     */
    public void setRawArguments(org.libertya.wse.common.ListedMap[] rawArguments) {
        this.rawArguments = rawArguments;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CustomServiceParameterBean)) return false;
        CustomServiceParameterBean other = (CustomServiceParameterBean) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.className==null && other.getClassName()==null) || 
             (this.className!=null &&
              this.className.equals(other.getClassName()))) &&
            ((this.methodName==null && other.getMethodName()==null) || 
             (this.methodName!=null &&
              this.methodName.equals(other.getMethodName()))) &&
            ((this.rawArguments==null && other.getRawArguments()==null) || 
             (this.rawArguments!=null &&
              java.util.Arrays.equals(this.rawArguments, other.getRawArguments())));
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
        if (getClassName() != null) {
            _hashCode += getClassName().hashCode();
        }
        if (getMethodName() != null) {
            _hashCode += getMethodName().hashCode();
        }
        if (getRawArguments() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRawArguments());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRawArguments(), i);
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
        new org.apache.axis.description.TypeDesc(CustomServiceParameterBean.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://parameter.bean.ws.libertya.org", "CustomServiceParameterBean"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("className");
        elemField.setXmlName(new javax.xml.namespace.QName("", "className"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("methodName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "methodName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("rawArguments");
        elemField.setXmlName(new javax.xml.namespace.QName("", "rawArguments"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://common.wse.libertya.org", "ListedMap"));
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
