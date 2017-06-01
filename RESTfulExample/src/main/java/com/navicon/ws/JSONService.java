package com.navicon.ws;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.rpc.ServiceException;

import org.libertya.ws.bean.parameter.BPartnerParameterBean;
import org.libertya.ws.bean.parameter.CustomServiceParameterBean;
import org.libertya.ws.bean.parameter.FilteredColumnsParameterBean;
import org.libertya.ws.bean.parameter.OrderParameterBean;
import org.libertya.ws.bean.result.CustomServiceResultBean;
import org.libertya.ws.bean.result.MultipleRecordsResultBean;
import org.libertya.ws.bean.result.ResultBean;
import org.openXpertya.util.CLogger;

import ws.libertya.org.LibertyaWS;
import ws.libertya.org.LibertyaWSServiceLocator;

import com.navicon.entities.Campania;
import com.navicon.entities.Carpeta;
import com.navicon.entities.CategoriaIVA;
import com.navicon.entities.Concepto;
import com.navicon.entities.EntidadComercial;
import com.navicon.entities.Filtro;
import com.navicon.entities.FormaDePago;
import com.navicon.entities.MensajesRespuesta;
import com.navicon.entities.Moneda;
import com.navicon.entities.OrdenTrabajo;
import com.navicon.entities.ProgramaVencimiento;
import com.navicon.util.StringUtils;
import com.navicon.ws.reader.JerseyConfig;

/**
 * Metodo GET
 * http://localhost:8080/NaviconWS/ws/importacion/getOrdenTrabajo
 * 
 * Metodo POST
 * http://localhost:8080/NaviconWS/ws/importacion/guardarCarpeta
 * @author luis_moyano
 *
 */
@Path("/importacion")
public class JSONService {
	
	protected CLogger log = CLogger.getCLogger(JSONService.class);
	
	private static String usuarioLibertya = "UsuarioWebservice";
	
	private static String passwordLibertya = "UsuarioWebservice";
	
	private static Integer CLIENT_ID = 1010016;
	private static Integer ORG_ID = 1010053;
	//"http://192.168.0.35 /axis/services/LibertyaWS"
	private static String urlLibertyaWS = JerseyConfig.properties.get("url").toString();
	//private static String urlLibertyaWS = "http://200.125.78.99/axis/services/LibertyaWS";
	
	@GET
	@Path("/getMockMensajeRespuesta")
	@Produces(MediaType.APPLICATION_JSON)
	public MensajesRespuesta getMensajeRespuesta() {
		MensajesRespuesta mensajesDemo = new MensajesRespuesta();
		mensajesDemo.agregarMensaje("Esto es un primer mensaje de ejemplo");
		mensajesDemo.agregarMensaje("Esto es un segundo mensaje de ejemplo");
		mensajesDemo.setIdOrden("500005");
		return mensajesDemo;

	}
	
	@GET
	@Path("/getMockFiltro")
	@Produces(MediaType.APPLICATION_JSON)
	public Filtro getFiltro() {
		Filtro filtro = new Filtro();
		filtro.setValorBusqueda("3");
		return filtro;

	}
	
	@GET
	@Path("/getMockCarpeta")
	@Produces(MediaType.APPLICATION_JSON)
	public Carpeta getCarpeta()
	{
		return Carpeta.getMock();
	}
	
	@GET
	@Path("/getMockConcepto")
	@Produces(MediaType.APPLICATION_JSON)
	public Concepto getConcepto()
	{
		return Concepto.getMock();
	}

	@GET
	@Path("/getMockOrdenTrabajo")
	@Produces(MediaType.APPLICATION_JSON)
	public OrdenTrabajo getOrdenTrabajo() {
		return OrdenTrabajo.getMock();
	}
	
	@GET
	@Path("/getMockEntidadComercial")
	@Produces(MediaType.APPLICATION_JSON)
	public EntidadComercial getEntidadComercial() {
		return EntidadComercial.getMock();
	}
	
	@GET
	@Path("/getCategoriaIVA")
	@Produces(MediaType.APPLICATION_JSON)
	public List<CategoriaIVA> getCategoriaIVA() {
		return getIdNombreCategoriaIVA();
	}
	
	@POST
	@Path("/existeCodigoDeProveedor")
	@Produces(MediaType.APPLICATION_JSON)
	public MensajesRespuesta existeCodigoDeProveedor(Filtro filtro) {
		return existeEntidadComercial(filtro.getValorBusqueda());
	}
	
	@POST
	@Path("/existeCliente")
	@Produces(MediaType.APPLICATION_JSON)
	public MensajesRespuesta existeCliente(Filtro filtro) {
		return existeEntidadComercial(filtro.getValorBusqueda());
	}
	
	private MensajesRespuesta existeEntidadComercial(String clave)  {
		MensajesRespuesta mensajesRespuesta = new MensajesRespuesta();
		try {
			
			LibertyaWSServiceLocator locator = new LibertyaWSServiceLocator();
			locator.setLibertyaWSEndpointAddress(urlLibertyaWS);
			
			LibertyaWS lyws = locator.getLibertyaWS();
			String id = getIDEntidadComercialByClave(lyws, clave, CLIENT_ID, ORG_ID);
			
			Boolean existe = id.length() != 0;
			
			if (existe) {
				mensajesRespuesta.agregarMensaje("Existe el proveedor/cliente " + clave + " en Libertya");
			} 
			else {
				mensajesRespuesta.agregarMensaje("No existe el proveedor/cliente " + clave + " en Libertya");
			}
		} catch (Throwable t) {
			mensajesRespuesta.agregarMensaje("Error al realizar la consulta" + t.getMessage());
		}
		
		return mensajesRespuesta;
	}
	
	@POST
	@Path("/filtrarConceptos")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Concepto> filtrarConceptos(Filtro filtro) {
		List<Concepto> conceptos = new ArrayList<Concepto>();
		
		try {
			LibertyaWSServiceLocator locator = new LibertyaWSServiceLocator();
			locator.setLibertyaWSEndpointAddress(urlLibertyaWS);
		
			LibertyaWS lyws = locator.getLibertyaWS();
			
			FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, CLIENT_ID, ORG_ID);
			recParam.addColumnToFilter("Value");
			recParam.addColumnToFilter("Name");
			String where = " isactive='Y' and ad_client_id = " + CLIENT_ID + " and value like '" + filtro.getValorBusqueda() + "%'";
			
			MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "M_Product",where , false);
			if (recResult.isError()) {
				return new ArrayList<Concepto>();
			}
			if (recResult.getRecords().isEmpty())
				return new ArrayList<Concepto>();
			
			for (int i = 0; i < recResult.getRecords().size(); i++) {
				Map<String, String> ret = recResult.getRecords().get(i);
				
				Concepto concepto = new Concepto(); 
				concepto.setClaveConcepto(ret.get("Value"));
				concepto.setDescripcion(ret.get("Name"));
				
				conceptos.add(concepto);
			}
		} catch (Throwable t) {
			
		}
		
		return conceptos;
	}
	
	@GET
	@Path("/getConceptos")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Concepto> getConceptos() {
		List<Concepto> conceptos = new ArrayList<Concepto>();
		
		try {
			LibertyaWSServiceLocator locator = new LibertyaWSServiceLocator();
			locator.setLibertyaWSEndpointAddress(urlLibertyaWS);
		
			LibertyaWS lyws = locator.getLibertyaWS();
			
			FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, CLIENT_ID, ORG_ID);
			recParam.addColumnToFilter("Value");
			recParam.addColumnToFilter("Name");
			String where = " isactive='Y' and ad_client_id = " + CLIENT_ID;
			
			MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "M_Product",where , false);
			if (recResult.isError()) {
				return new ArrayList<Concepto>();
			}
			if (recResult.getRecords().isEmpty())
				return new ArrayList<Concepto>();
			
			for (int i = 0; i < recResult.getRecords().size(); i++) {
				Map<String, String> ret = recResult.getRecords().get(i);
				
				Concepto concepto = new Concepto(); 
				concepto.setClaveConcepto(ret.get("Value"));
				concepto.setDescripcion(ret.get("Name"));
				
				conceptos.add(concepto);
			}
		} catch (Throwable t) {
			
		}
		
		return conceptos;
	}
	
	@GET
	@Path("/getMonedas")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Moneda> getMonedas() {
		List<Moneda> monedas = new ArrayList<Moneda>();
		
		try {
			LibertyaWSServiceLocator locator = new LibertyaWSServiceLocator();
			locator.setLibertyaWSEndpointAddress(urlLibertyaWS);
		
			LibertyaWS lyws = locator.getLibertyaWS();
			
			FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, CLIENT_ID, ORG_ID);
			recParam.addColumnToFilter("ISO_Code");
			recParam.addColumnToFilter("Description");
			String where = " isactive='Y'";
			
			MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "C_Currency",where , false);
			if (recResult.isError()) {
				return new ArrayList<Moneda>();
			}
			if (recResult.getRecords().isEmpty())
				return new ArrayList<Moneda>();
			
			for (int i = 0; i < recResult.getRecords().size(); i++) {
				Map<String, String> ret = recResult.getRecords().get(i);
				
				Moneda moneda = new Moneda(); 
				moneda.setCodigoMoneda(ret.get("ISO_Code"));
				moneda.setDescripcion(ret.get("Description"));
				
				monedas.add(moneda);
			}
		} catch (Throwable t) {
			
		}
		
		return monedas;
	}
	
	@GET
	@Path("/getVendedores")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getVendedores() {
		List<String> vendedores = new ArrayList<String>();
		
		try {
			LibertyaWSServiceLocator locator = new LibertyaWSServiceLocator();
			locator.setLibertyaWSEndpointAddress(urlLibertyaWS);
		
			LibertyaWS lyws = locator.getLibertyaWS();
			
			FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, CLIENT_ID, ORG_ID);
			recParam.addColumnToFilter("Name");
			String where = " ad_client_id=" + CLIENT_ID;
			
			MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "ad_user",where , false);
			if (recResult.isError()) {
				return new ArrayList<String>();
			}
			if (recResult.getRecords().isEmpty())
				return new ArrayList<String>();
			
			for (int i = 0; i < recResult.getRecords().size(); i++) {
				Map<String, String> ret = recResult.getRecords().get(i);
				
				String vendedor = ret.get("Name");
				
				vendedores.add(vendedor);
			}
		} catch (Throwable t) {
			
		}
		
		return vendedores;
	}
	
	@POST
	@Path("/filtrarEntidadComercial")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<EntidadComercial> filtrarEntidadComercial(Filtro nombre) {
		return getEntidadesComerciales(nombre.getValorBusqueda());
	}
	
	@GET
	@Path("/getUnidadNegocios")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Campania> getCampanias() {
		return consultarCampanias();
	}
	
	@GET
	@Path("/getProgramaVencimientos")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ProgramaVencimiento> getProgramaVencimientos() {
		return consultarProgramaVencimientos();
	}
	
	@GET
	@Path("/getFormaDePago")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FormaDePago> getFormaDePago() {
		return Arrays.asList(FormaDePago.values());
	}
	
	@POST
	@Path("/guardarCarpeta")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MensajesRespuesta guardarCarpeta(Carpeta carpeta) {
		return procesarCarpeta(carpeta);
	}

	@POST
	@Path("/guardarOrdenesTrabajo")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MensajesRespuesta guardarOrdenTrabajo(OrdenTrabajo ordenTrabajo) {
		return procesarOrdenTrabajo(ordenTrabajo);
	}
	
	@POST
	@Path("/guardarEntidadComercial")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MensajesRespuesta guardarEntidadComercial(EntidadComercial entidadComercial) {
		return procesarEntidadComercial(entidadComercial);
	}
	
	private List<ProgramaVencimiento> consultarProgramaVencimientos() {
		List<ProgramaVencimiento> lista = new ArrayList<ProgramaVencimiento>();
		try {
			LibertyaWSServiceLocator locator = new LibertyaWSServiceLocator();
			locator.setLibertyaWSEndpointAddress(urlLibertyaWS);
		
			LibertyaWS lyws = locator.getLibertyaWS();
			
			FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, CLIENT_ID, ORG_ID);
			recParam.addColumnToFilter("Name");
			
			String where = " AD_Client_ID = " + CLIENT_ID + " AND AD_Org_ID = " + ORG_ID;
			
			MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "C_PaymentTerm",where , false);
			if (recResult.isError()) 
				return new ArrayList<ProgramaVencimiento>();
				
			if (recResult.getRecords().isEmpty()) {
				where = " AD_Client_ID = " + CLIENT_ID;
				recResult = lyws.recordQuery(recParam, "C_PaymentTerm", where , false);
				if (recResult.getRecords().isEmpty() || recResult.isError()) 
					return new ArrayList<ProgramaVencimiento>();
			}
			
			
			for (Map<String, String> ret : recResult.getRecords()) {
				String name = ret.get("Name");
				ProgramaVencimiento programaVencimiento = new ProgramaVencimiento();
				programaVencimiento.setProgramaVencimientos(name);
				lista.add(programaVencimiento);
			}
		
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return lista;
	}

	private List<Campania> consultarCampanias() {
		List<Campania> lista = new ArrayList<Campania>();
		try {
			LibertyaWSServiceLocator locator = new LibertyaWSServiceLocator();
			locator.setLibertyaWSEndpointAddress(urlLibertyaWS);
		
			LibertyaWS lyws = locator.getLibertyaWS();
		
			FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, CLIENT_ID, ORG_ID);
			recParam.addColumnToFilter("Name");
			recParam.addColumnToFilter("Value");
			
			MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "C_Campaign", null , false);
			if (recResult.isError()) 
				return new ArrayList<Campania>();
			if (recResult.getRecords().isEmpty())
				return new ArrayList<Campania>();
			
			
			
			for (Map<String, String> ret : recResult.getRecords()) {
				String Name = ret.get("Name");
				String Value = ret.get("Value");
				
				Campania entidadComercial = new Campania();
				
				entidadComercial.setNombreUnidadNegocio(Name);
	
				entidadComercial.setClaveUnidadNegocio(Value);
				
				lista.add(entidadComercial);
			}
		
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return lista;
	}

	private List<EntidadComercial> getEntidadesComerciales(String nombre) {
		List<EntidadComercial> lista = new ArrayList<EntidadComercial>();
		try {
			LibertyaWSServiceLocator locator = new LibertyaWSServiceLocator();
			locator.setLibertyaWSEndpointAddress(urlLibertyaWS);
		
			LibertyaWS lyws = locator.getLibertyaWS();
		
			FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, CLIENT_ID, ORG_ID);
			recParam.addColumnToFilter("C_BPartner_ID");
			recParam.addColumnToFilter("TaxID");
			recParam.addColumnToFilter("Value");
			recParam.addColumnToFilter("TaxIdType");
			recParam.addColumnToFilter("Name");
			recParam.addColumnToFilter("IsVendor");
			recParam.addColumnToFilter("IsCustomer");
			recParam.addColumnToFilter("IsEmployee");
			recParam.addColumnToFilter("C_BP_Group_ID");
			recParam.addColumnToFilter("C_Categoria_Iva_ID");
						
			String where = "name like '%" + nombre + "%'";
			
			MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "C_BPartner",where , false);
			if (recResult.isError()) 
				return new ArrayList<EntidadComercial>();
			if (recResult.getRecords().isEmpty())
				return new ArrayList<EntidadComercial>();
			
			
			
			for (Map<String, String> ret : recResult.getRecords()) {
				String TaxID = ret.get("TaxID");
				String value = ret.get("Value");
				String taxidtype = ret.get("TaxIdType");
				String name = ret.get("Name");
				String isvendor = ret.get("IsVendor");
				String iscustomer = ret.get("IsCustomer");
				String isemployee = ret.get("IsEmployee");
				String c_bp_group_id = ret.get("C_BP_Group_ID");
				String C_Categoria_Iva_ID = ret.get("C_Categoria_Iva_ID");
				
				String bPartnerId = ret.get("C_BPartner_ID");
				
				String bPartnerIdLocationID = getIDLocationlByIdEntidadComercial(lyws, bPartnerId, CLIENT_ID, ORG_ID);
				
				Map<String, String> datosDireccion = getDatosDireccion(lyws, bPartnerIdLocationID, CLIENT_ID, ORG_ID); 
	
				String address1 = datosDireccion.get("Address1");
				String postal = datosDireccion.get("Postal");
				String city =  datosDireccion.get("City");
				String c_country_id = datosDireccion.get("C_Country_ID");
				String c_region_id = datosDireccion.get("C_Region_ID");
				
				EntidadComercial entidadComercial = new EntidadComercial();
				
				entidadComercial.setCliente(TaxID);
	
				entidadComercial.setClave(value);
				entidadComercial.setCodigoIdentificacion(TaxID);
				entidadComercial.setTipoIdentificacion(taxidtype);
				entidadComercial.setNombre(name);
				entidadComercial.setEsproveedor(isvendor);
				entidadComercial.setEsCliente(iscustomer);
				entidadComercial.setEsEmpleado(isemployee);
				entidadComercial.setCodigoGrupoEC(c_bp_group_id);
				entidadComercial.setCodigoIVA(C_Categoria_Iva_ID);
	
				entidadComercial.setDireccion(address1);
				entidadComercial.setCP(postal);
				entidadComercial.setCiudad(city);
				entidadComercial.setCodigoPais(c_country_id);
				entidadComercial.setProvincia(c_region_id);
	
				entidadComercial.setCuitProveedor(TaxID);
				
				lista.add(entidadComercial);
			}
		
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return lista;
	}

	private Map<String, String> getDatosDireccion(LibertyaWS lyws, String bPartnerIdLocationID, Integer clientId, Integer orgId) throws RemoteException {
		FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, clientId, orgId);
		recParam.addColumnToFilter("C_Location_ID");
		String where = "C_BPartner_Location_ID=" + bPartnerIdLocationID;
		MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "C_BPartner_Location",where , false);
		if (recResult.getRecords().isEmpty())
			return new HashMap<String, String>();
		
		Map<String, String> ret = recResult.getRecords().get(0);
		
		String C_Location_ID = ret.get("C_Location_ID");
		
		recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, clientId, orgId);
		
		recParam.addColumnToFilter("Address1");
		recParam.addColumnToFilter("Postal");
		recParam.addColumnToFilter("City");
		recParam.addColumnToFilter("C_Country_ID");
		recParam.addColumnToFilter("C_Region_ID");
		where = "C_Location_ID=" + C_Location_ID;
		recResult = lyws.recordQuery(recParam, "C_Location",where , false);
		
		if (recResult.getRecords().isEmpty())
			return new HashMap<String, String>();
		
		ret = recResult.getRecords().get(0);
		
		return ret;
	}
	
	private MensajesRespuesta procesarEntidadComercial(EntidadComercial entidadComercial) {
		MensajesRespuesta mensajesRespuesta = validarEntidadComercial(entidadComercial);

		if (mensajesRespuesta.hayMensajes()) {
			return mensajesRespuesta;
		}

		try {
			mensajesRespuesta = insertarOActualizarEntidadComercial(entidadComercial);
		} catch (Exception e) {
			mensajesRespuesta.agregarMensaje(e.getMessage());
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		return mensajesRespuesta;
	}
	
	private MensajesRespuesta validarEntidadComercial(
			EntidadComercial entidadComercial) {
		MensajesRespuesta mensajesRespuesta = new MensajesRespuesta();
		
		if (!StringUtils.isNumeric(entidadComercial.getCodigoIVA())) {
			mensajesRespuesta.agregarMensaje("Codigo de IVA debe ser numerico");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (!StringUtils.isNumeric(entidadComercial.getCodigoIdentificacion())) {
			mensajesRespuesta.agregarMensaje("CodigoIdentificacion debe ser numerico");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (!StringUtils.isNumeric(entidadComercial.getCodigoGrupoEC())) {
			mensajesRespuesta.agregarMensaje("codigoGrupoEC debe ser numerico");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (!StringUtils.isNumeric(entidadComercial.getTipoIdentificacion())) {
			mensajesRespuesta.agregarMensaje("tipoIdentificacion debe ser numerico");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		return mensajesRespuesta;
	}

	private MensajesRespuesta insertarOActualizarEntidadComercial(
			EntidadComercial entidadComercial) throws Exception {
		MensajesRespuesta mensajesRespuesta = new MensajesRespuesta();
		try {	
			LibertyaWSServiceLocator locator = new LibertyaWSServiceLocator();
			locator.setLibertyaWSEndpointAddress(urlLibertyaWS);
		
		
			LibertyaWS lyws = locator.getLibertyaWS();
			
			// Prueba 2: Crear una entidad comercial
			BPartnerParameterBean data2 = new BPartnerParameterBean(usuarioLibertya, passwordLibertya, CLIENT_ID, ORG_ID);
			
			data2.addColumnToBPartner("value", entidadComercial.getClave());
			data2.addColumnToBPartner("taxid", entidadComercial.getCodigoIdentificacion());
			data2.addColumnToBPartner("taxidtype", entidadComercial.getTipoIdentificacion());
			data2.addColumnToBPartner("name", entidadComercial.getNombre());
			data2.addColumnToBPartner("name2", "-");
			//data2.addColumnToBPartner("c_bp_group_id", "1010045");
			data2.addColumnToBPartner("isonetime", "N");
			data2.addColumnToBPartner("isprospect", "Y");
			data2.addColumnToBPartner("isvendor", entidadComercial.getEsproveedor());
			data2.addColumnToBPartner("iscustomer", entidadComercial.getEsCliente());
			data2.addColumnToBPartner("isemployee", entidadComercial.getEsEmpleado());
			data2.addColumnToBPartner("issalesrep", "N");
			data2.addColumnToBPartner("c_bp_group_id", entidadComercial.getCodigoGrupoEC());
			data2.addColumnToBPartner("C_Categoria_Iva_ID", entidadComercial.getCodigoIVA());
			
			//data2.addColumnToBPartner("c_paymentterm_id", "1010083");
			//data2.addColumnToBPartner("m_pricelist_id", "1010595");

			data2.addColumnToLocation("address1", entidadComercial.getDireccion());
			data2.addColumnToLocation("postal", entidadComercial.getCP());
			data2.addColumnToLocation("city", entidadComercial.getCiudad());
			data2.addColumnToLocation("c_country_id", entidadComercial.getCodigoPais());
			data2.addColumnToLocation("c_region_id", entidadComercial.getProvincia());
			
			ResultBean resultado = null;
			
			String bPartnerId = getIDEntidadComercialByCuit(lyws, entidadComercial.getCodigoIdentificacion(), CLIENT_ID, ORG_ID);
			
			if (StringUtils.isEmpty(bPartnerId)) {
			
				resultado = lyws.bPartnerCreate(data2);
			} else {
				
				String bPartnerIdLocationID = getIDLocationlByIdEntidadComercial(lyws, bPartnerId, CLIENT_ID, ORG_ID);
				
				resultado = lyws.bPartnerUpdate(data2, Integer.parseInt(bPartnerId), Integer.parseInt(bPartnerIdLocationID));
			}
			
			if (resultado.isError()) {
				mensajesRespuesta.agregarMensaje(resultado.getErrorMsg());
				mensajesRespuesta.setHayErrores(Boolean.TRUE);
				return mensajesRespuesta;
			}
			
			log.log(Level.SEVERE,resultado.toString());
			
		} catch (Exception e) {
			log.log(Level.SEVERE,"Error",e);
			throw e;
		}
		
		return mensajesRespuesta;
	}
	
	private String getIDLocationlByIdEntidadComercial(LibertyaWS lyws, String bPartnerId, Integer clientId, Integer orgId) throws RemoteException {
		
		FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, clientId, orgId);
		recParam.addColumnToFilter("C_BPartner_Location_ID");
		String where = "C_BPartner_ID=" + bPartnerId 
				+ " and isactive = 'Y' and isbillto = 'Y'"
				+ " order by created desc;";
		MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "C_BPartner_Location",where , false);
		if (recResult.getRecords().isEmpty())
			return "";
		
		Map<String, String> ret = recResult.getRecords().get(0);
		
		return ret.get("C_BPartner_Location_ID");
		
	}
	
	private List<CategoriaIVA> getIdNombreCategoriaIVA() {
		List<CategoriaIVA> categoriaIVAs = new ArrayList<CategoriaIVA>();
		
		try {
			LibertyaWSServiceLocator locator = new LibertyaWSServiceLocator();
			locator.setLibertyaWSEndpointAddress(urlLibertyaWS);
		
			LibertyaWS lyws = locator.getLibertyaWS();
			
			FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, CLIENT_ID, ORG_ID);
			recParam.addColumnToFilter("C_Categoria_Iva_ID");
			recParam.addColumnToFilter("Name");
			String where = " ad_client_id=" + CLIENT_ID;
			
			MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "C_Categoria_Iva",where , false);
			if (recResult.isError()) {
				return new ArrayList<CategoriaIVA>();
			}
			if (recResult.getRecords().isEmpty())
				return new ArrayList<CategoriaIVA>();
			
			
			
			for (int i = 0; i < recResult.getRecords().size(); i++) {
				Map<String, String> ret = recResult.getRecords().get(i);
				
				CategoriaIVA categoriaIVA = new CategoriaIVA();
				
				categoriaIVA.setIdCategoriaIVA(ret.get("C_Categoria_Iva_ID"));
				categoriaIVA.setNombre(ret.get("Name"));
				
				categoriaIVAs.add(categoriaIVA);
			}
		} catch (Throwable t) {
			
		}
		
		
		return categoriaIVAs;
	}

	private MensajesRespuesta procesarCarpeta(Carpeta carpeta) {
		MensajesRespuesta mensajesRespuesta = validarCarpeta(carpeta);

		if (mensajesRespuesta.hayMensajes()) {
			return mensajesRespuesta;
		}

		try {
			insertarOActualizarCarpeta(carpeta, mensajesRespuesta);
		} catch (Exception e) {
			mensajesRespuesta.agregarMensaje(e.getMessage());
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		return mensajesRespuesta;
	}

	private MensajesRespuesta procesarOrdenTrabajo(OrdenTrabajo ordenTrabajo) {

		MensajesRespuesta mensajesRespuesta = validarOrdenTrabajo(ordenTrabajo);

		if (!mensajesRespuesta.getMensajes().isEmpty()) {
			return mensajesRespuesta;
		}

		return insertarOrdenTrabajo(ordenTrabajo);
	}
	
	private String insertarOActualizarCarpeta(Carpeta carpeta, MensajesRespuesta mensajesRespuesta) throws Exception {
		
		log.log(Level.SEVERE, "Se insertara o actualizara la carpeta: " + carpeta);
		
		try {
		
			LibertyaWSServiceLocator locator = new LibertyaWSServiceLocator();
			locator.setLibertyaWSEndpointAddress(urlLibertyaWS);
		
			LibertyaWS lyws = locator.getLibertyaWS();
		
			String idCarpeta = existeCarpeta(lyws, carpeta.getClave());
			
			if (StringUtils.isEmpty(idCarpeta)) {
				log.log(Level.SEVERE,"No existe la carpeta");
				idCarpeta = agregarCarpeta(lyws, carpeta, mensajesRespuesta);
			}
			else
			{
				log.log(Level.SEVERE,"Existe la carpeta, se actualizara");
				actualizarCarpeta(lyws, idCarpeta, carpeta, mensajesRespuesta);
			}
			log.log(Level.SEVERE, "El id de carpeta a insertar en la orden es: " + idCarpeta);
			return idCarpeta;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error ", e);
			throw e;
		}
		
	}
	
	private void actualizarCarpeta(LibertyaWS lyws, String idCarpeta, Carpeta carpeta, MensajesRespuesta mensajesRespuesta)  {
		try {
			CustomServiceParameterBean test22 = new CustomServiceParameterBean(usuarioLibertya, passwordLibertya, 1010016, 0);
			// Nombre de la clase a invocar
			test22.setClassName("org.libertya.ws.service.ProjectUpdateService");
			
			// Especificacion de parametros
			test22.addParameter("userName", passwordLibertya);
			test22.addParameter("password", passwordLibertya);
			test22.addParameter("clientID", CLIENT_ID.toString());
			test22.addParameter("orgID", ORG_ID.toString());
			
			test22.addParameter("value", carpeta.getClave());
			test22.addParameter("name", carpeta.getNombre());
			test22.addParameter("datecontract", StringUtils.getFechaFormateado(carpeta.getFechaInicio(), "dd/MM/yyyy", "yyyy-MM-dd HH:mm:s") );
			test22.addParameter("datefinish", StringUtils.getFechaFormateado(carpeta.getFechaFin(), "dd/MM/yyyy", "yyyy-MM-dd HH:mm:s"));
			test22.addParameter("projectID", idCarpeta);
			
			CustomServiceResultBean customServiceResultBean = lyws.customService(test22);
			if (customServiceResultBean.isError()) {
				mensajesRespuesta.setHayErrores(Boolean.TRUE);
				log.log(Level.SEVERE, customServiceResultBean.getErrorMsg());
				mensajesRespuesta.agregarMensaje("La carpeta correspondiente a la OT  no pudo ser actualizada en la base de datos.");
			}
			
			log.log(Level.SEVERE, customServiceResultBean.toString());
		} catch (Throwable t) {
			log.log(Level.SEVERE, "Error", t);
		}
		
		
	}

	private String agregarCarpeta(LibertyaWS lyws, Carpeta carpeta, MensajesRespuesta mensajesRespuesta)  {
		try {
			CustomServiceParameterBean test22 = new CustomServiceParameterBean(usuarioLibertya, passwordLibertya, 1010016, 0);
			// Nombre de la clase a invocar
			test22.setClassName("org.libertya.ws.service.ProjectCreateService");
			
			// Especificacion de parametros
			test22.addParameter("userName", passwordLibertya);
			test22.addParameter("password", passwordLibertya);
			test22.addParameter("clientID", CLIENT_ID.toString());
			test22.addParameter("orgID", ORG_ID.toString());
			
			test22.addParameter("value", carpeta.getClave());
			test22.addParameter("name", carpeta.getNombre());
			test22.addParameter("datecontract", StringUtils.getFechaFormateado(carpeta.getFechaInicio(), "dd/MM/yyyy", "yyyy-MM-dd HH:mm:s") );
			test22.addParameter("datefinish", StringUtils.getFechaFormateado(carpeta.getFechaFin(), "dd/MM/yyyy", "yyyy-MM-dd HH:mm:s"));
			
			CustomServiceResultBean customServiceResultBean = lyws.customService(test22);
			
			if (customServiceResultBean.isError()) {
				mensajesRespuesta.setHayErrores(Boolean.TRUE);
				log.log(Level.SEVERE,"Error" + customServiceResultBean.getErrorMsg());
				mensajesRespuesta.agregarMensaje("La carpeta correspondiente a la OT  no existe en la base de datos y no pudo ser insertada.");
			}
			
			String idCarpeta = customServiceResultBean.getResult()[0].getValues()[0];// getMainResult().get("C_Project_ID");
			
			log.log(Level.SEVERE,customServiceResultBean.toString());
			
			return idCarpeta;
		} catch (Throwable t) {
			log.log(Level.SEVERE,"Error",t);
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
			mensajesRespuesta.agregarMensaje("La carpeta correspondiente a la OT  no existe en la base de datos y no pudo ser insertada.");
		}
		
		return "";
		
	}

	private String existeCarpeta(LibertyaWS lyws, String clave) throws RemoteException {
		FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, CLIENT_ID, ORG_ID);
		recParam.addColumnToFilter("C_Project_ID");
		MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "C_Project", "Value = '" + clave + "'", false);
		if (recResult.getRecords().isEmpty())
			return "";
		Map<String, String> ret = recResult.getRecords().get(0);
		
		return ret.get("C_Project_ID");
	}

	private MensajesRespuesta insertarOrdenTrabajo(OrdenTrabajo ordenTrabajoJson) {
		MensajesRespuesta mensajesRespuesta = new MensajesRespuesta();
		try {
			// Inserta o actualiza la carpeta.
			String idCarpeta = insertarOActualizarCarpeta(ordenTrabajoJson.getCarpeta(), mensajesRespuesta);
			
			if (mensajesRespuesta.getHayErrores()) {
				log.log(Level.SEVERE, "Existen mensajes de error: " + mensajesRespuesta);
				return mensajesRespuesta;
			}
			/*Se ELIMINA YA QUE ESTE PROCESO NO DARA DE ALTA NI ACTUALIZARA ENTIDADES COMERCIALES
			for (EntidadComercial entidadComercial : ordenTrabajoJson.getEntidadesComerciales())
			{
				MensajesRespuesta mensajesRespuestaComercial = new MensajesRespuesta();
				mensajesRespuestaComercial = insertarOActualizarEntidadComercial(entidadComercial);
				if (mensajesRespuestaComercial.getHayErrores())
					return mensajesRespuestaComercial;
			}*/
			
			// Conexión al WS
			LibertyaWSServiceLocator locator = new LibertyaWSServiceLocator();
			
			locator.setLibertyaWSEndpointAddress(urlLibertyaWS);
			//locator.setLibertyaWSEndpointAddress("http://192.168.0.35 /axis/services/LibertyaWS");
			// Recuperar el servicio
			LibertyaWS lyws = locator.getLibertyaWS();
			
			OrderParameterBean ordenDeTrabajo = new OrderParameterBean(usuarioLibertya, passwordLibertya, CLIENT_ID, ORG_ID);
			
			ordenDeTrabajo.addColumnToHeader("c_doctypetarget_id", getIdDocTypeTarget(lyws, CLIENT_ID, ORG_ID));
			ordenDeTrabajo.addColumnToHeader("dateordered", StringUtils.getFechaFormateado(ordenTrabajoJson.getFechaOrdenTrabajo(), "dd/MM/yyyy", "yyyy-MM-dd HH:mm:s")); // Fecha
			String idEntidadComercial = getIDEntidadComercialByClave(lyws, ordenTrabajoJson.getClaveCliente(), CLIENT_ID, ORG_ID);
			if (idEntidadComercial.isEmpty()) {
				mensajesRespuesta.agregarMensaje("El cliente informado no existe en la base de datos.");
				log.log(Level.SEVERE, mensajesRespuesta.toString());
				return mensajesRespuesta;
			}
			// Se envia en los parametros del constructor
			ordenDeTrabajo.addColumnToHeader("C_BPartner_Location_ID", getIdDireccionEntidadComercial(lyws, idEntidadComercial, CLIENT_ID, ORG_ID));
			ordenDeTrabajo.addColumnToHeader("M_Warehouse_ID", getIdAlmacen(lyws, CLIENT_ID, ORG_ID));
			ordenDeTrabajo.addColumnToHeader("M_PriceList_ID", getIdListaPrecioVentas(lyws, CLIENT_ID, ORG_ID));
			String idMoneda = getIdMoneda(lyws, ordenTrabajoJson.getCodigoMoneda(), CLIENT_ID, ORG_ID);
			if (StringUtils.isEmpty(idMoneda)) {
				mensajesRespuesta.setHayErrores(Boolean.TRUE);
				mensajesRespuesta.agregarMensaje("El código de moneda informado no existe en la base de datos.");
				return mensajesRespuesta;
			}
			ordenDeTrabajo.addColumnToHeader("C_Currency_ID", idMoneda);
			ordenDeTrabajo.addColumnToHeader("SalesRep_ID", getComercial(lyws, ordenTrabajoJson.getContactoCliente(), CLIENT_ID, ORG_ID));
			
			FormaDePago formaDePago = FormaDePago.getFormaDePago(ordenTrabajoJson.getFormaDePago());
			if (formaDePago == null) {
				mensajesRespuesta.agregarMensaje("La forma de pago " + ordenTrabajoJson.getFormaDePago() + " no existe en la base de datos, se ha asignado una forma de pagos por defecto.");
				formaDePago = FormaDePago.EFECTIVO;
			}
			ordenDeTrabajo.addColumnToHeader("PaymentRule", formaDePago.getCodigoLibertya());
			
			if (FormaDePago.A_CREDITO.equals(formaDePago)) {
				String idEsquemaVencimiento = getIdPaymentTermId(lyws, ordenTrabajoJson.getProgramaVencimientos(), CLIENT_ID, ORG_ID);
				if (StringUtils.isEmpty(idEsquemaVencimiento)) {
					mensajesRespuesta.setHayErrores(Boolean.TRUE);
					mensajesRespuesta.agregarMensaje("El esquema de vencimientos "+ ordenTrabajoJson.getProgramaVencimientos() + " no existe en la base de datos, se ha asignado un esquema de vencimientos por defecto.");
					return mensajesRespuesta;
				}
				ordenDeTrabajo.addColumnToHeader("C_PaymentTerm_ID", idEsquemaVencimiento);
			}
			log.log(Level.SEVERE, "Se asociara el id de carpeta: " + idCarpeta);
			ordenDeTrabajo.addColumnToHeader("C_Project_ID", idCarpeta);
			// MARCAR AL C_ORDER COMO Transaccion de venta
			// Se debe marcar IsSOTrx='Y'
			ordenDeTrabajo.addColumnToHeader("IsSOTrx", "Y");
			// La orden de trabajo esta activa
			ordenDeTrabajo.addColumnToHeader("IsActive", "Y");
			
			//VER el campo C_PaymentTerm_ID
			ordenDeTrabajo.addColumnToHeader("Description", "");
			
			String idCampania = getCampaniaPorValue(lyws, ordenTrabajoJson.getClaveUnidadNegocio(), CLIENT_ID, ORG_ID);
			
			if (StringUtils.isEmpty(idCampania)) {
				mensajesRespuesta.setHayErrores(Boolean.TRUE);
				mensajesRespuesta.agregarMensaje("La unidad de negocio informada para la Orden de trabajo no existe en la base de datos.");
				return mensajesRespuesta;
			}
			
			ordenDeTrabajo.addColumnToHeader("C_Campaign_ID", idCampania);
			ordenDeTrabajo.addColumnToHeader("Estado_Facturacion", "ESPERA DE APROBACION");
			ordenDeTrabajo.addColumnToHeader("Estado_Pedido_Proveedor", "ESPERA DE APROBACION");
			
			for (int i = 0; i  < ordenTrabajoJson.getConceptos().size() ; i++)
			{
				Concepto concepto = ordenTrabajoJson.getConceptos().get(i);
				
				String idProducto = getIdProducto(lyws, concepto.getClaveConcepto() , CLIENT_ID, ORG_ID);
				concepto.setIdConceptoLibertya(idProducto);
			}
			
			if (mensajesRespuesta.getHayErrores()) {
				log.log(Level.SEVERE, "Existen claves de conceptos que no existen en libertya.");
				return mensajesRespuesta;
			}
			
			mensajesRespuesta = armarMensajeErrorConceptoNoExiste(ordenTrabajoJson.getConceptos());
			if (mensajesRespuesta.getHayErrores()) {
				log.log(Level.SEVERE, "No existe alguno de los conceptos: " + mensajesRespuesta);
				return mensajesRespuesta;
			}
			
			mensajesRespuesta = crearVersionesListaPrecios(lyws, ordenTrabajoJson.getCarpeta().getNombre(), ordenTrabajoJson.getConceptos());
			if (mensajesRespuesta.getHayErrores()) {
				log.log(Level.SEVERE, "Error al crear las versiones de las listas de precios: " + mensajesRespuesta);
				return mensajesRespuesta;
			}
			
			for (int i = 0; i  < ordenTrabajoJson.getConceptos().size() ; i++)
			{
			
				Concepto concepto = ordenTrabajoJson.getConceptos().get(i);
				ordenDeTrabajo.newDocumentLine();
				ordenDeTrabajo.addColumnToCurrentLine("Line", (i + 1)+"");
				ordenDeTrabajo.addColumnToCurrentLine("QtyEntered", concepto.getCantidad());
				ordenDeTrabajo.addColumnToCurrentLine("PriceEntered", concepto.getPrecioFacturacion());	
				
				// columnas agregadas a c_orderline
				ordenDeTrabajo.addColumnToCurrentLine("preciomaximocompra", concepto.getPrecioMaximoCompra());
				ordenDeTrabajo.addColumnToCurrentLine("precioinformado", concepto.getPrecioInformado());
				
				ordenDeTrabajo.addColumnToCurrentLine("C_Tax_ID", getIdImpuesto(lyws, concepto.getClaveConcepto(), CLIENT_ID, ORG_ID));
				
				ordenDeTrabajo.addColumnToCurrentLine("M_Product_ID", concepto.getIdConceptoLibertya());
				
				if (concepto.getClaveProveedor() != null && !concepto.getClaveProveedor().isEmpty()) {
					// Fue enviado el codigo de proveedor y se evalua que exista en Libertya
					String idEntidadComercialProveedor = getIDEntidadComercialByClave(lyws, concepto.getClaveProveedor(), CLIENT_ID, ORG_ID);
					
					if (idEntidadComercialProveedor.isEmpty()) {
						mensajesRespuesta.agregarMensaje("No existe la clave de empresa " + concepto.getClaveProveedor() + " en Libertya");
						return mensajesRespuesta;
					}
					ordenDeTrabajo.addColumnToCurrentLine("proveedor_id", idEntidadComercialProveedor);
				}
			}
			// Para que deje la orden abierta y pueda ser modificada desde libertya.
			boolean completeOrder = false;
			boolean createInvoice = false;
			boolean completeInvoice = false;
			ResultBean orderResult = lyws.orderCreateCustomer(ordenDeTrabajo, Integer.valueOf(idEntidadComercial), null, null, completeOrder, createInvoice, completeInvoice);
			if (orderResult.isError()) {
				log.log(Level.SEVERE, orderResult.getErrorMsg());
				mensajesRespuesta.agregarMensaje("La  Orden de trabajo no pudo insertarse.");
				mensajesRespuesta.setHayErrores(Boolean.TRUE);
				return mensajesRespuesta;
			}
				
			log.log(Level.SEVERE, "Orden retornada : " + orderResult);
			String idOrden = orderResult.getMainResult().get("Order_DocumentNo");
			mensajesRespuesta.setIdOrden(idOrden);
			log.log(Level.SEVERE, "Mensaje a retornar: " + mensajesRespuesta);
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE,"Error" + e.getMessage(), e);
			mensajesRespuesta.agregarMensaje("La  Orden de trabajo no pudo insertarse.");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
			return mensajesRespuesta;
		}
		
		return mensajesRespuesta;

	}

	private MensajesRespuesta armarMensajeErrorConceptoNoExiste(
			List<Concepto> conceptos) {
		
		MensajesRespuesta mensajesRespuesta = new MensajesRespuesta();
		
		for (int i = 0; i  < conceptos.size() ; i++)
		{
			Concepto concepto = conceptos.get(i);
			
			if (concepto.getIdConceptoLibertya() == null || concepto.getIdConceptoLibertya().trim().isEmpty()) {
				mensajesRespuesta.setHayErrores(Boolean.TRUE);
				mensajesRespuesta.agregarMensaje("El codigo de concepto " + concepto.getClaveConcepto() + " no existe en la base de datos.");
			}
			
		}
		
		return mensajesRespuesta;
	}

	private MensajesRespuesta crearVersionesListaPrecios(LibertyaWS lyws, String nombreCarpeta, List<Concepto> conceptos) throws RemoteException {
		log.log(Level.SEVERE, "Se crearan las versiones de las listas de precions para los conceptos: " + conceptos);
		MensajesRespuesta mensajesRespuesta = new MensajesRespuesta();
		
		CustomServiceParameterBean parametros = new CustomServiceParameterBean(usuarioLibertya, passwordLibertya, 1010016, 0);
		// Nombre de la clase a invocar
		parametros.setClassName("org.libertya.ws.service.PriceListCreate");
		
		// Especificacion de parametros
		parametros.addParameter("userName", passwordLibertya);
		parametros.addParameter("password", passwordLibertya);
		parametros.addParameter("AD_Client_ID", CLIENT_ID.toString());
		parametros.addParameter("ad_org_id", ORG_ID.toString());
		
		parametros.addParameter("nombreCarpeta", nombreCarpeta);
		for (int i = 0; i < conceptos.size(); i++) {
			Concepto concepto = conceptos.get(i);
			
			String key = "precioFacturacion" + StringUtils.leftPad(i+"", 2, "0");
			parametros.addParameter(key, concepto.getPrecioFacturacion());
			
			key   = "preciomaximocompra" + StringUtils.leftPad(i+"", 2, "0");
			parametros.addParameter(key, concepto.getPrecioMaximoCompra());
			
			key = "M_Product_ID" + StringUtils.leftPad(i+"", 2, "0");
			parametros.addParameter(key, concepto.getIdConceptoLibertya());
			
		}
		
		CustomServiceResultBean customServiceResultBean = lyws.customService(parametros);
		
		if (customServiceResultBean.isError()) {
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
			mensajesRespuesta.agregarMensaje(customServiceResultBean.getErrorMsg());
		}
		
		log.log(Level.SEVERE, customServiceResultBean.toString());
		
		return mensajesRespuesta;
	}

	private String getIdPaymentTermId(LibertyaWS lyws, String programaVencimientos, Integer clientId, Integer orgId) throws RemoteException {
		FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, clientId, orgId);
		recParam.addColumnToFilter("C_PaymentTerm_ID");
		MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "C_PaymentTerm", "name = '" + programaVencimientos + "'" + " AND AD_Client_ID = " + clientId + " AND AD_Org_ID = " + orgId, false);
		
		if (recResult.getRecords().isEmpty()) {
			recResult = lyws.recordQuery(recParam, "C_PaymentTerm", "name = '" + programaVencimientos + "'" + " AND AD_Client_ID = " + clientId, false);
		
			if (recResult.getRecords().isEmpty()) {
				return "";
			}
		}
		Map<String, String> ret = recResult.getRecords().get(0);
		
		return ret.get("C_PaymentTerm_ID");
	}

	private String getCampaniaPorValue(LibertyaWS lyws, String unidadNegocio, Integer clientId, Integer orgId) throws RemoteException {
		FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, clientId, orgId);
		recParam.addColumnToFilter("C_Campaign_ID");
		MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "C_Campaign", "Value = '" + unidadNegocio + "'", false);
		if (recResult.getRecords().isEmpty())
			return "";
		Map<String, String> ret = recResult.getRecords().get(0);
		
		return ret.get("C_Campaign_ID");
	}

	private String getIdDocTypeTarget(LibertyaWS lyws, Integer clientId, Integer orgId) throws RemoteException {
		FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, clientId, orgId);
		recParam.addColumnToFilter("C_DocType_ID");
		StringBuffer where = new StringBuffer( "C_DocType.DocBaseType IN ('SOO', 'POO') ");
		where.append(" AND C_DocType.DocTypeKey NOT IN ('SOSOT','SOSOTD')  ");
		where.append(" AND isactive = 'Y' ");
		where.append(" AND C_DocType.IsSOTrx='Y' ");
		where.append(" AND C_DocType.AD_Client_ID = ").append(clientId);
		where.append(" AND C_DocType.Name = 'Pedido'");
		where.append(" AND C_DocType.EnableInCreateFromShipment = 'Y'");
		MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "C_DocType", where.toString(), false);
		if (recResult.getRecords().isEmpty())
			return "";
		Map<String, String> ret = recResult.getRecords().get(0);
		
		return ret.get("C_DocType_ID");
	}

	private String getIdProducto(LibertyaWS lyws, String claveConcepto, Integer clientId, Integer orgId) throws RemoteException {
		FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, clientId, orgId);
		recParam.addColumnToFilter("M_Product_ID");
		MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "M_Product", "value = '" + claveConcepto + "'", false);
		if (recResult.getRecords().isEmpty())
			return "";
		Map<String, String> ret = recResult.getRecords().get(0);
		
		return ret.get("M_Product_ID");
	}

	private String getIdImpuesto(LibertyaWS lyws, String claveConcepto, Integer clientId, Integer orgId) throws RemoteException {
		FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, clientId, orgId);
		recParam.addColumnToFilter("C_TaxCategory_ID");
		MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "M_Product", "value = '" + claveConcepto + "'", false);
		if (recResult.getRecords().isEmpty())
			return "";
		Map<String, String> ret = recResult.getRecords().get(0);
		
		return ret.get("C_TaxCategory_ID");
	}
	
	

	private String getComercial(LibertyaWS lyws, String contactoCliente, Integer clientId, Integer orgId) throws RemoteException {
		log.log(Level.SEVERE, "Se buscara el id del comercial de ventas : " + contactoCliente);
		FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, clientId, orgId);
		recParam.addColumnToFilter("ad_user_id");
		MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "ad_user", "name = '" + contactoCliente + "'", false);
		if (recResult.getRecords().isEmpty())
			return "";
		Map<String, String> ret = recResult.getRecords().get(0);
		
		return ret.get("AD_User_ID");
	}

	private String getIdMoneda(LibertyaWS lyws, String codigoISOMoneda, Integer clientId, Integer orgId) throws RemoteException {
		log.log(Level.SEVERE, "Se buscara el id de la moneda: " + codigoISOMoneda);
		FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, clientId, orgId);
		recParam.addColumnToFilter("C_Currency_ID");
		MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "C_Currency", "iso_code = '" + codigoISOMoneda + "'", false);
		if (recResult.getRecords().isEmpty())
			return "";
		Map<String, String> ret = recResult.getRecords().get(0);
		
		return ret.get("C_Currency_ID");
	}

	private String getIdListaPrecioVentas(LibertyaWS lyws, Integer clientId, Integer orgId) throws RemoteException {
		log.log(Level.SEVERE, "Se buscara la lista de precio de ventas para el cliente " + clientId);
		FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, clientId, orgId);
		recParam.addColumnToFilter("M_PriceList_ID");
		MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "M_PriceList", "name = 'Ventas' and ad_client_id = " + clientId.toString(), false);
		if (recResult.getRecords().isEmpty())
			return "";
		Map<String, String> ret = recResult.getRecords().get(0);
		
		return ret.get("M_PriceList_ID");
	}

	private String getIdAlmacen(LibertyaWS lyws, Integer clientId, Integer orgId) throws RemoteException {
		log.log(Level.SEVERE, "Se buscara el almacen de la entidad comercial: " + clientId);
		FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, clientId, orgId);
		recParam.addColumnToFilter("M_Warehouse_ID");
		MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "M_Warehouse", "AD_Org_ID =" + orgId.toString(), false);
		if (recResult.getRecords().isEmpty())
			return "";
		Map<String, String> ret = recResult.getRecords().get(0);
		
		return ret.get("M_Warehouse_ID");
	}

	private String getIdDireccionEntidadComercial(LibertyaWS locator, String value, Integer clientId, Integer orgId) throws NumberFormatException, RemoteException {
		log.log(Level.SEVERE, "Se buscara la direccion de la entidad comercial: " + value);
		FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, clientId, orgId);
		recParam.addColumnToFilter("C_BPartner_Location_ID");
		MultipleRecordsResultBean recResult = locator.recordQuery(recParam, "C_BPartner_Location", "c_bpartner_id =" + value, false);
		if (recResult.getRecords().isEmpty())
			return "";
		Map<String, String> ret = recResult.getRecords().get(0);
		
		return ret.get("C_BPartner_Location_ID");
	}
	
	private String getIDEntidadComercialByClave(LibertyaWS lyws, String value, Integer clientId, Integer orgId) throws RemoteException {
		log.log(Level.SEVERE, "Se buscara la entidad comercial: " + value);
		FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, clientId, orgId);
		recParam.addColumnToFilter("C_BPartner_ID");
		String where = "value = '" + value + "'";
		MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "C_BPartner",where , false);
		if (recResult.getRecords().isEmpty())
			return "";
		
		Map<String, String> ret = recResult.getRecords().get(0);
		
		return ret.get("C_BPartner_ID");
		
	}

	private String getIDEntidadComercialByCuit(LibertyaWS lyws, String cuit, Integer clientId, Integer orgId) throws RemoteException {
		
		FilteredColumnsParameterBean recParam = new FilteredColumnsParameterBean(usuarioLibertya, passwordLibertya, clientId, orgId);
		recParam.addColumnToFilter("C_BPartner_ID");
		String where = "TaxID = '" + cuit + "' or TaxID = '" + cuit.replace("-", "") + "'";
		MultipleRecordsResultBean recResult = lyws.recordQuery(recParam, "C_BPartner",where , false);
		if (recResult.getRecords().isEmpty())
			return "";
		
		Map<String, String> ret = recResult.getRecords().get(0);
		
		return ret.get("C_BPartner_ID");
		
	}
	
	private MensajesRespuesta validarCarpeta(Carpeta carpeta)
	{
		MensajesRespuesta mensajesRespuesta = new MensajesRespuesta();
		
		if  (carpeta == null || (StringUtils.isEmpty(carpeta.getClave()) 
									&& StringUtils.isEmpty(carpeta.getNombre()) 
									&& StringUtils.isEmpty(carpeta.getFechaFin()) 
									&& StringUtils.isEmpty(carpeta.getFechaInicio()))) {
			mensajesRespuesta.agregarMensaje("No se encuentra informada la carpeta correspondiente a la Orden de trabajo.");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
			return mensajesRespuesta;
		}
		
		if (StringUtils.isEmpty(carpeta.getClave())) {
			mensajesRespuesta.agregarMensaje("La clave de la carpeta esta vacia");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (StringUtils.isEmpty(carpeta.getNombre())) {
			mensajesRespuesta.agregarMensaje( "El nombre de la carpeta esta vacia");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (!StringUtils.isDate(carpeta.getFechaFin(),
				"dd/MM/yyyy")) {
			mensajesRespuesta.agregarMensaje( "la fecha de fin no es correcta");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (!StringUtils.isDate(carpeta.getFechaInicio(),
				"dd/MM/yyyy")) {
			mensajesRespuesta.agregarMensaje("la fecha de inicio no es correcta");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		return mensajesRespuesta;
	}

	private MensajesRespuesta validarOrdenTrabajo(OrdenTrabajo ordenTrabajo) {
		MensajesRespuesta mensajesRespuesta = new MensajesRespuesta();
		
		if (StringUtils.isEmpty(ordenTrabajo.getClaveCliente())) {
			mensajesRespuesta.agregarMensaje("No se ha informado el cliente correspondiente a la Orden de trabajo.");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (!StringUtils.isDate(ordenTrabajo.getFechaOrdenTrabajo(),
				"dd/MM/yyyy")) {
			mensajesRespuesta.agregarMensaje("La fecha de la orden de trabajo no fue informada o es inválida. Formato correcto: dd/MM/yyyy");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (!StringUtils.isAlpha(ordenTrabajo.getCodigoMoneda()))
		{
			mensajesRespuesta.agregarMensaje("El codigo de moneda debe contener solo caracteres.");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (StringUtils.isEmpty(ordenTrabajo.getClaveUnidadNegocio())) {
			mensajesRespuesta.agregarMensaje("La unidad de negocio no debe ser vacia.");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (StringUtils.isEmpty(ordenTrabajo.getCodigoMoneda())) {
			mensajesRespuesta.agregarMensaje("El codigo de moneda no debe ser vacia.");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (!StringUtils.isAlpha(ordenTrabajo.getFormaDePago()))
		{
			mensajesRespuesta.agregarMensaje("La forma de paga debe contener solo caracteres.");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (StringUtils.isEmpty(ordenTrabajo.getFormaDePago()))
		{
			mensajesRespuesta.agregarMensaje("La forma de paga debe contener solo caracteres.");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		MensajesRespuesta mensajesRespuestaCarpeta = validarCarpeta(ordenTrabajo.getCarpeta());
		
		mensajesRespuesta.agregarTodosLosMensajes(mensajesRespuestaCarpeta);
		
		MensajesRespuesta mensajesRespuestaConceptos = validarConceptos(ordenTrabajo.getConceptos());
		
		mensajesRespuesta.agregarTodosLosMensajes(mensajesRespuestaConceptos);
		
		MensajesRespuesta mensajesRespuestaEntidadesComerciales = validarEntidadComerciales(ordenTrabajo.getEntidadesComerciales());
		
		mensajesRespuesta.agregarTodosLosMensajes(mensajesRespuestaEntidadesComerciales);
		
		return mensajesRespuesta;
	}

	private MensajesRespuesta validarEntidadComerciales(List<EntidadComercial> entidadesComerciales) {
		MensajesRespuesta mensajesRespuestaConceptos = new MensajesRespuesta();
		if (entidadesComerciales == null || entidadesComerciales.isEmpty())
			return mensajesRespuestaConceptos;
		for (EntidadComercial entidadComercial : entidadesComerciales) {
			mensajesRespuestaConceptos.agregarTodosLosMensajes( validarEntidadComercial(entidadComercial));
		}
		
		return mensajesRespuestaConceptos;
	}

	private MensajesRespuesta validarConceptos(List<Concepto> conceptos) {
		MensajesRespuesta mensajesRespuestaConceptos = new MensajesRespuesta();
		if (conceptos == null || conceptos.isEmpty()) {
			mensajesRespuestaConceptos.agregarMensaje("No se informaron conceptos para la Orden de trabajo.");
			mensajesRespuestaConceptos.setHayErrores(Boolean.TRUE);
			return mensajesRespuestaConceptos;
		}
		for (Concepto concepto : conceptos) {
			mensajesRespuestaConceptos.agregarTodosLosMensajes( validarConcepto(concepto));
		}
		
		return mensajesRespuestaConceptos;
	}
	
	private MensajesRespuesta validarConcepto(Concepto concepto) {
		
		/*private String claveConcepto = "claveConcepto"; //</ClaveConcepto>
	    private String cantidad = "2"; // </Cantidad>
		private String precioFacturacion = "3000"; //</PrecioFacturacion>
		private String cuitProveedor = "30-39993993-3"; //</CuitProveedor>
	    private String precioMaximoCompra = "2000"; //</PrecioMaximoCompra><!--precio limite -->
		private String precioInformado = "3000"; ///PrecioInformado>*/
		
		MensajesRespuesta mensajesRespuesta = new MensajesRespuesta();
		
		if (StringUtils.isEmpty(concepto.getClaveConcepto())) {
			mensajesRespuesta.agregarMensaje("La clave del concepto no debe ser vacia");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
				
		if (!StringUtils.isNumeric(concepto.getCantidad()))
		{
			mensajesRespuesta.agregarMensaje("La cantidad debe ser numerico.");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (StringUtils.isEmpty(concepto.getPrecioFacturacion())) {
			mensajesRespuesta.agregarMensaje("El Precio Facturacion del concepto " + concepto.getClaveConcepto() +" no fue informado.");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (!StringUtils.isNumeric(concepto.getPrecioFacturacion())) {
			mensajesRespuesta.agregarMensaje("El Precio Facturacion del concepto " + concepto.getClaveConcepto() +" posee un valor invalido.");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (StringUtils.isEmpty(concepto.getPrecioMaximoCompra())) {
			mensajesRespuesta.agregarMensaje("El Precio Maximo Compra del concepto " + concepto.getClaveConcepto() +" no fue informado.");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (!StringUtils.isNumeric(concepto.getPrecioMaximoCompra()))
		{
			mensajesRespuesta.agregarMensaje("El Precio Maximo de Compra del concepto " + concepto.getClaveConcepto() +" posee un valor invalido.");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (StringUtils.isEmpty(concepto.getPrecioInformado())) {
			mensajesRespuesta.agregarMensaje("El Precio Informado del concepto " + concepto.getClaveConcepto() +" no fue informado.");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		if (!StringUtils.isNumeric(concepto.getPrecioInformado()))
		{
			mensajesRespuesta.agregarMensaje("El Precio Informado del concepto " + concepto.getClaveConcepto() +" posee un valor invalido.");
			mensajesRespuesta.setHayErrores(Boolean.TRUE);
		}
		
		return mensajesRespuesta;
	}

}