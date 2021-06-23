/*
* Copyright (©) 2000-2004, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 22/11 -04
* Beautified 21/6 -04
*/
package plugins;

import retrotector.*;

import java.io.*;
import java.util.*;

public class SliceOfDNA extends Executor {
		
/**
* Standard Executor constructor.
*/
	public SliceOfDNA() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(DNAFILEKEY, RetroTectorEngine.currentDirectory().getName() + FileNamer.TXTTERMINATOR);
		explanations.put(DNAFILEKEY, "The file to read DNA from");
		orderedkeys.push(DNAFILEKEY);
		parameters.put("StartAt", "0");
		explanations.put("StartAt", "");
		orderedkeys.push("StartAt");
		parameters.put("EndAt", "0");
		explanations.put("EndAt", "");
		orderedkeys.push("EndAt");
	} // end of constructor

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2004 11 23";
  } //end of version
	  
/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {
		int first = getInt("StartAt", -1);
		int last = getInt("EndAt", -1);
		DNA dna = getDNA(getString(DNAFILEKEY, ""), last > first);
		first = dna.internalize(first);
		last = dna.internalize(last);
		String s = dna.subString(first, last, true);
		System.out.println(s);
		return "";
	} // end of execute

} // end of SliceOfDNA
