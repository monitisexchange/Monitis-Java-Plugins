package org.monitis.logmonitor.logger;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;

/**
 * class that provide log records filtering and keeping 
 */
public class LogWriter extends Writer {

	private static List<String> list;
	private static int max_count = 100;

	
	public LogWriter(){
		super();
		if (list == null){
			list = new ArrayList<String>();
		}
	}
		
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		String _str = new String(cbuf, off, len);
		write(_str);
	}

	/* (non-Javadoc)
	 * @see java.io.Writer#write(java.lang.String)
	 */
	@Override
	public void write(String str) throws IOException {
		String _str = str;//filter(str);
		if (str != null && str.length() > 0){
			if (list.size() > max_count){
				list.remove(0);
				throw new IOException("The count of stored results exceed allowed max ("+max_count+")");
			}
			list.add(_str);
		}
	}

	/* (non-Javadoc)
	 * @see java.io.Writer#write(java.lang.String, int, int)
	 */
	@Override
	public void write(String str, int off, int len) throws IOException {
		String _str = str.substring(off, off+len);
		write(_str);
	}

	/**
	 * Returns the current size of accumulated list of filtered log-records
	 */
	public int getListLength(){
		return list.size();
	}
	
	/**
	 * Returns the accumulated list of filtered log-records
	 * 
	 * @param clear_source_list
	 *            if true, the source list will be cleaned after calling this method
	 */
	public List<String> getList(boolean clear_source_list) {
		List<String> _list = null;
		if (!list.isEmpty()) {
			_list = new ArrayList<String>();
			_list.addAll(list); // copy the list
			if (clear_source_list) {
				list.clear(); // clear the source list
			}
		}
		return _list;
	}
	
	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	private String filter(String str){
		String _str = null;
		if (str != null && str.length() > 0){
			// TODO if str match by pattern
			_str = str;
		}
		return _str;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
