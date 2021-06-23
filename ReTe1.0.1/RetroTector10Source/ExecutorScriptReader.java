/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 21/9 -06
* Beautified 21/9 -06
*/

package retrotector;

import java.io.*;
import java.util.*;

/**
* For reading Executor script files, with the structure specified
* in ExecutorScriptWriter.
*/
public class ExecutorScriptReader extends ParameterFileReader {

/**
* The name of the Executor specified in the script.
*/
	public final String EXECUTORNAME;
		
	private static Hashtable tempTable = new Hashtable();
	
/**
* Open a File for use as a script, checking its validity.
* @param	f	The file to use.
*/
	public ExecutorScriptReader(File f) throws RetroTectorException {
		super(f, tempTable);
		String s;
		s = readOneParameter();
		if ((s == null) || (!s.equals(EXECUTORKEY))) {
			tempTable = new Hashtable();
			RetroTectorException.sendError(this, FILEPATH, "is not an Executor script");
		}
		EXECUTORNAME = (String) tempTable.get(EXECUTORKEY);
		tempTable = new Hashtable();
	} // end of constructor(File)
	
/**
* Execute as prescribed by the script.
* @param	newThread	If true, execute in a new Thread.
*/
	public final void doExecute(boolean newThread) throws RetroTectorException {
		if (EXECUTORNAME == null) {
			close();
			RetroTectorException.sendError(this, FILEPATH, "is no ExecutorScript");
		}
		
		Executor target = Executor.getExecutor(EXECUTORNAME);
		
		if (newThread) {
			RetroTectorEngine.currentThread = new ExecutorThread(target, this);
			RetroTectorEngine.currentThread.start();
		} else {
			target.initialize(this);
			if (target.runFlag) {
				target.execute();
			}
		}
	} // end of doExecute(boolean)
		
} // end of ExecutorScriptReader
