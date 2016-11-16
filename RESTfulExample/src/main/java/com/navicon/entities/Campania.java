package com.navicon.entities;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Campania {

	private String nombreUnidadNegocio = "";
	private String claveUnidadNegocio = "";
	
	public String getNombreUnidadNegocio() {
		return nombreUnidadNegocio;
	}
	public void setNombreUnidadNegocio(String nombreUnidadNegocio) {
		this.nombreUnidadNegocio = nombreUnidadNegocio;
	}
	public String getClaveUnidadNegocio() {
		return claveUnidadNegocio;
	}
	public void setClaveUnidadNegocio(String claveUnidadNegocio) {
		this.claveUnidadNegocio = claveUnidadNegocio;
	}
	
}
