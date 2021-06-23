/*
* Copyright (©) 2000-2004, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector
*
* @author  G. Sperber
* @version 21/6 -04
* Beautified 21/6 -04
*/
package plugins;

import retrotector.*;

import java.io.*;
import java.util.*;


/**
* Executor which lists Chains within the current directory tree.
*<PRE>
*     Parameters:
*
*   OutputFile
* The name of the file for the list.
* Default: ChainList.txt.
*
*</PRE>	
*/
public class ListChains extends Executor implements Utilities.FileTreater {

	private PrintWriter outfile;
	private ParameterFileReader reader;
	private Stack st = new Stack();
		
/**
* Standard Executor constructor.
*/
	public ListChains() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(OUTPUTFILEKEY, "ChainList.txt");
		explanations.put(OUTPUTFILEKEY, "File for Chains list");
		orderedkeys.push(OUTPUTFILEKEY);
	} // end of constructor

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2004 06 21";
  } //end of version
	
/**
* The one that does it with all the Chains in one File.
* @param	f	A File to extract Chains from.
*/
	public File treatFile(File f) throws RetroTectorException {
		if (!FileNamer.isSelectedChainsFile(f.getName())) {
			return f;
		}
		String newname;
		String[ ] lines;
		parameters = new Hashtable();
		reader = new ParameterFileReader(f, parameters);
		newname = reader.readOneParameter();
		while (newname != null) {
			if (newname.startsWith("Chain")) {
				lines = getStringArray(newname);
				st.push(ChainGraphInfo.shortDescription(lines));
			}
			newname = reader.readOneParameter();
		}
		return f;
	} // end of treatFile

				
/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {
		String fileName = getString(OUTPUTFILEKEY, "");
		Utilities.treatFilesIn(RetroTectorEngine.currentDirectory(), this);
		ChainGraphInfo.ChainShortDescription[ ] csd = new ChainGraphInfo.ChainShortDescription[st.size()];
		st.copyInto(csd);
		Utilities.sort(csd);
		try {
			outfile = new PrintWriter(new FileWriter(new File(RetroTectorEngine.currentDirectory(), fileName)));
			for (int i=csd.length-1; i>=0; i--) {
				if ((i == 0) || !csd[i].doesEqual(csd[i - 1])) {
					outfile.println(csd[i].toString());
				}
			}
		} catch (IOException ioe) {
			haltError("Could not open " + getString(OUTPUTFILEKEY, ""));
		}
		outfile.close();
		return "";
	} // end of execute

} // end of ListChains
