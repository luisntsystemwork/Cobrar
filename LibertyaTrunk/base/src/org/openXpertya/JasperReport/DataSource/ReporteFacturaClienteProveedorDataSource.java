package org.openXpertya.JasperReport.DataSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.openXpertya.util.CPreparedStatement;
import org.openXpertya.util.Env;

public class ReporteFacturaClienteProveedorDataSource implements JRDataSource {

	/** Fecha desde y hasta de las facturas */
	private Date p_dateFrom;
	private Date p_dateTo;
	private int p_orgID;

	/** Tipo de transaccion */
	private String p_transactionType;

	/** Context */
	private Properties p_ctx;
	private String trxName = null;

	int m_currentRecord = -1;
	int total_lines = -1;

	/** Utilizado para mapear los campos con las invocaciones de los metodos */
	HashMap<String, String> methodMapper = new HashMap<String, String>();
	
	/** Data Sources */
	private InvoiceCustomerDataSource invoiceCustomerDataSource;
	
	private List<LineaPedido> m_lines;
	
	private LineaPedido m_line = null;
	
	private BigDecimal totalIngresado = BigDecimal.ZERO;

	public ReporteFacturaClienteProveedorDataSource(Properties ctx, Date p_dateFrom,
			Date p_dateTo, String p_transactionType, int p_OrgID,
			boolean groupCFInvoices, String trxName) {
		this.p_ctx = ctx;
		this.p_dateFrom = p_dateFrom;
		this.p_dateTo = p_dateTo;
		this.p_transactionType = p_transactionType;
		this.p_orgID = p_OrgID;
		setTrxName(trxName);
		
		methodMapper.put("TIPOCOMPROBANTE_LINEA", "getDocumento");
		methodMapper.put("NUMEROFACTURA_LINEA", "getNumeroDoc");
		methodMapper.put("CONCEPTO_LINEA", "getConcepto");
		methodMapper.put("IMPORTE_LINEA", "getLineTotalAmt");
		methodMapper.put("MONEDA_LINEA", "getIsoCode");
		methodMapper.put("IMPORTE_PESOS_LINEA", "getPrecioMaximoCompra");
		methodMapper.put("IMPORTE_HBL_LINEA", "getPrecioInformado");
		methodMapper.put("RAZON_SOCIAL_LINEA", "getRazonSocial");
		methodMapper.put("FECHA_LINEA", "getDateInvoicedFormated");
		methodMapper.put("TIPO_CAMBIO_LINEA", "getTipoCambio");

		methodMapper.put("AD_CLIENT_ID", "getAd_client_id");
		methodMapper.put("AD_ORG_ID", "getAd_org_id");
		methodMapper.put("ISACTIVE", "getIsActive");
		methodMapper.put("CREATED", "getCreated");
		methodMapper.put("CREATEDBY", "getCreatedby");
		methodMapper.put("UPDATED", "getUpdated");
		methodMapper.put("UPDATEDBY", "getUpdatedby");
		methodMapper.put("C_INVOICE_ID", "getC_invoice_id");
		methodMapper.put("ISSOTRX", "isSoTrx");
		methodMapper.put("DATEACCT", "getDateacct");
		methodMapper.put("DATEINVOICED", "getDateinvoiced");
		methodMapper.put("TIPODOCNAME", "getTipodocname");
		methodMapper.put("DOCUMENTNO", "getDocumentno");
		methodMapper.put("C_BPARTNER_NAME", "getBpartner_name");
		methodMapper.put("C_CATEGORIA_VIA_NAME", "getC_categoria_via_name");
		methodMapper.put("TAXID", "getTaxid");
		methodMapper.put("ITEM", "getItem");
		methodMapper.put("NETO", "getNeto");
		methodMapper.put("NETONOGRAVADO", "getNetoNoGravado");
		methodMapper.put("NETOGRAVADO", "getNetoGravado");
		methodMapper.put("IMPORTE", "getImporte");
		methodMapper.put("TOTAL", "getTotal");

	}

	private String getQuery() {
		
		StringBuffer query = new StringBuffer(
				" select doc.name as documento, i.documentno as numerodoc, p.name as concepto, il.linetotalamt, c.iso_code, ol.preciomaximocompra, ol.precioinformado, b.name as razonsocial, i.DateInvoiced, i.grandtotal as tipocambio "
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

		// Si no es ambos
		if (!p_transactionType.equals("B")) {
			// Si es transacción de ventas, C = Customer(Cliente)
			if (p_transactionType.equals("C")) {
				query.append(" AND i.issotrx = 'Y' ");
			}
			// Si es transacción de compra
			else {
				query.append(" AND i.issotrx = 'N' ");
			}
		}
		
		return query.toString() + " order by i.documentno, p.name";
	}

	public void loadData() throws RuntimeException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {

			int j = 1;
			pstmt = new CPreparedStatement(
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE,
					getQuery(), getTrxName(), true);
			
			pstmt.setInt(j++, Env.getAD_Client_ID(Env.getCtx()));
			pstmt.setInt(j++, Env.getAD_Org_ID(Env.getCtx()));
			pstmt.setTimestamp(j++, new Timestamp(this.p_dateFrom.getTime()));
			pstmt.setTimestamp(j++, new Timestamp(this.p_dateTo.getTime()));
			rs = pstmt.executeQuery();

			List<LineaPedido> allByCategoriaIVAList = new ArrayList<LineaPedido>();
			while (rs.next()) {
				
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
				
				String documento = rs.getString("documento");
				String numerodoc = rs.getString("numerodoc");
				String concepto = rs.getString("concepto");
				BigDecimal lineTotalAmt = rs.getBigDecimal("linetotalamt");
				String iso_code = rs.getString("iso_code");
				BigDecimal preciomaximocompra = rs.getBigDecimal("preciomaximocompra");
				BigDecimal precioinformado = rs.getBigDecimal("precioinformado");
				String razonsocial = rs.getString("razonsocial");
				Date dateInvoiced = rs.getDate("DateInvoiced");
				String dateInvoicedFormated = simpleDateFormat.format(dateInvoiced);
				BigDecimal tipocambio = rs.getBigDecimal("tipocambio");
				
				allByCategoriaIVAList.add(new LineaPedido(documento, numerodoc, concepto, lineTotalAmt, iso_code, preciomaximocompra, precioinformado, razonsocial, dateInvoicedFormated, tipocambio));
				
				if (precioinformado != null) {
					this.totalIngresado = this.totalIngresado.add(preciomaximocompra);
				}
			}
			
//			// Tabla de Impuestos
//			invoiceCustomerDataSource = new InvoiceCustomerDataSource();
//			Collections.sort(allByCategoriaIVAList);
//			invoiceCustomerDataSource.setReportLines(allByCategoriaIVAList);
			m_lines = new ArrayList<LineaPedido>();
			m_lines.addAll(allByCategoriaIVAList);
			total_lines = m_lines.size();
		
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				if(pstmt != null) pstmt.close();
				if(rs != null) rs.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	/* Retorna el valor correspondiente al campo indicado */
	public Object getFieldValue(JRField field) throws JRException {
		
		String name = null;
		Class<?> clazz = null;
		Method method = null;
		Object output = null;
		try {
			// Invocar al metodo segun el campo correspondiente
			name = field.getName().toUpperCase();
			clazz = Class
					.forName("org.openXpertya.JasperReport.DataSource.LineaPedido");
			method = clazz.getMethod(methodMapper.get(name));
			output = (Object) method.invoke(m_line);
		} catch (ClassNotFoundException e) {
			throw new JRException("No se ha podido obtener el valor del campo "
					+ name);
		} catch (NoSuchMethodException e) {
			throw new JRException("No se ha podido invocar el metodo "
					+ methodMapper.get(name));
		} catch (InvocationTargetException e) {
			throw new JRException("Excepcion al invocar el método "
					+ methodMapper.get(name));
		} catch (Exception e) {
			throw new JRException("Excepcion general al acceder al campo "
					+ name);
		}
		return output;
	}

	public static Object coalesce(Object object, Object defValue) {
		if (object == null)
			return defValue;
		return object;
	}

	/**
	 * Validacion por organización
	 */
	protected String getOrgCheck() {
		return (p_orgID > 0 ? " AND AD_Org_ID = " + p_orgID : "") + " ";
	}

	public boolean next() throws JRException {
		m_currentRecord++;

		if (m_currentRecord >= total_lines) {
			return false;
		}

		m_line = getLines().get(m_currentRecord);
		return true;
	}
	
	private List<LineaPedido> getLines() {
		return m_lines;
	}

	public JRDataSource getInvoiceCustomerDataSource() {
		return invoiceCustomerDataSource;
	}

	public String getTrxName() {
		return trxName;
	}

	public void setTrxName(String trxName) {
		this.trxName = trxName;
	}

	/*
	 *  *********************************************************** /
	 * OPDataSource: Clase que contiene la funcionalidad común a todos los
	 * DataSource de los subreportes del reporte.
	 * ***********************************************************
	 */
	class InvoiceCustomerDataSource implements JRDataSource {
		/** Lineas del informe */
		private List<LineaPedido> m_reportLines;
		/** Registro Actual */
		private int m_currentRecord = -1; // -1 porque lo primero que se hace es
											// un ++

		protected void setReportLines(List<LineaPedido> reportLines){
			m_reportLines = reportLines;
		}
		
		protected List<LineaPedido> getReportLines(){
			return m_reportLines;
		}
		
		public boolean next() throws JRException {
			m_currentRecord++;
			if (m_currentRecord >= m_reportLines.size())
				return false;

			return true;
		}

		public Object getFieldValue(JRField jrf) throws JRException {
			return getFieldValue(jrf.getName(), m_reportLines.get(m_currentRecord));
		}

		protected Object getFieldValue(String name, Object record)
				throws JRException {
			LineaPedido tax = (LineaPedido) record;
//			if (name.toUpperCase().equals("TIPOCOMPROBANTE")) {				
//				return tax.getDocTypeTarget();
//			} else if (name.toUpperCase().equals("NUMEROFACTURA")) {
//				return tax.getDocumentno();
//			} else if (name.toUpperCase().equals("CONCEPTO")) {
//				return tax.getName();
//			} else if (name.toUpperCase().equals("IMPORTE")) {
//				return tax.getLinetotalamt();
//			} 
			/*else if (name.toUpperCase().equals("SOPOTYPE")) {
				return tax.sopoType;
			} else if (name.toUpperCase().equals("TAXTYPE")) {
				return tax.taxType;
			} else if (name.toUpperCase().equals("TAXBASEAMOUNT")) {
				return tax.taxBaseAmount;
			} else if (name.equalsIgnoreCase("c_categoria_iva_name")) {
				return tax.categoriaIVAName;
			} else if(name.equalsIgnoreCase("total_categoria_iva_base_amt")){
				return tax.totalTaxBaseAmtByCategoriaIVA;
			}*/

			return null;
		}
	}

	public BigDecimal getTotalIngresado() {
		if (this.totalIngresado == null) {
			return BigDecimal.ZERO;
		}
		return this.totalIngresado;
	}
	
}


