package com.navicon.entities;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public enum FormaDePago {

	EFECTIVO      ("EFECTIVO",      "B"),
    A_CREDITO     ("CREDITO",       "P"),
    CHEQUE        ("CHEQUE",        "S"),
    TRANSFERENCIA ("TRANSFERENCIA", "Tr");

    private final String codigoNavicon;
    private final String codigoLibertya;
    
    FormaDePago(String codigoNavicon, String codigoLibertya) {
        this.codigoNavicon = codigoNavicon;
        this.codigoLibertya = codigoLibertya;
    }
    
    
   
    
    /*public static String getCodigoLibertyaByNavicon(String codigoNavicon)
    {
    	for (FormaDePago f: FormaDePago.values()) {
    		if (f.codigoNavicon.equals(codigoNavicon))
    			return f.codigoLibertya;
    	}
    	
    	return EFECTIVO.codigoLibertya;
    }*/

    public String getCodigoNavicon() {
		return codigoNavicon;
	}


	public String getCodigoLibertya() {
		return codigoLibertya;
	}


	public static FormaDePago getFormaDePago(String codigoNavicon) {
    	for (FormaDePago f: FormaDePago.values()) {
    		if (f.codigoNavicon.equals(codigoNavicon))
    			return f;
    	}
    	
    	return EFECTIVO;
    }
	
}
