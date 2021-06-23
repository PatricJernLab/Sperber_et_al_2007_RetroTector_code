/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 21/9 -06
* Beautified 21/9 -06
*/

package retrotector;

import java.io.*;
import java.util.*;

/**
* Modifier for the 'CpG-islandity', calculated as ([c] + [g]) * [cg] / ([c] * [g]) within
* ±100 bases.
*/
public class CpGModifier extends Modifier {

/**
* Half minimum width of a CpG island = 100.
*/
	public static final int HALFISLANDWIDTH = 100;
	
	private final float[ ] modifiers;
	private final float LOWERLIMIT;
	private final float UPPERLIMIT;
	private final float AVERAGE;
	private final DNA PARENTDNA;

/**
* Constructor.
* @param	theDNA	DNA this relates to.
*/
	public CpGModifier(DNA theDNA) throws RetroTectorException {
 		PARENTDNA = theDNA;
 		FRAMED = false;
 		modifiers = new float[theDNA.LENGTH];
		
		if (theDNA.LENGTH < (HALFISLANDWIDTH + 2)) {
			AVERAGE = 0;
			LOWERLIMIT = 0;
			UPPERLIMIT = 0;
			return;
		}
			
 		
 		int ccode = Compactor.BASECOMPACTOR.charToIntId('c');
 		int gcode = Compactor.BASECOMPACTOR.charToIntId('g');
 		
// nr of c within +-HALFISLANDWIDTH
 		int ccount = 0;
// nr of g within +-HALFISLANDWIDTH
 		int gcount = 0;
// nr of cg pairs within +-HALFISLANDWIDTH
 		float cgcount = 0;
 		
 		int base;
// count c and g at start
 		for (int j=0; j<=Math.min(HALFISLANDWIDTH*2, theDNA.LENGTH-1); j++) {
 			base = theDNA.get2bit(j);
 			if (base == ccode) {
 				ccount++;
 				if (theDNA.get2bit(j + 1) == gcode) {
 					cgcount++;
 				}
 			} else if (base == gcode) {
 				gcount++;
 			}
 		}
 		for (int pos = HALFISLANDWIDTH; pos < theDNA.LENGTH - HALFISLANDWIDTH - 2; pos++) {
// calculate modifier at centre of interval
      if (ccount * gcount == 0) {
        modifiers[pos] = 0.0f;
      } else {
        modifiers[pos] = (gcount + ccount) * cgcount / (ccount * gcount);
      }
// update counters for new interval
 			base = theDNA.get2bit(pos - HALFISLANDWIDTH);
 			if (base == ccode) {
 				ccount--;
 				if (theDNA.get2bit(pos - HALFISLANDWIDTH + 1) == gcode) {
 					cgcount--;
 				}
 			}
 			if (base == gcode) {
 				gcount--;
 			}
 			base = theDNA.get2bit(pos + HALFISLANDWIDTH + 1);
 			if (base == ccode) {
 				ccount++;
  			if (theDNA.get2bit(pos + HALFISLANDWIDTH + 2) == gcode) {
 					cgcount++;
 				}
 			}
 			if (base == gcode) {
 				gcount++;
			}
 		}
    
 		float lolim = Float.MAX_VALUE;
 		float hilim = -Float.MAX_VALUE;
 		float sum = 0.0f;
 		int n = 0;
 		float ff;
 		for (int po=0; po<modifiers.length; po++) {
 			ff = modifiers[po];
 			if (!Float.isNaN(ff)) {
	 			lolim = Math.min(lolim, ff);
	 			hilim = Math.max(hilim, ff);
	  		sum += ff;
	 			n++;
	 		}
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
	
}