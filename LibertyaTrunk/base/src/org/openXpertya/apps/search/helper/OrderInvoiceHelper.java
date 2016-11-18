package org.openXpertya.apps.search.helper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
import org.openXpertya.model.MPriceListVersion;
import org.openXpertya.model.MProductPrice;
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
		
		log.log(Level.SEVERE, "M_InOut_ID " + anInOut);
	}
	
	private void createPedidosProveedorYRemitos(MOrder ordenTrabajo, String trxName) throws ModelException, SQLException {
		
		Map<Integer, List<MOrderLine>> orderLinePorCuitProveedor = getOrderLinePorIDPartner(ordenTrabajo);
		
		/*
		 * I.	Si ninguna línea tiene código de proveedor:
1.	Indicar el estado Pedido de Proveedor como Finalizado.
2.	Finalizar proceso.

		 */
		if (orderLinePorCuitProveedor.isEmpty())
			actualizarEstadosFactura(ordenTrabajo, ESTADO_FACTURACION_FACTURADO, "FINALIZADO", trxName);
			
		
		for(Integer idPartner : orderLinePorCuitProveedor.keySet() ) {
			
			List<MOrderLine> conceptos = orderLinePorCuitProveedor.get(idPartner);
			// Se crea una lista de precio por pedido
			int idListaPrecio = getListaPrecio(ordenTrabajo.getAD_Client_ID(), ordenTrabajo.getAD_Org_ID(),  
					ordenTrabajo.getDocumentNo(), ordenTrabajo.getC_Project_ID(), ordenTrabajo.getC_Currency_ID(), 
					conceptos , Boolean.FALSE);
			
			MOrder anOrder = new MOrder(Env.getCtx(), 0, trxName);
			
			anOrder.set_Value ("M_PriceList_ID", idListaPrecio);
		
			anOrder.set_Value("AD_Client_ID", ordenTrabajo.getAD_Client_ID());
			anOrder.set_Value("AD_Org_ID", ordenTrabajo.getAD_Org_ID());
			anOrder.set_Value("C_DocTypeTarget_ID", getIdDocTypeTarget(ordenTrabajo.getAD_Client_ID()));
			anOrder.set_Value("DateOrdered", new java.sql.Timestamp((new Date()).getTime())); // Fecha
		
			// Se envia en los parametros del constructor
			anOrder.set_Value("C_BPartner_Location_ID", getIdDireccionEntidadComercial(idPartner));
			anOrder.set_Value("C_BPartner_ID",          idPartner);
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
			
			anOrder.set_Value("C_Project_ID",           ordenTrabajo.getC_Project_ID());
			// MARCAR AL C_ORDER COMO Transaccion de venta
			anOrder.set_Value("IsSOTrx", "N");
			// La orden de trabajo esta activa
			anOrder.set_Value("IsActive", "Y");
		
			//VER el campo C_PaymentTerm_ID
			anOrder.set_Value("Description", ordenTrabajo.getDescription());
			
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
				anOrderLine.setPriceActual( concepto.getPriceActual());
				anOrderLine.setQtyOrdered( concepto.getQtyOrdered());
				anOrderLine.setPriceEntered(concepto.getPriceEntered());
				
				// columnas agregadas a c_orderline
				//ordenDeTrabajo.addColumnToHeader("precioMaximoCompra", concepto.getPrecioMaximoCompra());
				//ordenDeTrabajo.addColumnToHeader("precioInformado", concepto.getPrecioInformado());
				
				anOrderLine.setC_Tax_ID( concepto.getC_Tax_ID());
				
				anOrderLine.setM_Product_ID(concepto.getM_Product_ID());
				
				if (!anOrderLine.save())
					throw new ModelException("Error al persistir linea de pedido:" + CLogger.retrieveErrorAsString());
				
			}
			if (!DocumentEngine.processAndSave(anOrder, DocAction.ACTION_Complete, false))
				throw new ModelException("Error al completar el pedido:" + Msg.parseTranslation(Env.getCtx(), anOrder.getProcessMsg()));
			
			log.log(Level.SEVERE, "C_Order_ID " + anOrder);
		
			this.createRemitosSalida(anOrder, trxName);
		}
	}
	
	private int getListaPrecio(int AD_Client_ID, int AD_Org_ID, String DocumentNo, int C_Project_ID, int C_Currency_ID, List<MOrderLine> mOrderLines,
			Boolean esVenta) throws SQLException {
		String getTrxName = "trxName";
		MPriceList mPriceList = new MPriceList(Env.getCtx(), 0, getTrxName);
		
		mPriceList.setIsActive(true);
		
		String nombreListaPrecio = DocumentNo + "-" + C_Project_ID + "-" + (esVenta ? "Ventas" : "Compras") + "-" + Env.getDateTime("yy-MM-dd HH:mm:ss");
		mPriceList.setName(nombreListaPrecio);
		mPriceList.setDescription("''");
		mPriceList.setIsTaxIncluded(false);
		
		mPriceList.setIsSOPriceList(esVenta);
		mPriceList.setIsDefault(true);
		mPriceList.setC_Currency_ID(118);
		mPriceList.setEnforcePriceLimit(false);
		mPriceList.setPricePrecision(new BigDecimal("2.000000"));
		
		mPriceList.setIsPerceptionsIncluded(false);
		
		if (!mPriceList.save())
			throw new SQLException(CLogger.retrieveErrorAsString());
		
		MPriceListVersion mPriceListVersion = new MPriceListVersion(mPriceList);
		
		mPriceListVersion.setIsActive(true);
		mPriceListVersion.setName(nombreListaPrecio);
		mPriceListVersion.setDescription("");
		
		mPriceListVersion.setM_DiscountSchema_ID(1010101);
		Date date = new Date();
		mPriceListVersion.setValidFrom(new Timestamp(date.getTime()));
		mPriceListVersion.setProcCreate("N");
		
		if (!mPriceListVersion.save())
			throw new SQLException(CLogger.retrieveErrorAsString());
		
		for (int i = 0; i < mOrderLines.size(); i++) {
			MOrderLine mOrderLine = mOrderLines.get(i);
			String precio = esVenta ? mOrderLine.get_ValueAsString("PriceEntered") : mOrderLine.get_ValueAsString("preciomaximocompra");
			int M_Product_ID = mOrderLine.getM_Product_ID();
			BigDecimal PriceList = new BigDecimal(precio);
			BigDecimal PriceStd =  new BigDecimal(precio);
			BigDecimal PriceLimit = new BigDecimal(precio);
			MProductPrice mProductPrice = new MProductPrice(mPriceListVersion, M_Product_ID, PriceList, PriceStd, PriceLimit);
			mProductPrice.setIsActive(true);
			
			if (!mProductPrice.save())
				throw new SQLException(CLogger.retrieveErrorAsString());
		}
		
		/* === Commitear transaccion === */
		Trx.getTrx(getTrxName).commit();
		
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
	
	private Integer getIdDireccionEntidadComercial(Integer c_bpartner_id) throws SQLException {
		
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Integer cBPartnerLocationID = null;
		
		try {
			String SQL = "select C_BPartner_Location_ID "
					+ "FROM C_BPartner_Location "
					+ "WHERE c_bpartner_id = ?";
	            
			pstmt = DB.prepareStatement( SQL );
	
			pstmt.setInt( 1, c_bpartner_id);
	
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
	private Map<Integer, List<MOrderLine>> getOrderLinePorIDPartner(MOrder ordenTrabajo) {
		
		Map<Integer, List<MOrderLine>> retorno = new HashMap<Integer, List<MOrderLine>>();
		MOrderLine[] lineas = ordenTrabajo.getLines();
		if (lineas == null || lineas.length == 0) {
			return retorno;
		}
	
		for (MOrderLine mOrderLine : lineas) {
			 Integer cBPartnerID = mOrderLine.getC_BPartner_ID();
			 // Pueden existir lineas sin entidad comercial, esto es porque son lineas de contratacion de servicios.
			 if (cBPartnerID == null)
				 continue;
			 
			 List<MOrderLine> lista = retorno.get(cBPartnerID);
			 
			 if (lista == null) {
				 lista = new ArrayList<MOrderLine>();
			 }
			 
			 lista.add(mOrderLine);
			 retorno.put(cBPartnerID, lista);
		}
		
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
	private MInvoice createInvoiceFromOrder(MOrder ordenTrabajo, int invoiceDocTypeTargetID, int invoicePuntoDeVenta, String invoiceTipoComprobante, 
			boolean completeInvoice, Timestamp dateInvoiced, Timestamp dateAcct, String trxName) throws ModelException, Exception 
	{
		try 
		{
			log.log(Level.SEVERE, "Orden a procesar: " + ordenTrabajo);
			
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
				// Crear nueva linea y setearle los datos originales de la linea de pedido
				MInvoiceLine anInvoiceLine = new MInvoiceLine(anInvoice);
				anInvoiceLine.setOrderLine(orderLines[i]);
				// Copia general de campos de cabecera
				CreateFromInvoice.copyLineValuesFromOrderLine(anInvoice, ordenTrabajo, anInvoiceLine, orderLines[i], ordenTrabajo.getCtx(), trxName);
				// Copiar los datos de la linea de pedido en la linea de la factura
				copyPOValues(orderLines[i], anInvoiceLine);
				// Persistir la linea
				if (!anInvoiceLine.save())
					throw new ModelException("Error al persistir linea de factura:" + CLogger.retrieveErrorAsString());
			}
			
			// Completar la factura si corresponde
			if (completeInvoice && !DocumentEngine.processAndSave(anInvoice, DocAction.ACTION_Complete, false))
				throw new ModelException("Error al completar la factura:" + Msg.parseTranslation(ordenTrabajo.getCtx(), anInvoice.getProcessMsg()));
	
			actualizarEstadosFactura(ordenTrabajo, ESTADO_FACTURACION_FACTURADO, "EN CURSO", trxName);
			
			log.log(Level.SEVERE, "C_Invoice_ID " + anInvoice.getC_Invoice_ID());
			
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
	
	private List<MOrderLine> crearLista(MOrderLine[] lines) {
		List<MOrderLine> array = new ArrayList<MOrderLine>();
		
		for (int i = 0; i < lines.length; i++) {
			MOrderLine mOrderLine = lines[i];
			array.add(mOrderLine);
		}
		
		return array;
	}

	private void actualizarEstadosFactura(MOrder anOrder, String estadoFacturacion, String estado_pedido_proveedor, String trxName) throws SQLException {
		
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
	public void ajustarOrdenYFactura(Integer cOrderID) throws SQLException {
		
		Connection con = DB.createConnection(false, Connection.TRANSACTION_READ_COMMITTED);
		try {
			// NO SERIA NECESARIO
			// decrementarSecuenciador(con);
		
			asignarEstadoFacturacionYReabrir(con, cOrderID, "ERROR");
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
	
	private void asignarEstadoFacturacionYReabrir(Connection con, Integer cOrderID, String estadoFacturacion) throws SQLException {
		String sql = "UPDATE c_order "
				+ "SET docstatus=?, "
				+ "docaction=?, "
				+ "processing=?, "
				+ "processed=?, "
				+ "estado_facturacion = ?"
				+ "WHERE c_order_id = ?";
		
		// PreparedStatement pstmt = new CPreparedStatement( ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE, sql, "Actualizacion" );
		
		PreparedStatement pstmt = con.prepareStatement(sql);

        pstmt.setString( 1, "DR");
        
        pstmt.setString( 2, "CO");
        
        pstmt.setString( 3, "N");
        
        pstmt.setString( 4, "N");
        
        pstmt.setString( 5, estadoFacturacion);
        
        pstmt.setInt( 6, cOrderID);

        pstmt.executeUpdate();

        pstmt.close();
	}

	private void decrementarSecuenciador(Connection con) throws SQLException {
		
			
		String sql = "UPDATE ad_sequence "
				+ " SET currentnext=? "
				+ "WHERE ad_sequence_id = ?";
		
		PreparedStatement pstmt = con.prepareStatement(sql);
        
		// pstmt = new CPreparedStatement( ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE, sql, "Actualizacion" );

        pstmt.setInt( 1, this.currentNext);
        
        pstmt.setInt( 2, this.adSequenceId);

        pstmt.executeUpdate();

        pstmt.close();
		
	}
	
	public boolean sePuedeRechazar(MOrder mOrder) {
		if (ESTADO_FACTURACION_RECHAZADA.equals(mOrder.get_Value("estado_facturacion"))
				|| ESTADO_FACTURACION_ERROR.equals(mOrder.get_Value("estado_facturacion")))
			return true;
		
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
			this.createInvoiceFromOrder(mOrder, invoiceDocTypeTargetID, invoicePuntoDeVenta, invoiceTipoComprobante, completeInvoice, dateInvoiced, dateAcct, trxName);
			
			/* === Commitear transaccion === */
			Trx.getTrx(trxName).commit();
		} catch (Throwable t) {
			/* === Rollback transaccion === */
			Trx.getTrx(trxName).rollback();
			
			throw t;
		}
		
	}
	
	

	private String getFechaFormateado(Date date, String formatoDestino) {
		
		SimpleDateFormat sdf = new SimpleDateFormat(formatoDestino);
		return sdf.format(date);
			
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

	/*public void createPriceList(MOrderLine[] lines) {
		
		MPriceList mPriceList = new MPriceList(Env.getCtx(), 0, "trxName");
		mPriceList.set_Value("ad_client_id, ad_client_id);
		
		MPriceListVersion priceListVersion = new MPriceListVersion(mPriceList);
		
		for (MOrderLine mOrderLine : lines) {
			
		}
		
	}*/
	
}
