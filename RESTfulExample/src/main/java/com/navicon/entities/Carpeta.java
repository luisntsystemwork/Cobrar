package com.navicon.entities;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 *  <Carpeta><!--nro de proyecto -->
        <ClaveCcarpeta>10</ClaveCcarpeta>
        <NombreCarpeta>Nombre del proyecto o carpeta</NombreCarpeta>
        <FechaInicio>dd/mm/aaaa</FechaInicio> <!-- opcional? que se pone por defecto? -->
        <FechaFin>dd/mm/aaaa</FechaFin> <!-- opcional? que se pone por defecto? -->
    </Carpeta>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Carpeta {

	private String clave = "Codigo de la carpeta.";
	private String nombre = "Nombre de la carpeta";
	private String fechaInicio = "dd/MM/yyyy"; // dd/MM/aaaa
	private String fechaFin = "dd/MM/yyyy"; // dd/MM/aaaa
	
	public Carpeta() {
	}

	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		this.clave = clave;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getFechaInicio() {
		return fechaInicio;
	}
	public void setFechaInicio(String fechaInicio) {
		this.fechaInicio = fechaInicio;
	}
	public String getFechaFin() {
		return fechaFin;
	}
	public void setFechaFin(String fechaFin) {
		this.fechaFin = fechaFin;
	}

	@Override
	public String toString() {
		return "Carpeta [clave=" + clave + ", nombre=" + nombre
				+ ", fechaInicio=" + fechaInicio + ", fechaFin=" + fechaFin
				+ "]";
	}
	
}
