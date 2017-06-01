package org.openXpertya.apps.search.helper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.openXpertya.apps.search.exception.ModelException;
import org.openXpertya.model.MBPartnerLocation;
import org.openXpertya.model.MInOut;
import org.openXpertya.model.MInOutLine;
import org.openXpertya.model.MInvoice;
import org.openXpertya.model.MInvoiceLine;
import org.openXpertya.model.MLocation;
import org.openXpertya.model.MOrder;
import org.openXpertya.model.MOrderLine;
import org.openXpertya.model.MPriceList;
import org.openXpertya.model.M_Column;
import org.openXpertya.model.M_Table;
import org.openXpertya.model.PO;
import org.openXpertya.process.DocAction;
import org.openXpertya.process.DocumentEngine;
import org.openXpertya.util.CLogger;
import org.openXpertya.util.CPreparedStatement;
import org.openXpertya.util.DB;
import org.openXpertya.util.Env;
import org.openXpertya.util.Msg;
import org.openXpertya.util.Trx;


public class OrderInvoiceHelper {
	
	protected CLogger log = CLogger.getCLogger(OrderInvoiceHelper.class);
	
	private Integer adSequenceId = null;
	private Integer currentNext = null;
	
	public static String ESTADO_FACTURACION_PENDIENTE = "PENDIENTE";
	public static String ESTADO_FACTURACION_FACTURADO = "FACTURADO";
	public static String ESTADO_FACTURACION_ERROR = "ERROR";
	public static String ESTADO_FACTURACION_RECHAZADA = "RECHAZADA";
	public static String ESTADO_ESPERA_DE_APROBACION = "ESPERA DE APROBACION";
	
	private String getDocumentNo( Object value ) {
        Integer C_DocType_ID = ( Integer )value;
        
        if( (C_DocType_ID == null) || (C_DocType_ID.intValue() == 0) ) {
            return "";
        }
        

        try {
            String SQL = "SELECT d.HasCharges,'N',d.IsDocNoControlled," 
            			+ "s.CurrentNext, d.DocBaseType, s.prefix, s.suffix, "
            		    + "s.ad_sequence_id "
            			+ "FROM C_DocType d, AD_Sequence s " 
            			+ "WHERE C_DocType_ID=?"    // 1
                         + " AND d.DocNoSequence_ID=s.AD_Sequence_ID(+)";
            PreparedStatement pstmt = DB.prepareStatement( SQL );

            pstmt.setInt( 1,C_DocType_ID.intValue());

            ResultSet rs = pstmt.executeQuery();

            if( rs.next()) {

               // DocumentNo
                this.currentNext = rs.getInt( 4 );
                String pre = rs.getString(6);
                String suf = rs.getString(7);
                this.adSequenceId = rs.getInt(8);

                // HACK
                
                if (pre == null)
                	pre = "";
                
                if (suf == null)
                	suf = "";
                
                if( rs.getString( 3 ).equals( "Y" )) {
                	return "<" + pre + currentNext + suf + ">" ;
                }
                
                
            }

            rs.close();
            pstmt.close();
        } catch( SQLException e ) {

            return e.getLocalizedMessage();
        }

        return "";
    }    // docType

	/**
	 * Gestiona la creación de una factura a partir del pedido, apoyandose en la clase CreateFromInvoice.
	 * La creación varia con respecto a la lógica en InvoiceDocumentHandler, y es por ésto que 
	 * fue necesario crear este método. 
	 * @param anOrder pedido a partir del cual se creará la factura
	 * @param invoiceDocTypeTargetID tipo de documento destino
	 * @param invoicePuntoDeVenta punto de venta
	 * @param invoiceTipoComprobante tipo de comprobante
	 * @param completeInvoice si debe completarse la factura
	 * @param dateInvoiced redefinición del valor (o se copia el dateOrdered del pedido en caso de recibir null)
	 * @param dateAcct redefinición del valor (o se copia el dateAcct del pedido en caso de recibir null)
	 */
	public MInvoice createInvoiceFromOrderLine(MOrder anOrder, int invoiceDocTypeTargetID, int invoicePuntoDeVenta, String invoiceTipoComprobante, 
			boolean completeInvoice, Timestamp dateInvoiced, Timestamp dateAcct, String trxName) throws ModelException, Exception 
	{
		try 
		{
			// Instanciar la nueva factura
			MInvoice anInvoice = new MInvoice(anOrder, invoiceDocTypeTargetID, anOrder.getDateOrdered());
			// Setear los parametros adicionales sobre el tipo de documento a generar
			anInvoice.setC_DocTypeTarget_ID(invoiceDocTypeTargetID);
			anInvoice.setPuntoDeVenta(invoicePuntoDeVenta);
			anInvoice.setTipoComprobante(invoiceTipoComprobante);
			
			anInvoice.setDocumentNo(getDocumentNo(invoiceDocTypeTargetID));
			
			// TODO: VER ESTE PUNTO PARA GENERAR UNA FACTURA POR LINEA DE ORDEN
			// setC_BPartner_ID(order.getBill_BPartner_ID());
			
			// Copia general de campos de cabecera
			CreateFromInvoice.copyHeaderValuesFromOrder(anInvoice, anOrder, anOrder.getCtx(), trxName);
			// Copiar los datos del pedido en la factura
			copyPOValues(anOrder, anInvoice);
			
			// Redefinir dateInvoiced y dateAcct en caso de estar seteadas, sino tomar la del pedido
			anInvoice.setDateInvoiced(dateInvoiced != null ? dateInvoiced : anOrder.getDateOrdered());
			anInvoice.setDateAcct(dateAcct != null ? dateAcct : anOrder.getDateAcct());
			
			// Almacenar la cabecera
			if (!anInvoice.save())
				throw new ModelException("Error al persistir Factura:" + CLogger.retrieveErrorAsString());
			
			// Instanciar y persistir las Lineas de factura a partir de las lineas de pedido
			MOrderLine[] orderLines = anOrder.getLines();
			for (int i=0; i<orderLines.length; i++)
			{
				// Crear nueva linea y setearle los datos originales de la linea de pedido
				MInvoiceLine anInvoiceLine = new MInvoiceLine(anInvoice);
				anInvoiceLine.setOrderLine(orderLines[i]);
				// Copia general de campos de cabecera
				CreateFromInvoice.copyLineValuesFromOrderLine(anInvoice, anOrder, anInvoiceLine, orderLines[i], anOrder.getCtx(), trxName);
				// Copiar los datos de la linea de pedido en la linea de la factura
				copyPOValues(orderLines[i], anInvoiceLine);
				// Persistir la linea
				if (!anInvoiceLine.save())
					throw new ModelException("Error al persistir linea de factura:" + CLogger.retrieveErrorAsString());
			}
			
			// Completar la factura si corresponde
			if (completeInvoice && !DocumentEngine.processAndSave(anInvoice, DocAction.ACTION_Complete, false))
				throw new ModelException("Error al completar la factura:" + Msg.parseTranslation(anOrder.getCtx(), anInvoice.getProcessMsg()));
	
			/* === Commitear transaccion === */
			Trx.getTrx(trxName).commit();
			// Retornar la factura generada
			return anInvoice;
		}catch (Exception e) {
			/* === Rollback transaccion === */
			Trx.getTrx(trxName).rollback();
			throw e;
		}
	}
	
	private void createRemitosSalida(MOrder anOrder, String trxName) throws ModelException {
		log.log(Level.SEVERE, "Creacion del remito");
		// Instanciar el nuevo remito
		MInOut anInOut = new MInOut(anOrder, 0, anOrder.getDateOrdered());
		// Copia general de campos de cabecera
		CreateFromShipment.copyHeaderValuesFromOrder(anInOut, anOrder, Env.getCtx(), trxName);
		// Copiar los datos del pedido en el remito
		copyPOValues(anOrder, anInOut);
		// Setear el tipo de documento
		anInOut.setC_DocType_ID();

		// Setear movementDate y dateAcct en caso de estar seteadas, sino tomar la del pedido
		anInOut.setMovementDate(anOrder.getDateOrdered());
		anInOut.setDateAcct(anOrder.getDateAcct());
		
		if (!anInOut.save())
			throw new ModelException("Error al persistir Remito:" + CLogger.retrieveErrorAsString());
		
		// Instanciar y persistir las Lineas de remito a partir de las lineas de pedido
		MOrderLine[] orderLines = anOrder.getLines();
		for (int i = 0; i < orderLines.length; i++)
		{
			// Crear nueva linea y setearle los datos originales de la linea de pedido
			MInOutLine anInOutLine = new MInOutLine(anInOut);
			anInOutLine.setOrderLine(orderLines[i], 0, orderLines[i].getQtyOrdered());
			// Copia general de campos de linea
			CreateFromShipment.copyLineValuesFromOrderLine(anInOut, anOrder, anInOutLine, orderLines[i], Env.getCtx(), trxName, false);
			// Copiar los datos de la linea de pedido en la linea del remito
			copyPOValues(orderLines[i], anInOutLine);
			// Persistir la linea
			if (!anInOutLine.save())
				throw new ModelException("Error al persistir linea de remito:" + CLogger.retrieveErrorAsString());
		}
		
		// Completar el remito si corresponde
		if (!DocumentEngine.processAndSave(anInOut, DocAction.ACTION_Complete, false))
			throw new ModelException("Error al completar el remito:" + Msg.parseTranslation(Env.getCtx(), anInOut.getProcessMsg()));
		
		log.log(Level.SEVERE, "Remito creado " + anInOut);
	}
	
	private void createPedidosProveedorYRemitos(MOrder ordenTrabajo, String trxName) throws ModelException, SQLException {
		
		Map<String, List<MOrderLine>> orderLinePorCuitProveedor = getLineasPorEntidadComercial(ordenTrabajo);
		
		/*
		 * I.	Si ninguna línea tiene código de proveedor:
1.	Indicar el estado Pedido de Proveedor como Finalizado.
2.	Finalizar proceso.

		 */
		if (orderLinePorCuitProveedor.isEmpty())
			actualizarEstadosFactura(ordenTrabajo, ESTADO_FACTURACION_FACTURADO, "FINALIZADO", trxName);
			
		
		for(String proveedorId : orderLinePorCuitProveedor.keySet() ) {
			
			List<MOrderLine> conceptos = orderLinePorCuitProveedor.get(proveedorId);
			// Se crea una lista de precio por pedido
			int idListaPrecio = getListaPrecio(ordenTrabajo.getAD_Client_ID(), ordenTrabajo.getAD_Org_ID(),  
					ordenTrabajo.getDocumentNo(), ordenTrabajo.getC_Project_ID(), ordenTrabajo.getC_Currency_ID(), 
					conceptos , Boolean.FALSE);
			
			MOrder anOrder = new MOrder(Env.getCtx(), 0, trxName);
			
			anOrder.set_Value ("M_PriceList_ID", idListaPrecio);
		
			anOrder.set_Value("AD_Client_ID",       ordenTrabajo.getAD_Client_ID());
			anOrder.set_Value("AD_Org_ID",          ordenTrabajo.getAD_Org_ID());
			anOrder.set_Value("C_DocTypeTarget_ID", getIdDocTypeTarget(ordenTrabajo.getAD_Client_ID()));
			anOrder.set_Value("DateOrdered",        new java.sql.Timestamp((new Date()).getTime())); // Fecha
		
			// Se envia en los parametros del constructor
			anOrder.set_Value("C_BPartner_Location_ID", getIdDireccionEntidadComercial(proveedorId));
			anOrder.set_Value("C_BPartner_ID",          Integer.valueOf(proveedorId));
			anOrder.set_Value("M_Warehouse_ID",         ordenTrabajo.getM_Warehouse_ID());
			
			anOrder.setC_Currency_ID(ordenTrabajo.getC_Currency_ID());
			//anOrder.set_Value("M_PriceList_ID",         getListaPrecio(anOrder.getAD_Client_ID(), anOrder.getAD_Org_ID(),  
			//														anOrder.getDocumentNo(), anOrder.getC_Project_ID(), anOrder.getC_Currency_ID(), 
			//														conceptos , Boolean.FALSE, trxName));
			anOrder.set_Value("C_Currency_ID",          ordenTrabajo.getC_Currency_ID());
			anOrder.set_Value("SalesRep_ID",            ordenTrabajo.getSalesRep_ID());
			anOrder.set_Value("PaymentRule",            ordenTrabajo.getPaymentRule());
			
			if (ordenTrabajo.get_Value("C_PaymentTerm_ID") != null) {
				anOrder.set_Value("C_PaymentTerm_ID", ordenTrabajo.get_Value("C_PaymentTerm_ID"));
			}
			
			anOrder.set_Value("C_Project_ID",         ordenTrabajo.getC_Project_ID());
			// MARCAR AL C_ORDER COMO Transaccion de venta
			anOrder.set_Value("IsSOTrx", "N");
			// La orden de trabajo esta activa
			anOrder.set_Value("IsActive", "Y");
		
			//VER el campo C_PaymentTerm_ID
			anOrder.set_Value("Description",   ordenTrabajo.getDescription());
			
			anOrder.set_Value("C_Campaign_ID", ordenTrabajo.get_Value("C_Campaign_ID"));
		
			anOrder.set_Value("estado_facturacion", "");
			anOrder.set_Value("estado_pedido_proveedor", "");
			
			if (!anOrder.save())
				throw new ModelException("Error al persistir pedido:" + CLogger.retrieveErrorAsString());
		
			int i = 0;
			for (MOrderLine concepto : conceptos)
			{
			
				MOrderLine anOrderLine = new MOrderLine(anOrder);
				
				anOrderLine.setLine(i++);
				anOrderLine.setQtyEntered( concepto.getQtyEntered());
				//anOrderLine.setPriceActual( concepto.getPriceActual());
				//Usar OT: 50054
				//maximocompra = 500 deberia llegar
				anOrderLine.setPriceActual(((BigDecimal)concepto.get_Value("preciomaximocompra")));
				anOrderLine.setQtyOrdered( concepto.getQtyOrdered());
				//anOrderLine.setPriceEntered(concepto.getPriceEntered());
				anOrderLine.setPriceEntered(((BigDecimal)concepto.get_Value("preciomaximocompra")));
				
				// columnas agregadas a c_orderline
				//ordenDeTrabajo.addColumnToHeader("precioMaximoCompra", concepto.getPrecioMaximoCompra());
				//ordenDeTrabajo.addColumnToHeader("precioInformado", concepto.getPrecioInformado());
				
				anOrderLine.setC_Tax_ID( concepto.getC_Tax_ID());
				
				anOrderLine.setM_Product_ID(concepto.getM_Product_ID());
				
				if (!anOrderLine.save())
					throw new ModelException("Error al persistir linea de pedido:" + CLogger.retrieveErrorAsString());
				
			}
			if (!DocumentEngine.processAndSave(anOrder, DocAction.ACTION_Complete, false)) {
				log.log(Level.SEVERE, "Error al completar el pedido");
				throw new ModelException("Error al completar el pedido:" + Msg.parseTranslation(Env.getCtx(), anOrder.getProcessMsg()));
			}
			
			log.log(Level.SEVERE, "Pedido de compra generado " + anOrder);
		
			this.createRemitosSalida(anOrder, trxName);
		}
	}
	
	private MPriceList getIdPriceList(Boolean esVenta, int AD_Client_ID, String getTrxName) throws SQLException {
		M_Table table = M_Table.get(Env.getCtx(), "M_PriceList");
		
		String sql = "SELECT * FROM M_PriceList where name = "+ (esVenta ? "'Ventas'" : "'Compras Inicial'") + " and ad_client_id = " + AD_Client_ID ;
		
		PreparedStatement ps = DB.prepareStatement(sql, getTrxName);
		ResultSet rs = ps.executeQuery();

		List<MPriceList> ordenesDeTrabajo = new ArrayList<MPriceList>();
		while (rs.next()) {
			ordenesDeTrabajo.add((MPriceList) table.getPO(rs, getTrxName));
		}
		
		return !ordenesDeTrabajo.isEmpty() ? ordenesDeTrabajo.get(0) : null;
	}
	
	private int getListaPrecio(int AD_Client_ID, int AD_Org_ID, String DocumentNo, int C_Project_ID, int C_Currency_ID, List<MOrderLine> mOrderLines,
			Boolean esVenta) throws SQLException {
		
		String getTrxName = "trxName";
		
		MPriceList mPriceList = getIdPriceList(esVenta, AD_Client_ID, getTrxName);
		
		return mPriceList.getM_PriceList_ID();
	}

	private Integer getIdDocTypeTarget(int adClientID) throws SQLException {
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Integer typeID = null;
		
		try {
			String SQL = "SELECT C_DocType_ID "
	            		+ "FROM C_DocType "
	            		+ "WHERE name like 'Pedido a%' "
	            		+ "AND DocBaseType IN ('SOO', 'POO') "
	            		+ "AND DocTypeKey NOT IN ('SOSOT','SOSOTD') "
	            		+ "AND isactive = 'Y' "
	            		+ "AND IsSOTrx='N' "
	            		+ "AND EnableInCreateFromShipment = 'Y' "
	            		+ "AND ad_client_id = ?";
	            
			pstmt = DB.prepareStatement( SQL );
	
			pstmt.setInt( 1,adClientID);
	
			rs = pstmt.executeQuery();
			
			if( rs.next()) {
				typeID = rs.getInt(1);
				
			}
		} finally {
			
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
		}

        return typeID;
	}
	
	private Integer getIdDireccionEntidadComercial(String proveedorId) throws SQLException {
		
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Integer cBPartnerLocationID = null;
		
		try {
			String SQL = "select C_BPartner_Location_ID "
					+ "FROM C_BPartner_Location "
					+ "WHERE c_bpartner_id = ?";
	            
			pstmt = DB.prepareStatement( SQL );
	
			pstmt.setInt( 1, Integer.valueOf(proveedorId));
	
			rs = pstmt.executeQuery();
			
			if( rs.next()) {
				cBPartnerLocationID = rs.getInt(1);
				
			}
		} finally {
			
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
		}

        return cBPartnerLocationID;
		
	}
	
	/**
	 * Retorna la lista de ordenes por Id de Entidad Comercial existente en la Orden Trabajo.
	 * @param ordenTrabajo
	 * @return
	 */
	private Map<String, List<MOrderLine>> getLineasPorEntidadComercial(MOrder ordenTrabajo) {
		
		Map<String, List<MOrderLine>> retorno = new HashMap<String, List<MOrderLine>>();
		MOrderLine[] lineas = ordenTrabajo.getLines();
		if (lineas == null || lineas.length == 0) {
			return retorno;
		}
	
		for (MOrderLine mOrderLine : lineas) {
			 String proveedorId = mOrderLine.get_ValueAsString("proveedor_id");
			 // Pueden existir lineas sin entidad comercial, esto es porque son lineas de contratacion de servicios.
			 if (proveedorId == null || proveedorId.length() == 0)
				 continue;
			 
			 List<MOrderLine> lista = retorno.get(proveedorId);
			 
			 if (lista == null) {
				 lista = new ArrayList<MOrderLine>();
			 }
			 
			 lista.add(mOrderLine);
			 retorno.put(proveedorId, lista);
		}
		
		log.log(Level.SEVERE, "Lineas ordenadas por Entidad " + retorno);
		
		return retorno;
	}
	
	/**
	 * En caso de que la cabecera cuente con campos de dirección ad-hoc, y que los 
	 * mismos no se hayan cargado a partir de la información recibida, tomar los
	 * datos de la dirección de la entidad comercial para rellenar la cabecera.
	 * @param header cabecera de documento (factura, pedido, etc.)
	 * @param bPartnerID entidad comercial a tomar como referencia
	 */
	protected void setBPartnerAddressInDocument(PO header, int bPartnerID, String trxName) 
	{
		// Recuperar el bpartnerlocation
		MBPartnerLocation[] aBPartnerLocation = MBPartnerLocation.getForBPartner(Env.getCtx(), bPartnerID);
		if (aBPartnerLocation==null || aBPartnerLocation.length==0)
			return;
		// Cargar el encabezado con los eventuales datos no contigurados en la cabecera relacionados con bPartnerLocation 
		copyPOValues(aBPartnerLocation[0], header);
		// Recuperar el location
		MLocation aLocation = MLocation.getBPLocation(Env.getCtx(), aBPartnerLocation[0].getC_BPartner_Location_ID(), trxName);
		if (aLocation==null || aLocation.getC_Location_ID()==0)
			return;
		// Cargar el encabezado con los eventuales datos no contigurados en la cabecera relacionados con Location		
		copyPOValues(aLocation, header);
	}

	/**
	 * Gestiona la creación de una factura a partir del pedido, apoyandose en la clase CreateFromInvoice.
	 * La creación varia con respecto a la lógica en InvoiceDocumentHandler, y es por ésto que 
	 * fue necesario crear este método. 
	 * @param ordenTrabajo pedido a partir del cual se creará la factura
	 * @param invoiceDocTypeTargetID tipo de documento destino
	 * @param invoicePuntoDeVenta punto de venta
	 * @param invoiceTipoComprobante tipo de comprobante
	 * @param completeInvoice si debe completarse la factura
	 * @param dateInvoiced redefinición del valor (o se copia el dateOrdered del pedido en caso de recibir null)
	 * @param dateAcct redefinición del valor (o se copia el dateAcct del pedido en caso de recibir null)
	 * @param trxName Nombre de la transaccion.
	 */
	private MInvoice createFacturaDesdePedido(MOrder ordenTrabajo, int invoiceDocTypeTargetID, int invoicePuntoDeVenta, String invoiceTipoComprobante, 
			boolean completeInvoice, Timestamp dateInvoiced, Timestamp dateAcct, String trxName) throws ModelException, Exception 
	{
		try 
		{
			
			if (!sePuedeGenerarFactura(ordenTrabajo)) {
				log.log(Level.SEVERE, "No se puede generar la factura: " + ordenTrabajo);
				return null;
			}
			
			log.log(Level.SEVERE, "Creacion de factura a partir de la orden: " + ordenTrabajo);
			
			// Instanciar la nueva factura
			MInvoice anInvoice = new MInvoice(ordenTrabajo, invoiceDocTypeTargetID, ordenTrabajo.getDateOrdered());
			// Setear los parametros adicionales sobre el tipo de documento a generar
			anInvoice.setC_DocTypeTarget_ID(invoiceDocTypeTargetID);
			anInvoice.setPuntoDeVenta(invoicePuntoDeVenta);
			anInvoice.setTipoComprobante(invoiceTipoComprobante);
			
			anInvoice.setDocumentNo(getDocumentNo(invoiceDocTypeTargetID));
			
			// Copia general de campos de cabecera
			CreateFromInvoice.copyHeaderValuesFromOrder(anInvoice, ordenTrabajo, ordenTrabajo.getCtx(), trxName);
			// Copiar los datos del pedido en la factura
			copyPOValues(ordenTrabajo, anInvoice);
			
			// Redefinir dateInvoiced y dateAcct en caso de estar seteadas, sino tomar la del pedido
			anInvoice.setDateInvoiced(dateInvoiced != null ? dateInvoiced : ordenTrabajo.getDateOrdered());
			anInvoice.setDateAcct(dateAcct != null ? dateAcct : ordenTrabajo.getDateAcct());
			anInvoice.set_Value ("M_PriceList_ID", getListaPrecio(ordenTrabajo.getAD_Client_ID(), ordenTrabajo.getAD_Org_ID(),  ordenTrabajo.getDocumentNo(), ordenTrabajo.getC_Project_ID(), 
							ordenTrabajo.getC_Currency_ID(), crearLista(ordenTrabajo.getLines()), Boolean.TRUE));
			//anInvoice.set_Value("M_PriceList_ID", getListaPrecio(anOrder.getAD_Client_ID(), anOrder.getAD_Org_ID(),  anOrder.getDocumentNo(), anOrder.getC_Project_ID(), 
			//				anOrder.getC_Currency_ID(), crearLista(anOrder.getLines()), Boolean.TRUE, trxName));
			
			anInvoice.setC_Currency_ID(ordenTrabajo.getC_Currency_ID());
			
			// Almacenar la cabecera
			if (!anInvoice.save())
				throw new ModelException("Error al persistir Factura:" + CLogger.retrieveErrorAsString());
			
			// Instanciar y persistir las Lineas de factura a partir de las lineas de pedido
			MOrderLine[] orderLines = ordenTrabajo.getLines();
			for (int i=0; i<orderLines.length; i++)
			{
				MOrderLine mOrderLine = orderLines[i];
				// No se generara la linea si el precio es igual a cero.
				if (BigDecimal.ZERO.compareTo(mOrderLine.getPriceEntered()) == 0) {
					continue;
				}
				
				// Crear nueva linea y setearle los datos originales de la linea de pedido
				MInvoiceLine anInvoiceLine = new MInvoiceLine(anInvoice);
				anInvoiceLine.setOrderLine(mOrderLine);
				// Copia general de campos de cabecera
				CreateFromInvoice.copyLineValuesFromOrderLine(anInvoice, ordenTrabajo, anInvoiceLine, mOrderLine, ordenTrabajo.getCtx(), trxName);
				// Copiar los datos de la linea de pedido en la linea de la factura
				copyPOValues(orderLines[i], anInvoiceLine);
				// Persistir la linea
				if (!anInvoiceLine.save())
					throw new ModelException("Error al persistir linea de factura:" + CLogger.retrieveErrorAsString());
			}
			
			
			if (completeInvoice && !DocumentEngine.processAndSave(anInvoice, DocAction.ACTION_Complete, false)) {
				log.log(Level.SEVERE, "Error al completar la factura");
				throw new ModelException("Error al completar la factura:" + Msg.parseTranslation(ordenTrabajo.getCtx(), anInvoice.getProcessMsg()));
			}
	
			actualizarEstadosFactura(ordenTrabajo, ESTADO_FACTURACION_FACTURADO, "EN CURSO", trxName);
			
			log.log(Level.SEVERE, "Factura creada: C_Invoice_ID " + anInvoice.getC_Invoice_ID());
			
			/* === Commitear transaccion === */
			/* El commit lo hace el metodo public void processOrdenTrabajo(MOrder mOrder, int invoiceDocTypeTargetID,
			int invoicePuntoDeVenta, String invoiceTipoComprobante, boolean completeInvoice,
			Timestamp dateInvoiced, Timestamp dateAcct, String trxName) 
			Por ello, este metodo paso a ser privado*/
			/*Trx.getTrx(trxName).commit();*/
			// Retornar la factura generada
			return anInvoice;
		} catch (Exception e) {
			/* El commit lo hace el metodo public void processOrdenTrabajo(MOrder mOrder, int invoiceDocTypeTargetID,
			int invoicePuntoDeVenta, String invoiceTipoComprobante, boolean completeInvoice,
			Timestamp dateInvoiced, Timestamp dateAcct, String trxName) 
			Por ello, este metodo paso a ser privado*/
			
			/* === Rollback transaccion === */
			/*Trx.getTrx(trxName).rollback();*/
			throw e;
		}
	}
	
	private boolean sePuedeGenerarFactura(MOrder ordenTrabajo) {
		
		MOrderLine[] orderLines = ordenTrabajo.getLines();
		for (int i=0; i<orderLines.length; i++)
		{
			MOrderLine mOrderLine = orderLines[i];
			// Hay un precio distinto a cero.
			if (BigDecimal.ZERO.compareTo(mOrderLine.getPriceEntered()) != 0) {
				return true;
			}
		}
		return false;
	}

	private List<MOrderLine> crearLista(MOrderLine[] lines) {
		List<MOrderLine> array = new ArrayList<MOrderLine>();
		
		for (int i = 0; i < lines.length; i++) {
			MOrderLine mOrderLine = lines[i];
			array.add(mOrderLine);
		}
		
		return array;
	}

	private void actualizarEstadosFactura(MOrder anOrder, String estadoFacturacion, String estado_pedido_proveedor, String trxName) throws SQLException {
		
		log.log(Level.SEVERE, "Actualizacion del estado de facturacion [id orden de trabajo, estadoFacturacion, estadoPedido]: [" + anOrder.getC_Order_ID() + "," + estadoFacturacion + "," + estado_pedido_proveedor + "]");
		
		String sql = "UPDATE c_order "
				+ "SET estado_facturacion=?, "
				+ "estado_pedido_proveedor=? "
				+ "WHERE c_order_id=?;";
		
		PreparedStatement pstmt = new CPreparedStatement( ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE, sql, trxName );
		
		pstmt.setString( 1, estadoFacturacion);
        
        pstmt.setString( 2, estado_pedido_proveedor);
        
        pstmt.setInt( 3, anOrder.getC_Order_ID());

        pstmt.executeUpdate();

        pstmt.close();
	}

	/**
	 * Copia los valores de un PO origen a uno destino,
	 * matcheando por el nombre de la columna unicamente.
	 * La copia se realiza SOLO si el destino no tiene un dato cargado
	 * Omite columnas especiales, de components y replicacion
	 * @param source PO origen
	 * @param target PO destino
	 */
	protected void copyPOValues(PO source, PO target)
	{
		// recuperar las columnas de la tabla destino
		M_Table targetTable = M_Table.get(target.getCtx(), target.get_TableName());
		M_Column[] targetColumns = targetTable.getColumns(false);
		
		// Recorrer las columnas destino y verificar si hay que copiar un dato
		for (M_Column targetColumn : targetColumns)
		{
			// Nombre de columna destino
			String columnName = targetColumn.getColumnName();
			
			// Si el destino ya tiene un dato, se omite
			if (target.get_Value(columnName)!=null && target.get_ValueAsString(columnName).length()>0)
				continue;
			
			// Columnas especiales se omiten
			if (columnName.startsWith("Created") ||
				columnName.startsWith("Updated") ||
				columnName.equals("AD_Client_ID") ||
				columnName.equals("AD_Org_ID") ||
				columnName.equals("AD_ComponentVersion_ID") ||
				columnName.equals("AD_ComponentObjectUID") ||
				columnName.equalsIgnoreCase("retrieveUID") ||
				columnName.equalsIgnoreCase("repArray") ||
				columnName.equalsIgnoreCase("dateLastSentJMS") ||
				targetColumn.isKey())
				continue;
			
			// Si el origen tiene un dato, entonces setear el destino
			if (source.get_Value(columnName)!=null && source.get_ValueAsString(columnName).length()>0)
				target.set_Value(columnName, source.get_Value(columnName));
		}
	}

	/**
	 * 
	 * Decrementar el numero de secuencia, si aplica
	 * Asignar el estado "ERROR" a la orden de trabajo
	 * Reabrir la orden de factura.
	 * */
	public void ajustarOrdenYFactura(Integer cOrderID, String descripcionError) throws SQLException {
		
		Connection con = DB.createConnection(false, Connection.TRANSACTION_READ_COMMITTED);
		try {
			// NO SERIA NECESARIO
			// decrementarSecuenciador(con);
		
			asignarEstadoFacturacionYReabrir(con, cOrderID, "ERROR", descripcionError);
		} catch (SQLException e) {
			
			if (con != null) {
				con.rollback();
				con = null;
			}
			
			throw e;
		}
		
		con.commit();
		con = null;
		
		
	}
	
	private void asignarEstadoFacturacionYReabrir(Connection con, Integer cOrderID, String estadoFacturacion, String descripcionError) throws SQLException {
		String sql = "UPDATE c_order "
				+ "SET docstatus=?, "
				+ "docaction=?, "
				+ "processing=?, "
				+ "processed=?, "
				+ "estado_facturacion = ?,"
				+ "description = ?"
				+ "WHERE c_order_id = ?";
		
		// PreparedStatement pstmt = new CPreparedStatement( ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE, sql, "Actualizacion" );
		
		PreparedStatement pstmt = con.prepareStatement(sql);

        pstmt.setString( 1, "DR");
        
        pstmt.setString( 2, "CO");
        
        pstmt.setString( 3, "N");
        
        pstmt.setString( 4, "N");
        
        pstmt.setString( 5, estadoFacturacion);
        
        pstmt.setString(6,  descripcionError.toString().substring(0,
                Math.min(255, descripcionError.toString().length())));
        
        pstmt.setInt( 7, cOrderID);

        pstmt.executeUpdate();

        pstmt.close();
        
        sql = "UPDATE c_orderline "
				+ "SET processed=?"
				+ "WHERE c_order_id = ?";
		
		// PreparedStatement pstmt = new CPreparedStatement( ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE, sql, "Actualizacion" );
		
		PreparedStatement pstmtLine = con.prepareStatement(sql);

		pstmtLine.setString( 1, "N");
        
		/*pstmtLine.setString(2,  descripcionError.toString().substring(0,
                Math.min(255, descripcionError.toString().length())));*/
        
		pstmtLine.setInt( 2, cOrderID);

		pstmtLine.executeUpdate();

		pstmtLine.close();
	}
	
	/**
	 * Cambia el estado de las OT seleccionadas que se encuentren en estado distinto de “Facturado” a “Rechazadas”.
	 * 
	 * @param mOrder
	 * @return
	 */
	public boolean sePuedeRechazar(MOrder mOrder) {
		Object estadoFacturacion = mOrder.get_Value("estado_facturacion");
		// No se puede rechazar una orden rechazada
		if (ESTADO_FACTURACION_RECHAZADA.equals(estadoFacturacion)) {
			return false;
		}
		// El estado es distinto a facturado => se puede rechazar
		if (!ESTADO_FACTURACION_FACTURADO.equals(estadoFacturacion)) {
			return true;
		}
		
		return false;
	}

	public boolean seDebeProcesar(MOrder mOrder) {
		if (ESTADO_FACTURACION_RECHAZADA.equals(mOrder.get_Value("estado_facturacion"))
				|| ESTADO_FACTURACION_ERROR.equals(mOrder.get_Value("estado_facturacion")) 
				|| ESTADO_FACTURACION_FACTURADO.equals(mOrder.get_Value("estado_facturacion")))
			return false;
		
		return true;
	}

	public void procesarOrdenTrabajo(MOrder mOrder, int invoiceDocTypeTargetID,
			int invoicePuntoDeVenta, String invoiceTipoComprobante, boolean completeInvoice,
			Timestamp dateInvoiced, Timestamp dateAcct, String trxName) throws Throwable {
		try {
			//lista de precios de compra
			this.createPedidosProveedorYRemitos(mOrder, trxName);
			// lista de precios de venta
			this.createFacturaDesdePedido(mOrder, invoiceDocTypeTargetID, invoicePuntoDeVenta, invoiceTipoComprobante, completeInvoice, dateInvoiced, dateAcct, trxName);
			
			// No Se Completa el pedido original ya que lo hizo el proceso JSON
			
			/* === Commitear transaccion === */
			Trx.getTrx(trxName).commit();
		} catch (Throwable t) {
			/* === Rollback transaccion === */
			Trx.getTrx(trxName).rollback();
			
			throw t;
		}
		
	}

	public void habilitarOrdenParaBatch(MOrder mOrder) throws SQLException {
		String trxName = mOrder.get_TrxName();
		
		try {
			actualizarEstadosFactura(mOrder, ESTADO_FACTURACION_PENDIENTE, "PENDIENTE", trxName);
			Trx.getTrx(trxName).commit();
		} catch (SQLException t) {
			/* === Rollback transaccion === */
			Trx.getTrx(trxName).rollback();
			
			throw t;
		}
		
	}
	
	public void rechazarOrdenTrabajo(MOrder mOrder) throws SQLException {
		String trxName = mOrder.get_TrxName();
		
		try {
			actualizarEstadosFactura(mOrder, ESTADO_FACTURACION_RECHAZADA, "PENDIENTE", trxName);
			Trx.getTrx(trxName).commit();
		} catch (SQLException t) {
			/* === Rollback transaccion === */
			Trx.getTrx(trxName).rollback();
			
			throw t;
		}
		
	}
	
}
