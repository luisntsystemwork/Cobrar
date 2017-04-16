package com.navicon.entities;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * <Concepto>
            <ClaveConcepto>art1</ClaveConcepto>
            <Cantidad>2</Cantidad>
			<PrecioFacturacion>3000</PrecioFacturacion>
			<CuitProveedor>30-39993993-3</CuitProveedor>
            <PrecioMaximoCompra>2000</PrecioMaximoCompra><!--precio limite -->
			<PrecioInformado>3000</PrecioInformado>
        </Concepto>
 * @author luis_moyano
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Concepto {
	
	private String claveConcepto; //</ClaveConcepto>
	private String idConceptoLibertya;
    private String cantidad; // </Cantidad>
	private String precioFacturacion; //</PrecioFacturacion>
	private String claveProveedor; //</CuitProveedor>
    private String precioMaximoCompra; //</PrecioMaximoCompra><!--precio limite -->
	private String precioInformado; ///PrecioInformado>
	private String descripcion;
	
	public static Concepto getMock() {
		Concepto concepto = new Concepto();
		
		concepto.setClaveConcepto( "Clave de búsqueda del concepto.");
		concepto.setCantidad ( "Cantidad a Adquirir y facturar.");
		concepto.setPrecioFacturacion ( "Precio al que se le debe facturar al cliente.");
		concepto.setClaveProveedor ( " Identificador único del proveedor al que debe pedirse el concepto, solo se debe informar si debe pedirse el concepto.");
		concepto.setPrecioMaximoCompra ( "Precio sobre el cual se realizará la orden de compra al proveedor.");
		concepto.setPrecioInformado ( "Precio a mostrar al proveedor.");
		
		return concepto;
	}
	
	public String getClaveConcepto() {
		return claveConcepto;
	}
	public void setClaveConcepto(String claveConcepto) {
		this.claveConcepto = claveConcepto;
	}
	public String getCantidad() {
		return cantidad;
	}
	public void setCantidad(String cantidad) {
		this.cantidad = cantidad;
	}
	public String getPrecioFacturacion() {
		return precioFacturacion;
	}
	public void setPrecioFacturacion(String precioFacturacion) {
		this.precioFacturacion = precioFacturacion;
	}
	/**
	 * @return the claveProveedor
	 */
	public String getClaveProveedor() {
		return claveProveedor;
	}
	/**
	 * @param claveProveedor the claveProveedor to set
	 */
	public void setClaveProveedor(String claveProveedor) {
		this.claveProveedor = claveProveedor;
	}
	public String getPrecioMaximoCompra() {
		return precioMaximoCompra;
	}
	public void setPrecioMaximoCompra(String precioMaximoCompra) {
		this.precioMaximoCompra = precioMaximoCompra;
	}
	public String getPrecioInformado() {
		return precioInformado;
	}
	public void setPrecioInformado(String precioInformado) {
		this.precioInformado = precioInformado;
	}
	/**
	 * @return the descripcion
	 */
	public String getDescripcion() {
		return descripcion;
	}
	/**
	 * @param descripcion the descripcion to set
	 */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
	/**
	 * @return the idConceptoLibertya
	 */
	public String getIdConceptoLibertya() {
		return idConceptoLibertya;
	}

	/**
	 * @param idConceptoLibertya the idConceptoLibertya to set
	 */
	public void setIdConceptoLibertya(String idConceptoLibertya) {
		this.idConceptoLibertya = idConceptoLibertya;
	}

	@Override
	public String toString() {
		return "Carpeta [claveConcepto=" + claveConcepto + ", "
				+ "cantidad=" + cantidad 
				+ ", precioFacturacion=" + precioFacturacion
				+ ", claveProveedor=" + claveProveedor 
				+ ", precioMaximoCompra=" + precioMaximoCompra 
				+ ", precioInformado=" + precioInformado 
				+ ", idConceptoLibertya=" + idConceptoLibertya
				+ "]";
	}

}
