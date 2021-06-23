/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 30/9 -06
* Beautified 30/9 -06
*/
package builtins;

import retrotector.*;

import java.io.*;
import java.util.*;


/**
* Executor which finds all .txt files within the current directory tree
*	which contain a specified string. Output goes to standard output.
*<PRE>
*     Parameters:
*
*   StringToFind
* The string to find.
* Default: "".
*
*</PRE>
*/
public class FindStringInFiles extends Executor implements Utilities.FileTreater {

/**
* Key for string file in name that is to be changed = "Change".
*/
	public static final String STRINGTOFINDKEY = "StringToFind";

	private String toFind;
		
/**
* Standard Executor constructor.
*/
	public FindStringInFiles() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(STRINGTOFINDKEY, "");
		explanations.put(STRINGTOFINDKEY, "The string to find");
		orderedkeys.push(STRINGTOFINDKEY);
	} // end of constructor()

/**
* Information about version date, to be displayed in parameter window.
*/
  public final String version() {
    return "2006 09 30";
  } //end of version
	  
/**
* Outputs path of f if it contains toFind.
* @param		f	File to search in.
* @return	f.
*/
	public final File treatFile(File f) throws RetroTectorException {
		if (f == null) {
			return null;
		}
		if (!f.getName().endsWith(FileNamer.TXTTERMINATOR)) {
			return f;
		}
		showProgress();
		String line;
		String path = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			while ((line = br.readLine()) != null) {
				if (line.indexOf(toFind) >= 0) {
					path = f.getPath();
				}
			}
			br.close();
		} catch (IOException ioe) {
			haltError("Trouble reading " + f.getPath());
		}
		if (path != null) {
			Utilities.outputString(path);
		}
		return f;
	} // end of treatFile(File)
				
/**
* Executes as specified above.
*/
	public final String execute() throws RetroTectorException {
		toFind = getString(STRINGTOFINDKEY, "");
		if (toFind.length() <= 0) {
			haltError("No search string defined");
		}
		Utilities.treatFilesIn(RetroTectorEngine.currentDirectory(), this);
		return "";
	} // end of execute()

} // end of FindStringInFiles
