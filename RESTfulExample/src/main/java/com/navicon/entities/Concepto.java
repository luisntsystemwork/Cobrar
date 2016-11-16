package com.navicon.entities;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

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
public class Concepto {
	
	private String claveConcepto = "Clave de búsqueda del concepto."; //</ClaveConcepto>
    private String cantidad = "Cantidad a Adquirir y facturar."; // </Cantidad>
	private String precioFacturacion = "Precio al que se le debe facturar al cliente."; //</PrecioFacturacion>
	private String tipoIdentificacion = "Codigo de tipo de identificación del proveedor. Es obligatorio si se envia codigoIdentificacion.";
	private String codigoIdentificacion = " Identificador único del proveedor al que debe pedirse el concepto, solo se debe informar si debe pedirse el concepto."; //</CuitProveedor>
    private String precioMaximoCompra = "Precio sobre el cual se realizará la orden de compra al proveedor."; //</PrecioMaximoCompra><!--precio limite -->
	private String precioInformado = "Precio a mostrar al proveedor."; ///PrecioInformado>
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
	public String getCodigoIdentificacion() {
		return codigoIdentificacion;
	}
	public void setCodigoIdentificacion(String codigoIdentificacion) {
		this.codigoIdentificacion = codigoIdentificacion;
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
	@Override
	public String toString() {
		return "Carpeta [claveConcepto=" + claveConcepto + ", cantidad="
				+ cantidad + ", precioFacturacion=" + precioFacturacion
				+ ", codigoIdentificacion=" + codigoIdentificacion + ", precioMaximoCompra="
				+ precioMaximoCompra + ", precioInformado=" + precioInformado
				+ "]";
	}

}
