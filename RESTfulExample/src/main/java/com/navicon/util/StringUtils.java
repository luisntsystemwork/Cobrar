package com.navicon.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {

    /**
      * <p>Checks if a CharSequence is empty ("") or null.</p>
      *
      * <pre>
      * StringUtils.isEmpty(null)      = true
      * StringUtils.isEmpty("")        = true
      * StringUtils.isEmpty(" ")       = false
      * StringUtils.isEmpty("bob")     = false
      * StringUtils.isEmpty("  bob  ") = false
      * </pre>
      *
      * <p>NOTE: This method changed in Lang version 2.0.
      * It no longer trims the CharSequence.
      * That functionality is available in isBlank().</p>
      *
      * @param cs  the CharSequence to check, may be null
      * @return {@code true} if the CharSequence is empty or null
      * @since 3.0 Changed signature from isEmpty(String) to isEmpty(CharSequence)
      */
    public static boolean isEmpty(final String cs) {
        return cs == null || cs.length() == 0;
    }

	public static boolean isNumeric(final String cs) {
		if (isEmpty(cs)) {
			return false;
		}
		final int sz = cs.length();
		for (int i = 0; i < sz; i++) {
			if (!Character.isDigit(cs.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isDate(String fechaOrdenTrabajo, String pattern) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);

        try {

            formatter.parse(fechaOrdenTrabajo);
            return true;
        }
        catch (Exception e) {
        	return false;
        }
	}

	public static String getFechaFormateado(String fecha, String formatoOriginal, String formatoDestino) {
		SimpleDateFormat formatter = new SimpleDateFormat(formatoOriginal);
		
		try {
			Date date = formatter.parse(fecha);
			
			
			SimpleDateFormat sdf = new SimpleDateFormat(formatoDestino);
			return sdf.format(date);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	* <p>Checks if the CharSequence contains only Unicode letters.</p>
	*
	* <p>{@code null} will return {@code false}.
	* An empty CharSequence (length()=0) will return {@code false}.</p>
	*
	* <pre>
	* StringUtils.isAlpha(null)   = false
	* StringUtils.isAlpha("")     = false
	* StringUtils.isAlpha("  ")   = false
	* StringUtils.isAlpha("abc")  = true
	* StringUtils.isAlpha("ab2c") = false
	* StringUtils.isAlpha("ab-c") = false
	* </pre>
	*
	* @param cs  the CharSequence to check, may be null
	* @return {@code true} if only contains letters, and is non-null
	* @since 3.0 Changed signature from isAlpha(String) to isAlpha(CharSequence)
	* @since 3.0 Changed "" to return false and not true
	*/
	public static boolean isAlpha(final String cs) {
		if (isEmpty(cs)) {
			return false;
		}
		final int sz = cs.length();
		for (int i = 0; i < sz; i++) {
			if (Character.isLetter(cs.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

}
