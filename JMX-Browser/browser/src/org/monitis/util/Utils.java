package org.monitis.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.Inflater;

import javax.management.openmbean.CompositeDataSupport;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils {

	public static String toMD5(String str){
    	MessageDigest md = null;
    	try {
			md = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
		}
		md.update(str.getBytes());
		 
        byte bytes[] = md.digest();
        return Convert.byteArraytoHexString2(bytes);
	}
	
	/**
	 * getting of formatted current date and time for GMT
	 * @return formated string with current date and time
	 *  	(Sat, 09 Jul 2011 09:41:26 GMT)
	 */
	public static String getGMTDate(){
		SimpleDateFormat f = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		f.setTimeZone(TimeZone.getTimeZone("GMT"));
		return f.format(new Date());
	}

	public static boolean isValidDate(String inDate, String pattern) {
		boolean ret = false;
		if (inDate != null) {
			if (pattern == null || pattern.length() <= 0){
				pattern = "yyyy-MM-dd";
			}
			SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

			if (inDate.trim().length() == dateFormat.toPattern().length()) {
				dateFormat.setLenient(false);
				try {// parse the inDate parameter
					dateFormat.parse(inDate.trim());
				} catch (Exception e) {
					return false;
				}
				ret = true;
			}
		}
		return ret;
	}

	public static byte[] readFile(String file_name) throws Exception {
		File file = new File(file_name);
		FileInputStream is = new FileInputStream(file);
		long file_length = file.length();
		byte[] buffer = new byte[(int)file_length];
		// Read in the buffer
		int offset = 0;
		int numRead = 0;
		while (offset < buffer.length
				&& (numRead=is.read(buffer, offset, buffer.length-offset)) >= 0) {
			offset += numRead;
		}
		is.close();
		return buffer;
	}

	public static void writeFile(String file_name, String data){
		byte[] buf = data.getBytes(Charset.forName("utf-8"));
		writeFile(file_name, buf, buf.length);
	}
	
	public static void writeFile(String file_name, byte[] data, int size) {
		try {
			File file = new File(file_name);
			FileOutputStream os = new FileOutputStream(file);
			os.write(data, 0, size);
			os.flush();
			os.close();
		} catch (Exception ex){
			ex.printStackTrace();
		}

	}
	
	/**
	 * 
	 * @param date
	 * @param pattern yyyy-MM-dd HH:mm:ss (default)
	 * @return formated string
	 */
	public static String getFormated(long timestamp, String pattern) {
		DateFormat myDateFormat = new SimpleDateFormat(pattern == null?"yyyy-MM-dd HH:mm:ss":pattern);
		try {
			return myDateFormat.format(new Date(timestamp));
		} catch (Exception e) {
			return String.valueOf(timestamp);
		}
	}

	/**
	 * Convert duration to the formated string like "0 days 00:05:15"
	 * 
	 * @param duration
	 *            duration in ms
	 * @return formated String
	 */
	public static String toFormatedTime(long duration) {
		long time = duration;
		int sec = (int) Math.floor((time / 1000) % 60);
		int min = (int) Math.floor((time / 60000) % 60);
		int hr = (int) Math.floor((time / 3600000) % 24);
		int da = (int) Math.floor(time / 86400000);
		return String.format("%d day %02d:%02d:%02d", da, hr, min, sec);
	}
	
	/**
	 * Put a new line into file
	 * 
	 * @param fileName
	 *            the path of file
	 * @param append
	 *            append new line to the existing file if this value is true
	 * @param sep
	 *            the separator that separates fields in the line
	 * @param line
	 *            the strings array (source)
	 * @return true on success
	 */
	public static boolean putIntoCSV(String fileName, boolean append, String sep, String[] line){
		FileWriter writer = null;
		boolean ret = true;
		File file = new File(fileName);
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			writer = new FileWriter(file, append);
			for (int i = 0; i < line.length; i++){
				if (i != 0){
					writer.append(sep);
				}
				writer.append(line[i]);
			}
			writer.append("\n");
		} catch (IOException e) {
			ret = false;
		} finally {
			if (writer != null){
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {/*ignore*/}
			}
		}
		return ret;
	}

	/**
	 * Put a new line into file
	 * 
	 * @param fileName
	 *            the path of file where every line is the JSON, e.g.
	 *            { "jmxObject": "java.lang:type=MemoryPool,name=PS Perm Gen","attribute": "Usage","key": "used", "format":"pg_pool_us:pg_pool_us::2"}
	 * @param append
	 *            append new line to the existing file if this value is true
	 * @param line
	 *            the strings array (source) where 
	 *            line[0] = jmxObject;
	 *            line[1] = attributeName;
	 *            line[2] = keyName (if exist);
	 *            line[3] = current value as a string;
	 * @return true on success
	 */
	public static boolean putAsJson(String fileName, boolean append, String[] line){
		FileWriter writer = null;
		boolean ret = true;
		String[] keys = {"jmxObject", "attribute", "key", "value", "format"};
		int last = keys.length - 1;
		
		JSONObject json = new JSONObject();
		for (int i = 0; i < keys.length; i++) {
			try {
				if (i < last) {
					if (line[i] != null && line[i].length() > 0) {
						json.putOpt(keys[i], line[i]);
					}
				} else {
					//parsing object
					String sep1 = ":";
					String sep2 = "ype=";
					String sep3 = ",";
					String sep4 = "name=";
					String str = "";
					int beg = 0;
					int end = line[0].indexOf(sep1);
					if (end > beg){
						str += line[0].substring(beg, end);
					}
					beg = line[0].indexOf(sep2, end + sep1.length())+sep2.length();
					end = line[0].indexOf(sep3, beg);
					if (end > beg){
						str += (str.length()>0?"_":"") + line[0].substring(beg, end);
					} else {
						str += (str.length()>0?"_":"") + line[0].substring(beg);
					}
					if (end > 0){
						beg = end;
					}
					end = line[0].indexOf(sep4, beg);
					if (end > 0){
						beg = end + sep4.length();
						end = line[0].indexOf(sep3, beg);
						if (end > beg){
							str += (str.length()>0?"_":"") + line[0].substring(beg, end);
						} else {
							str += (str.length()>0?"_":"") + line[0].substring(beg);
						}
					}
//					System.out.print(str);
					str += "_"  + line[1];
//					System.out.print(" -> "+str);					
					if (line[2] != null && line[2].length() > 0){
						str += "_" + line[2];
					}
//					System.out.print(" -> "+str);
					str = str+":"+str+"::2";
//					System.out.println(" -> "+str);
					json.putOpt(keys[i], str);
				}
			} catch (JSONException e) {/* ignore */	}
		}
		File file = new File(fileName);
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			writer = new FileWriter(file, append);
			writer.append(json.toString());
			writer.append(",\n");
		} catch (IOException e) {
			ret = false;
		} finally {
			if (writer != null){
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {/*ignore*/}
			}
		}
		return ret;
	}
}
