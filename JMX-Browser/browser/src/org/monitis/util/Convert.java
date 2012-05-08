package org.monitis.util;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;

public class Convert {
	
	/**
	 * Converts a 8 byte array of unsigned bytes to an double
	 * @param b an array of 8 unsigned bytes
	 * @return a double representing the 8 byte array
	 */
	public static double byteArrayToDouble(byte[] b, int offset) {
		long l = 0;
		l |= b[7+offset] & 0xFF;
		l <<= 8;
		l |= b[6+offset] & 0xFF;
		l <<= 8;
		l |= b[5+offset] & 0xFF;
		l <<= 8;
		l |= b[4+offset] & 0xFF;
		l <<= 8;
		l |= b[3+offset] & 0xFF;
		l <<= 8;
		l |= b[2+offset] & 0xFF;
		l <<= 8;
		l |= b[1+offset] & 0xFF;
		l <<= 8;
		l |= b[0+offset] & 0xFF;
		return Double.longBitsToDouble(l);
	}
	
	/**
	 * Converts a 8 byte array of unsigned bytes to an long
	 * @param b an array of 8 unsigned bytes
	 * @return a long representing the 8 byte array
	 */
	public static long byteArrayToInt64(byte[] b, int offset) {
		long l = 0;
		l |= b[7+offset] & 0xFF;
		l <<= 8;
		l |= b[6+offset] & 0xFF;
		l <<= 8;
		l |= b[5+offset] & 0xFF;
		l <<= 8;
		l |= b[4+offset] & 0xFF;
		l <<= 8;
		l |= b[3+offset] & 0xFF;
		l <<= 8;
		l |= b[2+offset] & 0xFF;
		l <<= 8;
		l |= b[1+offset] & 0xFF;
		l <<= 8;
		l |= b[0+offset] & 0xFF;
		return l;
	}

	/**
	 * Converts a 4 byte array of unsigned bytes to an int
	 * @param b an array of 4 unsigned bytes
	 * @return a long representing the 4 byte array
	 */
	public static long byteArrayToInt32(byte[] b, int offset) {
		long l = 0;
		l |= b[3+offset] & 0xFF;
		l <<= 8;
		l |= b[2+offset] & 0xFF;
		l <<= 8;
		l |= b[1+offset] & 0xFF;
		l <<= 8;
		l |= b[0+offset] & 0xFF;
		return l;
	}

	/**
	 * Converts a 2 byte array to an short integer
	 * @param b a byte array of length 2
	 * @return an short int representing the 2 byte array
	 */
	public static int byteArrayToInt16(byte[] b, int offset) {
		int i = 0;
		i |= b[1+offset] & 0xFF;
		i <<= 8;
		i |= b[0+offset] & 0xFF;
		return i;
	}

	/**
	 * Converts a zero ended byte array to an String object
	 * @param b a byte array of length 2
	 * @return an String representing the zero ended byte array
	 */
	public static String byteArrayToString(byte[] b, int offset, int max_count) {
		int count = 0;
		for (int i = 0; i < max_count; i++) {
			if (b[i + offset] == 0) {
				count = i; 
				break;
			}
		}
		if (count > 0) {
			return new String(b, offset, count);
		}
		return null;
	  }
	
	
	/**
	 * Converts a byte array to an HexString object
	 * @param b a byte array 
	 * @param offset indicates to start position for converting 
	 * @param count indicates how many bytes should be processed
	 * @return an hex string representing the specified byte array
	 */
	public static String byteArrayToHexString(byte[] b, int offset, int count) {
	    StringBuffer sb = new StringBuffer(count * 2);
	    int size = count;
	    int v;
	    for (int i = 0; i < size; i++) {
	      v = b[i+offset] & 0xff;
	      if (v < 16) {
	        sb.append('0');
	      }
	      sb.append(Integer.toHexString(v));
	    }
	    return sb.toString().toUpperCase();
	  }

	/**
	 * Converts a byte array to an HexString object
	 * @param array a byte array to converting
	 * @return hex string representing the specified byte array
	 */
	public static String byteArraytoHexString2(byte[] array) {
		String ret = null;
		if (array != null && array.length > 0) {
			try {
				ret = DatatypeConverter.printHexBinary(array);
			} catch (Exception e) {
				// nothing to do
			}
		}
		return ret;
	}

	/**
	 * Convert hex string to the corresponding byte array
	 * @param hex_string 
	 * @return byte array representing the specified hex string
	 */
	public static byte[] hexStringtoByteArray2(String hex_string) {
		byte[] ret = null;
		if (hex_string != null && hex_string.length() > 0) {
			try {
				ret = DatatypeConverter.parseHexBinary(hex_string);
			} catch (Exception e) {
				//nothing to do
			}
		}
		return ret;
	}
	
	public static String string2hex(String str) {
		StringBuffer strBuffer = new StringBuffer();
		if (str != null) {
			char[] chars = str.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				strBuffer.append(Integer.toHexString((int) chars[i]));
			}
		}
		return strBuffer.toString().toUpperCase();
	}
	
	//-------------------------------------------------- arrayToString2()
	// Convert an array of strings to one string.
	// Put the 'separator' string between each element.

	public static String arrayOfStringsToString2(String[] a, String separator) {
	    StringBuffer result = new StringBuffer();
	    if (a.length > 0) {
	        result.append(a[0]);
	        for (int i=1; i<a.length; i++) {
	            result.append(separator);
	            result.append(a[i]);
	        }
	    }
	    return result.toString();
	}

    //Convert String to byte[] using .getBytes() function
    public static byte[] string2bytes (String str){
    	return str.getBytes();
    }
    //Convert byte[] to String using new String(byte[])      
    public static String bytes2string (byte[] bytes){
    	return new String(bytes);
    }
 
	/**
	 * Convert any Object to the byte array
	 * @param obj
	 * @return byte array represents the input object
	 * @throws java.io.IOException
	 */
	public static byte[] getBytes(Object obj) throws java.io.IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(obj);
				oos.flush();
			oos.close();
		bos.close();
		return bos.toByteArray();
	}
	/**
	 * Internal method that retrieve JSON data from input String
	 * 
	 * @param tmp
	 *            String
	 * @return retrieved JSON object on success (otherwise - null)
	 * @throws Exception
	 */
	public static JSONObject toJson(String str) throws Exception {
		JSONObject json = null;
		if (str != null && str.length() > 0) {
			json = new JSONObject(str);
		}
		return json;
	}
	
	/**
	 * Returns Integer if input string contains integer value
	 * 
	 * @param str
	 *            String object to be converted
	 * @return Integer value if string contains the integer, otherwise returns NULL
	 */
	public static Integer getIntegerValue(String str) {
		Integer ret = null;
		try {
			ret = Integer.parseInt(str);
		} catch (Exception ex) {
			/* ignore */
		}
		return ret;
	}
		
	/**
	 * Returns Float if input string contains float value
	 * 
	 * @param str
	 *            String object to be converted
	 * @return Float value if string contains the floar, otherwise returns NULL
	 */
	public static Float getFloatValue(String str) {
		Float ret = null;
		try {
			ret = Float.parseFloat(str);
		} catch (Exception ex) {
			/* ignore */
		}
		return ret;
	}
}
