package org.openXpertya.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.openXpertya.apps.search.helper.OrderInvoiceHelper;
import org.openXpertya.model.MOrder;
import org.openXpertya.model.M_Table;
import org.openXpertya.util.CLogger;
import org.openXpertya.util.DB;
import org.openXpertya.util.Env;


public class ProjectBatchProcessNTSW extends SvrProcess {
	
	protected CLogger log = CLogger.getCLogger(ProjectBatchProcessNTSW.class);
	
	private int m_count = 0;

	@Override
	protected void prepare() {
		// nada por ahora.
	}
	
	private List<MOrder> getOrdenesTrabajosSegunEstado(String estado) throws SQLException {
    	// Recuperar los registros de la bbdd 
		M_Table table = M_Table.get(Env.getCtx(), "C_Order");
		String sql = "SELECT * FROM C_Order WHERE estado_facturacion = '" + estado + "'";
		// public static CPreparedStatement prepareStatement( String RO_SQL,String trxName ) {
		PreparedStatement ps = DB.prepareStatement(sql, estado);
		ResultSet rs = ps.executeQuery();

		List<MOrder> ordenesDeTrabajo = new ArrayList<MOrder>();
		while (rs.next()) {
			// public PO getPO(ResultSet rs, String trxName)
			ordenesDeTrabajo.add((MOrder) table.getPO(rs, estado));
		}
		log.log(Level.SEVERE, "Se obtuvieron " + ordenesDeTrabajo.size());
		
		return ordenesDeTrabajo;
	}
	
	private Boolean isResponsableInscripto(int cBPartnerID) {
		Boolean isResponsableInscripto = Boolean.FALSE;
    	try {
            String SQL = "SELECT c.NAME "
            		+ "FROM c_bpartner b INNER JOIN C_Categoria_Iva c ON b.C_Categoria_Iva_id = c.C_Categoria_Iva_id "
            		+ "WHERE i_tipo_iva = 'RI' "
            		+ "AND c_bpartner_id = " + cBPartnerID;
            
            PreparedStatement pstmt = DB.prepareStatement( SQL );

            ResultSet rs = pstmt.executeQuery();

            if( rs.next()) {
                
            	isResponsableInscripto = Boolean.TRUE;
            }

            rs.close();
            pstmt.close();
        } catch( SQLException e ) {
            log.log( Level.SEVERE,"c_doctype_id",e );

            return Boolean.FALSE;
        }

        return isResponsableInscripto;
	}
	
	private int getInvoiceDocTypeTargetID(int invoicePuntoDeVenta, int cBPartnerID, int adClientID, int adOrgID) {
    	
		Boolean isResponsableInscripto = isResponsableInscripto(cBPartnerID);
		
		Integer docTypeID = 0;
    	try {
            String SQL = "SELECT c_doctype_id " 
            			+ "FROM C_DocType " 
            		    + "WHERE ad_client_id = " + adClientID    // 1
                         + " AND name like 'Factura B%" + invoicePuntoDeVenta + "%'";
            if (isResponsableInscripto) {
            	
            	SQL = "SELECT c_doctype_id " 
            			+ "FROM C_DocType " 
            		    + "WHERE ad_client_id = " + adClientID    // 1
                         + " AND name like 'Factura A%" + invoicePuntoDeVenta + "%'";
            }
            
            PreparedStatement pstmt = DB.prepareStatement( SQL );

            ResultSet rs = pstmt.executeQuery();

            if( rs.next()) {
                
                docTypeID = rs.getInt(1);
            }

            rs.close();
            pstmt.close();
        } catch( SQLException e ) {
            log.log( Level.SEVERE,"c_doctype_id",e );

            return -1;
        }

        return docTypeID;
	}

	@Override
	protected String doIt() throws Exception {
		int     original = Env.getAD_Client_ID( Env.getCtx() );
		int     originalCurrency = Env.getContextAsInt(Env.getCtx(), "$C_Currency_ID");
		int     originalUserID = Env.getContextAsInt(Env.getCtx(), "#AD_User_ID");
		
		try {
		
			log.log(Level.SEVERE, "Inicio de ProjectBatchProcessNTSW");
			
			List<MOrder> ordenesTrabajo = getOrdenesTrabajosSegunEstado(OrderInvoiceHelper.ESTADO_FACTURACION_PENDIENTE);
			
			for (MOrder mOrder : ordenesTrabajo) {
	
	            OrderInvoiceHelper helper = new OrderInvoiceHelper();
	            try {
	            	Env.setContext(Env.getCtx(), "#AD_Client_ID", mOrder.getAD_Client_ID());
	            	// 118 es PESOS y es la moneda de la compania
	            	Env.setContext( Env.getCtx(),"$C_Currency_ID", 118);
	            	Env.setContext( Env.getCtx(),"#AD_User_ID", 1010717);
	            	Env.setContext( Env.getCtx(),"$AD_User_ID", 1010717);
					String invoiceTipoComprobante = "FC";
					Date today = new Date();
					Timestamp dateInvoiced = new Timestamp(today.getTime());
					Timestamp dateAcct = new Timestamp(today.getTime());
					int invoicePuntoDeVenta = 4;
					int invoiceDocTypeTargetID =  getInvoiceDocTypeTargetID(invoicePuntoDeVenta, mOrder.getC_BPartner_ID(), mOrder.getAD_Client_ID(), mOrder.getAD_Org_ID()) ;
					// Se actualiza la descripcion por si se reproceso la orden
					mOrder.setDescription("");
					
					// EN UNA UNICA TRANSACCION SE DEBERA:
					// CREAR LA FACTURA PARA EL CLIENTE
					// CREAR LOS PEDIDOS AL PROVEEDOR (AGRUPAR LOS CONCEPTOS DE LA OT y generar M_Order)
					// Si falla un rollback completo.
					helper.procesarOrdenTrabajo(mOrder, invoiceDocTypeTargetID, 
								invoicePuntoDeVenta, invoiceTipoComprobante, true, dateInvoiced, dateAcct, mOrder.get_TrxName());
					
					log.log(Level.SEVERE, "Se genero la factura");
					
				} catch (Throwable e1) {
					
					log.log(Level.SEVERE, "Error al gener al factura" + e1.getMessage(), e1.getCause());
					/*
					 * 
					 * Decrementar el numero de secuencia, si aplica
					 * Asignar el estado "ERROR" a la orden de trabajo
					 * Reabrir la orden de factura.
					 * */
					try {
						log.log(Level.SEVERE, "Se ajusta la orden");
						helper.ajustarOrdenYFactura(mOrder.getC_Order_ID(), e1.getMessage());
					} catch (Throwable e2) {
						log.log(Level.SEVERE, "Error al ajustar la orden", e2.getCause());
					}
					
					
				}
	            
	        }
		} catch (Throwable t) {
			log.log(Level.SEVERE, "Ocurrio una excepcion", t.getCause());
		} finally {
			Env.setContext( Env.getCtx(), "#AD_Client_ID", original);
			Env.setContext( Env.getCtx(), "$C_Currency_ID",originalCurrency);
			Env.setContext( Env.getCtx(), "$AD_User_ID",originalUserID);
		}
        //
        return "#" + m_count;
	}

}
