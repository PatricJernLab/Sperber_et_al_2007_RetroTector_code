/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 22/9 -06
* Beautified 22/9 -06
*/

package retrotector;

import java.io.*;
import java.util.*;

/**
* Modifier sublass representing proximity to stop codons.
*/
public class StopCodonModifier extends Modifier {

/**
* Defines the influence of stop codons on nearby scores. Element i represents effect
* at base distance +-3i.
*/
	static final float[ ] STOPPROFILE;
	
// parameters defining extent of stop codon influence
	private static final int STOPCODONKNEEAT = 40;  // distance (bases) to break in curve
	private static final float KNEEFACTOR = 0.9f; // amplitude at break in curve
	public static final int STOPCODONRANGE = 90; // half width (bases) of curve
	
// constructs	STOPPROFILE
	static {
		STOPPROFILE = new float[STOPCODONRANGE / 3];
		for (int i=0; i * 3 < STOPCODONRANGE; i++) {
			if (i * 3 < STOPCODONKNEEAT) {
				STOPPROFILE[i] = (1.0f - i * 3 * (1.0f - KNEEFACTOR) / STOPCODONKNEEAT);
			} else {
				STOPPROFILE[i] = KNEEFACTOR * (1.0f - (i * 3.0f - STOPCODONKNEEAT) / (STOPCODONRANGE - STOPCODONKNEEAT));
			}
		}
	} // end of static initializer
	
	private final float[ ] modifiers;
	private final float LOWERLIMIT;
	private final float UPPERLIMIT;
	private final float AVERAGE;
	private final DNA PARENTDNA;
	private int[ ] stops;

/**
* Constructor.
* @param	theDNA	DNA this relates to.
*/
 	public StopCodonModifier(DNA theDNA) throws RetroTectorException {
 		PARENTDNA = theDNA;
 		FRAMED = true;

 		modifiers = new float[theDNA.LENGTH];
 		stops = theDNA.getStopCodons();

 		int pos;
 		int p;
 		for (int i=0; i<stops.length; i++) {
 			pos = stops[i];
			modifiers[pos] += STOPPROFILE[0];
			for (int diff=1; diff<STOPPROFILE.length; diff++) {
				p = pos - 3 * diff;
				if (p >= 0) {
					modifiers[p] += STOPPROFILE[diff];
				}
				p = pos + 3 * diff;
				if (p<modifiers.length) {
					modifiers[p] += STOPPROFILE[diff];
				}
			}
		}
 		float lolim = Float.MAX_VALUE;
 		float hilim = -Float.MAX_VALUE;
 		float sum = 0.0f;
 		int n = 0;
 		for (int po=0; po<modifiers.length; po++) {
 			lolim = Math.min(lolim, modifiers[po]);
 			hilim = Math.max(hilim, modifiers[po]);
 			sum += modifiers[po];
 			n++;
 		}
 		AVERAGE = sum / n;
 		LOWERLIMIT = lolim;
 		UPPERLIMIT = hilim;
 	} // end of constructor(DNA)
 	
/**
* @param	position	An (internal) position in DNA.
* @return	The modifier value at position.
*/
	public final float modification(int position) {
		return modifiers[position];
	} // end of modification(int)

/**
* @param	position		An (internal) position in DNA.
* @param	firstlimit	Stop codons before this (internal) position are not used.
* @param	lastlimit		Stop codons after this (internal) position are not used.
* @return	The modifier value at position.
*/
	public final float stopCodonModifierAt(int position, int firstlimit, int lastlimit) {
		if ((firstlimit < (position - STOPCODONRANGE)) & (lastlimit > (position + STOPCODONRANGE))) {
			return modifiers[position];
		}
		if ((firstlimit > (position + STOPCODONRANGE)) | (lastlimit < (position - STOPCODONRANGE))) {
			return 0;
		}
		int firstIndex = Arrays.binarySearch(stops, Math.max(position - STOPCODONRANGE, firstlimit));
		if (firstIndex < 0) {
			firstIndex = -firstIndex - 1;
		}
		float result = 0;
		int diff;
		for (int i=firstIndex; (i<stops.length) && (stops[i]<=lastlimit); i++) {
			diff = position - stops[i];
			if ((diff % 3) == 0) {
				diff = Math.abs(diff / 3);
				if (diff < STOPPROFILE.length) {
					result += STOPPROFILE[diff];
				}
			}
		}
		return result;
	} // end of stopCodonModifierAt(int, int, int)
	
/**
* @return	Lowest modification value.
*/
	public final float getLowerLimit() {
		return LOWERLIMIT;
	} // end of getLowerLimit()

/**
* @return	Highest modification value.
*/
	public final float getUpperLimit() {
		return UPPERLIMIT;
	} // end of getUpperLimit()


/**
* @return	Average modification value over whole DNA.
*/
	public final float getAverage() {
		return AVERAGE;
	} // end of getAverage()

/**
* @return	The DNA to which this relates.
*/
	public final DNA getDNA() {
		return PARENTDNA;
	} // end of getDNA()

/**
* @return	A complete array of modification values.
*/
  public final float[ ] getArray() {
    return (float[ ]) modifiers.clone();
  } // end of getArray()

} // end of StopCodonModifier
