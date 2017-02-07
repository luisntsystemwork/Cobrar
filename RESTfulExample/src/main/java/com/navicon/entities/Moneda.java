package com.navicon.entities;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Moneda {
	
	private String codigoMoneda;
	private String descripcion;
	/**
	 * @return the codigoMoneda
	 */
	public String getCodigoMoneda() {
		return codigoMoneda;
	}
	/**
	 * @param codigoMoneda the codigoMoneda to set
	 */
	public void setCodigoMoneda(String codigoMoneda) {
		this.codigoMoneda = codigoMoneda;
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
	
}
