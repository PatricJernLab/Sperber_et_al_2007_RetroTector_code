/*
* Copyright (©) 2005-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector
*
* @author  G. Sperber
* @version 4/12 -06
* Beautified 4/12 -06
*/
package plugins;

import retrotector.*;
import builtins.*;

import java.io.*;
import java.util.*;


/**
* Executor which lists possible PBS.
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
public class PBSFind extends Executor {

	
	private BaseMotif[ ] pbsMotifs;
	private float[ ] max;
	
	private String tab = "\t";
		
/**
* Standard Executor constructor.
*/
	public PBSFind() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(DNAFILEKEY, RetroTectorEngine.currentDirectory().getName() + FileNamer.TXTTERMINATOR);
		explanations.put(DNAFILEKEY, "The file to read DNA from");
		orderedkeys.push(DNAFILEKEY);
		parameters.put("StartAt", "-1");
		explanations.put("StartAt", "The first position to search at");
		orderedkeys.push("StartAt");
		parameters.put("EndAt", "-1");
		explanations.put("EndAt", "The last position to search at");
		orderedkeys.push("EndAt");
	} // end of constructor()

/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 12 04";
  } // end of version()
	

	private String pbsType(BaseMotif mot) {
		String s = mot.MOTIFORIGIN;
		int i = s.indexOf("tRNA");
		if (i < 0) {
			return " ";
		}
		s = s.substring(i + 4, i + 7).toUpperCase();
		i = "LYS TYR TRP HIS THR SER LEU PRO ARG PHE ASN ASP GLU MET ILE CYS ALA GLY GLN VAL".indexOf(s);
		if (i < 0) {
			return " ";
		}
		return "" + "KYWHTSLPRFNDEMICAGQV".charAt(i / 4);
	} // end of pbsType(BaseMotif)

/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {
		int first = getInt("StartAt", -1);
		int last = getInt("EndAt", -1);
		DNA dna = getDNA(getString(DNAFILEKEY, ""), last > first);
		first = dna.internalize(first);
		last = dna.internalize(last);
		
		Motif[ ] mmm = RetroTectorEngine.getCurrentDatabase().getMotifs("Motifs", SubGene.PBS);
		pbsMotifs = new BaseMotif[mmm.length];
		max = new float[mmm.length];
		for (int mi=0; mi<mmm.length; mi++) {
			pbsMotifs[mi] = (BaseMotif) mmm[mi];
			pbsMotifs[mi].refresh(2.0f);
			max[mi] = pbsMotifs[mi].getBestRawScore();
		}

		float bestscore;
		float newscore;
		BaseMotif bestMotif;
		for (int i=first; i<last-2; i++) {
			if ((dna.getBase(i) == 't') & (dna.getBase(i + 1) == 'g') & (dna.getBase(i + 2) == 'g')) {
				bestscore = -Float.MAX_VALUE;
				bestMotif = null;
				for (int m=0; m<pbsMotifs.length; m++) {
					newscore = 100.0f * dna.baseRawScoreAt(pbsMotifs[m].MOTIFSTRAND, i, 2.0f) / max[m];
					if (newscore >= bestscore) {
						bestscore = newscore;
						bestMotif = pbsMotifs[m];
					}
				}
				if (bestscore > -Float.MAX_VALUE) {
					Utilities.outputString(dna.externalize(i) + tab + Utilities.formattedNumber(bestscore, 1, 1) + tab + dna.subString(i, i + bestMotif.getBasesLength() - 1, true) + tab + pbsType(bestMotif));
				}
			}
		}
		return "";
	} // end of execute()

} // end of PBSFind
