package com.navicon.entities;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;


/**
 * <OrdenTrabajo>
    <NroOT>1</NroOT>
    <Cliente>a1</Cliente><!--clave de busqueda del cliente? 
    (el cliente se carga en crm o se carga primero en libertya?? lo tenemos que importar)-->
    <ContactoCliente>Nombre contacto</ContactoCliente><!--Opcional_: ver si se quiere registrar el nombre del contacto del cliente-->
    <FechaOT>dd/mm/aaaa</FechaOT>
    <ClaveVendedor>V01</ClaveVendedor><!--Opcional_: ver si se quiere asociar la ot con un vendedor>!-->
    <Moneda>ARS</Moneda><!--moneda de facturación -->
    <FormaDePago>CRED</FormaDePago> <!-- opcional? que se pone por defecto? -->
    <ProgVtos>30</ProgVtos> <!--codigo de programa de vencimientos, opcional? que se pone por defecto? -->-->
    <Carpeta><!--nro de proyecto -->
        <ClaveCcarpeta>10</ClaveCcarpeta>
        <NombreCarpeta>Nombre del proyecto o carpeta</NombreCarpeta>
        <FechaInicio>dd/mm/aaaa</FechaInicio> <!-- opcional? que se pone por defecto? -->
        <FechaFin>dd/mm/aaaa</FechaFin> <!-- opcional? que se pone por defecto? -->
    </Carpeta>
    <Conceptos> <!--estos son los articulos-->
        <Concepto>
            <ClaveConcepto>art1</ClaveConcepto>
            <Cantidad>2</Cantidad>
			<PrecioFacturacion>3000</PrecioFacturacion>
			<CuitProveedor>30-39993993-3</CuitProveedor>
            <PrecioMaximoCompra>2000</PrecioMaximoCompra><!--precio limite -->
			<PrecioInformado>3000</PrecioInformado>
        </Concepto>
        <Concepto>
            <ClaveConcepto>art2</ClaveConcepto>
            <Cantidad>2</Cantidad>
			<PrecioFacturacion>1000</PrecioFacturacion>
			<CuitProveedor>30-39993993-3</CuitProveedor>
            <PrecioMaximoCompra>200</PrecioMaximoCompra><!--precio limite -->
			<PrecioInformado>3000</PrecioInformado>
        </Concepto>
        <Concepto>
            <ClaveConcepto>art3</ClaveConcepto>
            <Cantidad>2</Cantidad>
			<PrecioFacturacion>2000</PrecioFacturacion>
			<CuitProveedor>30-39993993-3</CuitProveedor>
            <PrecioMaximoCompra>1000</PrecioMaximoCompra><!--precio limite -->
			<PrecioInformado>3000</PrecioInformado>
        </Concepto>
    </Conceptos>
</OrdenTrabajo>
 * @author luis_moyano
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrdenTrabajo {
	
	private String numeroOrdenTrabajo = "1";
     private String cliente = "IdentificadorFiscalCliente"; // <!--clave de busqueda del cliente? (el cliente se carga en crm o se carga primero en libertya?? lo tenemos que importar)-->
     private String contactoCliente = "Nombre del contacto del cliente asignado, tal como fue creado en el sistema."; //<!--Opcional_: ver si se quiere registrar el nombre del contacto del cliente-->
    // Ver de pasar este argumento a tipo java.util.Date en lugar de String. Aunque lo estaria validando del lado del servidor.
     private String fechaOrdenTrabajo = "dd/MM/yyyy"; // dd/mm/aaaa
     private String claveVendedor = "Nombre del comercial asignado, tal como fue creado en el sistema."; // <!--Opcional_: ver si se quiere asociar la ot con un vendedor>!-->
     private String codigoMoneda = "ARS"; //<!--moneda de facturación -->
     private String formaDePago = "CRED"; // <!-- opcional? que se pone por defecto? -->
     private String programaVencimientos = "30"; // <!--codigo de programa de vencimientos, opcional? que se pone por defecto? -->-->
    private String claveUnidadNegocio = "Clave de búsqueda de la unidad de negocio";
    private List<Concepto> conceptos = new ArrayList<Concepto>(); // <!--nro de proyecto -->
    private Carpeta carpeta = new Carpeta();
    private List<EntidadComercial> entidadesComerciales = new ArrayList<EntidadComercial>();
    
    public OrdenTrabajo() {
		this.conceptos.add(new Concepto());
		this.conceptos.add(new Concepto());
		
		this.entidadesComerciales.add(new EntidadComercial());
		this.entidadesComerciales.add(new EntidadComercial());
	}
    
	public Carpeta getCarpeta() {
		return carpeta;
	}

	public void setCarpeta(Carpeta carpeta) {
		this.carpeta = carpeta;
	}

	public String getNumeroOrdenTrabajo() {
		return numeroOrdenTrabajo;
	}
	public void setNumeroOrdenTrabajo(String numeroOrdenTrabajo) {
		this.numeroOrdenTrabajo = numeroOrdenTrabajo;
	}
	public String getCliente() {
		return cliente;
	}
	public void setCliente(String cliente) {
		this.cliente = cliente;
	}
	public String getContactoCliente() {
		return contactoCliente;
	}
	public void setContactoCliente(String contactoCliente) {
		this.contactoCliente = contactoCliente;
	}
	public String getFechaOrdenTrabajo() {
		return fechaOrdenTrabajo;
	}
	public void setFechaOrdenTrabajo(String fechaOrdenTrabajo) {
		this.fechaOrdenTrabajo = fechaOrdenTrabajo;
	}
	public String getClaveVendedor() {
		return claveVendedor;
	}
	public void setClaveVendedor(String claveVendedor) {
		this.claveVendedor = claveVendedor;
	}
	public String getCodigoMoneda() {
		return codigoMoneda;
	}
	public void setCodigoMoneda(String codigoMoneda) {
		this.codigoMoneda = codigoMoneda;
	}
	public String getFormaDePago() {
		return formaDePago;
	}
	public void setFormaDePago(String formaDePago) {
		this.formaDePago = formaDePago;
	}
	public String getProgramaVencimientos() {
		return programaVencimientos;
	}
	public void setProgramaVencimientos(String programaVencimientos) {
		this.programaVencimientos = programaVencimientos;
	}
	
	public List<Concepto> getConceptos() {
		return conceptos;
	}

	public void setConceptos(List<Concepto> conceptos) {
		this.conceptos = conceptos;
	}

	public List<EntidadComercial> getEntidadesComerciales() {
		return entidadesComerciales;
	}

	public void setEntidadesComerciales(List<EntidadComercial> entidadesComerciales) {
		this.entidadesComerciales = entidadesComerciales;
	}
	public String getClaveUnidadNegocio() {
		return claveUnidadNegocio;
	}
	public void setClaveUnidadNegocio(String claveUnidadNegocio) {
		this.claveUnidadNegocio = claveUnidadNegocio;
	}
	@Override
	public String toString() {
		return "OrdenTrabajo [numeroOrdenTrabajo=" + numeroOrdenTrabajo
				+ ", cliente=" + cliente + ", contactoCliente="
				+ contactoCliente + ", fechaOrdenTrabajo=" + fechaOrdenTrabajo
				+ ", claveVendedor=" + claveVendedor + ", codigoMoneda="
				+ codigoMoneda + ", formaDePago=" + formaDePago
				+ ", programaVencimientos=" + programaVencimientos
				+ ", conceptos=" + conceptos + ", carpeta=" + carpeta
				+ ", entidadesComerciales=" + entidadesComerciales + "]";
	}
        
}
