package org.libertya.ws.bean.parameter;

public class ProjectParameterBean extends DocumentParameterBean {
	
	/**
	 * Constructor por defecto.  Ver superclase.
	 */
	public ProjectParameterBean() {
		super();
	}

	
	/**
	 * Constructor por defecto.  Ver superclase.
	 */
	public ProjectParameterBean(String userName, String password, int clientID,	int orgID) {
		super(userName, password, clientID, orgID);
	}

	/**
	 * Incorpora una nueva columna a los datos de parámetro la E.C. 
	 * @param columnName nombre de la columna
	 * @param columnValue valor de la columna
	 */
	public void addColumnToCProject(String columnName, String columnValue) {
		addColumnToMainTable(columnName, columnValue);
	}
}
