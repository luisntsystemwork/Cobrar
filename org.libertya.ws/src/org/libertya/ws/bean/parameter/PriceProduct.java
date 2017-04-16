package org.libertya.ws.bean.parameter;

import java.math.BigDecimal;

public class PriceProduct {
	
	private BigDecimal priceEntered;
	
	private BigDecimal precioMaximoCompra;
	
	private Integer mProductID;
	
	public PriceProduct(BigDecimal priceEntered, BigDecimal precioMaximoCompra, Integer mProductID) {
		this.priceEntered = priceEntered;
		this.precioMaximoCompra = precioMaximoCompra;
		this.mProductID = mProductID;
	}

	/**
	 * @return the priceEntered
	 */
	public BigDecimal getPriceEntered() {
		return priceEntered;
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
