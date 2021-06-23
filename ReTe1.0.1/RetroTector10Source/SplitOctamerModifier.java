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
* Modifier for proximity to split octamers common in LTR.
*/
public class SplitOctamerModifier extends Modifier {

/**
* Name of file with split octamers = "Splitoct.txt".
*/
	public  final static String OCTAMERFILENAME = "Splitoct.txt";
	
/**
* Name of parameter with split octamers = "Octamers".
*/
	public  final static String OCTAMERSKEY = "Octamers";
	
/**
* Profile of influence of hit at distances. Gaussian with SD = 50.
*/
	static float[ ] weightProfile;
	
// SD of weightProfile
	private static int sd = 50;
	
	static {
		int f = (int) (sd * Math.sqrt(2 * Math.log(100)));
		weightProfile = new float[f + 1];
		for (int ff=0; ff<=f; ff++) {
			weightProfile[ff] = (float) Math.exp(-ff * ff * 0.5f / (sd * sd));
		}
	}
	
// array of modifier values
	private final float[ ] modifiers;
	private final float LOWERLIMIT;
	private final float UPPERLIMIT;
	private final float AVERAGE;
	private final DNA PARENTDNA;

/**
* Constructor.
* @param	theDNA		DNA this relates to.
* @param	minratio	Low limit for score ratio.
*/
 	public SplitOctamerModifier(DNA theDNA, float minratio) throws RetroTectorException {
 		PARENTDNA = theDNA;
 		FRAMED = false;
 		modifiers = new float[theDNA.LENGTH];
 		File f = RetroTectorEngine.getCurrentDatabase().getFile(OCTAMERFILENAME);
 		Hashtable h = new Hashtable();
 		ParameterFileReader pfr = new ParameterFileReader(f, h);
 		pfr.readParameters();
 		pfr.close();
 		String[ ] ss = (String[ ]) h.get(OCTAMERSKEY);
 		Compactor baseCompactor = Compactor.BASECOMPACTOR;

 		int firstcodon;
 		int firstbase;
 		int lastcodon;
 		int lastbase;
 		int mindist;
 		int maxdist;
 		for (int line=0; line<ss.length; line++) {
			String[ ] lines = Utilities.splitString(ss[line]);
// throw out low-scoring and repetitive octamers
			if ((Utilities.decodeFloat(lines[4]) >= minratio) & !lines[0].equals(lines[1])) {
				if ((lines[0].length() != 4) | (lines[1].length() != 4)) {
					lineError(lines[line]);
				}
				for (int i=0; i<4; i++) {
					if (baseCompactor.charToIntId(lines[0].charAt(i)) < 0) {
						lineError(lines[line]);
					}
					if (baseCompactor.charToIntId(lines[1].charAt(i)) < 0) {
						lineError(lines[line]);
					}
				}
				firstcodon = (baseCompactor.charToIntId(lines[0].charAt(0))  << 4) |
											(baseCompactor.charToIntId(lines[0].charAt(1))  << 2) |
											baseCompactor.charToIntId(lines[0].charAt(2));
				firstbase = baseCompactor.charToIntId(lines[0].charAt(3));
				lastcodon = (baseCompactor.charToIntId(lines[1].charAt(0))  << 4) |
											(baseCompactor.charToIntId(lines[1].charAt(1))  << 2) |
											baseCompactor.charToIntId(lines[1].charAt(2));
				lastbase = baseCompactor.charToIntId(lines[1].charAt(3));
				mindist = Utilities.decodeInt(lines[2]);				
				maxdist = Utilities.decodeInt(lines[3]);
				boolean found;	
				for (int pos=0; pos<(theDNA.LENGTH - maxdist - 9); pos++) {
					if ((theDNA.get6bit(pos) == firstcodon) & (theDNA.get2bit(pos + 3) == firstbase)) {
						found = false;
						for (int pos2=pos + 4 + mindist; !found && (pos2<=pos + 4 + maxdist) && ((pos2+3)<theDNA.LENGTH); pos2++) {
							if ((theDNA.get6bit(pos2) == lastcodon) & (theDNA.get2bit(pos2 + 3) == lastbase)) {
								int pos3 = pos; // hot spot.
								int p;
								modifiers[pos3] += weightProfile[0];
								for (int diff=1; diff<weightProfile.length; diff++) {
									p = pos3 - diff;
									if (p >= 0) {
										modifiers[p] += weightProfile[diff];
									}
									p = pos3 + diff;
									if (p<modifiers.length) {
										modifiers[p] += weightProfile[diff];
									}
								}
								found = true;
							}
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
 	} // end of constructor(DNA float)
 	
	private final void lineError(String line) throws RetroTectorException {
		RetroTectorException.sendError(this, "Line syntax error", line);
	} //end of lineError(String)
	
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
	
} // end of SplitOctamerModifier
