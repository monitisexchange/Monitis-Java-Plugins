package org.monitis.logmonitor;

public class Utils {

	/**
	 * simple uri encoder, made from the spec at:
	 * http://www.ietf.org/rfc/rfc2396.txt
	 */
	public static String encodeURI(String argString) {
		final String mark = "-_.!~*'()\"";
		StringBuilder uri = new StringBuilder();

		char[] chars = argString.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')
					|| (c >= 'A' && c <= 'Z') || mark.indexOf(c) != -1) {
				uri.append(c);
			} else {
				uri.append("%");
				uri.append(Integer.toHexString((int) c));
			}
		}
		return uri.toString();
	}
	
}
