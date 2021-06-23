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
* Modifier sublass representing proximity to N-glycosylation sites.
*/
public class GlycSiteModifier extends Modifier {

/**
* Defines the influence of N-glycosylation sites on nearby scores. Element i represents effect
* at base distance +-3i.
*/
	static final float[ ] GLYCPROFILE;
	
// parameters defining extent of stop codon influence
	private static final int GLYCSITEKNEEAT = 40;
	private static final float KNEEFACTOR = 0.9f;
	private static final int GLYCSITERANGE = 90;
	
// constructs GLYCPROFILE
	static {
		GLYCPROFILE = new float[GLYCSITERANGE / 3];
		for (int i=0; i * 3 < GLYCSITERANGE; i++) {
			if (i * 3 < GLYCSITEKNEEAT) {
				GLYCPROFILE[i] = (1.0f - i * 3 * (1.0f - KNEEFACTOR) / GLYCSITEKNEEAT);
			} else {
				GLYCPROFILE[i] = KNEEFACTOR * (1.0f - (i * 3.0f - GLYCSITEKNEEAT) / (GLYCSITERANGE - GLYCSITEKNEEAT));
			}
		}
	} // end of static initializer
	
	private final float[ ] modifiers;
	private final float LOWERLIMIT;
	private final float UPPERLIMIT;
	private final float AVERAGE;
	private final DNA PARENTDNA;

/**
* Constructor.
* @param	theDNA	DNA this relates to.
*/
 	public GlycSiteModifier(DNA theDNA) throws RetroTectorException {
 		PARENTDNA = theDNA;
 		FRAMED = true;

 		modifiers = new float[theDNA.LENGTH];
 		int[ ] glycs = theDNA.getGlycosyls();

 		int pos;
 		int p;
 		for (int i=0; i<glycs.length; i++) {
 			pos = glycs[i];
			modifiers[pos] += GLYCPROFILE[0];
			for (int diff=1; diff<GLYCPROFILE.length; diff++) {
				p = pos - 3 * diff;
				if (p >= 0) {
					modifiers[p] += GLYCPROFILE[diff];
				}
				p = pos + 3 * diff;
				if (p<modifiers.length) {
					modifiers[p] += GLYCPROFILE[diff];
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
	
} // end of GlycSiteModifier
