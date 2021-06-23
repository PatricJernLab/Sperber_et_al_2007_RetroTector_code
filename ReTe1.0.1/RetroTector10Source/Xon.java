/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 12/10 -06
* Beautified 12/10 -06
*/
package retrotector;

import builtins.*;
import java.util.*;
import java.io.*;

/**
* Special case of AcidSequence created without the benefit of MotifHits or Alignment.
*/
public class Xon extends AcidSequence {

/**
* ="Shifts" .
*/
	public static final String SHIFTSKEY = "Shifts";
	
/**
* ="Xon" .
*/
	public static final String XONKEY = "Xon";
	
/**
* ="Stops" .
*/
	public static final String STOPSKEY = "Stops";
	
/**
* ="Length" .
*/
	public static final String LENGTHKEY = "Length";
	

/**
* The internal positions in the DNA of the acids.
*/
	public int[ ] acidPositions;
	
/**
* Used by XonID to mark that this Xon has not been discarded yet.
*/
	public boolean eligible = true;
	
/**
* Constructor. Just transfers to Putein constructor.
* @param	par	An ORFID.ParameterBlock.
*/
	public Xon(ORFID.ParameterBlock par) {
		super(par);
	} // end of constructor(ORFID.ParameterBlock)
	
/**
* Constructor.
* @param	inFile	A File with contents according to ParameterStream.
* @param	pBlock	ORFID.ParameterBlock with parameters.
*/
	public Xon(File inFile, ORFID.ParameterBlock pBlock) throws RetroTectorException {
		super(inFile, pBlock);
	} // end of constructor (File, ORFID.ParameterBlock)

/**
* Constructor for a specific Xon.
* @param	initpos					The position in DNA to start at.
* @param	termpos					The position in DNA to end at.
* @param	par							An ORFID.ParameterBlock, of the complete type.
* @param	faultThreshold	Max acceptable stops + 5*shifts per 51 acids.
*/
	public Xon(int initpos, int termpos, ORFID.ParameterBlock par, float faultThreshold) throws RetroTectorException {
		super(par);
		Utilities.Rectangle r = new Utilities.Rectangle(initpos, 0, termpos - initpos, 1);
// let ExonDynamicMatrix do the work
		ExonDynamicMatrix mat;
		try {
			mat = new ExonDynamicMatrix(r, PBLOCK);
		} catch (FinishedException fe) {
			throw new RetroTectorException("Xon", "End of DNA reached");
		}
		mat.getPath().pushTo(this);
		finishSequence(-Integer.MAX_VALUE, Integer.MAX_VALUE);
		if ((stopcountinside + 5 * shiftcountinside) * faultThreshold > insideLength) {
			throw new RetroTectorException("Xon", "Too many stops and shifts");
		}

// build acidPositions
		acidPositions = new int[insideLength];
		int po = 0;
		for (int i=0; i<path.length; i++) {
			if ((path[i].TODNA == path[i].FROMDNA + 3) & (path[i].FROMDNA >= estimatedFirst) & (path[i].TODNA <= estimatedLast)) {
				try {
					acidPositions[po] = path[i].FROMDNA;
				} catch (ArrayIndexOutOfBoundsException aie) {
					throw aie;
				}
				po++;
			}
		}

// check for crashing
		for (int a=0; a<PBLOCK.AVOIDPOSITIONS.length; a++) {
			int b = Utilities.puteinPositionsCompare(acidPositions, PBLOCK.AVOIDPOSITIONS[a]);
			if (b > 0) {
				throw new RetroTectorException("Xon", "Putein crash");
			}
		}
		
		estimatedFirst = path[0].FROMDNA;
		estimatedLast = path[path.length - 1].TODNA - 1;
	} // end of constructor(int, int, ORFID.ParameterBlock, float)
	
/**
* Adds information about this Xon to an ExecutorScriptWriter.
* @param	puteinWriter	The ExecutorScriptWriter.
*/
	public final void toFile(ExecutorScriptWriter writer) throws RetroTectorException {
		
		writer.writeSingleParameter(Putein.ESTIMATEDSTARTPOSITIONKEY, String.valueOf(PBLOCK.TARGETDNA.externalize(estimatedFirst)), false);
		writer.writeSingleParameter(Putein.ESTIMATEDLASTPOSITIONKEY, String.valueOf(PBLOCK.TARGETDNA.externalize(estimatedLast)), false);
		writer.writeSingleParameter(LENGTHKEY, String.valueOf(insideLength), false);
		writer.writeSingleParameter(SHIFTSKEY, String.valueOf(shiftcountinside), false);
		writer.writeSingleParameter(STOPSKEY, String.valueOf(stopcountinside), false);
		writer.startMultiParameter(XONKEY, false);
		writer.appendToMultiParameter(mainLines[MOTIFLINEINDEX], false);
		writer.appendToMultiParameter(mainLines[PUTEINLINEINDEX], false);
		writer.appendToMultiParameter(mainLines[FRAMELINEINDEX], false);
		writer.appendToMultiParameter(mainLines[FOURTHLINEINDEX], false);
		writer.appendToMultiParameter(mainLines[POSLINEINDEX], false);
		writer.finishMultiParameter(false);
	} // end of toFile(ExecutorScriptWriter)
	
/**
* @return	A string no longer than 6, with (external) starting position.
*/
	public final String positionString() {
		String sacc = String.valueOf(PBLOCK.TARGETDNA.externalize(estimatedFirst));
		if (sacc.length() > 6) {
			sacc = sacc.substring(sacc.length() - 6);
		}
		return sacc;
	} // end of positionString()
	
/**
* Constructs path in pathStack when acidPositions is known.
*/
	protected void positionsToPathStack() throws RetroTectorException {
		pathStack = new Stack();
		int po = acidPositions[0];
		for (int p=0; p<acidPositions.length-1; p++) {
			pushElement(new PathElement(acidPositions[p], acidPositions[p] + 3, 0, 0, 0, 0));
			po += 3;
			while (po < acidPositions[p + 1] - 1) {
				pushElement(new PathElement(po, po + 2, 0, 0, 0, 0));
				po += 2;
			}
			while (po < acidPositions[p + 1]) {
				pushElement(new PathElement(po, po + 1, 0, 0, 0, 0));
				po ++;
			}
		}
		pushElement(new PathElement(acidPositions[acidPositions.length - 1], acidPositions[acidPositions.length - 1] + 3, 0, 0, 0, 0));
	} // end of positionsToPathStack()
		
/**
* As required by Scorable.
* @return	insideLength * insideLength / (1.0f + stopcountinside + 5.0f * shiftcountinside).
*/
	public float fetchScore() {
		return insideLength * insideLength / (1.0f + stopcountinside + 5.0f * shiftcountinside);
	} // end of fetchScore
	
} // end of Xon
