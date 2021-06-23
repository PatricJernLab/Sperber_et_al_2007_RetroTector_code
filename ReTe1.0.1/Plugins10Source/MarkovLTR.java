/*
* Copyright (©) 2005, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector
*
* @author  G. Sperber
* @version 24/5 -06
*/
package plugins;

import retrotector.*;
import builtins.*;
import rtmarkov.*;

import java.io.*;
import java.util.*;
import javax.swing.*;


/**
* Executor which tries HMM on LTR.
*<PRE>
*     Parameters:
*
*   DNAFile
* DNA file to search in.
* Default: name of current directory + '.txt'.
*
*   StartAt
* The first position to search at.
* Default: -1.
*
*   EndAt
* The last position to search at.
* Default: -1.
*
*</PRE>	
*/
public class MarkovLTR extends Executor {

	private String chosen = null;
	private JComboBox theChoice = new JComboBox();
	
	private static int startAt = -1;
	private static int endAt = -1;
	
/**
* Standard Executor constructor.
*/
	public MarkovLTR() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(DNAFILEKEY, RetroTectorEngine.currentDirectory().getName() + FileNamer.TXTTERMINATOR);
		explanations.put(DNAFILEKEY, "The file to read DNA from");
		orderedkeys.push(DNAFILEKEY);
		parameters.put("StartAt", "" + startAt);
		explanations.put("StartAt", "The first position to search at");
		orderedkeys.push("StartAt");
		parameters.put("EndAt", "" + endAt);
		explanations.put("EndAt", "The last position to search at");
		orderedkeys.push("EndAt");
	} // end of constructor

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 05 19";
  } //end of version
	
/**
* Reads parameters from parameter window.
* @param	script	Should be null.
* @return	True if successful.
*/
	public void initialize(ParameterFileReader script) {
		runFlag = false;
		getDefaults();
		String[ ] ss = new RTMarkov().getDagNames();
		for (int i=0; i<ss.length; i++) {
			theChoice.addItem(ss[i]);
		}
		RetroTectorEngine.doParameterWindow(this, theChoice);
    chosen = (String) theChoice.getSelectedItem();
    interactive = true;
	} // end of initialize
	
/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {
		int first = getInt("StartAt", -1);
		int last = getInt("EndAt", -1);
		DNA dna;
		if (last >= first) {
			dna = getDNA(getString(DNAFILEKEY, ""), true);
			while ((last >= first) && (dna.internalize(first) < 0)) {
				first++;
			}
			while ((last >= first) && (dna.internalize(last) < 0)) {
				last--;
			}
			if (last <= first) {
				haltError("Not in valid DNA");
			}
		} else {
			dna = getDNA(getString(DNAFILEKEY, ""), false);
			while ((last < first) && (dna.internalize(first) < 0)) {
				first--;
			}
			while ((last < first) && (dna.internalize(last) < 0)) {
				last++;
			}
			if (last >= first) {
				haltError("Not in valid DNA");
			}
		}
		startAt = first;
		endAt = last;
		first = dna.internalize(first);
System.out.println(first);
		last = dna.internalize(last);
System.out.println(last);
		
		String sequence = dna.subString(first, last, true);
		
		String[ ] result = null;
		try {
			result = new RTMarkov().alignForRT(sequence, chosen);
		} catch (RTMarkovException rtme) {
			throw new RetroTectorException("MarkovLTR", rtme.MESSAGE);
		}
		
		Utilities.outputString(sequence);
		Utilities.outputString("");
		Utilities.outputString(result[0]);
		Utilities.outputString(result[2]);
		while (result[1].length() > 60) {
			Utilities.outputString(result[1].substring(0, 60));
			result[1] = result[1].substring(60);
			Utilities.outputString(result[3].substring(0, 60));
			result[3] = result[3].substring(60);
			Utilities.outputString("");
		}
		Utilities.outputString(result[1]);
		Utilities.outputString(result[3]);
		Utilities.outputString("");
		int ind;
		for (int i=5; i<result.length; i++) {
			if (result[i].equals(">target")) {	
				return "";
			}
			ind = result[i].trim().indexOf(" ");
			sequence = result[i].substring(0, ind).trim();
			Utilities.outputString(sequence + " " + dna.externalize(first + Integer.parseInt(result[i].substring(ind).trim())));
		}
		
		return "";
	} // end of execute

} // end of HMMonLTR
