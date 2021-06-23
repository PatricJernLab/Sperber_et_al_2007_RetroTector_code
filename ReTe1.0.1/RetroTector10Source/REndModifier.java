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
* Modifier sublass for proximity to sequences known to precede end of R in LTR.
* Very similar to PolyASiteModifier.
*/
public class REndModifier extends Modifier {

	private static class Marker {
	
		final String MARKER;
		final int DISTANCE;
		
		Marker(String marker, int distance) {
			MARKER = marker;
			DISTANCE = distance;
		} // end of Marker.constructor(String, int)
		
	} // end of Marker


/**
* Profile of influence of hit at distances. Triangular with width = 19 and height 10.
*/
	static float[ ] weightProfile;
	static Marker[ ] markers;

	static {
		weightProfile = new float[10];
		for (int ff=0; ff<10; ff++) {
			weightProfile[ff] = (float) (10 - ff);
		}
		markers = new Marker[19];
		markers[0] = new Marker("ccttt", 14);
		markers[1] = new Marker("tgtt", 6);
		markers[2] = new Marker("ttgc", 9);
		markers[3] = new Marker("gctgt", 13);
		markers[4] = new Marker("tctct", 9);
		markers[5] = new Marker("gcttt", 14);
		markers[6] = new Marker("ctttt", 12);
		markers[7] = new Marker("tgc", 7);
		markers[8] = new Marker("ctcctt", 1);
		markers[9] = new Marker("gctt", 10);
		markers[10] = new Marker("tgttg", 7);
		markers[11] = new Marker("ctc", 3);
		markers[12] = new Marker("gcctt", 8);
		markers[13] = new Marker("gattt", 9);
		markers[14] = new Marker("tttg", 14);
		markers[15] = new Marker("ccttg", 8);
		markers[16] = new Marker("ggtat", 10);
		markers[17] = new Marker("gagtat", 11);
		markers[18] = new Marker("tttt", 16);
	} // end of static initializer

	
// array of modifier values
	private final float[ ] modifiers;
	private final float LOWERLIMIT;
	private final float UPPERLIMIT;
	private final float AVERAGE;
	private final DNA PARENTDNA;

	private final void doOne(Marker m) throws RetroTectorException {
		byte[ ] pattern = new byte[m.MARKER.length()];
		for (int i=0; i<pattern.length; i++) {
			pattern[i] = (byte) Compactor.BASECOMPACTOR.charToIntId(m.MARKER.charAt(i));
		}

		AgrepContext context = new AgrepContext(pattern, 0, PARENTDNA.LENGTH - pattern.length, 0);
		int res = -1;
		int p = -1;
		int diff = -1;
		while ((res = PARENTDNA.agrepFind(context)) != Integer.MIN_VALUE) {
			p = Math.abs(res) + m.DISTANCE;
			if ((p >= 0) & (p<modifiers.length)) {
				modifiers[p] += weightProfile[0];
			}
			for (diff=1; diff<weightProfile.length; diff++) {
				p = Math.abs(res) + m.DISTANCE - diff;
				if ((p >= 0) & (p<modifiers.length)) {
					modifiers[p] += weightProfile[diff];
				}
				p = Math.abs(res) + m.DISTANCE + diff;
				if ((p >= 0) & (p<modifiers.length)) {
					modifiers[p] += weightProfile[diff];
				}
			}
		}
	} // end of doOne(Marker)
		
	
/**
* Constructor.
* @param	theDNA	DNA to which this relates.
*/
 	public REndModifier(DNA theDNA) throws RetroTectorException {
 		PARENTDNA = theDNA;
 		FRAMED = false;
 		modifiers = new float[theDNA.LENGTH];
		for (int s=0; s<markers.length; s++) {
			doOne(markers[s]);
		}
		modifiers[0] = 0;
		int acode = Compactor.BASECOMPACTOR.charToIntId('a');
		int ccode = Compactor.BASECOMPACTOR.charToIntId('c');
		for (int i=1; i<modifiers.length; i++) {
			modifiers[i] += 1;
			if (PARENTDNA.get2bit(i) == acode) {
				if (PARENTDNA.get2bit(i - 1) == ccode) {
					modifiers[i] *= 4;
				} else {
					modifiers[i] *= 2;
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
	
} // end of REndModifier
