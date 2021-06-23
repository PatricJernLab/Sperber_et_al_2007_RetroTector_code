/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 22/9 -06
* Beautified 22/9 -06
*/

package retrotector;

import java.util.*;

/**
* Superclass of classes using parameter files.
*/
public class ParameterUser {

/**
*	For parameters from script or parameter window.
*/
	protected Hashtable parameters;

/**
* Fetches an object from parameters.
* @param	key	The key string to search for.
* @param	def	Object to return if key not found.
* @return The object with this key, or the default.
*/
	public final Object getObject(String key, Object def) {
		if (parameters.containsKey(key)) {
			return parameters.get(key);
		}
		return def;
	} // end of getObject(String, Object)
	
/**
* Fetches a String from parameters.
* @param	key	The key string to search for.
* @param	def	String to return if key not found.
* @return The String with this key, or the default.
*/
	public final String getString(String key, String def) {
		Object o = getObject(key, def);
		if (o instanceof String) {
			return (String) o;
		}
		return def;
	} // end of getString(String, String)
		
/**
* Fetches a String array from parameters.
* @param	key	The key string to search for.
* @return The String array with this key, or null.
*/
	public final String[ ] getStringArray(String key) {
		Object o = getObject(key, null);
		if (o == null) {
			return null;
		} else if (o instanceof String[ ]) {
			return (String[ ]) o;
		} else {
			return null;
		}
	} // end of getStringArray(String)
	
/**
* Fetches an integer from parameters.
* @param	key	The key string to search for.
* @param	def	Value to return if key not found or not key to integer.
* @return The int with this key, or the default.
*/
	public final int getInt(String key, int def) {
		String s = getString(key, "");
		s = s.trim();
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException n) {
			return def;
		}
	} // end of getInt(String, int)
	
/**
* Fetches a real number from parameters.
* @param	key	The key string to search for.
* @param	def	Value to return if key not found or not key to float.
* @return The float with this key, or the default.
*/
	public final float getFloat(String key, float def) {
		String s = getString(key, "");
		s = s.trim();
		try {
			return Float.valueOf(s).floatValue();
		} catch (NumberFormatException n) {
			return def;
		}
	} // end of getFloat(String, float)
	
/**
* Fetches a long integer from parameters.
* @param	key	The key string to search for.
* @param	def	Value to return if key not found or not key to integer.
* @return The longint with this key, or the default.
*/
	public final long getLong(String key, long def) {
		String s = getString(key, "");
		s = s.trim();
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException n) {
			return def;
		}
	} // end of getLong(String, long)
	
/**
* Fetches parameter defaults from configuration file in multiparameter with key
* |Class name|defaults
* @return	True if defaults were found and valid.
*/
	public final boolean getDefaults() {
		String defaultkey = className() + "defaults";
		String[ ] lines = (String[ ]) RetroTectorEngine.configurationTable.get(defaultkey);
		if (lines == null) return false;
		String line;
		String key;
		int index;
		for (int l=0; l<lines.length; l++) {
			line = lines[l];
      if (line.trim().length() > 0) {
        index = line.indexOf(ParameterFileReader.SINGLEPARAMTERMINATOR);
        if (index < 0) {
          return false;
        }
        key = line.substring(0, index).trim();
        parameters.put(key, line.substring(index + 1).trim());
      }
		}
		return true;
	} // end of getDefaults()
		
/**
* @return The name of the class of this ParameterUser.
*/
	public final String className() {
		return Utilities.className(this);
	} // end of className()

/**
* @return	The Hashtable containing the parameters.
*/
	public final Hashtable getParameterTable() {
		return parameters;
	} // end of getParameterTable()
	
} // end of ParameterUser
