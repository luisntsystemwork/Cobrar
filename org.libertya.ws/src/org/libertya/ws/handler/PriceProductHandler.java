package org.libertya.ws.handler;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.libertya.ws.bean.parameter.PriceParameterBean;
import org.libertya.ws.bean.parameter.PriceProduct;
import org.libertya.ws.bean.result.ResultBean;
import org.libertya.ws.exception.ModelException;
import org.openXpertya.model.MPriceList;
import org.openXpertya.model.MPriceListVersion;
import org.openXpertya.model.MProductPrice;
import org.openXpertya.model.M_Table;
import org.openXpertya.util.CLogger;
import org.openXpertya.util.DB;
import org.openXpertya.util.Env;
import org.openXpertya.util.Trx;

public class PriceProductHandler extends DocumentHandler{
	
	public ResultBean priceProductCreate(PriceParameterBean data)
	{
		try
		{
			/* === Configuracion inicial === */
			init(data, new String[]{}, new Object[]{});
			
			/* === Procesar (logica especifica) === */			
			// Instanciar y persistir BPartner
			//MProject newBPartner = new MProject(getCtx(), 0, getTrxName());
			
			String orgID =    data.getMainTable().get("ad_org_id");
			String AD_Client_ID = data.getMainTable().get("AD_Client_ID");
			String nombreCarpeta = data.getMainTable().get("nombreCarpeta");
			
			getListaPrecio(AD_Client_ID, orgID, nombreCarpeta, data.priceProductIterator());
			
			/* === Retornar valor === */
			HashMap<String, String> result = new HashMap<String, String>();
			result.put("resultado", Integer.toString(0));
			return new ResultBean(false, null, result);
		}
		catch (ModelException me) {
			return processException(me, wsInvocationArguments(data));
		}
		catch (Exception e) {
			return processException(e, wsInvocationArguments(data));
		}
		finally	{
			closeTransaction();
		}
	}
	
	private int getListaPrecio(String AD_Client_ID, String AD_Org_ID, String nombreCarpeta, Iterator<PriceProduct> mOrderLines) throws SQLException {
		
		String getTrxName = "trxName";
		
		MPriceListVersion mPriceListVersionVenta = generarPriceListVersion(true, AD_Client_ID, AD_Org_ID, nombreCarpeta, getTrxName);
		
		MPriceListVersion mPriceListVersionCosto = generarPriceListVersion(false, AD_Client_ID, AD_Org_ID, nombreCarpeta, getTrxName);
		
		while(mOrderLines.hasNext()) {
			PriceProduct priceProduct = mOrderLines.next();
			
			generarMProductPrice(true, mPriceListVersionVenta, priceProduct);
			
			generarMProductPrice(false, mPriceListVersionCosto, priceProduct);
		}
		
		/* === Commitear transaccion === */
		Trx.getTrx(getTrxName).commit();
		
		return 0;
	}
	
	private MProductPrice generarMProductPrice(Boolean esVenta, MPriceListVersion mPriceListVersion, PriceProduct priceProduct) throws SQLException {
		
		BigDecimal precio = esVenta ? priceProduct.getPriceEntered() : priceProduct.getPrecioMaximoCompra();
		int M_Product_ID = priceProduct.getMProductID();
		BigDecimal PriceList = precio;
		BigDecimal PriceStd =  precio;
		BigDecimal PriceLimit = precio;
		MProductPrice mProductPrice = new MProductPrice(mPriceListVersion, M_Product_ID, PriceList, PriceStd, PriceLimit);
		mProductPrice.setIsActive(true);
		
		if (!mProductPrice.save())
			throw new SQLException(CLogger.retrieveErrorAsString()); 
		
		return mProductPrice;
	}
	
	private MPriceListVersion generarPriceListVersion(Boolean esVenta, String AD_Client_ID, String AD_Org_ID, String nombreCarpeta, String trxName) throws SQLException {
		MPriceList mPriceList = getIdPriceList(esVenta, AD_Client_ID, AD_Org_ID, trxName);

		String nombreListaPrecio = nombreCarpeta + "-" + (esVenta ? "Ventas" : "Compras Inicial") + "-" + Env.getDateTime("yy-MM-dd HH:mm:ss");
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
		
		return mPriceListVersion;
	}
	
	private MPriceList getIdPriceList(Boolean esVenta, String AD_Client_ID, String AD_Org_ID, String getTrxName) throws SQLException {
		M_Table table = M_Table.get(Env.getCtx(), "M_PriceList");
		
		String sql = "SELECT * "
				+ "FROM M_PriceList "
				+ "where name = "+ (esVenta ? "'Ventas'" : "'Compras Inicial'") 
				+ " and ad_client_id = " + AD_Client_ID 
				+ " and ad_org_id = " + AD_Org_ID;
		
		PreparedStatement ps = DB.prepareStatement(sql, getTrxName);
		ResultSet rs = ps.executeQuery();

		List<MPriceList> listaPrecio = new ArrayList<MPriceList>();
		while (rs.next()) {
			listaPrecio.add((MPriceList) table.getPO(rs, getTrxName));
		}
		
		return !listaPrecio.isEmpty() ? listaPrecio.get(0) : null;
	}
}
