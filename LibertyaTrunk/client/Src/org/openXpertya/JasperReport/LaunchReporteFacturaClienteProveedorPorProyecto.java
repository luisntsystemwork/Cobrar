package org.openXpertya.JasperReport;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import org.openXpertya.JasperReport.DataSource.JasperReportsUtil;
import org.openXpertya.JasperReport.DataSource.ReporteFacturaClienteDataSourcePorProyecto;
import org.openXpertya.model.MProcess;
import org.openXpertya.model.MProject;
import org.openXpertya.process.ProcessInfo;
import org.openXpertya.process.ProcessInfoParameter;
import org.openXpertya.process.SvrProcess;
import org.openXpertya.util.DB;
import org.openXpertya.util.Env;

public class LaunchReporteFacturaClienteProveedorPorProyecto extends SvrProcess {

		/** Jasper Report			*/
		private int AD_JasperReport_ID;
		
		/** Date Acct From			*/
		private Timestamp	p_dateFrom = null;
		/** Date Acct To			*/
		private Timestamp	p_dateTo = null;

		private int p_hoja;
		
		private int pProjectID = -1;
		
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
			
			ReporteFacturaClienteDataSourcePorProyecto dsCliente = new ReporteFacturaClienteDataSourcePorProyecto(
					this.pProjectID,
					(Date) p_dateFrom, (Date) p_dateTo, 
					get_TrxName(), getSQLQueryCliente());
			
			try {
				dsCliente.loadData();
			}
			catch (RuntimeException e)	{
				throw new RuntimeException("No se pueden cargar los datos del informe", e);
			}
			
			ReporteFacturaClienteDataSourcePorProyecto dsProveedor = new ReporteFacturaClienteDataSourcePorProyecto(
					this.pProjectID,
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
//			 Se agrega el informe compilado como par�metro.
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
			Integer orgID = Env.getAD_Org_ID(getCtx());
			jasperwrapper.addParameter("ORG_NAME", JasperReportsUtil.getOrgName(getCtx(), orgID));
			jasperwrapper.addParameter("LOCALIZACION", "");
			
			jasperwrapper.addParameter("CARPETA", new MProject(getCtx(), this.pProjectID, this.get_TrxName()).getName());
			jasperwrapper.addParameter("MONEDA", dsCliente.getMonedaProyecto());
			jasperwrapper.addParameter("CLIENTE", "");
			
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
					" select v.\"Tipo_Comprobante\", "
							+ "v.\"Nro_Comprobante \", "
							+ "v.\"Concepto\", "
							+ "v.ImporteMO, "
							+ "v.\"Mone_Comprob\", "
							+ "v.\"Mone_Proj\", "
							+ "v.\"CODIGO_Proyecto\", "
							+ "v.importe, "
							+ "v.importehbl, "
							+ "v.\"RazonSocial\", "
							+ "v.\"FECHA\", "
							+ "v.\"CotMulti\","
							+ "v.\"Nombre_Proyecto\" "
							+ "from c_project_ingresos_egresos_v as v "
							+ "where v.\"IngresoEgreso\" = 'EGRE' "
							+ "and v.ad_client_id = ? "
							+ "and v.ad_org_id = ? "
							+ "and to_date(v.\"FECHA\", 'YYYY/MM/DD')::date between ? ::date and ? ::date "
							+ "and v.\"CODIGO_Proyecto\" = ? ");
			
			return query.toString();
		}

		private String getSQLQueryCliente() {
			
			StringBuffer query = new StringBuffer(
					" select v.\"Tipo_Comprobante\", "
					+ "v.\"Nro_Comprobante \", "
					+ "v.\"Concepto\", "
					+ "v.ImporteMO, "
					+ "v.\"Mone_Comprob\", "
					+ "v.\"Mone_Proj\", "
					+ "v.\"CODIGO_Proyecto\", "
					+ "v.importe, "
					+ "v.importehbl, "
					+ "v.\"RazonSocial\", "
					+ "v.\"FECHA\", "
					+ "v.\"CotMulti\","
					+ "v.\"Nombre_Proyecto\" "
					+ "from c_project_ingresos_egresos_v as v "
					+ "where v.\"IngresoEgreso\" = 'INGRE' "
					+ "and v.ad_client_id = ? "
					+ "and v.ad_org_id = ? "
					+ "and to_date(v.\"FECHA\", 'YYYY/MM/DD')::date between ? ::date and ? ::date "
					+ "and v.\"CODIGO_Proyecto\" = ? ");
			
			return query.toString();
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
