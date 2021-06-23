/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 1/10 -06
* Beautified 1/10 -06
*/
package builtins;

import retrotector.*;

import java.io.*;
import java.util.*;


/**
* Executor which renames or deletes files within the current directory tree
*	according to specifications.
*<PRE>
*     Parameters:
*
*   Change
* The string to change.
* Default: "Script.txt".
*
*   Into
* The string to change the above into.
* Default: "Script_.txt".
*
*   ChangeIfPresent
* File name is changed only if it includes this string (unless empty).
* Default: "????".
*
*   DeleteIfPresent
* File is deleted only if name includes this string.
* Default: "".
*
*</PRE>
*/
public class RenameFiles extends Executor implements Utilities.FileTreater {

/**
* Key for string file in name that is to be changed = "Change".
*/
	public static final String CHANGEKEY = "Change";

/**
* Key for string to change the above into = "Into".
*/
	public static final String INTOKEY = "Into";

/**
* Key for string that must be present in name for file to be processed = "ChangeIfPresent".
*/
	public static final String CHANGEIFPRESENTKEY = "ChangeIfPresent";

/**
* Key for string that must be present in name for file to be deleted = "DeleteIfPresent".
*/
	public static final String DELETEIFPRESENTKEY = "DeleteIfPresent";

	private String change;
	private String into;
	private String changeIfPresent;
	private String deleteIfPresent;
	
	private int totalChanged = 0;
	private int totalDeleted = 0;
		
/**
* Standard Executor constructor.
*/
	public RenameFiles() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(CHANGEKEY, Utilities.SWEPTSCRIPTFILESUFFIX);
		explanations.put(CHANGEKEY, "The string to change");
		orderedkeys.push(CHANGEKEY);
		parameters.put(INTOKEY, Utilities.SWEEPABLESCRIPTFILESUFFIX);
		explanations.put(INTOKEY, "The string to change the above into");
		orderedkeys.push(INTOKEY);
		parameters.put(CHANGEIFPRESENTKEY, "????");
		explanations.put(CHANGEIFPRESENTKEY, "File name is changed only if it includes this string (unless empty)");
		orderedkeys.push(CHANGEIFPRESENTKEY);
		parameters.put(DELETEIFPRESENTKEY, "");
		explanations.put(DELETEIFPRESENTKEY, "File is deleted only if name includes this string");
		orderedkeys.push(DELETEIFPRESENTKEY);
	} // end of constructor

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 01 10";
  } //end of version
	  
/**
* As required by Utilities.FileTreater.
* @param		f	A File.
* @return	f if successful, null otherwise.
*/
public final File treatFile(File f) throws RetroTectorException {

		String s = f.getName();
		String newname;
		int ind1;
		if ((deleteIfPresent.length() > 0) && (s.indexOf(deleteIfPresent) >= 0)) {
			if (!f.delete()) {
				RetroTectorEngine.displayError(new RetroTectorException("RenameFiles", f.getPath(), "was not deleted"), 1);
				totalDeleted--;
			}
			totalDeleted++;
			return null;
		} else if (change.length() > 0) {
			if ((s.indexOf(change) >= 0) & ((changeIfPresent.length() == 0) || (s.indexOf(changeIfPresent) >= 0))) {
				ind1 = s.indexOf(change);
				if (s.endsWith(change)) {
					newname = s.substring(0, ind1) + into;
				} else if (s.startsWith(change)) {
					newname = into + s.substring(change.length());
				} else {
					newname = s.substring(0, ind1) + into + s.substring(ind1 + change.length());
				}
				File ff = new File(f.getParent(), newname);
				if (!f.renameTo(ff)) {
					RetroTectorEngine.displayError(new RetroTectorException("RenameFiles", f.getPath(), "was not changed"), 1);
					totalChanged--;
				}
				totalChanged++;
				return ff;
			}
		}
		return f;
	} // end of treatFile(File)
				
/**
* Executes as specified above.
*/
	public final String execute() throws RetroTectorException {
		change = getString(CHANGEKEY, Utilities.SWEPTSCRIPTFILESUFFIX);
		into = getString(INTOKEY, Utilities.SWEEPABLESCRIPTFILESUFFIX);
		changeIfPresent = getString(CHANGEIFPRESENTKEY, "");
		deleteIfPresent = getString(DELETEIFPRESENTKEY, "");
		if (deleteIfPresent.length() > 0) {
			if (!RetroTectorEngine.doQuestion("Are you sure that you want to delete all files in\n" + RetroTectorEngine.currentDirectory().getName() + "\nwhose names contain\n" + deleteIfPresent)) {
				return "";
			}
		}
		Utilities.treatFilesIn(RetroTectorEngine.currentDirectory(), this);
		Utilities.outputString("Renamed " + totalChanged);
		Utilities.outputString("Deleted " + totalDeleted);
		return "";
	} // end of execute()

} // end of RenameFiles
