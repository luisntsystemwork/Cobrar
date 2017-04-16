package org.libertya.ws.bean.parameter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PriceParameterBean extends DocumentParameterBean {
	
	private List<PriceProduct> priceProduct;

	/**
	 * Constructor por defecto.  Ver superclase.
	 */
	public PriceParameterBean() {
		super();
		this.priceProduct = new ArrayList<PriceProduct>();
	}

	
	/**
	 * Constructor por defecto.  Ver superclase.
	 */
	public PriceParameterBean(String userName, String password, int clientID,	int orgID) {
		super(userName, password, clientID, orgID);
		this.priceProduct = new ArrayList<PriceProduct>();
	}

	/**
	 * Incorpora una nueva columna a los datos de parámetro la E.C. 
	 * @param columnName nombre de la columna
	 * @param columnValue valor de la columna
	 */
	public void addColumnToCProject(String columnName, String columnValue) {
		addColumnToMainTable(columnName, columnValue);
	}
	
	public void addPriceProduct(PriceProduct priceProduct) {
		this.priceProduct.add(priceProduct);
	}
	
	public Iterator<PriceProduct> priceProductIterator() {
		return this.priceProduct.iterator();
	}
}
