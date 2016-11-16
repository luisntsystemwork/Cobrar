package org.libertya.ws.bean.parameter;

import java.util.ArrayList;
import java.util.HashMap;

import org.libertya.wse.common.SimpleMap;
import org.libertya.wse.param.DocumentLine;
import org.libertya.wse.utils.MapTranslator;

public class InvoiceParameterBean extends DocumentParameterBean {

	/** Otros impuestos de la factura */
	protected ArrayList<HashMap<String, String>> otherTaxes = new ArrayList<HashMap<String, String>>(); 

	/**
	 * Constructor por defecto.  Ver superclase.
	 */
	public InvoiceParameterBean() {
		super();
	}

	
	/**
	 * Constructor por defecto.  Ver superclase.
	 */
	public InvoiceParameterBean(String userName, String password, int clientID,	int orgID) {
		super(userName, password, clientID, orgID);
	}

	/**
	 * Constructor para el wrapper
	 */
	public InvoiceParameterBean(String userName, String password, int clientID,	int orgID, SimpleMap[] header, DocumentLine[] lines, DocumentLine[] otherTaxes) {
		super(userName, password, clientID, orgID, header, lines);
		load(otherTaxes);
	}
	
	/**
	 * Crea una nueva línea de otros impuestos, correspondiente a un conjunto de pares columna / valor
	 */
	public void newOtherTaxLine() {
		otherTaxes.add(new HashMap<String, String>());		
	}
	
	/**
	 * Incorpora una nueva columna a los datos de parámetro del impuesto actual
	 * @param columnName
	 * @param columnValue
	 */
	public void addColumnToCurrentOtherTaxLine(String columnName, String columnValue) {
		addColumnOnTable(otherTaxes.get(otherTaxes.size()-1), columnName, columnValue);
	}

	
	/** Basic getter para los impuestos */
	public ArrayList<HashMap<String, String>> getOtherTaxes() {
		return otherTaxes;
	}

	/** Basic setter para los impuestos */
	public void setOtherTaxes(ArrayList<HashMap<String, String>> otherTaxes) {
		this.otherTaxes = otherTaxes;
	}
	
	
	@Override
	public String toString() {
		StringBuffer out = new StringBuffer(super.toString());
		out.append("\n  Other taxes: ");
		if (otherTaxes!=null)
			for (HashMap<String, String> aTax : otherTaxes)
				if (aTax!=null) {
					out.append("\n    ");
					for (String key : aTax.keySet())
						out.append(key).append(" = ").
							append(aTax.get(key)).
							append("; ");
				}
		return out.toString();
	}
	
	public void load(DocumentLine[] taxes) {
		// Cargar impuestos
		if (otherTaxes!=null) {
			for (DocumentLine aTax : taxes)
				otherTaxes.add(MapTranslator.simpleMap2HashMap(aTax.getContent()));
		}
	}
}
