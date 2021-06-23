/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 4/12 -06
* Beautified 4/12 -06
*/
package plugins;

import retrotector.*;
import java.util.*;

/**
* Executor to align two base sequences.
*<PRE>
*     Parameters:
*
*   Sequence1
* The first sequence.
*
*   Sequence2
* The second sequence.
*</PRE>
*/
public class Align extends Executor {

/**
* Standard Executor constructor.
*/
	public Align() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put("Sequence1", "");
		explanations.put("Sequence1", "The first sequence");
		orderedkeys.push("Sequence1");
		parameters.put("Sequence2", "");
		explanations.put("Sequence2", "The second sequence");
		orderedkeys.push("Sequence2");
	} // end of constructor()

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2005 12 04";
  } // end of version()
		
	private String seq1;
	private String seq2;
	private String[ ] aline = new String[3];
	
/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {

		seq1 = getString("Sequence1", "").toLowerCase();
		seq2 = getString("Sequence2", "").toLowerCase();

		char c;
		StringBuffer sb = new StringBuffer(seq1);
		int i=0;
		while (i < sb.length()) {
			c = sb.charAt(i);
			if ((c != 'a') & (c != 'c') & (c != 'g') & (c != 't')) {
				sb.deleteCharAt(i);
			} else {
				i++;
			}
		}
		seq1 = sb.toString();
		sb = new StringBuffer(seq2);
		i=0;
		while (i < sb.length()) {
			c = sb.charAt(i);
			if ((c != 'a') & (c != 'c') & (c != 'g') & (c != 't')) {
				sb.deleteCharAt(i);
			} else {
				i++;
			}
		}
		seq2 = sb.toString();
		
// align them
		Utilities.alignBases(seq1.toCharArray(), seq2.toCharArray(), -1, 20, 0, 100, 0);
		System.out.println();
		System.out.println(Utilities.aline1);
		System.out.println();
		System.out.println(Utilities.aline2);
		aline[1] = Utilities.aline1;
		aline[2] = Utilities.aline2;
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		StringBuffer sb3 = new StringBuffer();
		char c1;
		char c2;
		
		for (int p=0; p<aline[1].length(); p++) {
			if ((p % 60 == 0) | (p == aline[1].length() - 1)) {
				System.out.println(sb1.toString());
				System.out.println(sb2.toString());
				System.out.println(sb3.toString());
				System.out.println();
				sb1 = new StringBuffer();
				sb2 = new StringBuffer();
				sb3 = new StringBuffer();
			}
			c1 = aline[1].charAt(p);
			c2 = aline[2].charAt(p);
			sb1.append(c1);
			sb2.append(c2);
			if (c1 == c2) {
				sb3.append("*");
			} else {
				sb3.append(" ");
			}
		}
		
		return "";

	} // end of execute()
	
} // end of Align
