/*
* Copyright (©) 2000-2007, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 12/3 -06
* Beautified 12/3 -06
*/

package retrotector;

import builtins.*;
import java.util.*;
import java.io.*;

/**
* A coherent sequence of PathElements, starting and ending with an acid.
*/
public class Putein extends AcidSequence {

/**
* A part of a Path with no shifts in it.
*/
	private class FrameSlice implements Scorable {
	
/**
* First element in slice.
*/
		final PathElement SLICEFIRST;
	
/**
* Last element in slice.
*/
		final PathElement SLICELAST;
	
/**
* Frame of slice.
*/
		final int SLICEFRAME;
	
/**
* Index of slice in slices.
*/
		final int SLICEINDEX;

/**
* Length of slice / sum of lengths of surrounding slices.
*/
		float strength;
		
/**
* Constructor.
* @param	firstElement		-> SLICEFIRST
* @param	lastElement			-> SLICELAST
* @param	ind							-> SLICEINDEX
*/
		FrameSlice(PathElement firstElement, PathElement lastElement, int ind) {
			SLICEFIRST = firstElement;
			SLICELAST = lastElement;
			SLICEFRAME = DNA.frameOf(firstElement.FROMDNA);
			SLICEINDEX = ind;
			strength = Float.MAX_VALUE;
		} // end of FrameSlice.constructor(PathElement, PathElement, int)
		
/**
* @return	strength
*/
		public float fetchScore() {
			return strength;
		} // end of FrameSlice.fetchScore()
		
	} // end of FrameSlice

/**
* Key for suggested position of start of putein in DNA = "EstimatedStartPosition".
*/
	public static final String ESTIMATEDSTARTPOSITIONKEY = "EstimatedStartPosition";
	
/**
* Key for suggested position of end of putein in DNA = "EstimatedLastPosition".
*/
	public static final String ESTIMATEDLASTPOSITIONKEY = "EstimatedLastPosition";

/**
*	= "Putein".
*/
	public static final String PUTEINKEY = "Putein";

/**
*	= "LengthInside".
*/
	public static final String LENGTHINSIDEKEY = "LengthInside";

/**
*	= "LengthTotal".
*/
	public static final String TOTALLENGTHKEY = "LengthTotal";

/**
*	= "AlignedAcids".
*/
	public static final String ALIGNEDACIDSKEY = "AlignedAcids";

/**
*	= "AverageScoreInside".
*/
	public static final String AVERAGESCOREINSIDEKEY = "AverageScoreInside";

/**
*	= "AverageScoreTotal".
*/
	public static final String AVERAGESCORETOTALKEY = "AverageScoreTotal";

/**
*	= "MostUsedRow".
*/
	public static final String MOSTUSEDROWKEY = "MostUsedRow";

/**
*	= "StopCodonsInside".
*/
	public static final String STOPCODONSINSIDEKEY = "StopCodonsInside";

/**
*	= "StopCodonsTotal".
*/
	public static final String STOPCODONSTOTALKEY = "StopCodonsTotal";

/**
*	= "AmbiguousAcidsInside".
*/
	public static final String AMBIGUOUSINSIDEKEY = "AmbiguousAcidsInside";

/**
*	= "AmbiguousAcidsTotal".
*/
	public static final String AMBIGUOUSTOTALKEY = "AmbiguousAcidsTotal";

/**
*	= "ShiftsInside".
*/
	public static final String SHIFTSINSIDEKEY = "ShiftsInside";

/**
*	= "ShiftsTotal".
*/
	public static final String SHIFTSTOTALKEY = "ShiftsTotal";

/**
*	= "LongestORF".
*/
	public static final String LONGESTORFKEY = "LongestORF";

/**
*	= "LongestRun".
*/
	public static final String LONGESTRUNKEY = "LongestRun";

/**
*	= "Hits".
*/
	public static final String HITSKEY = "Hits";

/**
*	= "Gene".
*/
	public static final String GENEKEY = "Gene";

/**
*	= "Genus".
*/
	public static final String GENUSKEY = "Genus";
	
/**
* = "CodonSequence".
*/
	public static final String CODONSEQUENCEKEY = "CodonSequence";

/**
* = "PolClass".
*/
	public static final String POLCLASSKEY = "PolClass";

/**
* Gag, Pro, Pol, Env or null.
*/
	public final String GENE;

/**
* Virus genus.
*/
	public final String GENUS;

/**
* Number of acids in longest ORF.
*/
	public int longestORFLength = -1;

/**
* Start position of longest ORF.
*/
	public int longestORFPos = -1;
	
	private AllORFModifier allomod;
	
	
/**
* Constructor.
* Putein is not useful until pushElement and finishPutein have been employed.
* @param	pBlock	ORFID.ParameterBlock with parameters.
*/
	public Putein(ORFID.ParameterBlock pBlock) {
		super(pBlock);
		GENE = PBLOCK.GENENAME;
		GENUS = String.valueOf(PBLOCK.GENUSCHAR);
	} // end of constructor(ORFID.ParameterBlock)
	
/**
* Constructor.
* @param	inFile	A File with contents according to ParameterStream.
* @param	pBlock	ORFID.ParameterBlock with parameters, typically of the simple type.
*/
	public Putein(File inFile, ORFID.ParameterBlock pBlock) throws RetroTectorException {
		super(inFile, pBlock);
		String sw = (String) ht.get(LONGESTORFKEY);
		if (sw != null) {
			String[ ] orfs = Utilities.splitString(sw);
			longestORFPos = PBLOCK.TARGETDNA.internalize(Utilities.decodeInt(orfs[0]));
			longestORFLength = orfs[1].length();
		}
		String gene = (String) ht.get(GENEKEY);
		if (gene == null) {
			gene = PBLOCK.GENENAME;
		}
		if (gene == null) {
			gene = FileNamer.getGeneFromPutein(inFile.getName());
		}
		GENE = gene;
		String genus = (String) ht.get(GENUSKEY);
		if (genus == null) {
			genus = String.valueOf(PBLOCK.GENUSCHAR);
		}
		if (genus.trim().length() == 0) {
			genus = FileNamer.getGenusFromPutein(inFile.getName());
		}
		
		GENUS = genus;
		totalLength = Utilities.decodeInt((String) ht.get(TOTALLENGTHKEY));
		alignedacidsinside = Utilities.decodeInt((String) ht.get(ALIGNEDACIDSKEY));
		
	} // end of constructor (File, ORFID.ParameterBlock)

	
/**
* Returns the (internal) start and end position of
* the longest ORF within the Putein estimated ends, and with some codon at pos.
*/
	private final int[ ] longestORFat(int pos) {
		int[ ] result = new int[2];
		result[0] = pos;
		result[1] = pos;
		int extpos = PBLOCK.TARGETDNA.externalize(pos);
		int newextpos;
		for (int i1=pos; i1-3>=estimatedFirst; i1 -= 3) {
			result[0] = i1;
			if (PBLOCK.TARGETDNA.getAcid(i1-3) == Compactor.STOPCHAR) {
				i1 = -1000;
			}
			newextpos = PBLOCK.TARGETDNA.externalize(i1 - 3);
			if ((newextpos == extpos - 3) | (newextpos == extpos + 3)) {
				extpos = newextpos;
			} else { //shift
				i1 = -1000;
			}
		}
		extpos = PBLOCK.TARGETDNA.externalize(pos - 3);
		for (int i2=pos; i2<=estimatedLast + 1; i2 += 3) {
			result[1] = i2 - 1;
			if (PBLOCK.TARGETDNA.getAcid(i2) == Compactor.STOPCHAR) {
				i2 = PBLOCK.TARGETDNA.LENGTH;
			}
			newextpos = PBLOCK.TARGETDNA.externalize(i2);
			if ((newextpos == extpos - 3) | (newextpos == extpos + 3)) {
				extpos = newextpos;
			} else { //shift
				i2 = PBLOCK.TARGETDNA.LENGTH;
			}
		}
		return result;
	} // end of longestORFat(int)
	
/**
* Returns a String consisting of the (external) start position and the acid string of
* the longest ORF within the Putein estimated ends, and with some acid position
* in common with the Putein.
* If the ORF extends beyond the Putein, '<<' and/or '>>' is added as appropriate.
*/
	private final String longestORF() {
		int[ ] best = new int[2];
		PathElement pe;
		int[ ] ne;
		for (int p=0; p<path.length; p++) {
			pe = path[p];
			if ((pe.FROMDNA >= estimatedFirst) & (pe.TODNA <= estimatedLast + 1) & (pe.TODNA - pe.FROMDNA == 3)) {
				ne = longestORFat(pe.FROMDNA);
				if ((ne[1] - ne[0]) > (best[1] - best[0])) {
					best = ne;
				}
			}
		}
		String result = "" + PBLOCK.TARGETDNA.externalize(best[0]) + " ";
		if ((best[0] >= 3) && (PBLOCK.TARGETDNA.getAcid(best[0] - 3) != Compactor.STOPCHAR)) {
			result = result + "<<";
		}
		result = result + PBLOCK.TARGETDNA.subString(best[0], best[1], false);
		if ((best[1] < PBLOCK.TARGETDNA.LENGTH - 3) && (PBLOCK.TARGETDNA.getAcid(best[1] + 1) != Compactor.STOPCHAR)) {
			result = result + ">>";
		}
		return result;
	} // end of longestORF()
	
	
/**
* Adds information about this Putein to an ExecutorScriptWriter. Not to be used if PBLOCK.MASTER == null.
* @param	puteinWriter	The ExecutorScriptWriter.
*/
	public final void toPuteinFile(ExecutorScriptWriter puteinWriter) throws RetroTectorException, FinishedException {
	
		if (path.length < 2) {
			throw new RetroTectorException("Putein", "Too short path");
		}

    puteinWriter.writeSingleParameter(GENEKEY, GENE, false);
    puteinWriter.writeSingleParameter(GENUSKEY, GENUS, false);
    puteinWriter.writeSingleParameter(Executor.DNAFILEKEY, PBLOCK.TARGETDNA.NAME, false);
    puteinWriter.writeSingleParameter(ESTIMATEDSTARTPOSITIONKEY, String.valueOf(PBLOCK.TARGETDNA.externalize(estimatedFirst)), false);
    puteinWriter.writeSingleParameter(ESTIMATEDLASTPOSITIONKEY, String.valueOf(PBLOCK.TARGETDNA.externalize(estimatedLast)), false);
    puteinWriter.writeSingleParameter(LENGTHINSIDEKEY, String.valueOf(insideLength), false);
    puteinWriter.writeSingleParameter(TOTALLENGTHKEY, String.valueOf(totalLength), false);
    puteinWriter.writeSingleParameter(ALIGNEDACIDSKEY, String.valueOf(alignedacidsinside), false);
    puteinWriter.writeSingleParameter(AVERAGESCOREINSIDEKEY, String.valueOf(scoresuminside * 1.0f / insideLength), false);
    puteinWriter.writeSingleParameter(AVERAGESCORETOTALKEY, String.valueOf(scoresum * 1.0f / totalLength), false);
    if (PBLOCK.MASTER != null) {
			puteinWriter.writeSingleParameter(MOSTUSEDROWKEY, "" + PBLOCK.MASTER.mostHitRow().IDNUMBER + " (" + PBLOCK.MASTER.mostHitRow().ROWORIGIN + ")", false);
		}
		
    int[ ] stopc = PBLOCK.TARGETDNA.stopcodoncounts(estimatedFirst, estimatedLast);
    puteinWriter.writeSingleParameter(STOPCODONSINSIDEKEY, "" + stopcountinside + "(" + stopc[1] + ", " + stopc[2] + ", " + stopc[3] + ")", false);
    puteinWriter.writeSingleParameter(STOPCODONSTOTALKEY, String.valueOf(stopcount), false);
    puteinWriter.writeSingleParameter(AMBIGUOUSINSIDEKEY, String.valueOf(ambigcountinside), false);
    puteinWriter.writeSingleParameter(AMBIGUOUSTOTALKEY, String.valueOf(ambigcount), false);
    puteinWriter.writeSingleParameter(SHIFTSINSIDEKEY, String.valueOf(shiftcountinside), false);
    puteinWriter.writeSingleParameter(SHIFTSTOTALKEY, String.valueOf(shiftcount), false);
		puteinWriter.writeSingleParameter(LONGESTRUNKEY, "" + bestRunLength + " at " + PBLOCK.TARGETDNA.externalize(bestRunPos), false);
    puteinWriter.writeComment("Starts at position " + PBLOCK.TARGETDNA.externalize(path[0].FROMDNA) + ", ends at " + PBLOCK.TARGETDNA.externalize(path[path.length - 1].TODNA));
		puteinWriter.writeSingleParameter(LONGESTORFKEY, longestORF(), false);
		puteinWriter.startMultiParameter(PUTEINKEY, false);
		puteinWriter.appendToMultiParameter(mainLines[MOTIFLINEINDEX], false);
		puteinWriter.appendToMultiParameter(mainLines[PUTEINLINEINDEX], false);
		puteinWriter.appendToMultiParameter(mainLines[FRAMELINEINDEX], false);
		puteinWriter.appendToMultiParameter(mainLines[FOURTHLINEINDEX], false);
		puteinWriter.appendToMultiParameter(mainLines[POSLINEINDEX], false);
		puteinWriter.finishMultiParameter(false);
    puteinWriter.writeSingleParameter(CODONSEQUENCEKEY, codonBuffer.toString(), false);
		if (PBLOCK.HITSINFO != null) {
			puteinWriter.writeMultiParameter(HITSKEY, PBLOCK.HITSINFO.toStringsIndexed(), false);
		}
	} // end of toPuteinFile(ExecutorScriptWriter)

// appends external position in DNA to appropriate line if multiple of 30
	private final void baseNumber(String[ ] ss, int pos) {
		for (int l=0; l<3; l++) {
			if ((PBLOCK.TARGETDNA.externalize(pos + l) % 30) == 0) { // time to output position in DNA?
				ss[l] = ss[l] + "       " + PBLOCK.TARGETDNA.externalize(pos + l);
			}
		}
	} // end of baseNumber(String[ ], int)

// increments dna marker, returning a descriptive line, starting with c
	private final String advanceBase(char c) throws FinishedException, RetroTectorException {
		int d = PBLOCK.TARGETDNA.getMarkExt();
		String line = PBLOCK.MASTER.EMPTYLINE + " " + PBLOCK.TARGETDNA.advanceBase();
		if ((d % 30) == 0) {
			line = line + "       " + d;
		}
		return c + line;
	} // end of advanceBase(char)
	
// adds AllORFModifier value to s
	private String nonAl(String s, int po) {
		StringBuffer sb = new StringBuffer(s);
		while (sb.length() < 40 + 2 * PBLOCK.MASTER.NROFROWS) {
			sb.append(" ");
		}
		sb.append(allomod.modification(po));
		return sb.toString();
	} // end of nonAl()
	
/**
* Adds debugging information to an ExecutorScriptWriter.
* Will not work if PBLOCK was of the simple type.
* @param	outputWriter	The ExecutorScriptWriter.
*/
	public final void toOutputFile(ExecutorScriptWriter outputWriter) throws RetroTectorException, FinishedException {

		if (path.length < 2) {
			throw new RetroTectorException("Putein", "Too short path");
		}
		
		PBLOCK.MASTER.reset();
		PBLOCK.TARGETDNA.setMark(path[0].FROMDNA);
		allomod = new AllORFModifier(PBLOCK.TARGETDNA);

		int po;
		for (int s=0; s<path.length; s++) {
			if ((path[s].TOMASTER == path[s].FROMMASTER + 1) & (path[s].TODNA == path[s].FROMDNA + 3)) {
				PBLOCK.MASTER.suggestCurrentRow(path[s].ROWCODE);
				try {
					char c = PBLOCK.MASTER.currentAcid();
					String[ ] ms = PBLOCK.MASTER.advanceMasterAcid(); // get three lines, the first desribing alignment
					PBLOCK.MASTER.getCurrentRow().hitcount++; // keep count of used AlignmentRow
					String[ ] ds = PBLOCK.TARGETDNA.advanceCodon(c);  // get lines descibing this and following bases
					ds[0] = ds[0] + " " + (PBLOCK.MASTER.getCurrentRow().IDNUMBER); // number of used AlignmentRow
					baseNumber(ds,  PBLOCK.TARGETDNA.getMark() - 3);
					for (int l=0; l<3; l++) { // output to output file
						outputWriter.appendToMultiParameter(nonAl(" " + ms[l] + " " + ds[l], PBLOCK.TARGETDNA.getMark() - 3 + l), false);
					}
				} catch (ArrayIndexOutOfBoundsException ae) {
					throw new FinishedException();
				}
			} else if ((path[s].TOMASTER == (path[s].FROMMASTER + 1)) & (path[s].TODNA == path[s].FROMDNA)) {
				if (path[s].FROMDNA == path[0].FROMDNA) {
					PBLOCK.MASTER.advanceMasterAcid();
				} else { // add a deletion to path
					outputWriter.appendToMultiParameter('*' + PBLOCK.MASTER.advanceMasterAcid()[0], false);
				}
			} else if ((path[s].TOMASTER == path[s].FROMMASTER) & (path[s].TODNA == (path[s].FROMDNA + 3))) {
				String[ ] lines = PBLOCK.TARGETDNA.advanceCodon(' ');
				baseNumber(lines,  PBLOCK.TARGETDNA.getMark() - 3);
				for (int i=0; i<3; i++) {
					outputWriter.appendToMultiParameter(nonAl('/' + PBLOCK.MASTER.EMPTYLINE + " " + lines[i], PBLOCK.TARGETDNA.getMark() - 3 + i), false);
				}
			} else if ((path[s].TOMASTER == path[s].FROMMASTER) & (path[s].TODNA == (path[s].FROMDNA + 2))) {
				outputWriter.appendToMultiParameter(nonAl(advanceBase('('), PBLOCK.TARGETDNA.getMark() - 1), false);
				outputWriter.appendToMultiParameter(nonAl(advanceBase('('), PBLOCK.TARGETDNA.getMark() - 1), false);
			} else if ((path[s].TOMASTER == path[s].FROMMASTER) & (path[s].TODNA == (path[s].FROMDNA + 1))) {
				outputWriter.appendToMultiParameter(nonAl(advanceBase('|'), PBLOCK.TARGETDNA.getMark() - 1), false);
			} else {
				throw new RetroTectorException(Utilities.className(this), "Erroneous path element");
			}
		}
	} // end of toOutputFile(ExecutorScriptWriter)
	
/**
* @return		This putein with unnecessary deviations from frame removed, or null if none found.
*/
	public Putein straightenedPutein() throws RetroTectorException {
		Stack sliceStack = new Stack();
		int ind = 0;
		int diff = 0;
		PathElement pe1;
		PathElement pe2;

// read off leader
		while (path[ind].FROMDNA < estimatedFirst) {
			ind++;
		}

		while (path[ind].TODNA < estimatedLast) {
// read off shifts
			while ((path[ind].TODNA < estimatedLast) && (path[ind].TODNA - path[ind].FROMDNA != 3)) {
				ind++;
			}
			if (path[ind].TODNA < estimatedLast) {
				pe1 = path[ind];
				pe2 = path[ind];
				ind++;
				diff = path[ind].TODNA - path[ind].FROMDNA;
// go on while no shift
				while ((path[ind].TODNA < estimatedLast) && ((diff == 3) || (diff == 0))) {
					if (diff == 3) {
						pe2 = path[ind];
					}
					ind++;
					diff = path[ind].TODNA - path[ind].FROMDNA;
				}
				sliceStack.push(new FrameSlice(pe1, pe2, sliceStack.size()));
			}
		}
		FrameSlice[ ] slices = new FrameSlice[sliceStack.size()];
		FrameSlice[ ] orderedSlices = new FrameSlice[sliceStack.size()];
		sliceStack.copyInto(slices);
		sliceStack.copyInto(orderedSlices);
		
		boolean stop;
		for (int i=1; i<slices.length-1; i++) {
			stop = false;
			if (slices[i - 1].SLICEFRAME == slices[i + 1].SLICEFRAME) { // surrounding slices in same frame?
// any stop codons between them?
				for (int po=slices[i-1].SLICELAST.TODNA; po<slices[i+1].SLICEFIRST.FROMDNA; po+=3) {
					if (PBLOCK.TARGETDNA.getAcid(po) == 'z') {
						stop = true;
					}
				}
				if (!stop) {
// update strength
					slices[i].strength = 1.0f * (slices[i].SLICELAST.TODNA - slices[i].SLICEFIRST.FROMDNA) / (slices[i + 1].SLICELAST.TODNA - slices[i + 1].SLICEFIRST.FROMDNA + slices[i - 1].SLICELAST.TODNA - slices[i - 1].SLICEFIRST.FROMDNA);
				}
			}
		}
		Utilities.sort(orderedSlices);
		
		FrameSlice bestSlice = orderedSlices[orderedSlices.length - 1];
		if (bestSlice.strength < 0.3f) {
			int pi = 0;
			Putein result = new Putein(PBLOCK);
			while (path[pi] != slices[bestSlice.SLICEINDEX - 1].SLICELAST) {
				result.pushElement(path[pi++]);
			}
			int masterpos = path[pi].TOMASTER;
			result.pushElement(path[pi]);
			
			for (int po=slices[bestSlice.SLICEINDEX-1].SLICELAST.TODNA; po<slices[bestSlice.SLICEINDEX+1].SLICEFIRST.FROMDNA; po+=3) {
				result.pushElement(new PathElement(po, po + 3, masterpos, masterpos, 0, 0));
			}
			while (path[pi] != slices[bestSlice.SLICEINDEX+1].SLICEFIRST) {
				pi++;
			}
			while (masterpos < path[pi].FROMMASTER) {
				result.pushElement(new PathElement(path[pi].FROMDNA, path[pi].FROMDNA, masterpos, masterpos + 1, 0, 0));
				masterpos++;
			}
			while (pi < path.length) {
				result.pushElement(path[pi++]);
			}
			result.finishSequence(estimatedFirst, estimatedLast);
			return result;
		} else {
			return null;
		}
	} // end of straightenedPutein()

/**
* As required by Scorable.
* @return	insideLength * insideLength / (1.0f + stopcountinside + 5.0f * shiftcountinside).
*/
	public float fetchScore() {
		return insideLength * insideLength / (1.0f + stopcountinside + 5.0f * shiftcountinside);
	} // end of fetchScore()
	
} // end of Putein
