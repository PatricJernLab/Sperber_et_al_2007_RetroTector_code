/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 23/9 -06
* Beautified 23/9 -06
*/

package retrotector;

import java.io.*;
import java.util.*;

/**
* Modifier sublass for proximity to transcription factor binding sites.
*/
public class TransSiteModifier extends Modifier {

/**
* Name of Transsites distances file = "Transsites.txt".
*/
	public static final String TRANSSITESFILENAME = "Transsites.txt";
	
/**
* Name of Transsites distances parameter = "Sites".
*/
	public static final String TRANSSITESKEY = "Sites";
	
	
/**
* 1, 2, 4...
*/
	static final int[ ] BITMASKS = new int[32];
	
/**
* 1l, 2l, 4l...
*/
	static final long[ ] LBITMASKS = new long[64];

/**
* Possible bases corresponding to base codes according to BaseMatrixData.
*/
	static final byte[ ][ ] AMBIGS = {
				{0}, //t
				{1}, //c
				{2}, //g
				{3}, // a
				{0, 3}, //w
				{0, 1}, //y
				{2, 3}, //r
				{1, 3}, //m
				{0, 2}, //k
				{0, 1, 2, 3}, //n
				{1, 2}, //s
				{1, 2, 3}, //v
				{0, 2, 3}, //d
				{0, 1, 3}, //h
				{0, 1, 2} //b
				};

/**
* Profile of influence of hit at distances. Gaussian with SD = 50.
*/
	static float[ ] weightProfile;

// SD of weightProfile
	private static int sd = 50;

	
	static {
		BITMASKS[0] = 1;
		for (int b=1; b<32; b++) {
			BITMASKS[b] = BITMASKS[b - 1] << 1;
		}
		LBITMASKS[0] = 1;
		for (int b=1; b<64; b++) {
			LBITMASKS[b] = LBITMASKS[b - 1] << 1;
		}
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

// array of possible bases in each position
	private byte[ ][ ] baseids;
	
// one possible pattern
	private byte[ ] pattern;
	
// to keep track of how many alternatives have been used
	private int[ ] counters;
	
// length of current pattern
	private int length;
	
// contents of Sites parameter
	private String[ ] siteStrings;

// makes next possible pattern, starting variation at position index
// returns true if this was not possible
	private final boolean fillPattern(int index) {
		try {
			pattern[index] = baseids[index][counters[index]];
		} catch (NullPointerException e) {
Utilities.debugPrint(this, e.toString());
		}
		if (index < (length - 1)) {
			if (fillPattern(index + 1)) {
				counters[index]++;
				if (counters[index] >= baseids[index].length) {
					counters[index] = 0;
					return true;
				} else {
					return false;
				}
			}
		} else {
			return true;
		}
		return true;
	} // end of fillPattern(int)
	
/**
* Runs match for all variants of this site.
* @param	dna				The DNA to search in.
* @param	from			Position in DNA to start search at.
* @param	to				Position in DNA to end search at.
* @param	hits			String array, normally of length (to - from + 1), to mark hits in.
* @param	faults		Max number of mismatches allowed.
* @return	Number of individual hits found.
*/
	private final int doMatches(DNA dna, int from, int to, String[ ] hits, int faults) {
		pattern = new byte[length];
		counters = new int[length];
		int count = 0;
		for (;;) {
			boolean b = fillPattern(0);
			count += Utilities.match(dna, from, to, pattern, hits, faults);
			if (b) {
				return count;
			}
		}
	} // end of doMatches(DNA, int, int, String[ ], int)

/**
* Runs match for all variants of this site. Max number of mismatches allowed = length / 10.
* @param	dna				The DNA to search in.
* @param	from			Position in DNA to start search at.
* @param	from			Position in DNA to end search at.
* @param	hits			Array to mark hits in.
* @return	Number of individual hits found.
*/
	private final int doMatches(DNA dna, int from, int to, String[ ] hits) {
		return doMatches(dna, from, to, hits, length / 10);
	} // end of doMatches(DNA, int, int, String[ ])
	
	
/**
* Runs match for all variants of this site.
* @param	dna				The DNA to search in.
* @param	hits			Array, normally of same length as dna, to mark hits in.
* @param	faults		Max number of mismatches allowed.
* @return	Number of individual hits found.
*/
	private final int doMatches(DNA dna, float[ ] hits, int faults) {
		pattern = new byte[length];
		counters = new int[length];
		int count = 0;
		for (;;) {
			boolean b = fillPattern(0);
			count += Utilities.match(dna, pattern, hits, faults, weightProfile);
			if (b) {
				return count;
			}
		}
	} // end of doMatches(DNA, float[ ], int)
	
/*
* Like above, faults is set automatically to LENGTH/10.
* @param	dna				The DNA to search in.
* @param	hits			Array, normally of same length as dna, to mark hits in.
* @return	Number of individual hits found.
*/
	private final int doMatches(DNA dna, float[ ] hits) {
		return doMatches(dna, hits, length / 10);
	} // end of doMatches(DNA, float[ ])
	
/**
* Constructor.
* @param	theDNA	DNA to which this relates.
*/
 	public TransSiteModifier(DNA theDNA) throws RetroTectorException {
 		PARENTDNA = theDNA;
 		FRAMED = false;
 		modifiers = new float[theDNA.LENGTH];
 		File f = RetroTectorEngine.getCurrentDatabase().getFile(TRANSSITESFILENAME);
 		Hashtable h = new Hashtable();
 		ParameterFileReader pfr = new ParameterFileReader(f, h);
 		pfr.readParameters();
 		pfr.close();
 		siteStrings = (String[ ]) h.get(TRANSSITESKEY);
 		Compactor baseCompactor = Compactor.BASECOMPACTOR;

		String s;
		char c;
		int ww;

		for (int line=0; line<siteStrings.length; line++) {
			s = siteStrings[line].substring(5).trim(); // pick contents part of line
			length = s.length();
			baseids = new byte[length][ ];
			pattern = new byte[length];
			counters = new int[length];
			for (int j=0; j<s.length(); j++) {
				c = s.charAt(j);
				if ((c == 'u') | (c == 'U')) {
					ww = 3;
				} else {
					ww = baseCompactor.charToIntId(s.charAt(j));
				}
				baseids[j] = AMBIGS[ww];
			}
			doMatches(theDNA, modifiers);
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
* Runs match for all variants of this site. Max number of mismatches allowed = length / 10.
* @param	dna				The DNA to search in.
* @param	from			Position in DNA to start search at.
* @param	to				Position in DNA to end search at.
* @param	hits			String array, normally of length (to - from + 1), to mark hits in.
* @return	Number of individual hits found.
*/
	public final void searchMatches(DNA dna, int from, int to, String[ ] hits) {
		String s;
		char c;
		int ww;

		for (int line=0; line<siteStrings.length; line++) {
			s = siteStrings[line].substring(5).trim(); // pick contents part of line
			length = s.length();
			baseids = new byte[length][ ];
			pattern = new byte[length];
			counters = new int[length];
			for (int j=0; j<s.length(); j++) {
				c = s.charAt(j);
				if ((c == 'u') | (c == 'U')) {
					ww = 3;
				} else {
					ww = Compactor.BASECOMPACTOR.charToIntId(s.charAt(j));
				}
				baseids[j] = AMBIGS[ww];
			}
			doMatches(dna, from, to, hits);
		}
	} // end of searchMatches((DNA, int, int, String[ ])
	
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
	
} // end of TransSiteModifier
