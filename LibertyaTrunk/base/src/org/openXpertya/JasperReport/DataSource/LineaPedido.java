package org.openXpertya.JasperReport.DataSource;

import java.math.BigDecimal;


/**
 * POJO.
 */
public class LineaPedido implements Comparable<LineaPedido>{

	private String documento;
	private String numeroDoc;
	private String concepto;
	private BigDecimal lineTotalAmt;
	private String isoCode;
	private BigDecimal precioMaximoCompra;
	private BigDecimal precioInformado;
	private String razonSocial;
	private String dateInvoicedFormated;
	private BigDecimal tipoCambio;



	public LineaPedido(String documento, String numerodoc, String concepto,
			BigDecimal lineTotalAmt, String iso_code,
			BigDecimal preciomaximocompra, BigDecimal precioinformado,
			String razonsocial, String dateInvoicedFormated,
			BigDecimal tipocambio) {
		super();
		
		this.documento = documento;
		this.numeroDoc = numerodoc;
		this.concepto = concepto;
		this.lineTotalAmt = lineTotalAmt;
		this.isoCode = iso_code;
		this.precioMaximoCompra = preciomaximocompra;
		this.precioInformado = precioinformado;
		this.razonSocial = razonsocial;
		this.dateInvoicedFormated = dateInvoicedFormated;
		this.tipoCambio = tipocambio;
	}

	


	/**
	 * @return the documento
	 */
	public String getDocumento() {
		return documento;
	}




	/**
	 * @return the numeroDoc
	 */
	public String getNumeroDoc() {
		return numeroDoc;
	}




	/**
	 * @return the concepto
	 */
	public String getConcepto() {
		return concepto;
	}




	/**
	 * @return the lineTotalAmt
	 */
	public BigDecimal getLineTotalAmt() {
		return lineTotalAmt;
	}




	/**
	 * @return the isoCode
	 */
	public String getIsoCode() {
		return isoCode;
	}




	/**
	 * @return the precioMaximoCompra
	 */
	public BigDecimal getPrecioMaximoCompra() {
		return precioMaximoCompra;
	}




	/**
	 * @return the precioInformado
	 */
	public BigDecimal getPrecioInformado() {
		return precioInformado;
	}




	/**
	 * @return the razonSocial
	 */
	public String getRazonSocial() {
		return razonSocial;
	}




	/**
	 * @return the dateInvoicedFormated
	 */
	public String getDateInvoicedFormated() {
		return dateInvoicedFormated;
	}




	/**
	 * @return the tipoCambio
	 */
	public BigDecimal getTipoCambio() {
		return tipoCambio;
	}




	@Override
	public int compareTo(LineaPedido o) {
		return numeroDoc.compareTo(o.numeroDoc);
	}		
}
