package com.navicon.entities;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MensajesRespuesta {
	
	private Boolean hayErrores = Boolean.FALSE;
	
	private String idOrden;
	
	private List<String> mensajes = new ArrayList<String>();
	
	public MensajesRespuesta() {
		
	}

	public List<String> getMensajes() {
		return mensajes;
	}
	
	public void agregarMensaje(String mensaje) {
		this.mensajes.add(mensaje);
	}
	
	public void agregarTodosLosMensajes(MensajesRespuesta mensajesRespuesta) {
		this.mensajes.addAll(mensajesRespuesta.getMensajes());
	}

	public Boolean getHayErrores() {
		return hayErrores;
	}

	public void setHayErrores(Boolean hayErrores) {
		this.hayErrores = hayErrores;
	}

	public Boolean hayMensajes() {
		return !this.mensajes.isEmpty();
	}
	
	public void setIdOrden(String idOrden) {
		this.idOrden = idOrden;
	}
	
	public String getIdOrden() {
		return idOrden;
	}

	@Override
	public String toString() {
		return "MensajesRespuesta [mensajes=" + mensajes + "]";
	}
	
	

}
