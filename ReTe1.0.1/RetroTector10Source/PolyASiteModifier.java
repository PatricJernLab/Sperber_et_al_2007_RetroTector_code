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
* Modifier sublass for proximity to sequences known to precede end of R in LTR. Effectively not in use at present.
* Very similar to REndeModifier.
*/
public class PolyASiteModifier extends Modifier {

	public static final String POLYASITESFILENAME = "PolyaSites.txt"; // Name of PolyaSites motifs distances file
	
	public static final String POLYASITESKEY = "PolyASites"; // Name of PolyaSites motifs parameter

/**
* Profile of influence of hit at distances. Triangular with width = 19 and height = 10.
*/
	static float[ ] weightProfile;

	static {
		weightProfile = new float[10];
		for (int ff=0; ff<10; ff++) {
			weightProfile[ff] = (float) (10 - ff);
		}
	} // end of static initializer

	
// array of modifier values
	private final float[ ] modifiers;
	private final float LOWERLIMIT;
	private final float UPPERLIMIT;
	private final float AVERAGE;
	private final DNA PARENTDNA;

// processes one line from PolyaSites.txt
	private final void doOne(String line) throws RetroTectorException {
		line = line.trim();
		int ind = line.indexOf(" ");
		byte[ ] pattern = new byte[ind];
		for (int i=0; i<pattern.length; i++) {
			pattern[i] = (byte) Compactor.BASECOMPACTOR.charToIntId(line.charAt(i));
		}
		int offset = Utilities.decodeInt(line.substring(ind).trim());

		AgrepContext context = new AgrepContext(pattern, 0, PARENTDNA.LENGTH - pattern.length, 0);
		int res = -1;
		int p = -1;
		int diff = -1;
		while ((res = PARENTDNA.agrepFind(context)) != Integer.MIN_VALUE) {
			modifiers[Math.abs(res) + offset] += weightProfile[0];
			for (diff=1; diff<weightProfile.length; diff++) {
				p = Math.abs(res) + offset - diff;
				if (p >= 0) {
					modifiers[p] += weightProfile[diff];
				}
				p = Math.abs(res) + offset + diff;
				if (p<modifiers.length) {
					modifiers[p] += weightProfile[diff];
				}
			}
		}
	} // end of doOne(String)
		
	
/**
* Constructor.
* @param	theDNA	DNA to which this relates.
*/
 	public PolyASiteModifier(DNA theDNA) throws RetroTectorException {
 		PARENTDNA = theDNA;
 		FRAMED = false;
 		modifiers = new float[theDNA.LENGTH];
 		File f = RetroTectorEngine.getCurrentDatabase().getFile(POLYASITESFILENAME);
 		Hashtable h = new Hashtable();
 		ParameterFileReader pfr = new ParameterFileReader(f, h);
 		pfr.readParameters();
 		pfr.close();
 		String[ ] siteStrings = (String[ ]) h.get(POLYASITESKEY);
		
		for (int s=0; s<siteStrings.length; s++) {
			doOne(siteStrings[s]);
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
	
} // end of PolyASiteModifier
