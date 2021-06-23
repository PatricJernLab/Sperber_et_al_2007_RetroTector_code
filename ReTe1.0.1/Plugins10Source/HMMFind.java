/*
* Copyright (©) 2005, Göran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector
*
* @author  G. Sperber
* @version 2/12 -05
*/
package plugins;

import retrotector.*;
import builtins.*;

import java.io.*;
import java.util.*;


/**
* Executor which searches for a split base sequence.
*<PRE>
*     Parameters:
*
*   DNAFile
* DNA file to search in.
* Default: name of current directory + '.txt'.
*
*   Template
* Description of the base sequence.
* Default: aaa(10<20)ccc.
*
*   Tolerance
* Maximum number of not found fragments.
* Default: 0.
*
*</PRE>	
*/
public class HMMFind extends Executor {

	private class Fragment {
	
		final String STRING;
		final long MER;
		final int MERLENGTH;
		final DistanceRange RANGETONEXT;
		
		Fragment next;
		
		Fragment(String s) throws RetroTectorException {
			STRING = s.trim();
			int ind = STRING.indexOf("(");
			if (ind < 0) {
				MERLENGTH = STRING.length();
				RANGETONEXT = null;
			} else {
				MERLENGTH = ind;
				int ind2 = STRING.indexOf(")");
				RANGETONEXT = new DistanceRange(STRING.substring(ind + 1, ind2));
			}
			long mer = 0;
			for (int i=0; i<MERLENGTH; i++) {
				mer = (mer << 2) + baseCompactor.charToIntId(STRING.charAt(i));
			}
			MER = mer;
			maxmerlength = Math.max(maxmerlength, MERLENGTH);
		} // end of Fragment.constructor
		
		boolean foundBetween(int first, int last) {
			for (int i=first; (i<=last) & (i<mers[MERLENGTH].length); i++) {
				if (MER == mers[MERLENGTH][i]) {
					if (endAt < 0) {
						endAt = i + MERLENGTH;
					}
					return true;
				}
			}
			return false;
		} // end of Fragment.foundBetween
		
		int faultsBetween(int first, int last) {
			int f = 1;
			if (foundBetween(first, last)) {
				f = 0;
			}
			if ((next == null) || (f > tolerance)) {
				return f;
			}
			f = f + next.faultsBetween(first + MERLENGTH + RANGETONEXT.LOWESTDISTANCE, last + MERLENGTH + RANGETONEXT.HIGHESTDISTANCE);
			return f;
		} // end of Fragment.faultsBetween
		
		public String toString() {
			if (RANGETONEXT == null) {
				return STRING + "  " + MERLENGTH + "  " + MER;
			} else {
				return STRING + "  " + MERLENGTH + "  " + RANGETONEXT.toString() + "  " + MER;
			}
		} // end of Fragment.toString()
		
	} // end of Fragment
	
	private static int maxmerlength = 0;
	private Compactor baseCompactor = Compactor.BASECOMPACTOR;
	private int[ ] basecodes;
	private int tolerance = 0;
	private String template = null;
	private Fragment[ ] fragments;
	private long[ ][ ] mers;
	private int endAt;
	
	
/**
* Standard Executor constructor.
*/
	public HMMFind() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(DNAFILEKEY, RetroTectorEngine.currentDirectory().getName() + FileNamer.TXTTERMINATOR);
		explanations.put(DNAFILEKEY, "The file to read DNA from");
		orderedkeys.push(DNAFILEKEY);
		parameters.put("Template", "aaa(10<20)ccc");
		explanations.put("Template", "Description of the base sequence");
		orderedkeys.push("Template");
		parameters.put("Tolerance", "0");
		explanations.put("Tolerance", "Maximum number of not found fragments");
		orderedkeys.push("Tolerance");
	} // end of constructor

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2005 12 02";
  } //end of version
	
/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {
		template = getString("Template", null);
		tolerance = getInt("Tolerance", 0);
		DNA dna = getDNA(getString(DNAFILEKEY, ""), true);
		Stack st = new Stack();
		int ind;
		while ((ind = template.indexOf(")")) >= 0) {
			st.push(new Fragment(template.substring(0, ind + 1)));
			template = template.substring(ind + 1);
		}
		st.push(new Fragment(template));
		fragments = new Fragment[st.size()];
		st.copyInto(fragments);
		
		basecodes = dna.getBaseCodes();
		mers = new long[maxmerlength + 1][ ];
		
		for (int i=0; i<fragments.length; i++) {
			System.out.println(fragments[i].toString());
			if (mers[fragments[i].MERLENGTH] == null) {
				mers[fragments[i].MERLENGTH] = Utilities.merize(basecodes, 0, basecodes.length - 1, fragments[i].MERLENGTH, false);
			}
			if (i + 1 < fragments.length) {
				fragments[i].next = fragments[i + 1];
			}
		}
		
		for (int p=0; p<basecodes.length; p++) {
			endAt = -1;
			if (fragments[0].faultsBetween(p, p) <= tolerance) {
				System.out.println(dna.externalize(p) + " " + dna.externalize(endAt));
				System.out.println(dna.subString(p, endAt, true));
			}
		}
		
		return "";
	} // end of execute

} // end of HMMFind
