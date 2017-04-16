package com.navicon.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {

	/**
     * <p>The maximum size to which the padding constant(s) can expand.</p>
     */
    private static final int PAD_LIMIT = 8192;
    
    /**
     * <p>Returns padding using the specified delimiter repeated
     * to a given length.</p>
     *
     * <pre>
     * StringUtils.repeat(0, 'e')  = ""
     * StringUtils.repeat(3, 'e')  = "eee"
     * StringUtils.repeat(-2, 'e') = ""
     * </pre>
     *
     * <p>Note: this method doesn't not support padding with
     * <a href="http://www.unicode.org/glossary/#supplementary_character">Unicode Supplementary Characters</a>
     * as they require a pair of {@code char}s to be represented.
     * If you are needing to support full I18N of your applications
     * consider using {@link #repeat(String, int)} instead.
     * </p>
     *
     * @param ch  character to repeat
     * @param repeat  number of times to repeat char, negative treated as zero
     * @return String with repeated character
     * @see #repeat(String, int)
     */
    public static String repeat(char ch, int repeat) {
        char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }
    
    /**
     * <p>Left pad a String with a specified String.</p>
     *
     * <p>Pad to a size of {@code size}.</p>
     *
     * <pre>
     * StringUtils.leftPad(null, *, *)      = null
     * StringUtils.leftPad("", 3, "z")      = "zzz"
     * StringUtils.leftPad("bat", 3, "yz")  = "bat"
     * StringUtils.leftPad("bat", 5, "yz")  = "yzbat"
     * StringUtils.leftPad("bat", 8, "yz")  = "yzyzybat"
     * StringUtils.leftPad("bat", 1, "yz")  = "bat"
     * StringUtils.leftPad("bat", -1, "yz") = "bat"
     * StringUtils.leftPad("bat", 5, null)  = "  bat"
     * StringUtils.leftPad("bat", 5, "")    = "  bat"
     * </pre>
     *
     * @param str  the String to pad out, may be null
     * @param size  the size to pad to
     * @param padStr  the String to pad with, null or empty treated as single space
     * @return left padded String or original String if no padding is necessary,
     *  {@code null} if null String input
     */
    public static String leftPad(String str, int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = " ";
        }
        int padLen = padStr.length();
        int strLen = str.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return leftPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            char[] padding = new char[pads];
            char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return new String(padding).concat(str);
        }
    }
    
	/**
     * <p>Left pad a String with a specified character.</p>
     *
     * <p>Pad to a size of {@code size}.</p>
     *
     * <pre>
     * StringUtils.leftPad(null, *, *)     = null
     * StringUtils.leftPad("", 3, 'z')     = "zzz"
     * StringUtils.leftPad("bat", 3, 'z')  = "bat"
     * StringUtils.leftPad("bat", 5, 'z')  = "zzbat"
     * StringUtils.leftPad("bat", 1, 'z')  = "bat"
     * StringUtils.leftPad("bat", -1, 'z') = "bat"
     * </pre>
     *
     * @param str  the String to pad out, may be null
     * @param size  the size to pad to
     * @param padChar  the character to pad with
     * @return left padded String or original String if no padding is necessary,
     *  {@code null} if null String input
     * @since 2.0
     */
    public static String leftPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > PAD_LIMIT) {
            return leftPad(str, size, String.valueOf(padChar));
        }
        return repeat(padChar, pads).concat(str);
    }
    
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
