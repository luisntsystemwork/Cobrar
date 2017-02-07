package com.navicon.entities;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntidadComercial {
	
	 private String clave;
	 private String nombre;
	 private String codigoIVA;
	 private String codigoIdentificacion;
	 private String nroIIBB;
	 private String codigoGrupoEC;
	 private String clientePotencial;
	 private String esCliente;
	 private String esproveedor;
	 private String esEmpleado;
	 private String direccion;
	 private String ciudad;
	 private String CP;
	 private String provincia; // --id de provincia
	 private String codigoPais; // --Argentina
	 private String telefono;
	 private String estadoCredito;
	 private String limitecredito;
	 private String tipoIdentificacion; // -cuit
	private String cliente;
	private String cuitProveedor;
	
	public static EntidadComercial getMock() {
		EntidadComercial entidad = new EntidadComercial();
		entidad.setClave( "nuevo");
		entidad.setNombre ( "Juan Perez");
		entidad.setCodigoIVA ( "1010065");
		entidad.setCodigoIdentificacion ("23111111111");
		entidad.setCodigoGrupoEC ("1010044");
		entidad.setClientePotencial ( "N");
		entidad.setEsCliente ( "Y");
		entidad.setEsproveedor ("N");
		entidad.setEsEmpleado ( "N");
		entidad.setDireccion ("Corrientes 880");
		entidad.setCiudad ("Capital Federal");
		entidad.setCP ("1043");
		entidad.setProvincia ( "1000082"); // --id de provincia
		entidad.setCodigoPais ( "119"); // --Argentina
		entidad.setTelefono ("6363-9833");
		entidad.setEstadoCredito ("Dejar vacío para que tome valor por defecto.");
		entidad.setLimitecredito ("0");
		entidad.setTipoIdentificacion ("80"); // -cuit
		
		return entidad;
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
	public String getCodigoIVA() {
		return codigoIVA;
	}
	public void setCodigoIVA(String codigoIVA) {
		this.codigoIVA = codigoIVA;
	}
	public String getCodigoIdentificacion() {
		return codigoIdentificacion;
	}
	public void setCodigoIdentificacion(String codigoIdentificacion) {
		this.codigoIdentificacion = codigoIdentificacion;
	}
	/*public String getNroIIBB() {
		return nroIIBB;
	}
	public void setNroIIBB(String nroIIBB) {
		this.nroIIBB = nroIIBB;
	}*/
	public String getClientePotencial() {
		return clientePotencial;
	}
	public void setClientePotencial(String clientePotencial) {
		this.clientePotencial = clientePotencial;
	}
	public String getEsCliente() {
		return esCliente;
	}
	public void setEsCliente(String esCliente) {
		this.esCliente = esCliente;
	}
	public String getEsproveedor() {
		return esproveedor;
	}
	public void setEsproveedor(String esproveedor) {
		this.esproveedor = esproveedor;
	}
	public String getEsEmpleado() {
		return esEmpleado;
	}
	public void setEsEmpleado(String esEmpleado) {
		this.esEmpleado = esEmpleado;
	}
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}
	public String getCiudad() {
		return ciudad;
	}
	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}
	public String getCP() {
		return CP;
	}
	public void setCP(String cP) {
		CP = cP;
	}
	public String getProvincia() {
		return provincia;
	}
	public void setProvincia(String provincia) {
		this.provincia = provincia;
	}
	public String getCodigoPais() {
		return codigoPais;
	}
	public void setCodigoPais(String codigoPais) {
		this.codigoPais = codigoPais;
	}
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	public String getEstadoCredito() {
		return estadoCredito;
	}
	public void setEstadoCredito(String estadoCredito) {
		this.estadoCredito = estadoCredito;
	}
	public String getLimitecredito() {
		return limitecredito;
	}
	public void setLimitecredito(String limitecredito) {
		this.limitecredito = limitecredito;
	}
	public String getTipoIdentificacion() {
		return tipoIdentificacion;
	}
	public void setTipoIdentificacion(String tipoIdentificacion) {
		this.tipoIdentificacion = tipoIdentificacion;
	}
	public String getCodigoGrupoEC() {
		return codigoGrupoEC;
	}
	public void setCodigoGrupoEC(String codigoGrupoEC) {
		this.codigoGrupoEC = codigoGrupoEC;
	}
	
	public void setCliente(String taxID) {
		this.cliente = taxID;
	}
	
	
	public String getCliente() {
		return cliente;
	}
	
	public void setCuitProveedor(String taxID) {
		this.cuitProveedor = taxID;
	}
	
	
	public String getCuitProveedor() {
		return cuitProveedor;
	}
	@Override
	public String toString() {
		return "EntidadComercial [clave=" + clave + ", nombre=" + nombre
				+ ", codigoIVA=" + codigoIVA + ", codigoIdentificacion=" + codigoIdentificacion + ", nroIIBB="
				+ nroIIBB + ", codigoGrupoEC=" + codigoGrupoEC
				+ ", clientePotencial=" + clientePotencial + ", esCliente="
				+ esCliente + ", esproveedor=" + esproveedor + ", esEmpleado="
				+ esEmpleado + ", direccion=" + direccion + ", ciudad="
				+ ciudad + ", CP=" + CP + ", provincia=" + provincia
				+ ", codigoPais=" + codigoPais + ", telefono=" + telefono
				+ ", estadoCredito=" + estadoCredito + ", limitecredito="
				+ limitecredito + ", tipoIdentificacion=" + tipoIdentificacion + "]";
	}
	
	
	
	

}
