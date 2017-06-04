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

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.openXpertya.util.CPreparedStatement;
import org.openXpertya.util.Env;

public class ReporteFacturaClienteDataSource implements JRDataSource {

	private int pProjectID;
	private int pCurrencyID;
	private int pBPartnerID;
	/** Fecha desde y hasta de las facturas */
	private Date p_dateFrom;
	private Date p_dateTo;

	private String trxName = null;

	private int currentRecord = -1;
	private int totalLines = -1;

	/** Utilizado para mapear los campos con las invocaciones de los metodos */
	HashMap<String, String> methodMapper = new HashMap<String, String>();
		
	private List<LineaPedido> lines;
	
	private LineaPedido line = null;
	
	private BigDecimal total = BigDecimal.ZERO;
	private BigDecimal totalHBL = BigDecimal.ZERO;
	
	private BigDecimal totalImporte = BigDecimal.ZERO;
	private BigDecimal totalImporteHBL = BigDecimal.ZERO;
	
	private String sQLQuery = null;
	
	public ReporteFacturaClienteDataSource(int pProjectID, int pCurrencyID, int pBPartnerID, 
			Date p_dateFrom,
			Date p_dateTo,
			String trxName, String sQLQuery, BigDecimal total, BigDecimal totalHBL) {
		this(pProjectID, pCurrencyID, pBPartnerID, 
				p_dateFrom,
				p_dateTo,
				trxName, sQLQuery);
		this.totalImporte = total;
		this.totalImporteHBL = totalHBL;
	}
	

	public ReporteFacturaClienteDataSource(int pProjectID, int pCurrencyID, int pBPartnerID, 
			Date p_dateFrom,
			Date p_dateTo,
			String trxName, String sQLQuery) {
		this.pProjectID = pProjectID;
		this.pCurrencyID = pCurrencyID;
		this.pBPartnerID = pBPartnerID;
		this.p_dateFrom = p_dateFrom;
		this.p_dateTo = p_dateTo;
		this.sQLQuery = sQLQuery;
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

	private String getQuery() {
		return this.sQLQuery;
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
			pstmt.setInt(j++, this.pProjectID);
			pstmt.setInt(j++, this.pCurrencyID);
			pstmt.setInt(j++, this.pBPartnerID);
			
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
				
				allByCategoriaIVAList.add(new LineaPedido(documento, numerodoc, concepto, lineTotalAmt, iso_code, preciomaximocompra, precioinformado, razonsocial, dateInvoicedFormated, tipocambio));
				
				if (preciomaximocompraBig != null) {
					this.total = this.total.add(preciomaximocompraBig);
				}
				if (precioinformadoBig != null) {
					this.totalHBL = this.totalHBL.add(precioinformadoBig);
				}
			}
			if (allByCategoriaIVAList.isEmpty()) {
				allByCategoriaIVAList.add(LineaPedido.NULL);
			}
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

	/* Retorna el valor correspondiente al campo indicado */
	public Object getFieldValue(JRField field) throws JRException {
		
		String name = field.getName().toUpperCase();
		
		if ("TOTAL_EGRESADO".equals(name)) {
			return this.getTotal();
		}
		
		if ("TOTAL_EGRESADO_HBL".equals(name)) {
			return getTotalHBL();
		}
		
		if ("TOTAL_IMPORTE".equals(name)) {
			return this.totalImporte.add(this.getTotal());
		}
		
		if ("TOTAL_IMPORTE_HBL".equals(name)) {
			return this.totalImporteHBL.add(getTotalHBL());
		}
		
		Class<?> clazz = null;
		Method method = null;
		Object output = null;
		try {
			// Invocar al metodo segun el campo correspondiente
			clazz = Class
					.forName("org.openXpertya.JasperReport.DataSource.LineaPedido");
			method = clazz.getMethod(methodMapper.get(name));
			output = (Object) method.invoke(line);
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

	public boolean next() throws JRException {
		currentRecord++;

		if (currentRecord >= totalLines) {
			return false;
		}

		line = getLines().get(currentRecord);
		return true;
	}
	
	private List<LineaPedido> getLines() {
		return lines;
	}

	public String getTrxName() {
		return trxName;
	}

	public void setTrxName(String trxName) {
		this.trxName = trxName;
	}
	
	public BigDecimal getTotal() {
		
		return this.total;
	}

	/**
	 * @return the totalHBL
	 */
	public BigDecimal getTotalHBL() {
		
		return this.totalHBL;
	}
	
}


