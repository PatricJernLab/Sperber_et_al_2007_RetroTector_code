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

/**
* For making an executor script, ie a parameter file beginning with
* a Singleparameter
* 'Executor: (executor name)'.
*/
public class ExecutorScriptWriter extends ParameterFileWriter {

/**
* Constructor.
* @param	f							Specification of the file.
* @param	executorName	The name to appear in the first line.
*/
	public ExecutorScriptWriter(File f, String executorName) throws RetroTectorException {
		super(f);
		writeSingleParameter(EXECUTORKEY, executorName, false);
	} // end of constructor(File, String)
	
} // end of ExecutorScriptWriter