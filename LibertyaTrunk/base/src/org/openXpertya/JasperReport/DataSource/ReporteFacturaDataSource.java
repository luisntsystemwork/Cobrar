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

public class ReporteFacturaDataSource implements JRDataSource {
	
	/** Utilizado para mapear los campos con las invocaciones de los metodos */
	private HashMap<String, String> methodMapper = new HashMap<String, String>();
	
	private Date pDateFrom;
	private Date pDateTo;
	private String pTransactionType;
	private String trxName = null;
	private BigDecimal total = BigDecimal.ZERO;
	private List<LineaPedido> lines;
	private LineaPedido mLine = null;
	private int totalLines = -1;
	private int mCurrentRecord = -1;
	
	public ReporteFacturaDataSource(Properties ctx, Date pDateFrom,
			Date pDateTo, String pTransactionType, 
			boolean groupCFInvoices, String trxName) {
		this.pDateFrom = pDateFrom;
		this.pDateTo = pDateTo;
		this.pTransactionType = pTransactionType;
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
			pstmt.setTimestamp(j++, new Timestamp(this.pDateFrom.getTime()));
			pstmt.setTimestamp(j++, new Timestamp(this.pDateTo.getTime()));
			rs = pstmt.executeQuery();

			List<LineaPedido> allByCategoriaIVAList = new ArrayList<LineaPedido>();
			while (rs.next()) {
				
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
				
				String documento = rs.getString("tipoComprobante");
				String numerodoc = rs.getString("numeroFC");
				String concepto = rs.getString("concepto");
				BigDecimal lineTotalAmtBig = rs.getBigDecimal("importeMO");
				String lineTotalAmt = lineTotalAmtBig == null ? BigDecimal.ZERO.toString() : lineTotalAmtBig.toString();
				String iso_code = rs.getString("moneda");
				BigDecimal preciomaximocompraBig = rs.getBigDecimal("importePesos");
				String preciomaximocompra = preciomaximocompraBig == null ? BigDecimal.ZERO.toString() : preciomaximocompraBig.toString();
				BigDecimal precioinformadoBig = rs.getBigDecimal("importeHBL");
				String precioinformado = precioinformadoBig == null ? BigDecimal.ZERO.toString() : precioinformadoBig.toString();
				String razonsocial = rs.getString("razonsocial");
				Date dateInvoiced = rs.getDate("fecha");
				String dateInvoicedFormated = simpleDateFormat.format(dateInvoiced);
				BigDecimal tipocambioBig = rs.getBigDecimal("tipocambio");
				String tipocambio = tipocambioBig == null ? BigDecimal.ZERO.toString() : tipocambioBig.toString();
				
				allByCategoriaIVAList.add(new LineaPedido(documento, numerodoc, concepto, lineTotalAmt, iso_code, preciomaximocompra, precioinformado, razonsocial, dateInvoicedFormated, tipocambio,"", "", ""));
				
				if (preciomaximocompraBig != null) {
					this.total = this.total.add(preciomaximocompraBig);
				}
			}
			
//			// Tabla de Impuestos
//			invoiceCustomerDataSource = new InvoiceCustomerDataSource();
//			Collections.sort(allByCategoriaIVAList);
//			invoiceCustomerDataSource.setReportLines(allByCategoriaIVAList);
			lines = new ArrayList<LineaPedido>();
			lines.addAll(allByCategoriaIVAList);
			totalLines = lines.size();
		
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
	
	protected String getQuery() {
		
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
		if (!pTransactionType.equals("B")) {
			// Si es transacción de ventas, C = Customer(Cliente)
			if (pTransactionType.equals("C")) {
				query.append(" AND i.issotrx = 'Y' ");
			}
			// Si es transacción de compra
			else {
				query.append(" AND i.issotrx = 'N' ");
			}
		}
		
		return query.toString() + " order by i.documentno, p.name";
	}

	public String getTrxName() {
		return trxName;
	}

	public void setTrxName(String trxName) {
		this.trxName = trxName;
	}

	@Override
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
			output = (Object) method.invoke(mLine);
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

	@Override
	public boolean next() throws JRException {
		mCurrentRecord++;

		if (mCurrentRecord >= totalLines) {
			return false;
		}

		mLine = getLines().get(mCurrentRecord);
		return true;
	}

	/**
	 * @return the mLines
	 */
	public List<LineaPedido> getLines() {
		return lines;
	}
	
	

}
