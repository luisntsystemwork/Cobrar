package org.libertya.ws.bean.parameter;

import java.math.BigDecimal;

public class PriceProduct {
	
	private BigDecimal precioFacturacion;
	
	private BigDecimal precioMaximoCompra;
	
	private Integer mProductID;
	
	public PriceProduct(BigDecimal precioFacturacion, BigDecimal precioMaximoCompra, Integer mProductID) {
		this.precioFacturacion = precioFacturacion;
		this.precioMaximoCompra = precioMaximoCompra;
		this.mProductID = mProductID;
	}

	/**
	 * @return the priceEntered
	 */
	public BigDecimal getPrecioFacturacion() {
		return precioFacturacion;
	}

	/**
	 * @return the precioMaximoCompra
	 */
	public BigDecimal getPrecioMaximoCompra() {
		return precioMaximoCompra;
	}

	/**
	 * @return the mProductID
	 */
	public Integer getMProductID() {
		return mProductID;
	}

}
