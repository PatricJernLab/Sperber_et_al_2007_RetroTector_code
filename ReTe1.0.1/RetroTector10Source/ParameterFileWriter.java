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
import java.io.*;

/**
* For writing parameter files with the structure specified in ParameterStream.
*/
public class ParameterFileWriter extends ParameterStream {

/**
* The name of the file written to.
*/
	final String FILENAME;

	private final BufferedWriter THEWRITER;
	private String belongsIn = null; // parent directory
	
/**
* Sets up to use a file as a ParameterFileWriter.
* @param	f	The File in question.
*/
	public ParameterFileWriter(File f) throws RetroTectorException {
		FILENAME = f.getName();
		FileWriter fw;
		BufferedWriter bw = null;
		try {
			belongsIn = f.getParent();
			fw = new FileWriter(f);
			bw = new BufferedWriter(fw);
		} catch (IOException ioe) {
			RetroTectorException.sendError(this, "There was trouble opening ", FILENAME);
		}
		THEWRITER = bw;
		int ind = belongsIn.lastIndexOf("/");
		if (ind >=0) {
			belongsIn = belongsIn.substring(ind + 1);
		}
	} // end of constructor(File)
	
/**
* Output an unformatted line.
* @param	line	The line to output.
*/
	public final void writeLine(String line) throws RetroTectorException {
		try {
			THEWRITER.write(line);
			THEWRITER.newLine();
		} catch (IOException ioe) {
			RetroTectorException.sendError(this, "Could not write line", line);
		}
	} // end of writeLine(String)
	
/**
* Output a comment.
* @param	comment	The comment to output.
*/
	public final void writeComment(String comment) throws RetroTectorException {
		try {
			THEWRITER.write(COMMENTMARKER + " " + comment);
			THEWRITER.newLine();
		} catch (IOException ioe) {
			RetroTectorException.sendError(this, "Could not write comment", comment);
		}
	} // end of writeComment(String)
	
/**
* Output a Singleparameter, or a comment describing one.
* @param	key				The key of the parameter.
* @param	value			The value of the parameter.
* @param	asComment	If true, make a comment line.
*/
	public final void writeSingleParameter(String key, String value, boolean asComment) throws RetroTectorException {
		try {
			if (asComment) {
				THEWRITER.write(COMMENTMARKER + " ");
			}
			THEWRITER.write(key + SINGLEPARAMTERMINATOR + " " + value);
			THEWRITER.newLine();
		} catch (IOException ioe) {
			RetroTectorException.sendError(this, "Could not write single parameter", key, value);
		}
	} // end of writeSingleParameter(String, String, boolean)
	
/**
* Output first line of a Multiparameter, or a comment describing one.
* @param	key				The key of the parameter.
* @param	asComment	If true, make a comment line.
*/
	public final void startMultiParameter(String key, boolean asComment) throws RetroTectorException {
		try {
			if (asComment) {
				THEWRITER.write(COMMENTMARKER + " ");
			}
			THEWRITER.write(key + MULTIPARAMTERMINATOR);
			THEWRITER.newLine();
		} catch (IOException ioe) {
			RetroTectorException.sendError(this, "Could not start multi parameter", key);
		}
	} // end of startMultiParameter(String, boolean)

/**
* Append one line to a Multiparameter, or a comment describing one.
* @param	line			The line to output.
* @param	asComment	If true, make a comment line.
*/
	public final void appendToMultiParameter(String line, boolean asComment) throws RetroTectorException {
		try {
			if (asComment) {
				THEWRITER.write(COMMENTMARKER + " ");
			}
			THEWRITER.write(line);
			THEWRITER.newLine();
		} catch (IOException ioe) {
			RetroTectorException.sendError(this, "Could not append to multi parameter", line);
		}
	} // end of appendToMultiParameter(String, boolean)
	
/**
* Append lines to a Multiparameter, or comments describing them.
* @param	lines			The lines to output.
* @param	asComment	If true, make a comment line.
*/
	public final void appendToMultiParameter(String[ ] lines, boolean asComment) throws RetroTectorException {
		for (int l=0; l<lines.length; l++) {
			appendToMultiParameter(lines[l], asComment);
		}
	} // end of appendToMultiParameter(String[ ], boolean)
	
/**
* Output last line of a Multiparameter, or a comment describing one.
* @param	asComment	If true, make a comment line.
*/
	public final void finishMultiParameter(boolean asComment) throws RetroTectorException {
		try {
	 		if (asComment) {
				THEWRITER.write(COMMENTMARKER + " ");
			}
			THEWRITER.write(MULTIPARAMTERMINATOR);
			THEWRITER.newLine();
		} catch (IOException ioe) {
			RetroTectorException.sendError(this, "Could not finish multi parameter");
		}
	} // end of finishMultiParameter(boolean)

/**
* Output a complete Multiparameter, or comments describing one.
* @param	key				The key of the parameter.
* @param	contents	The body of the parameter.
* @param	asComment	If true, make a comment line.
*/
	public final void writeMultiParameter(String key, String[ ] contents, boolean asComment) throws RetroTectorException {
		startMultiParameter(key, asComment);
		if (contents != null) {
			for (int s=0; s<contents.length; s++) {
				appendToMultiParameter(contents[s], asComment);
			}
		}
		finishMultiParameter(asComment);
	} // end of writeMultiParameter(String, String[ ], boolean)
	
/**
* Output a complete Multiparameter. Mainly useful if contents contains \n.
* @param	key				The key of the parameter.
* @param	contents	The body of the parameter.
*/
	public final void writeMultiParameter(String key, String contents) throws RetroTectorException {
		startMultiParameter(key, false);
		if (contents != null) {
			appendToMultiParameter(contents, false);
		}
		finishMultiParameter(false);
	} // end of writeMultiParameter(String, String)
	
/**
* Close this thing, adding directory and time information
*/
	public final void close() throws RetroTectorException {
		writeComment("Belongs in " + belongsIn);
		Date da = new Date();
		writeComment("Created " + da.toString() + " under " + RetroTectorEngine.VERSIONSTRING);
		long dal = Database.getDatabase(Executor.ORDINARYDATABASE).DATABASESOURCE.lastModified();
		da = new Date(dal);
		writeComment("using Database:Ordinary last modified " + da.toString());
		try {
			THEWRITER.flush();
			THEWRITER.close();
		} catch (IOException ioe) {
			RetroTectorException.sendError(this, "Could not close parameter file", FILENAME);
		}
	} // end of close()
	
/**
* Output all Singleparameters in a particular Hashtable, in comment form.
* @param	theTable	The Hashtable in question.
*/
	public final void listSingleParameters(Hashtable theTable) throws RetroTectorException {
		Enumeration e = theTable.keys();
		while (e.hasMoreElements()) {
			Object o = e.nextElement();
			Object v = theTable.get(o);
			if (v instanceof String) {
				writeSingleParameter((String) o, (String) v, true);
			}
		}
	} // end of listSingleParameters(Hashtable)
	
} // end of ParameterFileWriter
		