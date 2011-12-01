package org.monitis.logmonitor.logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class MonitisFilter extends Filter {
	private String filterPattern = null;
	private Pattern cmp_pattern = null;
	private Level minAllowLevel = Level.WARN;


	@Override
	public void activateOptions() {
		if (filterPattern != null && filterPattern.length() > 0) {
			try {
				cmp_pattern = Pattern.compile(filterPattern, Pattern.CASE_INSENSITIVE);
			} catch (Exception e){
				LogLog.debug(e.getMessage());
			}
		}
	}
	  
	@Override
	public int decide(LoggingEvent event) {
		int ret = DENY;
		if (event.getLevel().isGreaterOrEqual(minAllowLevel)){
			if (cmp_pattern != null){
				String str = (String) event.getMessage();
				Matcher matcher = cmp_pattern.matcher(str);
				if (matcher.find()) {
					ret = ACCEPT;
				}
			} else {
				ret = ACCEPT;
			}
		}
		return ret;
	}

	public String getFilterPattern() {
		return filterPattern;
	}

	public void setFilterPattern(String filterPattern) {
		this.filterPattern = filterPattern;
	}


/**
 * DENY    = -1;
 * NEUTRAL = 0;
 * ACCEPT  = 1;
 */
	public static void main(String[] args) {
//		Category logger = new Category("blin");
//		MonitisFilter mf = new MonitisFilter();
//		mf.setFilterPattern("(Error|Fatal|Warn*|127.0.0.1)");
//		mf.activateOptions();
//		String[] astr = {"error message", "normal message", "warn_ing message", "fatal message", "request from 127.0.0.1"};
//		for (int i = 0; i < astr.length; i++) {
//			String str = astr[i];
//			LoggingEvent event = new LoggingEvent("org.apache.log4j", (Category)logger, (org.apache.log4j.Priority)Level.ERROR, (Object)str, new Throwable());
//			System.out.println("Filter decided ("+str+") = "+mf.decide(event));
//		}
	}


}
