package org.monitis.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import org.json.JSONObject;
import org.json.JSONTokener;

public class MConfig {
	
	public enum prop_extension {//properties file possible extensions
		undefined,
		properties,
		json,
		xml
	}
	
	//default path to the properties file - JSON
	private final static String def_path = "/properties/mconfig.json";
	
	//default path to the properties file - Java properties
//  protected static final String def_path = "/properties/mconfig.properties"; 
	
	//relative path to the properties file
    protected String config_filename = null;
    //at the end of searching loop it will contain an absolute path to the properties file (if found)
    private String file = null;

    private static ArrayList<String> hash_instances = new ArrayList<String>();
    private static ArrayList<MConfig> instances = new ArrayList<MConfig>();
    private Properties props = new Properties();// loaded properties

    /**
     * Constructs MConfig class for default  
     * @throws Exception
     */
    private MConfig() throws Exception {
    	loadConfig();
    }

    private MConfig(String relPath) throws Exception {
    	config_filename = relPath.trim();
    	loadConfig();
    }
    
	/**
	 * Returns Mconfig class instance that is tuned to get properties from specified configuration file path
	 * 
	 * @param relPath
	 *            relative or absolute path to the configuration file (in Java properties or JSON form)
	 * @return new instance for Mconfig class if specified path isn't yet read, 
	 *            otherwise returns already existing instance
	 *            Returns "null" in case of cannot find specified configuration file.
	 */
	public static synchronized MConfig getConfig(String relPath) throws Exception {
		String str = relPath;
		if (str == null || str.length() <= 0) {
			str = def_path;
		}
		MConfig mc = null;
		String md5 = Utils.toMD5(str);
		if (hash_instances.isEmpty() || !hash_instances.contains(md5)) {
			mc = new MConfig(str);
			hash_instances.add(md5);
			instances.add(mc);
		} else {
			mc = instances.get(hash_instances.indexOf(md5));
		}
		return mc;
	}
	/**
	 * Loading the configuration file values 
	 * 
	 * NOTE: Unnecessary to call this
	 * function directly because it is already called during class
	 * initialization (the default path is "properties")
	 * 
	 * @throws Exception
	 */
	public synchronized void loadConfig() throws Exception {
		search_properties_file();
		if (file == null) {
			throw new Exception("Couldn't find properties file ("+config_filename+")");
		} else {//Load properties
			prop_extension ext = prop_extension.undefined;
	    	int i = file.lastIndexOf('.');
	    	if (i > 0){
	    		try {
	    			ext = prop_extension.valueOf(file.substring(++i).toLowerCase());
	    		} catch (Exception e){
	    			ext = prop_extension.undefined;
	    		}
	    	}
	    	boolean ok = false;
			switch(ext){
			case properties:
				ok = loadJavaProperties();
				break;
			case json:
				ok = loadJSONProperties();
				break;
			case xml:
				ok = loadXMLProperties();
				break;
			default:
				ok = loadXMLProperties() || loadJSONProperties() || loadJavaProperties();
			}
			if (!ok)
				throw new Exception("Properties file ("+config_filename+") couldn't be loaded.");
			
/* DEBUG 	props.list(System.out);*/
			
		}
	}

	/**
	 * Loads a properties represented by the JSON document from the input stream
	 * @return true on success
	 */
	private boolean loadJSONProperties() {
		boolean ret = false;
		FileInputStream stream = null;
		JSONObject json = null;
		try {
			System.out.println("Try to read a file as JSON properties.");
			stream = new FileInputStream(new File(file));
			json = new JSONObject(new JSONTokener(stream));
			if (json != null) {// Yes!!! It is JSON file.
				convertJSONtoProperties(json, props, null);
				ret = true;
			}
		} catch (Exception ex) {
			ret = false;
		} finally {
			try {
				stream.close();
			} catch (Exception ex) { }
		}
		return ret;		
	}

	/**
	 * Reads a properties list (key and element pairs) from the input stream.
	 * @return true on success
	 */
	private boolean loadJavaProperties() {
		boolean ret = true;
		FileInputStream stream = null;
		try {
			System.out.println("Try to read a file as Java properties.");
			stream = new FileInputStream(new File(file));
			props.load(stream);
		} catch (Exception e) {
			ret = false;
		} finally {
			try {
				stream.close();
			} catch (Exception ex) { }
		}
		return ret;		
	}

	/**
	 * Loads a properties represented by the XML document from the input stream
	 * @return true on success
	 */
	private boolean loadXMLProperties(){
		boolean ret = true;
		FileInputStream stream = null;
		try {
			System.out.println("Try to read a file as XML properties.");
			stream = new FileInputStream(new File(file));
			props.loadFromXML(stream);
		} catch (Exception e) {
			ret = false;
		} finally {
			try {
				stream.close();
			} catch (Exception ex) { }
		}
		return ret;		
	}
    /**
     * Searching of properties file beginning from 'start_dir' and continue up to specified 'end_dir'. 
     * At the return the properties object will be loaded if success (otherwise it will be untouched)
     * @throws IOException 
     */
	private void search_properties_file() throws Exception {
		final String end_dir = "/";	  	//root folder as the end directory for searching
		final String start_dir = "."; 	//current folder as the start directory for searching
		String cur_path = new File(start_dir).getCanonicalPath();	// current folder
		File prop_file;
		do {
			prop_file = new File(cur_path, config_filename);
			if (prop_file.canRead()) {
				file = prop_file.getCanonicalPath();
				System.out.println("Found configuration file: " + file);
				break;
			} else {
/* DEBUG 		System.out.println("NOT found " + prop_file.getCanonicalPath()); */
				cur_path = new File(cur_path).getParent();
			}
		} while (!cur_path.equalsIgnoreCase(end_dir));
	}    
        
	/**
	 * Converting of JSON properties to the Java Properties class Note: if there
	 * is hierarchy of keys they will be concatenated like depicted below so the
	 * properties will contain complex key
	 * 
	 * <pre>
	 * 	{a:{b:{c:121}}} -> a.b.c=121
	 * </pre>
	 * 
	 * @param json
	 *            source JSONObject (representing properties)
	 * @param prop
	 *            the empty Properties class that will receive properties
	 * @param key
	 *            the current value of key (working parameter used for internal
	 *            purpose). Usually should be null when calling. Sometimes it
	 *            can be used if you want to add some prefix to the all names of
	 *            properties.
	 * @throws Exception
	 */
    private void convertJSONtoProperties (JSONObject json, Properties prop, String key) throws Exception{
    	String skey, ckey;
    	if (json != null){
        	Iterator<String> i = json.keys();
        	while (i.hasNext()) {
        	    skey = i.next();
        	    if (key == null || key.length() <= 0){
        	    	ckey = skey;
        	    } else {
        	    	ckey = key+"."+skey;
        	    }
        	    Object value = json.get(skey);
/*DEBUG        	    System.out.println("sKey: " + skey+"\tsValue: " + value+" (JSONObject? "+(value instanceof JSONObject)+")");*/
        	    if (value instanceof JSONObject){
        	    	convertJSONtoProperties((JSONObject) value, prop, ckey);
        	    } else {
        	      	prop.put(ckey, value.toString());
/*DEBUG        	    	System.out.println("****** "+ ckey+": " + value);*/
        	    }
        	}
    	}
    }

    public synchronized String getConfigForPrinting() {
        String msg;
        StringBuilder buf = new StringBuilder();
        buf.append("********** Config ************").append("\n");
        Enumeration<?> keys = props.propertyNames();
        while (keys.hasMoreElements()) {
            msg = (String) keys.nextElement();
            buf.append(msg).append("=").append(props.getProperty(msg)).append("\n");
        }
        buf.append("********************").append("\n");
        return buf.toString();
    }

    public String getConfigStringValue(String key) {
        return props.getProperty(key);
    }

    public int getConfigIntValue(String key) {
        return Integer.parseInt(props.getProperty(key, "0"));
    }

    public String getConfigStringValue(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public int getConfigIntValue(String key, int defaultValue) {
        return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
    }

    public int getConfigIntValue(String key, int minValue, int maxValue) {
        int value = getConfigIntValue(key);
        if (value < minValue) {
            return minValue;
        }
        if (value > maxValue) {
            return maxValue;
        }
        return value;
    }

    public boolean getConfigBooleanValue(String key) {
        try {
            //First check on numeric value
            int val = getConfigIntValue(key);
            if (val != 0) {
                return true;
            }
        } catch (NumberFormatException ex) {
            //Now check on string value
            String str = props.getProperty(key);
            if (str != null
                    && (str.compareToIgnoreCase("true") == 0
                    || str.compareToIgnoreCase("yes") == 0
                    || str.compareToIgnoreCase("on") == 0)) {
                return true;
            }
        }
        return false;
    }

    public int getPropertiesCount(){
    	return props.size();
    }
    
	/**
	 * @return the absolute path to the properties file
	 */
	public String getPropFile() {
		return file;
	}
	
	/**
	 * @return loaded Properties class
	 */
	public Properties getProp(){
		return this.props;
	}
}
