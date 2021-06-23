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

public class Clustaltest extends Executor {
		
/**
* Standard Executor constructor.
*/
	public Clustaltest() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put("String1", "");
		explanations.put("String1", "");
		orderedkeys.push("String1");
		parameters.put("String2", "");
		explanations.put("String2", "");
		orderedkeys.push("String2");
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
		String s1 = getString("String1", "");
		String s2 = getString("String2", "");
		Clustalj clus = new Clustalj();
		float f = clus.clustal(s1, s2, true, false);
		System.out.println(f + " " + clus.midpos0 + " " + clus.midpos1);
		System.out.println();
		System.out.println(clus.aline0);
		System.out.println();
		System.out.println(clus.aline1);
		System.out.println();
		return "";
	} // end of execute

} // end of Clustaltest
