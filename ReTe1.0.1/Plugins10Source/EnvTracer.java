/*
* Copyright (©) 2006, Göran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector
*
* @author  G. Sperber
* @version 13/11 -07
* Beautified 13/11 -07
*/
package plugins;

import retrotector.*;
import builtins.*;

import java.io.*;
import java.util.*;


/**
* Executor evaluates silent Env.
*<PRE>
*     Parameters:
*
*   DNAFile
* DNA file to search in.
* Default: name of current directory + '.txt'.
*
*   Chain
* Parent Chain name.
* Default: "".
*
*   StartEnv
* The first position to search at.
* Default: -1.
*
*   FirstEndEnv
* The first position to end trace at.
* Default: -1.
*
*   EndEnv
* The last position to search at.
* Default: -1.
*
*</PRE>	
*/
public class EnvTracer extends ORFID {

/**
* A possible trace, consisting of one or more shift-free parts.
*/
	private class EnvTraceTry extends Xon {
	
		private int MINPARTLENGTH = 50; // minimum number of acids in a part 
	
/**
* Constructor. Will throw RetroTectorException if a valid EnvTraceTry was not possible.
* @param	indexInPath	Pointer into mainPath to start at.
* @param	parts				Number of parts to try.
*/
		EnvTraceTry(int indexInPath, int parts) throws RetroTectorException {
			super(pblock);
			if (!isCodon(indexInPath)) {
				throw new RetroTectorException("EnvTracer", "Not codon");
			}
			PathElement pel = mainPath[indexInPath];
			int i1 = pel.FROMDNA;
// extend backwards to stop codon or limit
			while ((i1 - 3 >= firstInDNA) && (dna.getAcid(i1 -3) != 'z')) {
				i1 -= 3;
			}
			int le = 0;
// push leading codons, with le as counter
			while (i1 < pel.FROMDNA) {
				pushElement(new PathElement(i1, i1 + 3, 0, 0, 0, 0));
				i1 += 3;
				le++;
			}
			
			while (parts > 0) {
				if (indexInPath >= mainPath.length) {
					throw new RetroTectorException("EnvTracer", "Not enough parts");
				}
// push elements in same frame from mainPath
				while ((indexInPath < mainPath.length) && (isCodon(indexInPath))) {
					pushElement(mainPath[indexInPath]);
					i1 = mainPath[indexInPath].TODNA;
					indexInPath++;
					le++;
				}
// was this not final part, and too short?
				if ((indexInPath < mainPath.length) && (le < MINPARTLENGTH) && (parts > 1)) {
					throw new RetroTectorException("EnvTracer", "Too short path");
				}
// read off shifts
				while ((indexInPath < mainPath.length) && (!isCodon(indexInPath))) {
					indexInPath++;
				}
				
				parts--;
				if (parts > 0) {
					le = 0;
				}
			}
			
// add trailing non-stop codons
			while ((i1 + 3 <= lastInDNA) && (dna.getAcid(i1) != 'z')) {
				pushElement(new PathElement(i1, i1 + 3, 0, 0, 0, 0));
				i1 += 3;
				le++;
			}
// is trailing part too short?
			if (le < MINPARTLENGTH) {
				throw new RetroTectorException("EnvTracer", "Too short path");
			}
			
			if (i1 < firstEndInDNA) {
				throw new RetroTectorException("EnvTracer", "Too early end");
			}

			finishSequence(-Integer.MAX_VALUE, Integer.MAX_VALUE);

			estimatedFirst = path[0].FROMDNA;
			estimatedLast = path[path.length - 1].TODNA - 1;
		} // end of EnvTraceTry.constructor(int, int)
		
	} // end of EnvTraceTry


/**
* Minimum number of acids in trace.
*/
	public static final int MINENVTRACELENGTH = 60;

	private float faultThreshold = 51.0f / 12.0f;

/**
* Standard Executor constructor.
*/
	public EnvTracer() {
		parameters = new Hashtable();
		explanations = new Hashtable();
		orderedkeys = new Stack();
		parameters.put(DNAFILEKEY, RetroTectorEngine.currentDirectory().getName() + FileNamer.TXTTERMINATOR);
		explanations.put(DNAFILEKEY, "The file to read DNA from");
		orderedkeys.push(DNAFILEKEY);
		parameters.put("Chain", "");
		explanations.put("Chain", "The parent Chain");
		orderedkeys.push("Chain");
		parameters.put("StartEnv", "-1");
		explanations.put("StartEnv", "The first position to search at");
		orderedkeys.push("StartEnv");
		parameters.put("FirstEndEnv", "-1");
		explanations.put("FirstEndEnv", "The first position to end trace at");
		orderedkeys.push("FirstEndEnv");
		parameters.put("EndEnv", "-1");
		explanations.put("EndEnv", "The last position to search at");
		orderedkeys.push("EndEnv");
	} // end of constructor()

	private int firstInDNA;
	private int firstEndInDNA;
	private int lastInDNA;
	private PathElement[ ] mainPath;
	private ParameterBlock pblock;
	
/**
* Information about version date, to be displayed in parameter window.
*/
  public String version() {
    return "2006 11 29";
  } // end of version()
	
/**
* @param	p	A PathElement.
* @return	True if p is a codon.
*/
	private final boolean isCodon(PathElement p) {
		return (p.TODNA == p.FROMDNA + 3);
	} // end of isCodon(PathElement)

/**
* @param	pi	An index into mainPath.
* @return	True if PathElement at p is a codon.
*/
	private final boolean isCodon(int pi) {
		return isCodon(mainPath[pi]);
	} // end of isCodon(int)
	
/**
* @param	indexInPath	Pointer into mainPath to start at.
* @param	parts				Number of parts to try.
* @return	An EnvTraceTry as specified, or null if not possible.
*/
	private EnvTraceTry makeTry(int indexInPath, int parts) {
		try {
			return new EnvTraceTry(indexInPath, parts);
		} catch (RetroTectorException re) {
			return null;
		}
	} // end of makeTry(int, int)
	
/**
* Executes as specified above.
*/
	public String execute() throws RetroTectorException {
		firstInDNA = getInt("StartEnv", -1);
		firstEndInDNA = getInt("FirstEndEnv", -1);
		lastInDNA = getInt("EndEnv", -1);
		dna = getDNA(getString(DNAFILEKEY, ""), lastInDNA > firstInDNA);
		firstInDNA = dna.internalize(firstInDNA);
		firstEndInDNA = dna.internalize(firstEndInDNA);
		lastInDNA = dna.internalize(lastInDNA);
		if (firstInDNA >= lastInDNA) {
			return " ";
		}
    database = RetroTectorEngine.getCurrentDatabase();
    nonAlignedScore = getFloat(ORFID.NONALIGNEDSCOREKEY, 0.4f);
    stopCodonFactor = getFloat(ORFID.STOPCODONFACTORKEY, -0.4f);
    frameShiftPenalty = getFloat(ORFID.FRAMESHIFTPENALTYKEY, -1.5f);
    orfHexamerFactor = getFloat(ORFID.ORFHEXAMERFACTORKEY, 0.2f);
    nonOrfHexamerFactor = getFloat(ORFID.NONORFHEXAMERFACTORKEY, -0.1f);
    glycSiteFactor = getFloat(GLYCSITEFACTORKEY, 0.2f);
		genename = "env";
		avoidPositions = new int[0][0];
		pblock = new ParameterBlock(this);
		
		Utilities.Rectangle r = new Utilities.Rectangle(firstInDNA, 0, lastInDNA - firstInDNA, 1);
// let ExonDynamicMatrix do the work
		ExonDynamicMatrix mat;
		try {
			mat = new ExonDynamicMatrix(r, pblock);
		} catch (FinishedException fe) {
			throw new RetroTectorException("EnvTracer", "End of DNA reached");
		} catch (ArrayIndexOutOfBoundsException ae) {
			return "";
		}
		mainPath = mat.getPath().getPath();
		
		EnvTraceTry bestTry = null;
		EnvTraceTry etry;
		Stack tryStack = new Stack();
		int ind = 0;
		int pa;
		while (ind < mainPath.length) {
			while ((ind < mainPath.length) && !isCodon(ind)) {
				ind++;
			}
			if (ind < mainPath.length) {
				pa = 1;
				while ((etry = makeTry(ind, pa)) != null) {
					tryStack.push(etry);
					pa++;
				}
			}
			while ((ind < mainPath.length) && isCodon(ind)) {
				ind++;
			}
		}
		if (tryStack.size() == 0) {
			return "";
		}
		EnvTraceTry[ ] tries = new EnvTraceTry[tryStack.size()];
		tryStack.copyInto(tries);
		Utilities.sort(tries);
		bestTry = tries[0];

// find longest ORF inside limits
		int bestpos = -1;
		int bestlength = 0;
		int l;
		for (int pos=firstInDNA; pos<=lastInDNA; pos++) {
			l = orfLast(pos);
			while (l > lastInDNA) {
				l -= 3;
			}
			l = l - pos;
			if (l > bestlength) {
				bestlength = l;
				bestpos = pos;
			}
		}
		l = bestpos + bestlength;
// count glycosylation sites in ORF
		int[ ] glycosyls = dna.getGlycosyls();
		
		int count = 0;
		for (int i=0; i<glycosyls.length; i++) {
			if ((glycosyls[i] >= bestpos) && (glycosyls[i] <= l) && (((glycosyls[i] - bestpos) % 3) == 0)) {
				count++;
			}
		}

		if (bestTry == null) {
			return "";
		}
		File f = FileNamer.createEnvTraceFile(getString("Chain", ""));
		ExecutorScriptWriter writer = new ExecutorScriptWriter(f, "Puteinview");
		writer.writeSingleParameter(NROFHITSKEY, "2", false);
		writer.writeSingleParameter(Executor.DNAFILEKEY, dna.NAME, false);
		writer.writeSingleParameter("Chain", getString("Chain", ""), false);
		bestTry.toFile(writer);
		int gc = Utilities.glycSitesInString(bestTry.mainLines[AcidSequence.PUTEINLINEINDEX]);
		writer.writeSingleParameter("XonGlycosylationSites", "" + gc, false);
		writer.writeSingleParameter("LongestORFStart", "" + dna.externalize(bestpos), false);
		writer.writeSingleParameter("LongestORFLength", "" + bestlength / 3, false);
		writer.writeSingleParameter("LongestORF", dna.subString(bestpos, l, false), false);
		writer.writeSingleParameter("LongestORFGlycosylationSites", "" + count, false);
		writer.close();
		
		
		return "";
	} // end of execute()

// last position in ORF including startAt
	private int orfLast(int startAt) {
		int[ ] stopcodons = dna.getStopCodons();
		for (int i=0; i<stopcodons.length; i++) {
			if ((stopcodons[i] >= startAt) && ((stopcodons[i] - startAt) % 3 == 0)) {
				return stopcodons[i] - 3;
			}
		}
		int j;
		for (j=startAt; j<dna.LENGTH; j+=3) {
		}
		return j - 3;
	} // end of orfLast(int)
	
} // end of EnvTracer
