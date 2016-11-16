package org.libertya.ws.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.libertya.ws.bean.parameter.ProjectParameterBean;
import org.libertya.ws.bean.result.ResultBean;
import org.libertya.ws.handler.ProjectHandler;
import org.openXpertya.plugin.common.CustomServiceInterface;
import org.openXpertya.plugin.common.DynamicArgument;
import org.openXpertya.plugin.common.DynamicResult;


public class ProjectCreateService implements CustomServiceInterface{

	/**
	 * Metodo dinámico a implementar para dar soporte genérico
	 * 
	 * @param 
	 * 	args es la serie de parámetros necesarios para la invocación
	 * @param
	 * 	ctx es el contexto
	 * @param
	 * 	trxName el nombre de la transacción
	 * @return 
	 * 	el resultado correspondiente de la invocación, en la que se debe cargar
	 * 	no solo los valores resultantes, sino tambien los valores isError y errorMsg
	 */
	public DynamicResult execute(DynamicArgument args, Properties ctx, String trxName) {
		
		Map<String, ArrayList<String>> content = args.getContent();

		String userName = content.get("userName").get(0);
		String password = content.get("password").get(0);
		String clientID = content.get("clientID").get(0);
		String orgID    = content.get("orgID").get(0);
		
		
		String value        = content.get("value").get(0);
		String name         =  content.get("name").get(0);
		String datecontract = content.get("datecontract").get(0);
		String datefinish   = content.get("datefinish").get(0);
		
		ProjectParameterBean project = new ProjectParameterBean(userName, password, Integer.parseInt(clientID), Integer.parseInt(orgID));
		// String userName, String password, int clientID,	int orgID
		project.addColumnToCProject("value", value);
		project.addColumnToCProject("name", name);
		project.addColumnToCProject("datecontract", datecontract);
		project.addColumnToCProject("datefinish", datefinish);
		
		project.addColumnToCProject("ad_client_id", clientID);
		project.addColumnToCProject("ad_org_id", orgID);
		project.addColumnToCProject("created", getFechaFormateado(new Date(), "yyyy-MM-dd HH:mm:s"));
		project.addColumnToCProject("updated", getFechaFormateado(new Date(), "yyyy-MM-dd HH:mm:s"));
		
		ResultBean p = new ProjectHandler().projectCreate(project);
		
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
	
	private String getFechaFormateado(Date date, String formatoDestino) {
		
		SimpleDateFormat sdf = new SimpleDateFormat(formatoDestino);
		return sdf.format(date);
			
		
	}

}
