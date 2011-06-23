package org.sourcio.monitis.utils;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.sourcio.monitis.exception.MonitisException;

/**
 * 
 * @author ngaspary
 *
 */
public class StringUtils {
	
	private StringUtils(){}
	
	public static String getFormatedString(String str, int length){
		String returnStr = str.substring(0, str.length()<length ? str.length() : length);
		returnStr += getString(" ", length - returnStr.length());
		return returnStr;
	}
	
	public static String getString(String substr, int count){
		String str = "";
		for (int i=0; i<count; i++){
			str += substr;
		} 
		return str;
	}
	
	public static String arrayToString(Object [] array, String separator, int beginIndex, int endIndex){
		StringBuffer buf = new StringBuffer();
		for (int i = beginIndex; i < array.length && i < endIndex; buf.append(array[i++]).append(separator));
		String encoded = buf.toString();
		return encoded;
	}
	
	public static String urlEncode(String str) throws MonitisException{
		String encodedStr;
		try {
			encodedStr = URLEncoder.encode(str, Constants.ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new MonitisException(e, "Error encoding string: "+str);
		}
		return encodedStr;
	}
	
	public static String urlDecode(String str) throws MonitisException{
		String decodedStr;
		try {
			decodedStr = URLDecoder.decode(str, Constants.ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new MonitisException(e, "Error decoding string: "+str);
		}
		return decodedStr;
	}
	static final byte[] HEX_CHAR_TABLE = {
    	(byte)'0', (byte)'1', (byte)'2', (byte)'3',
    	(byte)'4', (byte)'5', (byte)'6', (byte)'7',
    	(byte)'8', (byte)'9', (byte)'a', (byte)'b',
    	(byte)'c', (byte)'d', (byte)'e', (byte)'f'
	};    
	
	public static String getHexString(byte[] raw) throws UnsupportedEncodingException{
	  	byte[] hex = new byte[2 * raw.length];
	    int index = 0;
	
	    for (byte b : raw) {
	      int v = b & 0xFF;
	      hex[index++] = HEX_CHAR_TABLE[v >>> 4];
	      hex[index++] = HEX_CHAR_TABLE[v & 0xF];
	    }
	    return new String(hex, "ASCII");
	}
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	
	public static String join(Collection s, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator iter = s.iterator();
        if (iter.hasNext()) {
            buffer.append(iter.next());
            while (iter.hasNext()) {
                buffer.append(delimiter);
                buffer.append(iter.next());
            }
        }
        return buffer.toString();
    }

	public static String prettyXMLFormat(String input_xml, int desired_indent) {
	    try {
	        Source xmlInput = new StreamSource(new StringReader(input_xml));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", desired_indent);
	        Transformer transformer = transformerFactory.newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.transform(xmlInput, xmlOutput);
	        return xmlOutput.getWriter().toString();
	    } catch (Exception e) {
	        throw new RuntimeException(e); // simple exception handling, please review it
	    }
	}

}
