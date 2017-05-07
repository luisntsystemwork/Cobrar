package org.libertya.ws.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.libertya.ws.bean.parameter.PriceParameterBean;
import org.libertya.ws.bean.parameter.PriceProduct;
import org.libertya.ws.bean.result.ResultBean;
import org.libertya.ws.handler.PriceProductHandler;
import org.openXpertya.plugin.common.CustomServiceInterface;
import org.openXpertya.plugin.common.DynamicArgument;
import org.openXpertya.plugin.common.DynamicResult;
import org.openXpertya.util.StringUtil;

public class PriceListCreate implements CustomServiceInterface {

	@Override
	public DynamicResult execute(DynamicArgument args, Properties ctx,
			String trxName) {
		Map<String, ArrayList<String>> content = args.getContent();

		String userName = content.get("userName").get(0);
		String password = content.get("password").get(0);
		String clientID = content.get("AD_Client_ID").get(0);
		String orgID    = content.get("ad_org_id").get(0);
		
		String nombreCarpeta = content.get("nombreCarpeta").get(0);
		
		PriceParameterBean priceParameterBean = new PriceParameterBean(userName, password, Integer.parseInt(clientID), Integer.parseInt(orgID));
		// String userName, String password, int clientID,	int orgID
		priceParameterBean.addColumnToMainTable("userName", userName);
		priceParameterBean.addColumnToMainTable("password", password);
		priceParameterBean.addColumnToMainTable("ad_org_id", orgID);
		priceParameterBean.addColumnToMainTable("AD_Client_ID", clientID);
		
		priceParameterBean.addColumnToMainTable("nombreCarpeta", nombreCarpeta);
		
		for (int i = 0; i < 99; i++) 
		{
			String key = "precioFacturacion" + StringUtils.leftPad(i+"", 2, "0");
			ArrayList<String> precioFacturacionArray = content.get(key);
			
			key   = "preciomaximocompra" + StringUtils.leftPad(i+"", 2, "0");
			ArrayList<String> preciomaximocompraArray = content.get(key);
			
			key = "M_Product_ID" + StringUtils.leftPad(i+"", 2, "0");
			ArrayList<String> mProductIDArray = content.get(key);
			
			if (precioFacturacionArray == null || preciomaximocompraArray == null || mProductIDArray == null) {
				break;
			}
			String precioFacturacion = precioFacturacionArray.get(0);
			String preciomaximocompra = preciomaximocompraArray.get(0);
			String mProductID = mProductIDArray.get(0);
			PriceProduct priceProduct = new PriceProduct(new BigDecimal(precioFacturacion), new BigDecimal(preciomaximocompra), Integer.valueOf(mProductID));
			
			priceParameterBean.addPriceProduct(priceProduct);
		}
		
		ResultBean p = new PriceProductHandler().priceProductCreate(priceParameterBean);
		
		DynamicResult dynamicResult = new DynamicResult();
		
		if (p.isError()) {
			dynamicResult.setError(p.isError());
			dynamicResult.setErrorMsg(p.getErrorMsg());
		}
		else 
		{
			HashMap<String, ArrayList<String>> content2 = new HashMap<String, ArrayList<String>>();
			ArrayList<String> valueResultado = new ArrayList<String>();
			valueResultado.add(p.getMainResult().get("C_Project_ID"));
			content2.put("C_Project_ID", valueResultado);
			dynamicResult.setContent(content2);
		}
		return dynamicResult;
	}

}
