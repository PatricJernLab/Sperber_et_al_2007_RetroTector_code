/*
* Copyright (©) 2000-2005, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 6/10 -06
* Beautified 6/10 -06
*/
package builtins;

import retrotector.*;

import java.io.*;
import java.util.*;
import javax.swing.*;


/**
* Executor which takes all files with names ending with SWEEPABLESCRIPTFILESUFFIX
* within the current directory tree and (if they are scripts)
* executes them and changes their names, removing that _.
*/
public class SweepScripts extends Executor {

/**
* Controlled by Stop SweepScripts menu item.
*/
	public static JMenuItem stopSweepScriptsMenuItem;


	private String currentName = "";
	
/**
* Constructor. No parameters to specify.
*/
	public SweepScripts() {
	} // end of constructor()

/**
* Dummy. Nothing to to.
*/
	public void initialize(ParameterFileReader script) {
		runFlag = true;
	} // end of initialize(ParameterFileReader)
	
/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 10 06";
  } // end of version
	
	private Stack triedFiles = new Stack();
	
// have we already been at  this one, but not changed its name?
	private final boolean isTried(File fi) {
		for (int i=0; i<triedFiles.size(); i++) {
			if (fi.equals(triedFiles.elementAt(i))) {
				return true;
			}
		}
		return false;
	} // end of isTried(File)
  
// sweeps one directory. For recursive use. Returns true if a script was executed
	private final boolean sweepDirectory (File theDir) throws RetroTectorException {

		RetroTectorEngine.setCurrentDirectory(theDir, "swps");
		ExecutorScriptReader theReader = null;
		boolean used = false;
		boolean executed;

// get all file names and look through them
		String[ ] names = theDir.list();
		try {
			Arrays.sort(names);
		} catch (NullPointerException npe) {
			RetroTectorEngine.displayError(new RetroTectorException("SweepScripts", "NullPointerException in " + theDir.getPath()), RetroTectorEngine.ERRORLEVEL);
			return false;
		}
		for (int n=0; n<names.length; n++) {
			showProgress();
			File f = new File(RetroTectorEngine.currentDirectory(), names[n]);
			currentName = f.getPath();
// is it a sweepable script?
			if (!isTried(f) & (names[n].endsWith(Utilities.SWEEPABLESCRIPTFILESUFFIX))) {
				if (abortSweepScripts) {
					abortSweepScripts = false;
					stopSweepScriptsMenuItem.setEnabled(false);
					haltError("SweepScripts aborted");
				}
				executed = false;
				triedFiles.push(f);
				RetroTectorEngine.setScriptField(f, "swps");
				try {
					theReader = new ExecutorScriptReader(f);
					RetroTectorEngine.setExecutorField("SweepScripts>" + theReader.EXECUTORNAME + " executing", "swps");
          RetroTectorEngine.toLogFile("----SweepScripts executes " + f.getPath() + "----");
					theReader.doExecute(false);
					try {
						theReader.close();
					} catch (Exception e) {
					}
					executed = true;
				} catch (RetroTectorException rse) {
					try {
						theReader.close();
					} catch (Exception e) {
					}
          if (!ORFIDSCRIPTSLEFTMESSAGE.equals(rse.messagePart(1))) {
						RetroTectorEngine.displayError(rse, RetroTectorEngine.ERRORLEVEL);
					}
				} catch (RuntimeException e) {
					String sw = "SweepScripts";
					if (theReader != null) {
						sw = sw + ">" + theReader.EXECUTORNAME;
					}
					RetroTectorEngine.displayError(
						new RetroTectorException(sw, "A Java RuntimeException occurred",
							"There should be more information in the text window")
					);
					RetroTectorEngine.toLogFile(e.toString());
					throw e;
				}
				if (executed) { // script executed OK. Change its name
					triedFiles.pop();
					String newname = names[n].substring(0, names[n].length() - Utilities.SWEEPABLESCRIPTFILESUFFIX.length()) + Utilities.SWEPTSCRIPTFILESUFFIX;
					File ff = new File(RetroTectorEngine.currentDirectory(), newname);
					if (ff.exists()) {
						haltError("Cannot rename " + names[n], newname + " already exists");
					} else {
						f.renameTo(ff);
					}
					used = true;
				}
			} else {
				if (f.isDirectory()) { // is it a directory?
					if (sweepDirectory(f)) { // yes, sweep it
						used = true;
					}
					RetroTectorEngine.setCurrentDirectory(theDir, "swps"); // reset current directory
				}
			}
		}
		return used;
	} // end of sweepDirectory(File)
				
/**
* Executes as specified above.
*/
	public final String execute() throws RetroTectorException {
		abortSweepScripts = false;
		if (stopSweepScriptsMenuItem != null) {
			stopSweepScriptsMenuItem.setEnabled(true);
		}
		File currDir = RetroTectorEngine.currentDirectory();
		while (sweepDirectory(currDir)) {
		}
		if (triedFiles.size() > 0) {
			Utilities.outputString("The following scripts were tried unsuccessfully:");
			for (int i=0; i<triedFiles.size(); i++) {
				Utilities.outputString(((File) triedFiles.elementAt(i)).getPath());
			}
		}
		RetroTectorEngine.toLogFile("----SweepScripts finished");
		if (stopSweepScriptsMenuItem != null) {
			stopSweepScriptsMenuItem.setEnabled(false);
		}
		return "";
	} // end of execute()

} // end of SweepScripts
