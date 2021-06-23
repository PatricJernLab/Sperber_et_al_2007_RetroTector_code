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
* Modifier for proximity to hexamers rare in ORFs.
*/
public class NonORFHexamerModifier extends Modifier {

	public static final String NONORFHEXAMERSFILENAME = "NonORFHexamers.txt"; // Name of file
	
	public static final String NONORFHEXAMERSKEY = "NonORFHexamers"; // Name of parameter

/**
* Profile of influence of hit at distances. Gaussian with SD = 17 codons.
*/
	static float[ ] weightProfile;

// SD of weightProfile
	private static int sd = 17;

	
	static {
		int f = (int) (sd * Math.sqrt(2 * Math.log(100)));
		weightProfile = new float[f + 1];
		for (int ff=0; ff<=f; ff++) {
			weightProfile[ff] = (float) Math.exp(-ff * ff * 0.5f / (sd * sd));
		}
	} // end of static initializer

	
// array of modifier values
	private final float[ ] modifiers;
	private final float LOWERLIMIT;
	private final float UPPERLIMIT;
	private final float AVERAGE;
	private final DNA PARENTDNA;

	
/**
* Constructor.
* @param	theDNA	DNA to which this relates.
*/
 	public NonORFHexamerModifier(DNA theDNA) throws RetroTectorException {
 		PARENTDNA = theDNA;
 		FRAMED = true;
 		modifiers = new float[theDNA.LENGTH];
 		File f = RetroTectorEngine.getCurrentDatabase().getFile(NONORFHEXAMERSFILENAME);
 		Hashtable h = new Hashtable();
 		ParameterFileReader pfr = new ParameterFileReader(f, h);
 		pfr.readParameters();
 		pfr.close();
 		String[ ] ss = (String[ ]) h.get(NONORFHEXAMERSKEY);
 		Compactor baseCompactor = Compactor.BASECOMPACTOR;

		String s;
		int firstcodon;
		int lastcodon;
		int p;

		for (int line=0; line<ss.length; line++) {
			s = ss[line].substring(0, 7).trim(); // pick contents part of line
			firstcodon = (baseCompactor.charToIntId(s.charAt(0))  << 4) |
											(baseCompactor.charToIntId(s.charAt(1))  << 2) |
											baseCompactor.charToIntId(s.charAt(2));
			lastcodon = (baseCompactor.charToIntId(s.charAt(3))  << 4) |
											(baseCompactor.charToIntId(s.charAt(4))  << 2) |
											baseCompactor.charToIntId(s.charAt(5));
			for (int pos=0; pos<(theDNA.LENGTH - 6); pos++) {
				if ((theDNA.get6bit(pos) == firstcodon) && (theDNA.get6bit(pos + 3) == lastcodon)) {
					modifiers[pos] += weightProfile[0];
					for (int diff=1; diff<weightProfile.length; diff++) {
						p = pos - 3 * diff;
						if (p >= 0) {
							modifiers[p] += weightProfile[diff];
						}
						p = pos + 3 * diff;
						if (p<modifiers.length) {
							modifiers[p] += weightProfile[diff];
						}
					}
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
	
} // end of NonORFHexamerModifier
