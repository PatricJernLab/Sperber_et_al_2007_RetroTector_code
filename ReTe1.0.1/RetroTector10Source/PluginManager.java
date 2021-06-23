/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 18/9 -06
* Beautified 18/9 -06
*/

package retrotector;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

/**
*	Does householding for 'builtins' and 'plugins' packages.
*/
abstract class PluginManager {

/**
* Name of directory with plugin classes = "plugins".
*/
	public static final String PLUGINSDIRNAME = "plugins";

/**
* Name of package with plugin classes = "plugins".
*/
	public static final String PLUGINSPACKAGENAME = "plugins";

/**
* Name of package with builtin classes = "builtins".
*/
	public static final String BUILTINSPACKAGENAME = "builtins";

/**
* Array of all loaded classes from builtins and plugins.
*/
  public static final Class[ ] BUILTANDPLUGINS;
  
/**
* Hashtable with all loaded classes from builtins and plugins, with their names as keys.
*/
  public static final Hashtable BUILTANDPLUGNAMES = new Hashtable();
  
  static {
    Stack ps = new Stack();
    Class c;
    String na;
    String s;
    File dir = new File(PLUGINSDIRNAME);
    if (dir.exists()) { // collect files from plugins directory
      String[ ] fileList = dir.list();
      for (int i=0; i<fileList.length; i++) {
        int ind = fileList[i].indexOf(".class");
        if ((ind > 0) & (fileList[i].indexOf('$') < 0)) {
          s = fileList[i].substring(0, ind).trim();
          c = null;
          na = PLUGINSPACKAGENAME + "." + s;
          try {
            c = Class.forName(na);
            ps.push(c);
            BUILTANDPLUGNAMES.put(s, c);
          } catch (NoClassDefFoundError er) {
						if (RetroTectorEngine.retrotector == null) {
							System.err.println("! ! ! Could not find class " + na);
						} else {
							RetroTectorEngine.displayError(new RetroTectorException("RetroTector", "Could not find class", na));
						}
          } catch (ClassNotFoundException e) {
						if (RetroTectorEngine.retrotector == null) {
							System.err.println("! ! ! Could not find class " + na);
						} else {
							RetroTectorEngine.displayError(new RetroTectorException("RetroTector", "Could not find class", na));
						}
          }
        }
      }
    }

		JarFile rtjar = null;
		try {
			rtjar = new JarFile(RetroTectorEngine.JARFILENAME);
		} catch (IOException ioe) {
			if (RetroTectorEngine.retrotector == null) {
				System.err.println("! ! ! Could not open " + RetroTectorEngine.JARFILENAME);
			} else {
				RetroTectorEngine.displayError(new RetroTectorException("RetroTector", "Could not open", RetroTectorEngine.JARFILENAME));
			}
		}
		Enumeration classes = rtjar.entries();
		ZipEntry ze;
		int ind;
		while (classes.hasMoreElements()) { // go through all classes
			ze = (ZipEntry) classes.nextElement();
			s = ze.getName();
			if (s.startsWith(BUILTINSPACKAGENAME)) {
				ind = s.indexOf('$');
				if (ind < 0) { // not inner class
					ind = s.indexOf(".class");
					if (ind > 0) {
						s = s.substring(BUILTINSPACKAGENAME.length() + 1, ind);
						if (BUILTANDPLUGNAMES.get(s) == null) { // plugin has precedence
							c = null;
							na = BUILTINSPACKAGENAME + "." + s;
							try {
								c = Class.forName(na);
								ps.push(c);
								BUILTANDPLUGNAMES.put(s, c);
							} catch (ClassNotFoundException e) {
								if (RetroTectorEngine.retrotector == null) {
									System.err.println("! ! ! Could not find class" + na);
								} else {
									RetroTectorEngine.displayError(new RetroTectorException("RetroTector", "Could not find class ", na));
								}
							}
						}
					}
				}
      }
    }
    BUILTANDPLUGINS = new Class[ps.size()];
    ps.copyInto(BUILTANDPLUGINS);
  } // end of static initializer

  
}