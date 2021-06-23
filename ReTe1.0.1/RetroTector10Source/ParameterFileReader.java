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

import java.io.*;
import java.util.*;

/**
* For reading parameter files or parameter arrays,
* with the structure specified in ParameterStream.
*/
public class ParameterFileReader extends ParameterStream {

/**
* = "String array".
*/
	public static final String STRINGARRAY = "String array";

/**
* = "ChainP".
*/
	public static final String CHAINP = "ChainP";
	
/**
* = "ChainS".
*/
	public static final String CHAINS = "ChainS";
	
/**
* Path of the underlying file, or "String array".
*/
	public final String FILEPATH;
	
	private final String[ ] INSTRINGS; // null if reading from file
	private final BufferedReader THEREADER; // null if reading from array

	private int stringNr; // line counter in INSTRINGS
	private Hashtable theTable; // to read parameters into
	private int maxchainnumber = -1; // The highest n encountered in a key of form Chainn, ChainPn or ChainSn.
	private Stack keyStack = new Stack(); // for keys
	
/**
* Sets up to read parameters from a File into a Hashtable.
* @param	f	The File in question.
* @param	h	The Hashtable in question.
*/
	public ParameterFileReader(File f, Hashtable h) throws RetroTectorException {
		theTable = h;
		FILEPATH = f.getPath();
		BufferedReader tr = null;
		try {
			if (f instanceof ZFile) {
				ZFile zf = (ZFile) f;
				tr = new BufferedReader(new InputStreamReader(zf.ZIPFILE.getInputStream(zf.ZIPENTRY)));
			} else {
				tr = new BufferedReader(new FileReader(f));
			}
		} catch (IOException ioe) {
			RetroTectorException.sendError(this, "There was trouble opening ", FILEPATH);
		}
		THEREADER = tr;
		INSTRINGS = null;
	} // end of constructor(File, Hashtable)

/**
* Sets up to read parameters from a String array into a Hashtable.
* @param	strings	The String array in question.
* @param	h				The Hashtable in question.
*/
	public ParameterFileReader(String[ ] strings, Hashtable h) {
		theTable = h;
		FILEPATH = STRINGARRAY;
		INSTRINGS = strings;
		stringNr = 0;
		THEREADER = null;
	} // end of constructor(String[ ], Hashtable)
	
/**
* Dummy constructor. Never reads any input.
*/
	ParameterFileReader(Hashtable h) throws RetroTectorException {
		theTable = h;
		FILEPATH = "Dummy";
		INSTRINGS = null;
		THEREADER = null;		
	} // end of constructor (Hashtable)

	
/**
*	Reads singleparameters only into Hashtable.
*	@return	True if file is syntactically correct.
*/
	public final boolean getSingleParameters() throws RetroTectorException {
		boolean readingmulti = false;
		String line;
		int ind;
		while ((line = nextLine()) != null) {
			line = line.trim();
			if (line.length() > 0) {
				if (readingmulti) {
					if (line.equals(MULTIPARAMTERMINATOR)) {
						readingmulti = false;
					}
				} else if (!line.startsWith(COMMENTMARKER)) {
					if (line.endsWith(MULTIPARAMTERMINATOR)) {
						readingmulti = true;
					} else if ((ind = line.indexOf(SINGLEPARAMTERMINATOR)) > 0) {
						theTable.put(line.substring(0, ind).trim(), line.substring(ind + 1).trim());
					} else {
						return false;
					}
				}
			}
		}
		return !readingmulti;
	} // end of getSingleParameters()
			
/**
* Reads the next available parameter, possibly updating maxchainnumber.
* @return Its key, or null.
*/
	public final String readOneParameter() throws RetroTectorException {
		String line = nextLine();
// Read off comment lines and empty lines
		while ((line != null) && ((line.trim().length() == 0) || (line.trim().startsWith(COMMENTMARKER)))) {
			line = nextLine();
		}
		if (line == null) { // end of file
			return null;
		}

		String key;
		Stack multi;
		String[ ] multia;

		line = line.trim();
		if (line.endsWith(MULTIPARAMTERMINATOR)) {
			key = line.substring(0, line.length() - MULTIPARAMTERMINATOR.length()).trim();
			multi = new Stack();
			line = nextLine();
			while ((line != null) && (!line.equals(MULTIPARAMTERMINATOR))) {
				multi.push(line);
				line = nextLine();
			}
			if (line == null) {
				RetroTectorException.sendError(this, "Multiparameter not terminated by ::", key + " in " + FILEPATH);
			}
			multia = new String[multi.size()];
			multi.copyInto(multia);
			theTable.put(key, multia);
			if (key.startsWith(CHAINP)) {
				try {
					int ii = Integer.parseInt(key.substring(6));
					maxchainnumber = Math.max(maxchainnumber, ii);
				} catch (NumberFormatException e) {
				}
			} else if (key.startsWith(CHAINS)) {
				try {
					int ii = Integer.parseInt(key.substring(6));
					maxchainnumber = Math.max(maxchainnumber, ii);
				} catch (NumberFormatException e) {
				}
			} else if (key.startsWith("Chain")) {
				try {
					int ii = Integer.parseInt(key.substring(5));
					maxchainnumber = Math.max(maxchainnumber, ii);
				} catch (NumberFormatException e) {
				}
			}
			keyStack.push(key);
			return key;
		} else {
			int index = line.indexOf(SINGLEPARAMTERMINATOR);
			if (index < 0) {
				RetroTectorException.sendError(this, "Syntax error in parameter file", FILEPATH, line);
			}
			key = line.substring(0, index).trim();
			theTable.put(key, line.substring(index + 1).trim());
			keyStack.push(key);
			return key;
		}
	} // end of readOneParameter()
	
/**
* Read all available parameters.
*/
	public final void readParameters() throws RetroTectorException {
		String s;
		while ((s = readOneParameter()) != null) {
		}
	} // end of readParameters()
	
/**
* Close this thing.
* @return	Array of keys.
*/
	public final String[ ] close() throws RetroTectorException{
		if (THEREADER != null) {
			try {
				THEREADER.close();
			} catch (IOException ioe) {
				RetroTectorException.sendError(this, "Trouble closing", FILEPATH);
			}
		}
		String[ ] ss = new String[keyStack.size()];
		keyStack.copyInto(ss);
		return ss;
	} // end of close()

/**
* Select a new Hashtable to read into.
* @param	h	The Hashtable in question.
*/
	public final void setTable(Hashtable h) {
		theTable = h;
	} // end of setTable(Hashtable)

/**
* @return	The highest n encountered in a key of form Chainn, ChainPn or ChainSn.
*/
	public final int getMaxchainnumber() {
		return maxchainnumber;
	} // end of getMaxchainnumber()

	
// reads the next line
	private final String nextLine() throws RetroTectorException {
		if (THEREADER != null) { // read from file
			try {
				return THEREADER.readLine();
			} catch (IOException ioe) {
				RetroTectorException.sendError(this, "Trouble reading from", FILEPATH);
				return null;
			}
		} else if (INSTRINGS != null) {
			if (stringNr >= INSTRINGS.length) {
				return null;
			} else {
				return INSTRINGS[stringNr++];
			}
		} else {
			return null;
		}
	} // end of nextLine()

} // end of ParameterFileReader