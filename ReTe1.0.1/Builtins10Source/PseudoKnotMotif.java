/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 1/10 -06
* Beautified 1/10 -06
*/
package builtins;

import retrotector.*;
import java.util.*;

/**
* Motif subclass for Motifs identifying potential pseudoknots.
* Builds on the pseudoknot model:
* stem1A, loop1, stem2a, interval, stem1b, loop2, stem2b
*<PRE>
*     Parameters:
*
*   Loop2MinA
* Lowest acceptable number of 'a' in loop 2.
* Default: 1.
*
*   Stem1MaxAT
* Highest acceptable number of 'a' or 't' in stem 1.
* Default: 2.
*</PRE>
*/
public class PseudoKnotMotif extends Motif {

/**
* A pseudoknot with specified stem positions.
* Score is <0 if criteria not met.
*/
	public class KnotTry {
	
/**
* = stem1Astart.
*/
		final int STEM1ASTART;
	
/**
* = stem1Aend.
*/
		final int STEM1AEND;
	
/**
* = stem2Astart.
*/
		final int STEM2ASTART;
	
/**
* = stem2Aend.
*/
		final int STEM2AEND;
	
/**
* = stem1Bstart.
*/
		final int STEM1BSTART;
	
/**
* = stem1Bend.
*/
		final int STEM1BEND;
	
/**
* = stem2Bstart.
*/
		final int STEM2BSTART;
	
/**
* = stem2Bend.
*/
		final int STEM2BEND;
		
/**
*  = STEM2BEND - STEM1ASTART + 1.
*/
		final int LENGTH;

/**
* <0 if unacceptable, otherwise based on complementarity of stems.
*/
		final float SCORE;
		
/**
* Constructor.
* @param	stem1Astart		Suggested internal position for start of stem1A.
* @param	stem1Aend			Suggested internal position for end of stem1A.
* @param	stem2Astart		Suggested internal position for start of stem2A.
* @param	stem2Aend			Suggested internal position for end of stem2A.
* @param	stem1Bstart		Suggested internal position for start of stem1B.
* @param	stem1Bend			Suggested internal position for end of stem1B.
* @param	stem2Bstart		Suggested internal position for start of stem2B.
* @param	stem2Bend			Suggested internal position for end of stem2B.
*/
		KnotTry(int stem1Astart, int stem1Aend, int stem2Astart, int stem2Aend, int stem1Bstart, int stem1Bend, int stem2Bstart, int stem2Bend) {
			STEM1ASTART = stem1Astart;
			STEM1AEND = stem1Aend;
			STEM2ASTART = stem2Astart;
			STEM2AEND = stem2Aend;
			STEM1BSTART = stem1Bstart;
			STEM1BEND = stem1Bend;
			STEM2BSTART = stem2Bstart;
			STEM2BEND = stem2Bend;
			LENGTH = STEM2BEND - STEM1ASTART + 1;
			
			int loop1length = STEM2ASTART - STEM1AEND - 1;
			int loop2length = STEM2BSTART - STEM1BEND - 1;
			int intervallength = STEM1BSTART - STEM2AEND - 1;
			int acount = 0;
// count a in loop 2
			for (int ai=STEM1BEND+1; ai<STEM2BSTART; ai++) {
				if (currentDNA.get2bit(ai) == acode) {
					acount++;
				}
			}
			int tacount = 0;
// count t and a in stem 1
			for (int n=STEM1ASTART; n<=STEM1AEND; n++) {
				if ((currentDNA.get2bit(n) == acode) | (currentDNA.get2bit(n) == tcode)) {
					tacount++;
				}
			}
			for (int n=STEM1BSTART; n<=STEM1BEND; n++) {
				if ((currentDNA.get2bit(n) == acode) | (currentDNA.get2bit(n) == tcode)) {
					tacount++;
				}
			}
			if (tacount > stem1MaxAT) {
				SCORE = -999;
			} else if ((loop1length < LOOP1MINLENGTH) | (loop1length > LOOP1MAXLENGTH)) {
				SCORE = -1000;
			} else if ((loop2length < LOOP2MINLENGTH) | (loop2length > LOOP2MAXLENGTH)) {
				SCORE = -1001;
			} else if ((intervallength < INTERVALMINLENGTH) | (intervallength > INTERVALMAXLENGTH)) {
				SCORE = -1002;
			} else if (acount < loop2MinA) {
				SCORE = -1005;
			} else {
				int i1 = complementscore(STEM1ASTART, STEM1AEND, STEM1BSTART, STEM1BEND);
				if (i1 >= 0) {
					int i2 = complementscore(STEM2ASTART, STEM2AEND, STEM2BSTART, STEM2BEND);
					if (i2 >= 0) {
						SCORE = i1 + i2;
					} else {
						SCORE = -1003;
					}
				} else {
					SCORE = -1004;
				}
			}
		} // end of constructor(int, int, int, int, int, int, int, int)
			
	} // end of KnotTry


/**
* Lowest acceptable length for stem 1 = 5.
*/
	public final static int STEM1MINLENGTH = 5;

/**
* Highest considered length for stem 1 = 6.
*/
	public final static int STEM1MAXLENGTH = 6;

/**
* Lowest acceptable length for loop 1 = 1.
*/
	public final static int LOOP1MINLENGTH = 1;

/**
* Highest considered length for loop 1 = 4.
*/
	public final static int LOOP1MAXLENGTH = 4;

/**
* Lowest acceptable length for stem 2 = 5.
*/
	public final static int STEM2MINLENGTH = 5;

/**
* Highest considered length for stem 2 = 8.
*/
	public final static int STEM2MAXLENGTH = 8;

/**
* Lowest acceptable length for interval = 0.
*/
	public final static int INTERVALMINLENGTH = 0;

/**
* Highest considered length for interval = 1.
*/
	public final static int INTERVALMAXLENGTH = 1;

/**
* Lowest acceptable length for loop 2 = 7.
*/
	public final static int LOOP2MINLENGTH = 7;

/**
* Highest considered length for loop 2 = 13.
*/
	public final static int LOOP2MAXLENGTH = 13;

/**
* Key for parameter defining lowest acceptable number of 'a' in loop 2 = "Loop2MinA".
* Default value is 1.
*/
	public final static String LOOP2MINAKEY = "Loop2MinA";

/**
* Key for parameter defining highest acceptable number of 'a' or 't' in stem 1 = "Stem1MaxAT".
* Default value is 2.
*/
	public final static String STEM1MAXATKEY = "Stem1MaxAT";

/**
* @return	String identifying this class = "PKN".
*/
	public final static String classType() {
    return "PKN";
  } // end of classType()
	
/**
* The KnotTry behind the most recently non-null MotifHit
* returned by getMotifHitAt.
*/
	public KnotTry latestTry = null;
	
/*
* Inversed and complemented codons.
*/
	static int[ ] inverseCodons = new int[64];
	
	static {
		for (int i=0; i<64; i++) {
			inverseCodons[i] = (3 - (i & 3)) * 16 + (3 - (i & 12) / 4) * 4 + (3 - (i & 48) / 16);
		}
	} // end of static initializer
	
  private int stem1MaxAT = 2;
  private int loop2MinA = 1;
  private DNA currentDNA;
			
  
/**
 * Constructs an instance, mainly from dataLine, which should be a line from Motifs.txt
 */
  public PseudoKnotMotif() throws RetroTectorException {
   	super(dataLine, motifDataBase);
    frameDefined = false;
		showAsBases = true;
    parameters = new Hashtable();
    parameters.put(STEM1MAXATKEY, "2");
    parameters.put(LOOP2MINAKEY, "1");
    getDefaults();
  } // end of constructor()
  
/**
* Reset Motif with new parameters.
* @param	theInfo	RefreshInfo with required information.
*/
  public final void refresh(RefreshInfo theInfo) throws RetroTectorException {
    currentDNA = theInfo.TARGETDNA;
		stem1MaxAT = getInt(STEM1MAXATKEY, 2);
		loop2MinA = getInt(LOOP2MINAKEY, 1);
  } // end of refresh(RefreshInfo)
  
/**
* Dummy.
*/
  public final void localRefresh(int firstpos, int lastpos) throws RetroTectorException {
  } // end of localRefresh(int, int)
  
/**
* @return	A Motif similar to this, refreshed but not applied.
*/
  public final Motif motifCopy() throws RetroTectorException {
    dataLine = SOURCESTRING;
    PseudoKnotMotif result = new PseudoKnotMotif();
    motifCopyHelp(result);
    return result;
  } // end of motifCopy()
	
	int ccode = Compactor.BASECOMPACTOR.charToIntId('c');
	int gcode = Compactor.BASECOMPACTOR.charToIntId('g');
	int tcode = Compactor.BASECOMPACTOR.charToIntId('t');
	int acode = Compactor.BASECOMPACTOR.charToIntId('a');

// checks complementarity between bases at m1 and m2
// full complementarity returns 2
// tg pair returns 1
	private final int fit(int m1, int m2) {
		m1 = currentDNA.get2bit(m1);
		m2 = currentDNA.get2bit(m2);
		if ((m1 + m2) == 3) {
			return 2;
		}
		if ((m1 == gcode) & (m2 == tcode)) {
			return 1;
		}
		if ((m2 == gcode) & (m1 == tcode)) {
			return 1;
		}
		return 0;
	} // end of fit(int, int)
	
/**
* Evaluation of complementarity between two segments.
*/
	private final int complementscore(int start1, int end1, int start2, int end2) {
		if ((end1 - start1) != (end2 - start2)) {
			return -1;
		}
		int sum = 0;
		int temp;
		for (int i=0; i<=end1 - start1; i++) {
			temp = fit(start1+i, end2-i);
			if (temp < 1) {
				return -2;
			} else {
				sum += temp;
			}
		}
		return sum;
	} // end of complementscore(int, int, int, int)
	
	
/**
* @param	stem1Astart	An (internal) positon in current DNA.
* @return	A MotifHit at stem1Astart, or null.
*/
  public final MotifHit getMotifHitAt(int stem1Astart) throws RetroTectorException {
		KnotTry bestTry = null;
		KnotTry tempTry;
		for (int stem1Bend=stem1Astart+STEM1MINLENGTH+LOOP1MINLENGTH+STEM2MINLENGTH+INTERVALMINLENGTH+STEM1MINLENGTH-1; (stem1Bend<currentDNA.LENGTH) && (stem1Bend<=(stem1Astart+STEM1MAXLENGTH+LOOP1MAXLENGTH+STEM2MAXLENGTH+INTERVALMAXLENGTH+STEM1MAXLENGTH-1)); stem1Bend++) {
			if ((fit(stem1Astart, stem1Bend) > 0) & (fit(stem1Astart + 1, stem1Bend - 1) > 0) & (fit(stem1Astart + 2, stem1Bend - 2) > 0) & (fit(stem1Astart + 3, stem1Bend - 3) > 0)) {
				for (int stem2Astart=stem1Astart+STEM1MINLENGTH+LOOP1MINLENGTH; (stem2Astart<=(stem1Bend+1-STEM1MINLENGTH-INTERVALMINLENGTH - STEM2MINLENGTH)) && (stem2Astart<=(stem1Astart+STEM1MAXLENGTH+LOOP1MAXLENGTH)); stem2Astart++) {
					for (int stem2Bend=stem1Bend+LOOP2MINLENGTH+STEM2MINLENGTH; (stem2Bend<currentDNA.LENGTH) && (stem2Bend<=(stem1Bend+LOOP2MAXLENGTH+STEM2MAXLENGTH)); stem2Bend++) {
						if ((fit(stem2Astart, stem2Bend) > 0) & (fit(stem2Astart + 1, stem2Bend - 1) > 0) & (fit(stem2Astart + 2, stem2Bend - 2) > 0) & (fit(stem2Astart + 3, stem2Bend - 3) > 0)) {
							for (int stem1Aend=stem1Astart+STEM1MINLENGTH-1; stem1Aend<=stem1Astart+STEM1MAXLENGTH-1; stem1Aend++) {
								for (int stem2Aend=stem2Astart+STEM2MINLENGTH-1; stem2Aend<=stem2Astart+STEM2MAXLENGTH-1; stem2Aend++) {
									for (int stem1Bstart=stem1Bend-STEM1MAXLENGTH+1; stem1Bstart<=stem1Bend-STEM1MINLENGTH+1; stem1Bstart++) {
										for (int stem2Bstart=stem2Bend-STEM2MAXLENGTH+1; stem2Bstart<=stem2Bend-STEM2MINLENGTH+1; stem2Bstart++) {
											tempTry = new KnotTry(stem1Astart, stem1Aend, stem2Astart, stem2Aend, stem1Bstart, stem1Bend, stem2Bstart, stem2Bend);
											if ((bestTry == null) || (tempTry.SCORE > bestTry.SCORE)) {
												bestTry = tempTry;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		if ((bestTry != null) && (bestTry.SCORE > 0)) {
			latestTry = bestTry;
			return new MotifHit(this, MAXSCORE * bestTry.SCORE / (2 * (STEM1MAXLENGTH + STEM2MAXLENGTH)), bestTry.STEM1ASTART, bestTry.STEM1ASTART, bestTry.STEM1ASTART + bestTry.LENGTH - 1, currentDNA);
    } else {
			return null;
		}
  } // end of getMotifHitAt(int)
  
} // end of PseudoKnotMotif

