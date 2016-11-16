package com.navicon.entities;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoriaIVA {

	private String idCategoriaIVA;
	private String nombre;

	
	public String getIdCategoriaIVA() {
		return idCategoriaIVA;
	}

	public String getNombre() {
		return nombre;
	}

	public void setIdCategoriaIVA(String idCategoriaIVA) {
		this.idCategoriaIVA = idCategoriaIVA;
		
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

}
