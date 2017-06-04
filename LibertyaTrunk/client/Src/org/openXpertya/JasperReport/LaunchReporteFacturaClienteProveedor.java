package org.openXpertya.JasperReport;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import org.openXpertya.JasperReport.DataSource.JasperReportsUtil;
import org.openXpertya.JasperReport.DataSource.ReporteFacturaClienteDataSource;
import org.openXpertya.model.MProcess;
import org.openXpertya.process.ProcessInfo;
import org.openXpertya.process.ProcessInfoParameter;
import org.openXpertya.process.SvrProcess;
import org.openXpertya.util.DB;
import org.openXpertya.util.Env;

	public class LaunchReporteFacturaClienteProveedor extends SvrProcess {

		/** Jasper Report			*/
		private int AD_JasperReport_ID;
		
		/** Date Acct From			*/
		private Timestamp	p_dateFrom = null;
		/** Date Acct To			*/
		private Timestamp	p_dateTo = null;

		private int p_hoja;
		
		private int pProjectID = -1;
		
		private int pCurrencyID = -1;
		
		private int pBPartnerID = -1;
		
		@Override
		protected void prepare() {

			// Determinar JasperReport para wrapper, tabla y registro actual
			ProcessInfo base_pi = getProcessInfo();
			int AD_Process_ID = base_pi.getAD_Process_ID();
			MProcess proceso = MProcess.get(Env.getCtx(), AD_Process_ID);
			if(proceso.isJasperReport() != true)
				return;

			AD_JasperReport_ID = proceso.getAD_JasperReport_ID();
			/*[ProcessInfoParameter[C_Project_ID=1010142.000000{java.math.BigDecimal} (00000001_00000001), 
			 * ProcessInfoParameter[C_Currency_ID=118.000000{java.math.BigDecimal} (ARS), 
			 * ProcessInfoParameter[C_BPartner_ID=1015444.000000{java.math.BigDecimal} (AMESUR S.A.), 
			 * ProcessInfoParameter[DateAcct=2017-01-01 00:00:00.0{java.sql.Timestamp} (01.01.2017) - 2017-06-03 00:00:00.0{java.sql.Timestamp} (03.06.2017), 
			 * ProcessInfoParameter[Hoja=0.000000{java.math.BigDecimal} (0)]*/
	        ProcessInfoParameter[] para = getParameter();
	        for( int i = 0;i < para.length;i++ ) {
	            String name = para[ i ].getParameterName();
	            if( para[ i ].getParameter() == null ) ;	            
	            else {
	            	if(name.equals("C_Project_ID"))
		            {
		            	BigDecimal tmp = ( BigDecimal )para[ i ].getParameter();
		            	pProjectID = tmp == null ? null : tmp.intValue();
		            }
	            	if(name.equals("C_Currency_ID"))
		            {
		            	BigDecimal tmp = ( BigDecimal )para[ i ].getParameter();
		            	pCurrencyID = tmp == null ? null : tmp.intValue();
		            }
	            	if(name.equals("C_BPartner_ID"))
		            {
		            	BigDecimal tmp = ( BigDecimal )para[ i ].getParameter();
		            	pBPartnerID = tmp == null ? null : tmp.intValue();
		            }
	            	if(name.equals("DateAcct"))
					{
						p_dateFrom = (Timestamp)para[i].getParameter();
						p_dateTo = (Timestamp)para[i].getParameter_To();
					}
		            if(name.equals("Hoja"))
		            {
		            	BigDecimal tmp = ( BigDecimal )para[ i ].getParameter();
		            	p_hoja = tmp == null ? null : tmp.intValue();
		            }
	            }
	        }
			
		}
		
		@Override
		protected String doIt() throws Exception {
			return createReport();
		}

		private String createReport() throws Exception	{
						
			MJasperReport jasperwrapper = new MJasperReport(getCtx(), AD_JasperReport_ID, get_TrxName());
			
			ReporteFacturaClienteDataSource dsCliente = new ReporteFacturaClienteDataSource(
					this.pProjectID,
					this.pCurrencyID, this.pBPartnerID,
					(Date) p_dateFrom, (Date) p_dateTo, 
					get_TrxName(), getSQLQueryCliente());
			
			try {
				dsCliente.loadData();
			}
			catch (RuntimeException e)	{
				throw new RuntimeException("No se pueden cargar los datos del informe", e);
			}
			
			ReporteFacturaClienteDataSource dsProveedor = new ReporteFacturaClienteDataSource(
					this.pProjectID,
					this.pCurrencyID, this.pBPartnerID,
					(Date) p_dateFrom, (Date) p_dateTo, 
					get_TrxName(), getSQLQueryProveedor(),
					dsCliente.getTotal(), dsCliente.getTotalHBL());
			
			try {
				dsProveedor.loadData();
			}
			catch (RuntimeException e)	{
				throw new RuntimeException("No se pueden cargar los datos del informe", e);
			}
			
			///////////////////////////////////////
			MJasperReport proveedorSubreport = getFacturaProveedorSubreport(); 
//			 Se agrega el informe compilado como parámetro.
			jasperwrapper.addParameter("COMPILED_SUBREPORT_PROVEEDOR", new ByteArrayInputStream(proveedorSubreport.getBinaryData()));
//			 Se agrega el datasource del subreporte.
			jasperwrapper.addParameter("SUBREPORT_PROVEEDOR_DATASOURCE", dsProveedor);
			
			// Establecemos parametros
		 	Integer clientID = Env.getAD_Client_ID(getCtx());
			jasperwrapper.addParameter("AD_Client_ID", clientID);
			jasperwrapper.addParameter("TOTALCOMPROBANTES", "");
			jasperwrapper.addParameter("TOTALIMPORTES", "");
			jasperwrapper.addParameter("TOTALGRAVADOS", "-");
			jasperwrapper.addParameter("TOTALNOGRAVADOS", "-");
			jasperwrapper.addParameter("TOTAL_INGRESADO", dsCliente.getTotal());
			jasperwrapper.addParameter("TOTAL_INGRESADO_HBL", dsCliente.getTotalHBL());

			jasperwrapper.addParameter("HOJA", p_hoja);
			jasperwrapper.addParameter("COMPANIA", JasperReportsUtil.getClientName(getCtx(), clientID));
			jasperwrapper.addParameter("LOCALIZACION", "");
			
			jasperwrapper.addParameter("FECHADESDE", (Date)p_dateFrom);
			jasperwrapper.addParameter("FECHAHASTA",(Date) p_dateTo);
			
			try {
				jasperwrapper.fillReport(dsCliente);
				jasperwrapper.showReport(getProcessInfo());
			}
				
			catch (RuntimeException e)	{
				throw new RuntimeException ("No se ha podido rellenar el informe.", e);
			}
			
			return "doIt";
		}
		
		private String getSQLQueryProveedor() {
			StringBuffer query = new StringBuffer(
					" select doc.name as tipoComprobante, "
					+ "i.documentno as numeroFC, p.name as concepto, "
					+ "il.linetotalamt as importeMO, c.iso_code as moneda, "
					+ "ol.preciomaximocompra importePesos, "
					+ "ol.precioinformado importeHBL, "
					+ "b.name as razonsocial, i.DateInvoiced as fecha, "
					+ "i.grandtotal as tipocambio "
					+ "from C_Invoice i  "
					+ "inner join c_invoiceline il on i.c_invoice_id = il.c_invoice_id "
					+ "inner join m_product p on il.m_product_id = p.m_product_id "
					+ "inner join c_currency c on c.c_currency_id = i.c_currency_id "
					+ "inner join c_orderline ol on il.c_orderline_id = ol.c_orderline_id "
					+ "inner join c_bpartner b on b.c_bpartner_id = i.c_bpartner_id "
					+ "inner join c_doctype doc on doc.c_doctype_id = i.c_doctypetarget_id "
					+ "where i.IsActive='Y' "
					+ "and i.ad_client_id = ? " 
					+ "and i.ad_org_id = ? "
					+ "AND i.DateInvoiced::date BETWEEN ? ::date AND ? ::date ");

			query.append(" AND i.c_project_id = ? ");
			query.append(" AND c.c_currency_id = ? ");
			query.append(" AND b.c_bpartner_id = ? ");
			
			query.append(" AND i.issotrx = 'N' ");
			
			return query.toString() + " order by i.documentno, p.name";
		}

		private String getSQLQueryCliente() {
			
			StringBuffer query = new StringBuffer(
					" select doc.name as tipoComprobante, "
					+ "i.documentno as numeroFC, p.name as concepto, "
					+ "il.linetotalamt as importeMO, c.iso_code as moneda, "
					+ "ol.preciomaximocompra importePesos, "
					+ "ol.precioinformado importeHBL, "
					+ "b.name as razonsocial, i.DateInvoiced as fecha, "
					+ "i.grandtotal as tipocambio "
					+ "from C_Invoice i  "
					+ "inner join c_invoiceline il on i.c_invoice_id = il.c_invoice_id "
					+ "inner join m_product p on il.m_product_id = p.m_product_id "
					+ "inner join c_currency c on c.c_currency_id = i.c_currency_id "
					+ "inner join c_orderline ol on il.c_orderline_id = ol.c_orderline_id "
					+ "inner join c_bpartner b on b.c_bpartner_id = i.c_bpartner_id "
					+ "inner join c_doctype doc on doc.c_doctype_id = i.c_doctypetarget_id "
					+ "where i.IsActive='Y' "
					+ "and i.ad_client_id = ? " 
					+ "and i.ad_org_id = ? "
					+ "AND i.DateInvoiced::date BETWEEN ? ::date AND ? ::date ");

			query.append(" AND i.c_project_id = ? ");
			query.append(" AND c.c_currency_id = ? ");
			query.append(" AND b.c_bpartner_id = ? ");
			
			query.append(" AND i.issotrx = 'Y' ");
			
			return query.toString() + " order by i.documentno, p.name";
		}

		private MJasperReport getFacturaProveedorSubreport() throws Exception {
			String name = "SubReporte_FacturaProveedor";
			
			Integer jasperReport_ID = 
				(Integer)DB.getSQLObject(get_TrxName(), "SELECT AD_JasperReport_ID FROM AD_JasperReport WHERE Name ilike ?", new Object[] { name });
			if(jasperReport_ID == null || jasperReport_ID == 0)
				throw new Exception("Jasper Report not found - Tax-LibroIVA");
			
			MJasperReport jasperReport = new MJasperReport(getCtx(), jasperReport_ID, get_TrxName());
			return jasperReport;
		}
		
		
		
		
		
}
